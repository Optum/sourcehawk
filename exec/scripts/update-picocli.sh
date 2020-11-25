#!/bin/bash

set -e

#########################################################################
#
# Update the version of picocli
#
# https://github.com/remkop/picocli
#
#########################################################################

# Script Arguments
VERSION=${1:-'4.5.1'}

# Global Variables
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
URL="https://raw.githubusercontent.com/remkop/picocli/v${VERSION}/src/main/java/picocli/CommandLine.java"
FILE_PATH="$DIR/src/main/java/picocli/CommandLine.java"

# Download the source to the java file
curl -ksf "$URL" > "$FILE_PATH"

# Add some warning suppression to the java source file
sed -i 's/public\sclass\sCommandLine/@SuppressWarnings({"rawtypes", "deprecation" })\npublic class CommandLine/g' "$FILE_PATH"

# Replace the version in pom.xml file
sed -i "s/<picocli.version>[-[:alnum:]./]\{1,\}<\/picocli.version>/<picocli.version>$VERSION<\/picocli.version>/" "$DIR/pom.xml"

# Remove TODOs s not highlighted in editor
sed -i 's/TODO/TIDO/g' "$FILE_PATH"

echo "Picocli updated to version: $VERSION"