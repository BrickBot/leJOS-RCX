#!/bin/sh

TEST_CLASSES="Test04 Test06 Test07 Test11 Test12 Test13 Test14 Test26 Test28 Test33 TestRuntime TestPriorities TestDaemon TestThreadState TestMonitor TestNat"
OUT_FILE=regression.log
GOLD_FILE=regression.gold
export CLASSPATH=.
export PATH=../bin:$PATH

# allow core dump

rm $OUT_FILE
for i in $TEST_CLASSES
do
  echo ------------------ Compiling $i
  lejosc $i.java
  echo ------------------ Linking $i
  emu-lejos $i -o $i.tvm
  echo ------------------ Running $i
  echo "----------------- Run of $i.tvm" >> $OUT_FILE
  emu-lejosrun $i.tvm >> $OUT_FILE 2>&1
  if [ -f core ];
  then
    echo "----------------- Saving backtrace to $i.backtrace"
    gdb --quiet --command=backtrace.gdb --batch ../bin/emu-lejosrun core >> $i.backtrace
    rm core
  fi
done

if [ ! -f $GOLD_FILE ]; 
then
  echo "##### CREATED GOLD FILE #####"
  cp $OUT_FILE $GOLD_FILE
else
  echo "##### BEGIN REGRESSION DIFF #####"
  diff -w $GOLD_FILE $OUT_FILE 
  echo "#####  END REGRESSION DIFF  #####"
fi
