#!/bin/sh

CLASSPATH=.
tvmc View.java
tvmld View -o View.tvm

echo "Run 'tvm View.tvm' to download the program."