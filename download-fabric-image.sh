
#!/bin/bash -eu
#
# Copyright IBM Corp. All Rights Reserved.
#
# SPDX-License-Identifier: Apache-2.0
#


##################################################
# This script pulls docker images from hyperledger
# docker hub repository and Tag it as
# hyperledger/fabric-<image> latest tag
##################################################



dockerPull1() {
  local FABRIC_TAG=$1
  for IMAGES in peer orderer ccenv tools ca; do
      echo "==> FABRIC IMAGE: $IMAGES"
      echo
      docker pull hyperledger/fabric-$IMAGES:$FABRIC_TAG
      docker tag hyperledger/fabric-$IMAGES:$FABRIC_TAG hyperledger/fabric-$IMAGES
  done
}


dockerPull2() {
  local FABRIC_TAG=$1
  for IMAGES in couchdb kafka zookeeper baseos; do
      echo "==> FABRIC IMAGE: $IMAGES"
      echo
      docker pull hyperledger/fabric-$IMAGES:$FABRIC_TAG
      docker tag hyperledger/fabric-$IMAGES:$FABRIC_TAG hyperledger/fabric-$IMAGES
  done
}

dockerPull3() {
  local FABRIC_TAG=$1
  echo "==> FABRIC IMAGE: javaenv"
  echo
  docker pull hyperledger/fabric-javaenv:$FABRIC_TAG
  docker tag hyperledger/fabric-javaenv:$FABRIC_TAG hyperledger/fabric-javaenv
}

dockerPull1 1.3.0

dockerPull2 0.4.14

dockerPull3 1.3.0
