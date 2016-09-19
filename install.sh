#!/bin/bash

# Exit on failure
set -e

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew update

    brew install llvm # main dependency
    brew install crosstool-ng # to replace undesired function from llvm core
    brew install swig # wrappers generator

    TEST_SUPPORTED_EXTS=".c;.cpp;.m"
    TEST_SUPPORTED_DIRS="c;cpp;o-c"
fi