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
BINTRAY_REPOSITORY="${3}"
BINTRAY_PACKAGE="${4}"
BINTRAY_DISTRIBUTIONS="${5}"
BINTRAY_ARCHITECTURES="${6}"

# Bintray Common Variables
BINTRAY_API_URL="https://api.bintray.com"
BINTRAY_SUBJECT="optum"
BINTRAY_COMPONENT="main"
BINTRAY_PUBLISH="1"

# Chose the appropriate version to publish
if [[ "$PACKAGE_VERSION" == *"-SNAPSHOT" ]]; then
  BINTRAY_PACKAGE_VERSION="${PACKAGE_VERSION%"-SNAPSHOT"}~$(date '+%Y%m%d')+$(git rev-parse HEAD | cut -c1-7)"
else
  BINTRAY_PACKAGE_VERSION="$PACKAGE_VERSION"
fi

BINTRAY_UPLOAD_PATH="pool/$BINTRAY_COMPONENT/$(echo "$BINTRAY_PACKAGE" | cut -c1-1)/$BINTRAY_PACKAGE-$BINTRAY_REPOSITORY-$BINTRAY_PACKAGE_VERSION.deb"

# Construct the URL for publishing
BINTRAY_PUBLISH_URL="$BINTRAY_API_URL/content/$BINTRAY_SUBJECT/$BINTRAY_REPOSITORY/$BINTRAY_PACKAGE/$BINTRAY_PACKAGE_VERSION/$BINTRAY_UPLOAD_PATH"
BINTRAY_PUBLISH_URL="${BINTRAY_PUBLISH_URL};deb_distribution=$BINTRAY_DISTRIBUTIONS;deb_component=$BINTRAY_COMPONENT;deb_architecture=$BINTRAY_ARCHITECTURES;publish=$BINTRAY_PUBLISH"

# Publish Package
echo -n "Publishing package to $BINTRAY_PUBLISH_URL..."
curl -sfLS -X PUT -T "$DEBIAN_PATH" -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_PUBLISH_URL"
echo "done"

# Calculate Metadata for Package Repository
echo -n "Forcing metadata calculation..."
curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_API_URL/calc_metadata/$BINTRAY_SUBJECT/$BINTRAY_REPOSITORY"
echo "done"
