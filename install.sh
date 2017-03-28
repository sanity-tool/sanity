#!/bin/bash

# Exit on failure
set -e

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew update

    brew cask install java
    brew install llvm # main dependency
    brew install cmake || brew upgrade cmake || echo warning ^
    brew install swig # wrappers generator

    [ -z $EXTRA_BREW ] || brew install $EXTRA_BREW
fi

if [[ "$LANG" == "Rust" ]]; then
    curl https://sh.rustup.rs -sSf | sh -s -- -y
fi
