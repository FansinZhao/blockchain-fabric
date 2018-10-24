#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#
# Exit on first error
set -e

# clean the keystore
rm -rf ./hfc-key-store

# launch network; create channel and join peer to channel
cd ../basic-network
./start.sh




# don't rewrite paths for Windows Git Bash users
export MSYS_NO_PATHCONV=1
starttime=$(date +%s)
LANGUAGE=${1:-"golang"}
CC_SRC_PATH=github.com/chaincode/account
if [ "$LANGUAGE" = "node" -o "$LANGUAGE" = "NODE" ]; then
	CC_SRC_PATH=/opt/gopath/src/github.com/account/node
fi

ORG1_CC_SRC_PATH=github.com/chaincode/org1
ORG2_CC_SRC_PATH=github.com/chaincode/org2
USER_CC_SRC_PATH=github.com/chaincode/user

# Now launch the CLI container in order to install, instantiate chaincode
# and prime the ledger with our 10 cars
docker-compose -f ./docker-compose.yaml up -d cli


#install chaincode on peer0 org1
docker exec -e "CORE_PEER_LOCALMSPID=Org2MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org2.example.com:7051" cli peer chaincode install -n account -v 0.1 -p "$CC_SRC_PATH" -l "$LANGUAGE"
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" cli peer chaincode install -n account -v 0.1 -p "$CC_SRC_PATH" -l "$LANGUAGE"

#init chaincode on peer0
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051"  cli peer chaincode instantiate -o orderer.example.com:7050 -C mychannel -n account -l "$LANGUAGE" -v 0.1 -c '{"Args":["","0.1","初始化"]}' -P "AND ('Org1MSP.member','Org2MSP.member')"

#docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" cli peer chaincode invoke -o orderer.example.com:7050 -C mychannel -n account -c '{"function":"init","Args":[""]}'

#################org1##################
#install chaincode on peer0 org1
docker exec -e "CORE_PEER_LOCALMSPID=Org2MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org2.example.com:7051" cli peer chaincode install -n org1 -v 0.1 -p "$ORG1_CC_SRC_PATH" -l "$LANGUAGE"
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" cli peer chaincode install -n org1 -v 0.1 -p "$ORG1_CC_SRC_PATH" -l "$LANGUAGE"

#init chaincode on peer0
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org2.example.com:7051" cli peer chaincode instantiate -o orderer.example.com:7050 -C mychannel -n org1 -l "$LANGUAGE" -v 0.1 -c '{"Args":["","0.1","初始化"]}' -P "AND ('Org1MSP.member','Org2MSP.member')"

#docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" cli peer chaincode invoke -o orderer.example.com:7050 -C mychannel -n org1 -c '{"function":"init","Args":[""]}'

#################org2##################
#install chaincode on peer0 org1
docker exec -e "CORE_PEER_LOCALMSPID=Org2MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org2.example.com:7051" cli peer chaincode install -n org2 -v 0.1 -p "$ORG2_CC_SRC_PATH" -l "$LANGUAGE"
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" cli peer chaincode install -n org2 -v 0.1 -p "$ORG2_CC_SRC_PATH" -l "$LANGUAGE"

#init chaincode on peer0
docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer0.org1.example.com:7051" cli peer chaincode instantiate -o orderer.example.com:7050 -C mychannel -n org2 -l "$LANGUAGE" -v 0.1 -c '{"Args":["","0.1","初始化"]}' -P "AND ('Org1MSP.member','Org2MSP.member')"


#printf "\nTotal setup execution time : $(($(date +%s) - starttime)) secs ...\n\./n\n"
#printf "Start by installing required packages run 'npm install'\n"
#printf "Then run 'node enrollAdmin.js', then 'node registerUser'\n\n"
#printf "The 'node invoke.js' will fail until it has been updated with valid arguments\n"
#printf "The 'node query.js' may be run at anytime once the user has been registered\n\n"

