/*
*  09/23/2002 david <david@csse.uwa.edu.au> modified to support linux usb tower
*/


#include <jni.h>
#include "josx_rcxcomm_Tower.h"
#include <stdio.h>
#include <string.h>

#if defined(_WIN32)
#include <windows.h>
#endif

extern "C" {
#include "rcx_comm.h"
}


#include <stdlib.h>


#define TIME_OUT 100

#define WAKEUP_TIME_OUT 4000

#if !defined(_WIN32)
extern int errno;

int GetLastError() {
  return errno;
}
#endif


// open - Open the IR Tower

JNIEXPORT jint JNICALL 
Java_josx_rcxcomm_Tower_open(JNIEnv *env, jobject obj, jstring jport)
{
  jclass cls;
  jfieldID fid;
  int err = 0;
  FILEDESCR fh;

  int res = 0;

#ifdef TRACE
  rcx_set_debug(1);
  printf("Entering open\n");
#endif

   // Get the port parameter 
  
  char * tty = (char *) env->GetStringUTFChars(jport,0);

  // Get a handle for the tower device

  fh = rcx_init(tty,0);

  // USB Tower does not need wake-up
  
  if (fh == BADFILE) {
	  res = RCX_OPEN_FAIL;
  } else if (!rcx_is_usb()) {
	  res = rcx_wakeup_tower(fh, WAKEUP_TIME_OUT);
  }

  // Get the OS error, if a failure has occurred
  
  if (res != 0) {
    err = GetLastError();
  }

  // Set err in the Java class

  cls = env->GetObjectClass(obj);
  fid = env->GetFieldID(cls,"err","I");

  if (fid == 0) {

#ifdef TRACE
    printf("Could not get field id for err.\n");
#endif

    return (jint) RCX_INTERNAL_ERR;
  }

  env->SetIntField(obj,fid,err);

  // Set usbFlag

  fid = env->GetFieldID(cls,"usbFlag","I");

  if (fid == 0) {

#ifdef TRACE
    printf("Could not get field id for usbFlag.\n");
#endif

    return (jint) RCX_INTERNAL_ERR;
  }

  env->SetIntField(obj,fid,rcx_is_usb());

  // Set the handle

  fid = env->GetFieldID(cls, "fh", "J");

  if (fid == 0) {

#ifdef TRACE
    printf("Could not get field id for fh.\n");
#endif

    return (jint) RCX_INTERNAL_ERR;
  }

  env->SetLongField(obj,fid,(jlong) fh);

  // Release the string parameter

  env->ReleaseStringUTFChars(jport, tty);

#ifdef TRACE
  printf("Exiting open\n");
#endif

  return (jint) res;

}

// close - Close the IR Tower

JNIEXPORT jint JNICALL 
Java_josx_rcxcomm_Tower_close(JNIEnv *env, jobject obj)
{
  jclass cls;
  jfieldID fid;
  FILEDESCR fh;

#ifdef TRACE
  printf("Entering close\n");
#endif

  // Get the file handle

  cls = env->GetObjectClass(obj);
  fid = env->GetFieldID(cls,"fh","J");

  if (fid == 0) {

#ifdef TRACE
      printf("Could not get fh field id.\n");
#endif

      return (jint) RCX_INTERNAL_ERR;
  }

  fh = (FILEDESCR) env->GetLongField(obj,fid);

  // Close the handle

  if (fh == BADFILE) {

#ifdef TRACE
      printf("File already closed\n");
#endif

      return (jint) RCX_ALREADY_CLOSED;
  }
  
  rcx_close(fh);

  // Write closed handle back

  fh = BADFILE;

  env->SetLongField(obj,fid,(jlong) fh);

#ifdef TRACE
  printf("Exiting close\n");
#endif

}

// write - write bytes to IR Tower

JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_write(JNIEnv *env, jobject obj, jbyteArray arr, jint n)
{
    int err = 0;
    FILEDESCR fh;
    jclass cls;
    jfieldID fid;
    size_t actual;

#ifdef TRACE
    printf("Entering write\n");
#endif

    // Get the file handle

    cls = env->GetObjectClass(obj);
    fid = env->GetFieldID(cls,"fh","J");

    if (fid == 0) {

#ifdef TRACE
        printf("Could not get fh field id.\n");
#endif

        return (jint) RCX_INTERNAL_ERR;
    }

    fh = (FILEDESCR) env->GetLongField(obj,fid);

    // Check that file is open

    if (fh == BADFILE) {

#ifdef TRACE
        printf("File not open\n");
#endif

        return (jint) RCX_NOT_OPEN;
    }

    // Get the array

    jbyte *body = env->GetByteArrayElements(arr, 0);
#ifdef TRACE
    hexdump("tower writes", body, n);
#endif

    actual = rcx_write(fh, body, n);

    if (actual < 0) {
        err = GetLastError();
    }

    // Set err in the Java class

    cls = env->GetObjectClass(obj);
    fid = env->GetFieldID(cls,"err","I");

    if (fid == 0) {

#ifdef TRACE
        printf("Could not get field id for err.\n");
#endif

        return (jint) RCX_INTERNAL_ERR;
    }

    env->SetIntField(obj,fid,err);

    // Release the array

    env->ReleaseByteArrayElements(arr, body, 0);

#ifdef TRACE
    printf("Exiting write\n");
#endif

    return (jint) actual;
}

// read - Read Bytes from IR Tower

JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_read(JNIEnv *env, jobject obj, jbyteArray arr)
{
    int err = 0, jsize;
    FILEDESCR fh;
    jclass cls;
    jfieldID fid;
    size_t actual;

#ifdef TRACE
    printf("Entering read\n");
#endif

    // Get the file handle

    cls = env->GetObjectClass(obj);
    fid = env->GetFieldID(cls,"fh","J");

    if (fid == 0) {

#ifdef TRACE
        printf("Could not get fh field id.\n");
#endif  

        return (jint) RCX_INTERNAL_ERR;
    }

    fh = (FILEDESCR) env->GetLongField(obj,fid);

    // Check that file is open

    if (fh == BADFILE) {

#ifdef TRACE
        printf("File not open\n");
#endif

        return (jint) RCX_NOT_OPEN;
    }

    // Get the array

    jsize = env->GetArrayLength(arr);
    jbyte *body = env->GetByteArrayElements(arr, 0);

    actual = rcx_read(fh,body,jsize,TIME_OUT);

    if (actual < 0) err = GetLastError();


    // Set err in the Java class

    cls = env->GetObjectClass(obj);
    fid = env->GetFieldID(cls,"err","I");

    if (fid == 0) {

#ifdef TRACE
        printf("Could not get field id for err.\n");
#endif

        return RCX_INTERNAL_ERR;
    }

    env->SetIntField(obj,fid,err);

    // Release the array and copy it back

    env->ReleaseByteArrayElements(arr, body, 1);

#ifdef TRACE
    printf("Exiting read\n");
#endif  

    return (jint) actual;
}

// send - send a message to IR Tower

JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_send(JNIEnv *env, jobject obj, jbyteArray arr, jint n)
{
    int err = 0;
    FILEDESCR fh;
    jclass cls;
    jfieldID fid;
    size_t actual;

#ifdef TRACE
    printf("Entering send\n");
#endif

    // Get the file handle

    cls = env->GetObjectClass(obj);
    fid = env->GetFieldID(cls,"fh","J");

    if (fid == 0) {

#ifdef TRACE
        printf("Could not get fh field id.\n");
#endif

        return (jint) RCX_INTERNAL_ERR;
    }

    fh = (FILEDESCR) env->GetLongField(obj,fid);

    // Check that file is open

    if (fh == BADFILE) {

#ifdef TRACE
        printf("File not open\n");
#endif

        return (jint) RCX_NOT_OPEN;
    }

    // Get the array

    jbyte *body = env->GetByteArrayElements(arr, 0);

    // Write the bytes

    actual = rcx_send(fh,body,n,1);


    if (actual < 0) {
        err = GetLastError();
    }

    // Flush buffers

#if defined(_WIN32) || defined(__CYGWIN32__)
    FlushFileBuffers (fh);
#endif

    // Set err in the Java class

    cls = env->GetObjectClass(obj);
    fid = env->GetFieldID(cls,"err","I");

    if (fid == 0) {

#ifdef TRACE
        printf("Could not get field id for err.\n");
#endif

        return (jint) RCX_INTERNAL_ERR;
    }

    env->SetIntField(obj,fid,err);

    // Release the array

    env->ReleaseByteArrayElements(arr, body, 0);

#ifdef TRACE
    printf("Exiting send\n");
#endif

    return (jint) actual;
}

