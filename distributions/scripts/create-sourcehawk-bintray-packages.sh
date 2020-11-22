#!/bin/bash

##############################################################################
#
# Create Bintray Packages in Optum's repos
#
# Packages
# - https://bintray.com/beta/#/optum/deb/sourcehawk
# - https://bintray.com/beta/#/optum/ubuntu/sourcehawk
# - https://bintray.com/beta/#/optum/centos/sourcehawk
# - https://bintray.com/beta/#/optum/fedora/sourcehawk
#
##############################################################################

set -e

ORG="optum"
NAME="sourcehawk"
DESCRIPTION="Sourcehawk is an extensible compliance as code tool which allows development teams to run compliance scans on their source code."

# Bintray Common Variables
BINTRAY_API_URL="https://api.bintray.com"
BINTRAY_ORG="$ORG"
BINTRAY_PACKAGE="$NAME"
BINTRAY_PACKAGE_URL="$BINTRAY_API_URL/packages/$BINTRAY_ORG"

cat > package.json << EOF
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

REPOS=(deb ubuntu centos fedora)

for repo in "${REPOS[@]}"; do

  echo "Creating $NAME package in $BINTRAY_ORG/$repo..."
  echo ""
  curl -sfLS -X POST -u "${BINTRAY_USERNAME}:${BINTRAY_API_KEY}" \
    -H "Content-type: application/json" --data-binary "@package.json" \
    "$BINTRAY_PACKAGE_URL/$repo"
  echo ""

done


