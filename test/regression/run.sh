#!/bin/sh

TEST_CLASSES="Test04 Test06 Test11 Test12 Test13 Test14 Test15 Test17"
OUT_FILE=regression.log
GOLD_FILE=regression.gold
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

if [ ! -f $GOLD_FILE ]; 
then
  echo "##### CREATED GOLD FILE #####"
  cp $OUT_FILE $GOLD_FILE
else
  echo "##### BEGIN REGRESSION DIFF #####"
  diff $OUT_FILE $GOLD_FILE
  echo "#####  END REGRESSION DIFF  #####"
fi
