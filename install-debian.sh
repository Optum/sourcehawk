#!/usr/bin/env bash

# Retrieve Latest Version
VERSION=$(curl -sI https://github.com/optum/sourcehawk/releases/latest | grep -i location | awk -F"/" '{ printf "%s", $NF }' | tr -d '\r\n')

# Download the binary and make it executable
ARCH="$(uname -m)"
DOWNLOAD_URL="https://github.com/optum/sourcehawk/releases/download/$VERSION/sourcehawk-linux-$ARCH.deb"
DEB_PACKAGE="/tmp/sourcehawk-$VERSION.deb"

echo "Downloading Sourcehawk package..."
if curl -sLk "$DOWNLOAD_URL" -o "$DEB_PACKAGE"; then
  echo "Installing..."
  sudo dpkg -i "$DEB_PACKAGE"
  # shellcheck disable=SC1090
  source ~/.bashrc
  sourcehawk -V
  sourcehawk --help
  rm -rf "$DEB_PACKAGE"
else
  echo "Sourcehawk is not yet available on your architecture: $ARCH"
  exit 1
fi
