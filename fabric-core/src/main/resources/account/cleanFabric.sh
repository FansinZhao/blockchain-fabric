#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#
# Exit on first error
set -e

#rm -rf ../chaincode/hyperledger/*

rm -f /home/zhaofeng/IdeaProjects/block-fabric/*.tail


lines=`docker ps -a | grep 'dev-peer' | wc -l`

if [ "$lines" -gt 0 ]; then
docker ps -a | grep 'dev-peer' | awk '{print $1}' | xargs docker rm -f
fi

lines=`docker images |grep -E 'none|dev-peer' | awk '{print $3}' | wc -l`
if [ "$lines" -gt 0 ]; then
docker images | grep -E 'none|dev-peer' | awk '{print $3}' | xargs docker rmi -f
fi
