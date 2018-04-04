#!/usr/bin/env bash

# Exit on failure
set -e

CLANGS="clang clang-3.3 clang-3.6 clang-3.7 clang-3.8 /usr/local/opt/llvm@4/bin/clang-4.0 /usr/local/opt/llvm@5/bin/clang-5.0"

for CLANG_BIN in $CLANGS; do
    JAVA_HOME=`/usr/libexec/java_home -v 1.8` mvn clean test -P parser-native
done
