#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#
# Exit on first error
set -e
cd ../basic-network/
docker-compose -f docker-compose.yaml down
cd -
./cleanFabric.sh
