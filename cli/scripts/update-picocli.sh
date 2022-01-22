#!/bin/bash

set -e

#########################################################################
#
# Update the version of picocli
#
# https://github.com/remkop/picocli
#
#########################################################################

# Retrieve Latest Version
VERSION=$(curl -ksI https://github.com/remkop/picocli/releases/latest | grep -i location: | awk -F"/" '{ printf "%s", $NF }' | tr -d 'v' | tr -d '\r\n')

# Global Variables
DIR="$(dirname "$(cd -- "$(dirname "$0")"; pwd -P)")"
ROOT_DIR="$(dirname "$(dirname "$(cd -- "$(dirname "$0")"; pwd -P)")")"
BASE_URL="https://raw.githubusercontent.com/remkop/picocli"
LICENSE_URL="$BASE_URL/v$VERSION/LICENSE"
LICENSE_FILE_PATH="$DIR/src/main/resources/META-INF/licenses/picocli.txt"
RELATIVE_SOURCE_FILE_PATH="src/main/java/picocli/CommandLine.java"
SOURCE_URL="$BASE_URL/v$VERSION/$RELATIVE_SOURCE_FILE_PATH"
SOURCE_FILE_PATH="$DIR/$RELATIVE_SOURCE_FILE_PATH"

# Download the license and source file
curl -ksf "$LICENSE_URL" > "$LICENSE_FILE_PATH"
curl -ksf "$SOURCE_URL" > "$SOURCE_FILE_PATH"

# Add some warning suppression to the java source file
sed -i.bak -e 's/public class CommandLine/@SuppressWarnings({"rawtypes", "deprecation" })\npublic class CommandLine/g' \
  -e 's/TODO/TIDO/g' "$SOURCE_FILE_PATH" \
  && rm -rf "$SOURCE_FILE_PATH.bak"

# Remove TODOs so not highlighted in editor
sed -i.bak 's/TODO/TIDO/g' "$SOURCE_FILE_PATH"

# Replace the version in pom.xml file for plugin references
sed -i.bak "s/<picocli.version>[-[:alnum:]./]\{1,\}<\/picocli.version>/<picocli.version>$VERSION<\/picocli.version>/" "$DIR/pom.xml" \
  && rm -rf "$DIR/pom.xml.bak"

# Replace the version in attribution.txt file
sed -i.bak "s/Package: info.picocli:[-[:alnum:]./]\{1,\}/Package: info.picocli:$VERSION/" "$ROOT_DIR/attribution.txt" \
  && rm -rf "$ROOT_DIR/attribution.txt.bak"

echo "Picocli updated to version: $VERSION"