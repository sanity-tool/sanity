#!/bin/bash

# Exit on failure
set -e

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew update

    brew cask install java
    brew install llvm # main dependency
    brew install swig # wrappers generator

    [ -z $EXTRA_BREW ] || brew install $EXTRA_BREW
fi
