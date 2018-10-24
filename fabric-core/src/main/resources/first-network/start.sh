#!/usr/bin/env bash

./byfn.sh down
./clean.sh
./byfn.sh generate -c mychannel
./byfn.sh up -c mychannel -s couchdb
rm -rf ../../../../../fabric-api/src/main/resources/crypto-config
mkdir -p ../../../../../fabric-api/src/main/resources/crypto-config
cp -r ./crypto-config/* ../../../../../fabric-api/src/main/resources/crypto-config