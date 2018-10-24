package com.smy.bc.fabric.core.configuration;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.smy.bc.fabric.core.HFUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.PEMWriter;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.List;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <p>Title: FabricConfiguration</p>
 * <p>Description: </p>
 *
 * @author zhaofeng
 * @version 1.0
 * @date 18-9-12
 * //
 */
@Component
@ConfigurationProperties(prefix = "blockchain")
@Slf4j
@Data
public class FabricConfiguration {

    private String name = "account";
    private String storageType = "file";
    private String storageRootDir;
    private CAClient caClient = new CAClient();
    private Organization organization = new Organization();
    private String channelName ="mychannel";
    private List<EndPoint> channelPeers = CollUtil.list(true,new EndPoint("anchor0","grpc://127.0.0.1:7051",null),new EndPoint("anchor1","grpc://127.0.0.1:9051",null));
    private List<EndPoint> channelOrders = CollUtil.list(true,new EndPoint("orderer","grpc://127.0.0.1:7050",null));
    private List<EndPoint> channelEvents= CollUtil.list(true,new EndPoint("peer0","grpc://127.0.0.1:7053",null),new EndPoint("peer0","grpc://127.0.0.1:8053",null));

    @Bean
    public HFClient hfClient() {

        try {

            // create fabric-ca client

            HFCAClient hfCaClient = getHfCaClient(caClient);

            // enroll or load admin
            HFUser admin = getAdmin(hfCaClient);
            log.info(admin.toString());

            // register and enroll new user
            HFUser hfUser = getUser(hfCaClient, admin, organization.getUser());
            log.info(hfUser.toString());

            // get HFC client instance
            HFClient client = getHfClient();
            // set user context
            client.setUserContext(admin);

            // get HFC channel using the client
            Channel channel = getChannel(client);
            log.info("Channel: " + channel.getName());

            return client;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HFCAClient getHfCaClient(CAClient client) throws Exception {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFCAClient caClient = HFCAClient.createNewInstance(client.getUrl(), client.caClientProperties);
        caClient.setCryptoSuite(cryptoSuite);
        return caClient;
    }

    public HFUser getAdmin(HFCAClient hfcaClient) throws Exception {
        HFUser admin = tryDeserialize(caClient.getAdminUser());
        if (admin == null) {

            EnrollmentRequest enrollmentRequest = new EnrollmentRequest();

            if(caClient.getTlsEnable()){

                log.info("Enable CAClient TLS... ");
                //This shows how to get a client TLS certificate from Fabric CA
                // we will use one client TLS certificate for orderer peers etc.
                enrollmentRequest.addHost(NetUtil.getIpByHost(caClient.getUrl()));
                enrollmentRequest.setProfile("tls");
                Enrollment adminEnrollment = hfcaClient.enroll(caClient.getAdminUser(), caClient.getAdminSecret(),enrollmentRequest);
                admin = new HFUser(caClient.getAdminUser(), organization.getAffiliation(), organization.getMspid(), adminEnrollment);

                //未设置,则自动生成ECDSA公私钥
                final String tlsCertPEM = adminEnrollment.getCert();
                final String tlsKeyPEM = getPEMStringFromPrivateKey(adminEnrollment.getKey());
                final Properties tlsProperties = new Properties();
                tlsProperties.put("clientKeyBytes", tlsKeyPEM.getBytes(UTF_8));
                tlsProperties.put("clientCertBytes", tlsCertPEM.getBytes(UTF_8));
                this.getCaClient().setCaClientProperties(tlsProperties);
                log.info("tls properties:{}",new Gson().newBuilder().create().toJson(tlsProperties));
            }else {
                Enrollment adminEnrollment = hfcaClient.enroll(caClient.getAdminUser(), caClient.getAdminSecret(),enrollmentRequest);
                admin = new HFUser(caClient.getAdminUser(), organization.getAffiliation(), organization.getMspid(), adminEnrollment);
            }

            serialize(admin);
        }

        return admin;
    }

    public Channel getChannel(HFClient client) throws InvalidArgumentException, TransactionException {
        // initialize channel
        Channel channel = client.newChannel(channelName);
        for (EndPoint peer : channelPeers) {
            // peer name and endpoint in fabcar network
            channel.addPeer(client.newPeer(peer.getName(),peer.getUrl(),getRealPath(peer.getProperties())));
        }

        for (EndPoint orderer : channelOrders) {
            // peer name and endpoint in fabcar network
            channel.addOrderer(client.newOrderer(orderer.getName(),orderer.getUrl(),getRealPath(orderer.getProperties())));
        }

        for (EndPoint eventHub : channelEvents) {
            // eventhub name and endpoint in fabcar network
            channel.addEventHub(client.newEventHub(eventHub.getName(),eventHub.getUrl(),getRealPath(eventHub.getProperties())));
        }

        channel.initialize();
        return channel;
    }

    public HFClient getHfClient() throws Exception {
        // initialize default cryptosuite
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // setup the client
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        return client;
    }

    public HFUser getUser(HFCAClient caClient, HFUser registrar, String userId) throws Exception {
        HFUser hfUser = tryDeserialize(userId);
        if (hfUser == null) {
            RegistrationRequest rr = new RegistrationRequest(userId, organization.getAffiliation());
            String enrollmentSecret = caClient.register(rr, registrar);
            Enrollment enrollment = caClient.enroll(userId, enrollmentSecret);
            hfUser = new HFUser(userId, organization.getAffiliation(), organization.getMspid(), enrollment);
            serialize(hfUser);
        }
        return hfUser;
    }


    public String getRootDir(){
        if ("file".equalsIgnoreCase(storageType) && StrUtil.isNotBlank(storageRootDir)){
            return StrUtil.trimToEmpty(storageRootDir)+ File.separator;
        }else{
            return StrUtil.EMPTY;
        }
    }

    public void serialize(HFUser hfUser) throws IOException {

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
                Paths.get(getRootDir()+hfUser.getName() + ".tail")))) {
            oos.writeObject(hfUser);
        }
    }

    public HFUser tryDeserialize(String name) throws Exception {
        if (Files.exists(Paths.get(getRootDir()+name + ".tail"))) {
            return deserialize(name);
        }
        return null;
    }

    public HFUser deserialize(String name) throws Exception {
        try (ObjectInputStream decoder = new ObjectInputStream(
                Files.newInputStream(Paths.get(getRootDir()+name + ".tail")))) {
            return (HFUser) decoder.readObject();
        }
    }

    public String getPEMStringFromPrivateKey(PrivateKey privateKey) throws IOException {
        StringWriter pemStrWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(pemStrWriter);

        pemWriter.writeObject(privateKey);

        pemWriter.close();

        return pemStrWriter.toString();
    }


    public Properties getRealPath(Properties properties){
        if (properties.containsKey("pemFile")){
            String path = ClassUtil.getClassPath();
            properties.put("pemFile",path+properties.get("pemFile"));
        }
        return properties;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class CAClient {

        private String url ="http://127.0.0.1:7054";
        private Boolean tlsEnable = false;
        private Properties caClientProperties;
        private String adminUser = "admin";
        private String adminSecret = "adminpw";

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class Organization {
        private String name="peerOrg1";
        private String user ="user1";
        private String mspid="Org1MSP";
        private String affiliation ="org1";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class EndPoint {
        private String name;
        private String url;
        private Properties properties;

    }

}
