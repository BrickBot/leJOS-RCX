#
# This script installs a cross gcc for h8300 on your system.
#
# Copy these archives to the directory where this script resides:
# - binutils-2.13.2.tar.gz
# - gcc-core-3.2.1.tar.gz
# - newlib-1.11.0.tar.gz
# - gdb-5.2.1.tar.gz
#
# TODO
# - convert this script to a makefile
#
# Markus Heiden (Markus_Heiden@public.uni-hamburg.de)
#

echo ""
echo "====> extract binutils"
echo ""
tar xfz binutils-2.13.2.tar.gz
echo ""
echo "====> build binutils"
echo ""
cd binutils-2.13.2
mkdir build_binutils
cd build_binutils
../configure --prefix=/usr/local --target=h8300-hms
make CFLAGS="-O2 -fomit-frame-pointer" all
make install
cd ../..

echo ""
echo "====> extract gcc"
echo ""
export PATH=$PATH:/usr/local/bin
tar xfz gcc-core-3.2.1.tar.gz
tar xfz newlib-1.11.0.tar.gz
echo ""
echo "====> build gcc"
echo ""
cd gcc-3.2.1
ln -s ../newlib-1.11.0/newlib .
patch -p1 < ../h8300-hms-gcc-3.1-1.patch
mkdir build_gcc
cd build_gcc
../configure --prefix=/usr/local --target=h8300-hms --enable-languages=c --with-newlib
make CFLAGS="-O2 -fomit-frame-pointer" all
make install
cd ../..

echo ""
echo "====> extract gdb"
echo ""
tar xfz gdb-5.2.1.tar.gz
echo ""
echo "====> build gdb"
echo ""
cd gdb-5.2.1
mkdir build_gdb
cd build_gdb
../configure --prefix=/usr/local --target=h8300-hms
make CFLAGS="-O2 -fomit-frame-pointer" all
make install
cd ../..
