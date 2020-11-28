#!/usr/bin/env bash

# Retrieve Latest Version
VERSION=$(curl -sI https://github.com/optum/sourcehawk/releases/latest | grep -i location | awk -F"/" '{ printf "%s", $NF }' | tr -d '\r\n')

# Download the binary and make it executable
ARCH="$(uname -m)"
DOWNLOAD_URL="https://github.com/optum/sourcehawk/releases/download/$VERSION/sourcehawk-linux-$ARCH"
INSTALL_LOCATION="/usr/bin"
INSTALL_PATH="$INSTALL_LOCATION/sourcehawk"

if [ ! -w "$INSTALL_LOCATION" ]; then
  echo "You do not have write permissions to $INSTALL_LOCATION.  Try using 'sudo' to perform install"
  exit 1
fi

echo "Downloading Sourcehawk binary..."
if curl -sLk "$DOWNLOAD_URL" -o "$INSTALL_PATH"; then
  echo "Installing..."
  chmod +x "$INSTALL_PATH"
  sourcehawk -V
  sourcehawk --help
else
  echo "Sourcehawk is not yet available on your architecture: $ARCH"
  exit 1
fi
