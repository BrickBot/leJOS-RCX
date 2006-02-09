#!/bin/bash

#
# downloadDistributionCheck: utility to download a test class for the lejOS distribution to the RCX
#
# 09/02/06  created Matthias Paul Scholz

echo downloading test class
../bin/lejosdl DistributionSmokeTest.bin
echo done. If no error occurred, please press the RUN Button on your RCX now. The LCD of the RCX should display SMOKE, then TEST
