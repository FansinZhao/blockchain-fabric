#!/bin/bash

echo
echo " ____    _____      _      ____    _____ "
echo "/ ___|  |_   _|    / \    |  _ \  |_   _|"
echo "\___ \    | |     / _ \   | |_) |   | |  "
echo " ___) |   | |    / ___ \  |  _ <    | |  "
echo "|____/    |_|   /_/   \_\ |_| \_\   |_|  "
echo
echo "Build your first network (BYFN) end-to-end test"
echo
CHANNEL_NAME="$1"
DELAY="$2"
LANGUAGE="$3"
TIMEOUT="$4"
VERBOSE="$5"
: ${CHANNEL_NAME:="mychannel"}
: ${DELAY:="3"}
: ${LANGUAGE:="golang"}
: ${TIMEOUT:="10"}
: ${VERBOSE:="false"}
LANGUAGE=`echo "$LANGUAGE" | tr [:upper:] [:lower:]`
COUNTER=1
MAX_RETRY=5

echo "Channel name : "$CHANNEL_NAME

# import utils
. scripts/utils.sh



CC_SRC_PATH="github.com/hyperledger/fabric/chaincode/org1/"
CC_NAME="org1"
VESION=1.3
echo "Installing chaincode on peer0.org1..."
installChaincode 0 1 ${VESION}
echo "Install chaincode on peer0.org2..."
installChaincode 0 2 ${VESION}

##
upgradeChaincode 0 2 ${VESION}

# Invoke chaincode on peer0.org1 and peer0.org2
echo "Sending invoke transaction on peer0.org1 peer0.org2..."
echo "测试智能合约接口..."
#invoke only need CC_NAME
CC_FUNC='{"Args":["create","{\"sysCode\":\"CTS\",\"custNo\":\"a123456789\",\"orderId\":\"0123456\",\"bankCardNo\":\"123456789\",\"transAmt\":123.45}"]}'
CC_NAME="org1"
# Invoke chaincode on peer0.org1 and peer0.org2
echo "Sending invoke transaction on peer0.org1 peer0.org2..."
chaincodeInvoke 0 1 0 2

CC_NAME="org1"
CC_FUNC='{"Args":["query","0123456","CTS"]}'
echo "等待3s"

chaincodeInvoke 0 1 0 2

echo
echo "========= All GOOD, BYFN execution completed =========== "
echo

echo
echo " _____   _   _   ____   "
echo "| ____| | \ | | |  _ \  "
echo "|  _|   |  \| | | | | | "
echo "| |___  | |\  | | |_| | "
echo "|_____| |_| \_| |____/  "
echo

exit 0
