#!/bin/bash

##############################################################################
#
# Publish Debian packages to Bintray
#
# Packages
# - https://bintray.com/beta/#/optum/deb/sourcehawk
# - https://bintray.com/beta/#/optum/ubuntu/sourcehawk
#
##############################################################################

set -e

# Script Arguments
PACKAGE_VERSION="${1}"
DEBIAN_PATH="${2}"
BINTRAY_ORG="${3}"
BINTRAY_REPOSITORY="${4}"
BINTRAY_PACKAGE="${5}"
BINTRAY_DISTRIBUTIONS="${6}"
BINTRAY_ARCHITECTURES="${7}"

# Bintray Variables
BINTRAY_API_URL="https://api.bintray.com"
BINTRAY_COMPONENT="main"
BINTRAY_PUBLISH="1"
BINTRAY_PACKAGE_VERSION="${PACKAGE_VERSION//"-SNAPSHOT"/}" # Remove "-SNAPSHOT" from Package Version
BINTRAY_PACKAGE_BASE_VERSION="$(echo "$BINTRAY_PACKAGE_VERSION" | cut -f1 -d'~')"
BINTRAY_PRIMARY_ARCHITECTURE=$(echo "$BINTRAY_ARCHITECTURES" | cut -d',' -f1)
BINTRAY_UPLOAD_PATH="pool/$BINTRAY_COMPONENT/$(echo "$BINTRAY_PACKAGE" | cut -c1-1)/$BINTRAY_PACKAGE_BASE_VERSION/$BINTRAY_PACKAGE-$BINTRAY_REPOSITORY-$BINTRAY_PACKAGE_VERSION-$BINTRAY_PRIMARY_ARCHITECTURE.deb"

# Construct the URL for publishing
BINTRAY_PUBLISH_URL="$BINTRAY_API_URL/content/$BINTRAY_ORG/$BINTRAY_REPOSITORY/$BINTRAY_PACKAGE/$BINTRAY_PACKAGE_VERSION/$BINTRAY_UPLOAD_PATH"
BINTRAY_PUBLISH_URL="${BINTRAY_PUBLISH_URL};deb_distribution=$BINTRAY_DISTRIBUTIONS;deb_component=$BINTRAY_COMPONENT;deb_architecture=$BINTRAY_ARCHITECTURES;publish=$BINTRAY_PUBLISH"

# Publish Package
echo "Publishing package to $BINTRAY_PUBLISH_URL..."
curl -sfLS -X PUT -T "$DEBIAN_PATH" -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_PUBLISH_URL"

# Calculate Metadata for Package Repository
echo "Forcing metadata calculation..."
curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_API_URL/calc_metadata/$BINTRAY_ORG/$BINTRAY_REPOSITORY"
