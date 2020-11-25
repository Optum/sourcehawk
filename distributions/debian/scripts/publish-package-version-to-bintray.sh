#!/bin/bash

##############################################################################
#
# Publish Debian Package Versions to Bintray
#
# Packages
# - https://bintray.com/beta/#/optum/dev-snapshots/sourcehawk
# - https://bintray.com/beta/#/optum/deb/sourcehawk
# - https://bintray.com/beta/#/optum/ubuntu/sourcehawk
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
BINTRAY_DISTRIBUTIONS="${BINTRAY_DISTRIBUTIONS:-6}"
BINTRAY_ARCHITECTURES="${BINTRAY_ARCHITECTURES:-7}"

# Bintray Variables
BINTRAY_API_URL="https://api.bintray.com"
BINTRAY_COMPONENT="main"
BINTRAY_PACKAGE_VERSION="${PACKAGE_VERSION//"-SNAPSHOT"/}" # Remove "-SNAPSHOT" from Package Version
BINTRAY_PACKAGE_BASE_VERSION="$(echo "$BINTRAY_PACKAGE_VERSION" | cut -f1 -d'~')"
BINTRAY_PRIMARY_DISTRIBUTION="$(echo "$BINTRAY_DISTRIBUTIONS" | cut -d ',' -f1)"
BINTRAY_PRIMARY_ARCHITECTURE="$(echo "$BINTRAY_ARCHITECTURES" | cut -d',' -f1)"

# Construct the upload path
BINTRAY_UPLOAD_PATH="pool/$BINTRAY_COMPONENT/$(echo "$BINTRAY_PACKAGE" | cut -c1-1)/$BINTRAY_PACKAGE_BASE_VERSION/$BINTRAY_PACKAGE-$BINTRAY_REPOSITORY-$BINTRAY_PACKAGE_VERSION-$BINTRAY_PRIMARY_ARCHITECTURE.deb"
if [[ "$BINTRAY_REPOSITORY" == "dev-snapshots" ]]; then
  BINTRAY_UPLOAD_PATH="$BINTRAY_PACKAGE_BASE_VERSION/$BINTRAY_PACKAGE-$BINTRAY_PACKAGE_VERSION-$BINTRAY_RELEASE_REPOSITORY-$BINTRAY_PRIMARY_DISTRIBUTION-$BINTRAY_PRIMARY_ARCHITECTURE.deb"
fi
BINTRAY_UPLOAD_PATH="${BINTRAY_UPLOAD_PATH//"-SNAPSHOT"/}" # Remove "-SNAPSHOT" from Upload Path

# Construct the URL for publishing
BINTRAY_PUBLISH_URL="$BINTRAY_API_URL/content/$BINTRAY_ORGANIZATION/$BINTRAY_REPOSITORY/$BINTRAY_PACKAGE/$BINTRAY_PACKAGE_VERSION/$BINTRAY_UPLOAD_PATH"
if [[ "$BINTRAY_REPOSITORY" != "dev-snapshots" ]]; then
  BINTRAY_PUBLISH_URL="${BINTRAY_PUBLISH_URL};publish=1;deb_distribution=$BINTRAY_DISTRIBUTIONS;deb_component=$BINTRAY_COMPONENT;deb_architecture=$BINTRAY_ARCHITECTURES"
fi

# Publish Package
echo "Publishing Debian package to: $BINTRAY_PUBLISH_URL"
echo -n "Response: "
curl -sfLS -X PUT -T "$PACKAGE_FILE_PATH" -H "Accept: application/json" -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_PUBLISH_URL"
echo " "

if [[ "$BINTRAY_REPOSITORY" != "dev-snapshots" ]]; then
  # Calculate Metadata for Debian Package Repository
  echo "Forcing metadata calculation..."
  echo -n "Response: "
  curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" "$BINTRAY_API_URL/calc_metadata/$BINTRAY_ORGANIZATION/$BINTRAY_REPOSITORY"
  echo " "
fi