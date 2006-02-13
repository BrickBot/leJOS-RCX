#!/bin/sh
if [ -d /Developer/SDKs/MacOSX10.4u.sdk ] ; then
  echo "-isysroot /Developer/SDKs/MacOSX10.4u.sdk -arch i386 -arch ppc"
fi

