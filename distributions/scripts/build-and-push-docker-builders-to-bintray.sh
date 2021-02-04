#!/bin/bash

##############################################################################################################
#
# Push Docker Builders to Bintray
#
##############################################################################################################

set -e

ROOT_DIR="$( cd "$( dirname "$( dirname "$( dirname "${BASH_SOURCE[0]}" )")")" && pwd )"
DOCKER_BUILDERS_DIR="$ROOT_DIR/distributions/docker-builders"

# Bintray Variables
BINTRAY_API_URL="https://api.bintray.com"
BINTRAY_ORG="optum"
BINTRAY_REPO="builders"
REGISTRY="optum-docker-builders.bintray.io"

# Native Image
docker build -t $REGISTRY/nativeimage:graalvm-ce-21.0.0-java8 -f "$DOCKER_BUILDERS_DIR/Dockerfile-nativeimage" --build-arg FROM=oracle/graalvm-ce:21.0.0-java8 .
docker build -t $REGISTRY/nativeimage:graalvm-ce-21.0.0-java11 -f "$DOCKER_BUILDERS_DIR/Dockerfile-nativeimage" --build-arg FROM=oracle/graalvm-ce:21.0.0-java11 .

# RPM Build
docker build -t $REGISTRY/rpmbuild:centos7 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=centos:7 .
docker build -t $REGISTRY/rpmbuild:centos8 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=centos:8 .
docker build -t $REGISTRY/rpmbuild:fedora32 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=fedora:32 .
docker build -t $REGISTRY/rpmbuild:fedora33 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=fedora:33 .
docker build -t $REGISTRY/rpmbuild:fedora34 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=fedora:34 .

# Login to Registry
echo "${BINTRAY_API_KEY}" | docker login --username "${BINTRAY_USERNAME}" --password-stdin $REGISTRY

# Push All Builders to Remote Registry
docker push $REGISTRY/nativeimage:graalvm-ce-21.0.0-java8
docker push $REGISTRY/nativeimage:graalvm-ce-21.0.0-java11
docker push $REGISTRY/rpmbuild:centos7
docker push $REGISTRY/rpmbuild:centos8
docker push $REGISTRY/rpmbuild:fedora32
docker push $REGISTRY/rpmbuild:fedora33
docker push $REGISTRY/rpmbuild:fedora34

# Publish Images on Bintray
curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" $BINTRAY_API_URL/content/$BINTRAY_ORG/$BINTRAY_REPO/nativeimage/graalvm-ce-21.0.0-java8/publish
curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" $BINTRAY_API_URL/content/$BINTRAY_ORG/$BINTRAY_REPO/nativeimage/graalvm-ce-21.0.0-java11/publish
curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" $BINTRAY_API_URL/content/$BINTRAY_ORG/$BINTRAY_REPO/rpmbuild/centos8/publish
curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" $BINTRAY_API_URL/content/$BINTRAY_ORG/$BINTRAY_REPO/rpmbuild/fedora32/publish
curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" $BINTRAY_API_URL/content/$BINTRAY_ORG/$BINTRAY_REPO/rpmbuild/fedora33/publish
curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" $BINTRAY_API_URL/content/$BINTRAY_ORG/$BINTRAY_REPO/rpmbuild/fedora34/publish