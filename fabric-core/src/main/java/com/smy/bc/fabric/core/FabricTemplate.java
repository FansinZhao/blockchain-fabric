package com.smy.bc.fabric.core;

import com.smy.bc.fabric.core.configuration.FabricConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * <p>Title: FabricTemplate</p>
 * <p>Description: </p>
 *
 * @author zhaofeng
 * @version 1.0
 * @date 18-9-14
 */
@Slf4j
@Component
public class FabricTemplate {

    @Resource
    private HFClient client;

    @Resource
    private FabricConfiguration configuration;

    private Pattern chainCodePattern = Pattern.compile(".*");
    private Pattern eventPattern = Pattern.compile(Pattern.quote("create"));


    /**
     * queryBlockByTransactionID
     *
     * @param txID
     * @return
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    public BlockInfo queryBlockByTransactionID(String txID) throws ProposalException, InvalidArgumentException {

        log.info("调用智能合约> txId查询：{}",txID);

        Channel channel = client.getChannel(configuration.getChannelName());

        BlockInfo blockInfo = channel.queryBlockByTransactionID(channel.getPeers(), txID);

        return blockInfo;
    }

    /***
     * invoke blockChain
     * @param funcName
     * @param args
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    public String  invokeBlockChain(String chainCodeName, String funcName, String... args) throws ProposalException, InvalidArgumentException {

        log.info("调用智能合约> chainCodeName:{} funcName:{} args:{}",chainCodeName,funcName,args);

        Channel channel = client.getChannel(configuration.getChannelName());
        // 构建proposal
        TransactionProposalRequest req = client.newTransactionProposalRequest();
        // 指定要调用的chaincode
        ChaincodeID cid = ChaincodeID.newBuilder().setName(chainCodeName).build();
        req.setChaincodeID(cid);
        req.setFcn(funcName);

        if (args.length > 0) {
            req.setArgs(args);
        }

        if(configuration.getCaClient().getTlsEnable()){

        }

        // 发送proprosal
        Collection<ProposalResponse> resps = channel.sendTransactionProposal(req, channel.getPeers());

        String transactionID = null;
        String payload;
        for (ProposalResponse resp : resps) {
            if(!resp.isVerified()){
                log.error("peerName:{} peerUrl:{} chaincodeID:{} status:{} msg:{}", resp.getPeer().getName(), resp.getPeer().getUrl(),resp.getChaincodeID(), resp.getStatus(), resp.getMessage());
                //清空
                transactionID = null;
                break;
            }
            byte[] payloadBytes = resp.getProposalResponse().getResponse().getPayload().toByteArray();
            payload = new String(payloadBytes);
            transactionID = resp.getTransactionID();
            log.info("TransactionID:{} payloadBytes:{}", transactionID,payload);
        }

        // 提交给orderer节点
        channel.sendTransaction(resps).thenApply(transactionEvent -> {

            log.info("{}", transactionEvent.getTransactionID());

            return null;
        }).exceptionally(e -> {
            if (e instanceof TransactionEventException) {
                BlockEvent.TransactionEvent te = ((TransactionEventException) e).getTransactionEvent();
                if (te != null) {
                    throw new AssertionError(format("Transaction with txid %s failed. %s", te.getTransactionID(), e.getMessage()), e);
                }
            }

            throw new AssertionError(format("Test failed with %s exception %s", e.getClass().getName(), e.getMessage()), e);
        }).whenComplete((f, throwable) -> {
            log.info("{}", throwable);
        }).exceptionally((e) -> {
            if (e instanceof TransactionEventException) {
                BlockEvent.TransactionEvent te = ((TransactionEventException) e).getTransactionEvent();
                if (te != null) {
                    throw new AssertionError(format("Transaction with txid %s failed. %s", te.getTransactionID(), e.getMessage()), e);
                }
            }

            throw new AssertionError(format("Test failed with %s exception %s", e.getClass().getName(), e.getMessage()), e);
        });
        log.info("异步调用智能合约！transactionID={}",transactionID);

        return transactionID;
    }


    /***
     * query blockChain
     * @param funcName
     * @param args
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    public String queryByChaincode(String chainCodeName, String funcName, String... args) throws ProposalException, InvalidArgumentException {
        log.info("调用智能合约> chainCodeName:{} funcName:{} args:{}",chainCodeName,funcName,args);

        // get channel instance from client
        Channel channel = client.getChannel(configuration.getChannelName());
        // create chaincode request
        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        // build cc id providing the chaincode name. Version is omitted here.
        ChaincodeID fabcarCCId = ChaincodeID.newBuilder().setName(chainCodeName).build();
        qpr.setChaincodeID(fabcarCCId);
        // CC function to be called
        qpr.setFcn(funcName);

        if (args.length > 0) {
            qpr.setArgs(args);
        }

        Collection<ProposalResponse> res = channel.queryByChaincode(qpr, channel.getPeers());

        // display response
        String stringResponse = null;
        for (ProposalResponse pres : res) {

            if (pres.isVerified()) {
                log.info("peerName:{} peerUrl:{} chaincodeID:{} payLoad:{}", pres.getPeer().getName(), pres.getPeer().getUrl(),pres.getChaincodeID(), stringResponse);
                stringResponse = new String(pres.getChaincodeActionResponsePayload());
            } else {
                log.error("peerName:{} peerUrl:{} chaincodeID:{} status:{} msg:{}", pres.getPeer().getName(), pres.getPeer().getUrl(),pres.getChaincodeID(), pres.getStatus(), pres.getMessage());
                return null;
            }
        }

        return stringResponse;
    }


    public String registerChaincodeEventListener() {
        // get channel instance from client
        Channel channel = client.getChannel(configuration.getChannelName());
        String chaincodeEventListenerHandle = null;
        try {
            chaincodeEventListenerHandle = channel.registerChaincodeEventListener(chainCodePattern,
                    eventPattern,
                    (handle, blockEvent, chaincodeEvent) -> {


                        String es = blockEvent.getPeer() != null ? blockEvent.getPeer().getName() : blockEvent.getEventHub().getName();

                        log.info("RECEIVED Chaincode event with handle: {}, chaincode Id: {}, chaincode event name: {}, "
                                        + "transaction id: {}, event payload: \"{}\", from eventhub: {} , blockNumber:{}",
                                handle, chaincodeEvent.getChaincodeId(),
                                chaincodeEvent.getEventName(),
                                chaincodeEvent.getTxId(),
                                new String(chaincodeEvent.getPayload()), es, blockEvent.getBlockNumber());

                    });
        } catch (InvalidArgumentException e) {
            log.error("事件监听异常!", e);
        }
        log.info("绑定智能合约监听事件: {}", chaincodeEventListenerHandle);
        return chaincodeEventListenerHandle;
    }

    public boolean unregisterChaincodeEventListener(String handle) {
        Channel channel = client.getChannel(configuration.getChannelName());
        boolean b = false;
        try {
            b = channel.unregisterChaincodeEventListener(handle);
        } catch (InvalidArgumentException e) {
            log.error("解绑异常!", e);
        }
        log.info("解绑智能合约监听事件: {}", handle);
        return b;

    }

    public String registerBlockListener() {
        // get channel instance from client
        Channel channel = client.getChannel(configuration.getChannelName());
        String blockEventListenerHandle = null;
        try {
            blockEventListenerHandle = channel.registerBlockListener((blockEvent) -> {
                log.info("RECEIVED block number: {}, "
                                + "dataCount: {}, name \"{}\", from eventhub: {}",
                        blockEvent.getBlockNumber(), blockEvent.getBlock().getData().getDataCount(), blockEvent.getPeer().getName());
            });
        } catch (InvalidArgumentException e) {
            log.error("事件监听异常!", e);
        }
        log.info("绑定区块监听事件: {}", blockEventListenerHandle);
        return blockEventListenerHandle;
    }

    public boolean unregisterBlockListener(String handle) {
        Channel channel = client.getChannel(configuration.getChannelName());
        boolean b = false;
        try {
            b = channel.unregisterBlockListener(handle);
        } catch (InvalidArgumentException e) {
            log.error("解绑异常!", e);
        }
        log.info("解绑区块监听事件: {}", handle);
        return b;

    }

}
