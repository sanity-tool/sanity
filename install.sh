#!/bin/bash

# Exit on failure
#set -e

if [[ "$TRAVIS_OS_NAME" == "linux" ]]; then
    pwd
    wget http://llvm.org/releases/3.9.0/clang+llvm-3.9.0-x86_64-linux-gnu-ubuntu-14.04.tar.xz
    tar xf clang+llvm-3.9.0-x86_64-linux-gnu-ubuntu-14.04.tar.xz

    sudo add-apt-repository ppa:ubuntu-toolchain-r/test -y
    sudo apt-get update
    sudo apt-get install libstdc++6-4.7-dev
    sudo apt-get install swig
fi

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew update

    brew install llvm # main dependency
    brew install crosstool-ng # to replace undesired function from llvm core
    brew install swig # wrappers generator
fi