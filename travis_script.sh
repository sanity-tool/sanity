#!/usr/bin/env bash

# Exit on failure
set -e

for TESTED_LANG in $TESTED_LANGS; do
  mvn -P $SANITY_PROFILE clean test -B
  bash <(curl -s https://codecov.io/bash) -c -F $TESTED_LANG
done
