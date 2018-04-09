#!/usr/bin/env bash

# Exit on failure
set -e

RUSTS="rustc $HOME/.cargo/bin/rustc"

for RUST_BIN in $RUSTS; do
    TESTED_LANG=Rust JAVA_HOME=`/usr/libexec/java_home -v 1.8` mvn clean test -P parser-native
done

CLANGS="clang /usr/local/opt/llvm/bin/clang clang-3.3 clang-3.6 clang-3.7 clang-3.8 /usr/local/opt/llvm@4/bin/clang-4.0 /usr/local/opt/llvm@5/bin/clang-5.0"

for CLANG_BIN in $CLANGS; do
    for TESTED_LANG in "C Cpp ObjectiveC"; do
        TESTED_LANG=$TESTED_LANG CLANG_BIN=$CLANG_BIN JAVA_HOME=`/usr/libexec/java_home -v 1.8` mvn clean test -P parser-native
    done
done

# check other langs and defaults
JAVA_HOME=`/usr/libexec/java_home -v 1.8` mvn clean test -P parser-native