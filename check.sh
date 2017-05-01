#!/bin/sh
# Exit on failure
set -e

for LANG in $LANGS; do
  echo $LANG

  bash <(curl -s https://codecov.io/bash) -F $LANG
done