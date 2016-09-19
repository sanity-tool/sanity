#!/bin/bash

case `uname` in
    Linux)
        # use defaults
    ;;
    Darwin)
        TEST_SUPPORTED_EXTS=".c;.cpp;.m"
        TEST_SUPPORTED_DIRS="c;cpp;o-c"
    ;;
esac