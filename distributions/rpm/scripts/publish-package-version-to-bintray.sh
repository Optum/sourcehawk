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
BINTRAY_RELEASE_REPOSITORY="${BINTRAY_REPOSITORY:-3}"
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

# Construct the upload path
BINTRAY_UPLOAD_PATH="$BINTRAY_PACKAGE_BASE_VERSION/$BINTRAY_PACKAGE-$BINTRAY_PACKAGE_VERSION-$BINTRAY_ARCHITECTURE.rpm"
if [[ "$BINTRAY_REPOSITORY" == "dev-snapshots" ]]; then
  BINTRAY_UPLOAD_PATH="$BINTRAY_PACKAGE_BASE_VERSION/$BINTRAY_PACKAGE-$BINTRAY_PACKAGE_VERSION-$BINTRAY_RELEASE_REPOSITORY-$BINTRAY_ARCHITECTURE.rpm"
fi
BINTRAY_UPLOAD_PATH="${BINTRAY_UPLOAD_PATH//"-SNAPSHOT"/}" # Remove "-SNAPSHOT" from Upload Path

# Construct the URL for publishing
BINTRAY_PUBLISH_URL="$BINTRAY_API_URL/content/$BINTRAY_ORGANIZATION/$BINTRAY_REPOSITORY/$BINTRAY_PACKAGE/$BINTRAY_PACKAGE_VERSION/$BINTRAY_UPLOAD_PATH;publish=1"

# Publish Package
echo "Publishing RPM package to $BINTRAY_PUBLISH_URL"
echo -n "Response: "
curl -sfLS -X PUT -T "$PACKAGE_FILE_PATH" -H "Accept: application/json" -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_PUBLISH_URL"
echo " "
