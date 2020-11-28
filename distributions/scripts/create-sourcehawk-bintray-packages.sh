#!/bin/bash

##############################################################################################################
#
# Create Bintray Packages in Optum's repos
#
##############################################################################################################

set -e

DIR="$( cd "$( dirname "$( dirname "$( dirname "${BASH_SOURCE[0]}" )")")" && pwd )"

ORG="optum"
NAME="sourcehawk"
DESCRIPTION="Sourcehawk is an extensible compliance as code tool which allows development teams to run compliance scans on their source code."

# Bintray Common Variables
BINTRAY_API_URL="https://api.bintray.com"
BINTRAY_ORG="$ORG"
BINTRAY_PACKAGE="$NAME"
BINTRAY_PACKAGE_URL="$BINTRAY_API_URL/packages/$BINTRAY_ORG"

# Create request body
cat > "$DIR/package.json" << EOF
{
  "name": "$BINTRAY_PACKAGE",
  "desc": "$DESCRIPTION",
  "labels": ["$BINTRAY_PACKAGE", "compliance"],
  "licenses": ["GPL-3.0"],
  "vcs_url": "https://github.com/$ORG/$NAME.git",
  "website_url": "https://$ORG.github.io/$NAME",
  "issue_tracker_url": "https://github.com/$ORG/$NAME/issues",
  "github_repo": "$ORG/$NAME",
  "public_download_numbers": true,
  "public_stats": true
}
EOF

REPOS=(deb ubuntu centos fedora dev-snapshots)

for repo in "${REPOS[@]}"; do

  echo "Creating $NAME package in $BINTRAY_ORG/$repo..."
  echo ""
  curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" \
    -H "Content-Type: application/json" -H "Accept: application/json" \
    --data-binary "@$DIR/package.json" \
    "$BINTRAY_PACKAGE_URL/$repo"
  echo ""

done


