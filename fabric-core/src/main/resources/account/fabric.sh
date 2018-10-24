#!/usr/bin/env bash
#
# Copyright IBM Corp. All Rights Reserved.
#
# SPDX-License-Identifier: Apache-2.0
#
# simple batch script making it easier to cleanup and start a relatively fresh fabric env.



# don't rewrite paths for Windows Git Bash users
export MSYS_NO_PATHCONV=1
starttime=$(date +%s)
LANGUAGE=${1:-"golang"}
CC_SRC_PATH=github.com/account/go
if [ "$LANGUAGE" = "node" -o "$LANGUAGE" = "NODE" ]; then
	CC_SRC_PATH=/opt/gopath/src/github.com/account/node
fi


# clean the keystore
rm -rf ./hfc-key-store

# launch network; create channel and join peer to channel
cd ../basic-network

if [ ! -e "../docker-compose.yaml" ];then
  echo "../docker-compose.yaml not found."
  exit 8
fi


ORG_HYPERLEDGER_FABRIC_SDKTEST_VERSION=${ORG_HYPERLEDGER_FABRIC_SDKTEST_VERSION:-}

function clean(){

  rm -rf /var/hyperledger/*

  if [ -e "../../../../../*.tail" ];then
    rm -f "../../../../../*.tail"
  fi

  lines=`docker ps -a | grep 'dev-peer' | wc -l`

  if [ "$lines" -gt 0 ]; then
    docker ps -a | grep 'dev-peer' | awk '{print $1}' | xargs docker rm -f
  fi

  lines=`docker images | grep 'dev-peer' | wc -l`
  if [ "$lines" -gt 0 ]; then
    docker images | grep 'dev-peer' | awk '{print $1}' | xargs docker rmi -f
  fi

}

function up(){
   docker-compose up --force-recreate
}

function down(){
  docker-compose down;
}

function stop (){
  docker-compose  stop;
}

function start (){
     ./start.sh

     # Now launch the CLI container in order to install, instantiate chaincode
     # and prime the ledger with our 10 cars
     docker-compose -f ./docker-compose.yaml up -d cli


     #install chaincode on peer0
     docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" cli peer chaincode install -n account -v 1.4 -p "$CC_SRC_PATH" -l "$LANGUAGE"

     #init chaincode on peer0
     docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" cli peer chaincode instantiate -o orderer.example.com:7050 -C mychannel -n account -l "$LANGUAGE" -v 1.4 -c '{"Args":[""]}' -P "OR ('Org1MSP.member','Org2MSP.member')"

     sleep 5
     #install chaincode on peer1
     docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" -e "CORE_PEER_ADDRESS=peer1.org1.example.com:7051" cli peer chaincode install -n account -v 1.4 -p "$CC_SRC_PATH" -l "$LANGUAGE"

     docker exec -e "CORE_PEER_LOCALMSPID=Org1MSP" -e "CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp" cli peer chaincode invoke -o orderer.example.com:7050 -C mychannel -n account -c '{"function":"init","Args":[""]}'

     #printf "\nTotal setup execution time : $(($(date +%s) - starttime)) secs ...\n\n\n"
     #printf "Start by installing required packages run 'npm install'\n"
     #printf "Then run 'node enrollAdmin.js', then 'node registerUser'\n\n"
     #printf "The 'node invoke.js' will fail until it has been updated with valid arguments\n"
     #printf "The 'node query.js' may be run at anytime once the user has been registered\n\n"

}


for opt in "$@"
do

    case "$opt" in
        up)
            up
            ;;
        down)
            down
            ;;
        stop)
            stop
            ;;
        start)
            start
            ;;
        clean)
            clean
            ;;
        restart)
            down
            clean
            up
            ;;

        *)
            echo $"Usage: $0 {up|down|start|stop|clean|restart}"
            exit 1

esac
done
