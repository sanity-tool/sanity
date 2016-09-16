#!/bin/bash

# Exit on failure
set -e

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew update

    brew cask install java
    brew install llvm # main dependency
    brew install cmake || brew upgrade cmake || echo warning ^
    brew install swig # wrappers generator

    # tested languages
    brew install rust
    [ -z $EXTRA_BREW ] || brew install $EXTRA_BREW
fi
