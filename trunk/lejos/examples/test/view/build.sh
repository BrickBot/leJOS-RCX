#!/bin/sh

CLASSPATH=.
export CLASSPATH
../../bin/lejosc *.java
../../bin/lejos -verbose View -o View.bin > View.sig

echo "Run 'lejosrun View.bin' to download the program."
