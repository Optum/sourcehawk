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

# Script Arguments
PACKAGE_VERSION="${1}"
PACKAGE_RELEASE="${2}"
RPM_PATH="${3}"
BINTRAY_ORG="${4}"
BINTRAY_REPOSITORY="${5}"
BINTRAY_PACKAGE="${6}"
BINTRAY_ARCHITECTURE="${7}"

# Bintray Common Variables
BINTRAY_API_URL="https://api.bintray.com"
BINTRAY_PACKAGE_BASE_VERSION="${PACKAGE_VERSION//"-SNAPSHOT"/}" # Remove "-SNAPSHOT" from Package Version
BINTRAY_PACKAGE_VERSION="$BINTRAY_PACKAGE_BASE_VERSION-$PACKAGE_RELEASE" # Append Release Metadata
BINTRAY_UPLOAD_PATH="$BINTRAY_PACKAGE_BASE_VERSION/$BINTRAY_PACKAGE-$BINTRAY_PACKAGE_VERSION-$BINTRAY_ARCHITECTURE.rpm"

# Construct the URL for publishing
BINTRAY_PUBLISH_URL="$BINTRAY_API_URL/content/$BINTRAY_ORG/$BINTRAY_REPOSITORY/$BINTRAY_PACKAGE/$BINTRAY_PACKAGE_VERSION/$BINTRAY_UPLOAD_PATH;publish=1"

# Publish Package
echo "Publishing package to $BINTRAY_PUBLISH_URL..."
curl -sfLS -X PUT -T "$RPM_PATH" -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_PUBLISH_URL"
