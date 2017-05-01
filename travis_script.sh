#!/usr/bin/env bash

for LANG in $LANGS; do
  mvn -P $SANITY_PROFILE test -B
  bash <(curl -s https://codecov.io/bash) -c -F $LANG
done
