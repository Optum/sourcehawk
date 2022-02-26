#!/bin/sh

# shellcheck disable=SC2164
DIR="${DIR:-"$(dirname "$(dirname "$(cd -- "$(dirname "$0")"; pwd -P)")")"}"

GH_PAGES_DIR="$DIR/gh-pages"

# TODO: copy native image JAR
# TODO: copy completion script


# TODO: build native image
BUILD_DIR="$DIR/distributions/linux"

. "$DIR"/distributions/scripts/extract-file-from-docker-container.sh /home/sourcehawk/sourcehawk /sourcehawk
