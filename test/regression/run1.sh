#!/bin/sh
export CLASSPATH=.
export PATH=.:../bin:$PATH

echo ------------------ Compiling $1
lejosjc $1.java
echo ------------------ Linking $1
emu-lejos -o $1.tvm $1 >$1.sig
echo ------------------ Running $1
emu-lejosrun -v $1.tvm
