#!/usr/bin/env bash

set -o errexit

if (( $# != 1 )); then
  >&2 echo Usage: $(basename ${0}) GPG-KEYNAME
  >&2 echo
  >&2 echo Arguments:
  >&2 echo "  GPG-KEYNAME  The short key ID of the GPG key used to sign the deliverables."
  >&2 echo "               Although using GPG sub-keys would be superior, owing to difficulties with"
  >&2 echo "               Maven, this build system is currently designed to handle only master keys."
  exit 1
fi

set -o xtrace

export TZ=UTC

# Aim for a clean build
find . -depth -name target -type d -exec rm -r {} +

# Deploy signed artifacts
mvn -Prelease verify deploy -Dgpg.keyname=$@
