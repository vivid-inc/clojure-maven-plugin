#!/usr/bin/env bash

set -e
set -x

export TZ=UTC

# Aim for a clean build
find . -depth -name target -type d -exec rm -r {} +

# Deploy signed artifacts
# The GPG signing key ID needs to be the first parameter to this script
mvn -Prelease verify deploy -Dgpg.keyname=$@
