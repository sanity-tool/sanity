#!/usr/bin/env bash

for TESTED_LANG in $TESTED_LANGS; do
  mvn -P $SANITY_PROFILE clean test -B
  bash <(curl -s https://codecov.io/bash) -c -F $TESTED_LANG
done
