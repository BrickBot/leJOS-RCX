#!/bin/sh

TEST_CLASSES="Test04 Test06 Test11 Test12 Test13 Test15 Test16 Test17"
OUT_FILE=regression.log
export TINYVMPATH=.

rm $OUT_FILE
for i in $TEST_CLASSES
do
  echo ------------------ Compiling $i
  tvmc $i.java
  echo ------------------ Linking $i
  tvmld-emul $i -o $i.tvm
  echo ------------------ Running $i
  echo "----------------- Run of $i.tvm" >> $OUT_FILE
  tvm-emul $i.tvm >> $OUT_FILE
done
