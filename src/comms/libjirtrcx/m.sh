jdk="C:\JBuilder3\java"
export jdk
gcc -mno-cygwin -I$jdk/include -I$jdk/include/win32 -Wl,--add-stdcall-alias -shared -o irtower.dll irtower.cpp rcx_comm.cpp
