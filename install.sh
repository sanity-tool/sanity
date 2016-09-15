#!/bin/bash

# Exit on failure
set -e

if [[ "$TRAVIS_OS_NAME" == "linux" ]]; then
    sudo apt-add-repository "deb http://llvm.org/apt/trusty/ llvm-toolchain-trusty-3.8 main"
    
    wget -O - http://apt.llvm.org/llvm-snapshot.gpg.key|sudo apt-key add -
    sudo apt-get update
    sudo apt-get install clang-3.8 clang-3.8-doc libclang-common-3.8-dev libclang-3.8-dev libclang1-3.8 libclang1-3.8-dbg libllvm-3.8-ocaml-dev libllvm3.8 libllvm3.8-dbg lldb-3.8 llvm-3.8 llvm-3.8-dev llvm-3.8-doc llvm-3.8-examples llvm-3.8-runtime clang-modernize-3.8 clang-format-3.8 python-clang-3.8 lldb-3.8-dev
    #sudo apt-get install llvm # main dependency
    sudo apt-get install swig # wrappers generator

    #sudo apt-get install clang
fi

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew update

    brew install llvm # main dependency
    brew install crosstool-ng # to replace undesired function from llvm core
    brew install swig # wrappers generator
fi