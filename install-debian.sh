#!/usr/bin/env bash

# Retrieve Latest Version
VERSION=$(curl -sI https://github.com/optum/sourcehawk/releases/latest | grep -i location | awk -F"/" '{ printf "%s", $NF }' | tr -d '\r\n')

# Download the binary and make it executable
DOWNLOAD_URL="https://github.com/optum/sourcehawk/releases/download/$VERSION/sourcehawk-debian-amd64.deb"
DEB_PACKAGE="/tmp/sourcehawk-$VERSION.deb"

echo "Downloading Sourcehawk package..."
if curl -sLk "$DOWNLOAD_URL" -o "$DEB_PACKAGE"; then
  echo "Installing..."
  sudo apt install "$DEB_PACKAGE"
  # shellcheck disable=SC1090
  source ~/.bashrc
  sourcehawk -V
  sourcehawk --help
  rm -rf "$DEB_PACKAGE"
else
  echo "Sourcehawk debian package installation failed: $?"
  exit 1
fi
