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

# Script Environment / Arguments
PACKAGE_FILE_PATH="${PACKAGE_FILE_PATH:-1}"
BINTRAY_ORGANIZATION="${BINTRAY_ORGANIZATION:-2}"
[ -n "${BINTRAY_REPOSITORY_OVERRIDE}" ] && BINTRAY_REPOSITORY="${BINTRAY_REPOSITORY_OVERRIDE}"
BINTRAY_REPOSITORY="${BINTRAY_REPOSITORY:-3}"
BINTRAY_PACKAGE="${BINTRAY_PACKAGE:-4}"
PACKAGE_VERSION="${PACKAGE_VERSION:-5}"
PACKAGE_RELEASE="${PACKAGE_RELEASE:-6}"
BINTRAY_DISTRIBUTIONS="${BINTRAY_DISTRIBUTIONS:-7}"
BINTRAY_ARCHITECTURES="${BINTRAY_ARCHITECTURES:-8}"

# Bintray Variables
BINTRAY_API_URL="https://api.bintray.com"
BINTRAY_COMPONENT="main"
BINTRAY_PACKAGE_VERSION="${PACKAGE_VERSION//"-SNAPSHOT"/}" # Remove "-SNAPSHOT" from Package Version
BINTRAY_PACKAGE_BASE_VERSION="$(echo "$BINTRAY_PACKAGE_VERSION" | cut -f1 -d'~')"
BINTRAY_PRIMARY_ARCHITECTURE=$(echo "$BINTRAY_ARCHITECTURES" | cut -d',' -f1)

BINTRAY_UPLOAD_PATH="${BINTRAY_UPLOAD_PATH:-"pool/$BINTRAY_COMPONENT/$(echo "$BINTRAY_PACKAGE" | cut -c1-1)/$BINTRAY_PACKAGE_BASE_VERSION/$BINTRAY_PACKAGE-$BINTRAY_REPOSITORY-$BINTRAY_PACKAGE_VERSION-$BINTRAY_PRIMARY_ARCHITECTURE.deb"}"
BINTRAY_UPLOAD_PATH="${BINTRAY_UPLOAD_PATH//"-SNAPSHOT"/}" # Remove "-SNAPSHOT" from Upload Path

# Construct the URL for publishing
BINTRAY_PUBLISH_URL="$BINTRAY_API_URL/content/$BINTRAY_ORGANIZATION/$BINTRAY_REPOSITORY/$BINTRAY_PACKAGE/$BINTRAY_PACKAGE_VERSION/$BINTRAY_UPLOAD_PATH;publish=1"
if [[ "$BINTRAY_REPOSITORY" != "dev-snapshots" ]]; then
  BINTRAY_PUBLISH_URL="${BINTRAY_PUBLISH_URL};deb_distribution=$BINTRAY_DISTRIBUTIONS;deb_component=$BINTRAY_COMPONENT;deb_architecture=$BINTRAY_ARCHITECTURES"
fi

# Publish Package
echo "Publishing package to $BINTRAY_PUBLISH_URL..."
#curl -sfLS -X PUT -T "$PACKAGE_FILE_PATH" -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_PUBLISH_URL"

# Calculate Metadata for Package Repository
echo "Forcing metadata calculation..."
#curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_API_URL/calc_metadata/$BINTRAY_ORGANIZATION/$BINTRAY_REPOSITORY"
