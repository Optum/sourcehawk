#!/bin/bash

##############################################################################################################
#
# Push Docker Builders to Remote Registry
#
##############################################################################################################

set -e

ROOT_DIR="$( cd "$( dirname "$( dirname "$( dirname "${BASH_SOURCE[0]}" )")")" && pwd )"
DOCKER_BUILDERS_DIR="$ROOT_DIR/distributions/docker-builders"

# Variables
DOCKER_ORG="optum/ci"
REGISTRY="ghcr.io"

# Native Image
docker build -t $REGISTRY/$DOCKER_ORG/nativeimage:graalvm-ce-21.3.0-java8 -f "$DOCKER_BUILDERS_DIR/Dockerfile-nativeimage" --build-arg FROM=ghcr.io/graalvm/graalvm-ce:21.3.0-java8 .
docker build -t $REGISTRY/$DOCKER_ORG/nativeimage:graalvm-ce-21.3.0-java11 -f "$DOCKER_BUILDERS_DIR/Dockerfile-nativeimage" --build-arg FROM=ghcr.io/graalvm/graalvm-ce:21.3.0-java11 .
docker build -t $REGISTRY/$DOCKER_ORG/nativeimage:graalvm-ce-21.3.0-java17 -f "$DOCKER_BUILDERS_DIR/Dockerfile-nativeimage" --build-arg FROM=ghcr.io/graalvm/graalvm-ce:21.3.0-java17 .

# RPM Build
docker build -t $REGISTRY/$DOCKER_ORG/rpmbuild:centos7 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=centos:7 .
docker build -t $REGISTRY/$DOCKER_ORG/rpmbuild:centos8 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=centos:8 .
docker build -t $REGISTRY/$DOCKER_ORG/rpmbuild:fedora32 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=fedora:32 .
docker build -t $REGISTRY/$DOCKER_ORG/rpmbuild:fedora33 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=fedora:33 .
docker build -t $REGISTRY/$DOCKER_ORG/rpmbuild:fedora34 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=fedora:34 .
docker build -t $REGISTRY/$DOCKER_ORG/rpmbuild:fedora35 -f "$DOCKER_BUILDERS_DIR/Dockerfile-rpmbuild" --build-arg FROM=fedora:35 .

# Login to Registry
echo "${DOCKER_PASSWORD}" | docker login --username "${DOCKER_USERNAME}" --password-stdin $REGISTRY

# Push All Builders to Remote Registry
docker push $REGISTRY/nativeimage:graalvm-ce-21.3.0-java8
docker push $REGISTRY/nativeimage:graalvm-ce-21.3.0-java11
docker push $REGISTRY/nativeimage:graalvm-ce-21.3.0-java17
docker push $REGISTRY/rpmbuild:centos7
docker push $REGISTRY/rpmbuild:centos8
docker push $REGISTRY/rpmbuild:fedora32
docker push $REGISTRY/rpmbuild:fedora33
docker push $REGISTRY/rpmbuild:fedora34
docker push $REGISTRY/rpmbuild:fedora35