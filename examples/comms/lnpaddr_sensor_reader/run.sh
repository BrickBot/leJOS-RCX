#!/bin/sh
set -x
java -classpath .\;../../../lib/pcrcxcomm.jar -Djava.library.path=../../../bin LNPAddrReadSensor
