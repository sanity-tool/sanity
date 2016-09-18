#!/bin/bash

# Exit on failure # todo restore
#set -e

if [[ "$TRAVIS_OS_NAME" == "linux" ]]; then
    sudo apt-get update && sudo apt-get install oracle-java8-installer

    sudo add-apt-repository ppa:ubuntu-toolchain-r/test -y
    sudo apt-get update
    sudo apt-get install gcc-4.9 g++-4.9
    sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.9 60 --slave /usr/bin/g++ g++ /usr/bin/g++-4.9

    pwd
    wget http://llvm.org/releases/3.9.0/clang+llvm-3.9.0-x86_64-linux-gnu-ubuntu-14.04.tar.xz
    tar xf clang+llvm-3.9.0-x86_64-linux-gnu-ubuntu-14.04.tar.xz

    wget https://github.com/swig/swig/archive/rel-3.0.10.tar.gz
    tar xf rel-3.0.10.tar.gz
    (cd swig-rel-3.0.10 && ./autogen.sh && mkdir -p build/build && cd build/build && ../../configure && make && sudo make install)

    sudo add-apt-repository ppa:ubuntu-toolchain-r/test -y
    sudo apt-get update
    sudo apt-get install libstdc++6-4.7-dev
fi

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew update

    brew install llvm # main dependency
    brew install crosstool-ng # to replace undesired function from llvm core
    brew install swig # wrappers generator
fi