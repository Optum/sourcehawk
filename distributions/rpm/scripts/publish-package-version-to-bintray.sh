#!/bin/bash

##############################################################################
#
# Publish RPM packages to Bintray
#
# Packages
# - https://bintray.com/beta/#/optum/centos/sourcehawk
# - https://bintray.com/beta/#/optum/fedora/sourcehawk
#
##############################################################################

set -e

# Script Environment / Arguments
PACKAGE_FILE_PATH="${PACKAGE_FILE_PATH:-1}"
BINTRAY_ORGANIZATION="${BINTRAY_ORGANIZATION:-2}"
[ -n "${BINTRAY_REPOSITORY_OVERRIDE}" ] && BINTRAY_REPOSITORY="${BINTRAY_REPOSITORY_OVERRIDE}"
BINTRAY_REPOSITORY="${BINTRAY_REPOSITORY:-3}"
BINTRAY_PACKAGE="${BINTRAY_PACKAGE:-4}"
PACKAGE_VERSION="${PACKAGE_VERSION:-5}"
PACKAGE_RELEASE="${PACKAGE_RELEASE:-6}"
BINTRAY_ARCHITECTURE="${BINTRAY_ARCHITECTURE:-7}"

# Bintray Common Variables
BINTRAY_API_URL="https://api.bintray.com"
BINTRAY_PACKAGE_BASE_VERSION="${PACKAGE_VERSION//"-SNAPSHOT"/}" # Remove "-SNAPSHOT" from Package Version
BINTRAY_PACKAGE_VERSION="$BINTRAY_PACKAGE_BASE_VERSION-$PACKAGE_RELEASE" # Append Release Metadata
BINTRAY_UPLOAD_PATH="${BINTRAY_UPLOAD_PATH:-"$BINTRAY_PACKAGE_BASE_VERSION/$BINTRAY_PACKAGE-$BINTRAY_PACKAGE_VERSION-$BINTRAY_ARCHITECTURE.rpm"}"
BINTRAY_UPLOAD_PATH="${BINTRAY_UPLOAD_PATH//"-SNAPSHOT"/}" # Remove "-SNAPSHOT" from Upload Path

# Construct the URL for publishing
BINTRAY_PUBLISH_URL="$BINTRAY_API_URL/content/$BINTRAY_ORGANIZATION/$BINTRAY_REPOSITORY/$BINTRAY_PACKAGE/$BINTRAY_PACKAGE_VERSION/$BINTRAY_UPLOAD_PATH;publish=1"

# Publish Package
echo "Publishing package to $BINTRAY_PUBLISH_URL..."
curl -sfLS -X PUT -T "$PACKAGE_FILE_PATH" -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_PUBLISH_URL"