// read - Read Bytes from IR Tower

JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_receive(JNIEnv *env, jobject obj, jbyteArray arr)
{
    int err = 0, jsize;
    FILEDESCR fh;
    jclass cls;
    jfieldID fid;
    size_t actual;

#ifdef TRACE
    printf("Entering receive\n");
#endif

    // Get the file handle

    cls = env->GetObjectClass(obj);
    fid = env->GetFieldID(cls,"fh","J");

    if (fid == 0) {

#ifdef TRACE
        printf("Could not get fh field id.\n");
#endif  

        return (jint) RCX_INTERNAL_ERR;
    }

    fh = (FILEDESCR) env->GetLongField(obj,fid);

    // Check that file is open

    if (fh == BADFILE) {

#ifdef TRACE
        printf("File not open\n");
#endif

        return (jint) RCX_NOT_OPEN;
    }

    // Get the array

    jsize = env->GetArrayLength(arr);
    jbyte *body = env->GetByteArrayElements(arr, 0);

    // Receive a packet

    actual = rcx_recv(fh, body, jsize, TIME_OUT, 1);

    if (actual < 0) err = GetLastError();

    // Set err in the Java class

    cls = env->GetObjectClass(obj);
    fid = env->GetFieldID(cls,"err","I");

    if (fid == 0) {

#ifdef TRACE
        printf("Could not get field id for err.\n");
#endif

        return (jint) RCX_INTERNAL_ERR;
    }

    env->SetIntField(obj,fid,err);

    // Release the array and copy it back

    env->ReleaseByteArrayElements(arr, body, 1);

#ifdef TRACE
    printf("Exiting receive\n");
#endif  

    return (jint) actual;
}

// hexdump - print a hex dump to stdout

JNIEXPORT void JNICALL
Java_josx_rcxcomm_Tower_hexdump(JNIEnv *env, jobject obj, jstring jprefix, jbyteArray arr, jint n)
{

#ifdef TRACE
    printf("Entering hexdump\n");
#endif

    //  Get the prefix
    
    const char *prefix = env->GetStringUTFChars(jprefix,0);

    // Get the array

    jbyte *body = env->GetByteArrayElements(arr, 0);

    // Dump the bytes

    hexdump((char *) prefix,body,n);

    // Release the string parameter

    env->ReleaseStringUTFChars(jprefix,prefix);

    // Release the array

    env->ReleaseByteArrayElements(arr, body, 0);

#ifdef TRACE
    printf("Exiting hexdump\n");
#endif

}

// isAlive - test if IR Tower is alive

JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_isAlive(JNIEnv *env, jobject obj)
{
    FILEDESCR fh;
    jclass cls;
    jfieldID fid;
    jint alive;
    
#ifdef TRACE
    printf("Entering isAlive\n");
#endif

    // Get the file handle

    cls = env->GetObjectClass(obj);
    fid = env->GetFieldID(cls,"fh","J");

    if (fid == 0) {

#ifdef TRACE
        printf("Could not get fh field id.\n");
#endif  

        return (jint) RCX_INTERNAL_ERR;
    }

    fh = (FILEDESCR) env->GetLongField(obj,fid);
  
    // Check if RCX is alive

    alive = rcx_is_alive(fh,1);

    
#ifdef TRACE
    printf("Exiting isAlive\n");
#endif

    return (jint) alive;

}

// strerror - get error message corresponding to a result code

JNIEXPORT jstring JNICALL
Java_josx_rcxcomm_Tower_strerror(JNIEnv *env, jobject obj, jint errno)
{

#ifdef TRACE
    printf("Entering strerror\n");
#endif


#ifdef TRACE
    printf("Exiting strerror\n");
#endif

    return env->NewStringUTF((const char *) rcx_strerror((int) errno));

}
