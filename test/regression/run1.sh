#!/bin/sh
set -x
export CLASSPATH=.
export PATH=.:../bin:$PATH

echo ------------------ Compiling $1
lejosc $1.java
echo ------------------ Linking $1
emu-lejos -verbose -o $1.tvm $1 >$1.sig
echo ------------------ Running $1
emu-lejosrun -v $1.tvm
