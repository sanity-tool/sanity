#!/bin/bash

# Exit on failure
set -e

if [[ "$TRAVIS_OS_NAME" == "linux" ]]; then
    apt-get install llvm # main dependency
    apt-get install swig # wrappers generator

    apt-get install clang
fi

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew update

    brew install llvm # main dependency
    brew install crosstool-ng # to replace undesired function from llvm core
    brew install swig # wrappers generator
fi