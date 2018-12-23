#!/bin/bash

# Exit on failure
set -e

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew update

    brew cask install java
    brew install cmake || brew upgrade cmake || echo warning ^
    brew install swig # wrappers generator
    brew install ccache

    [ -z $EXTRA_BREW ] || brew install $EXTRA_BREW
fi

if [[ "$TESTED_LANGS" == "Rust" ]]; then
    curl https://sh.rustup.rs -sSf | sh -s -- -y
fi
