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

#include "Tower.h"

#define TIME_OUT 100
#define WAKEUP_TIME_OUT 4000

#if !defined(_WIN32)
#include <errno.h>

int GetLastError() {
  return errno;
}
#endif

// open - Open the IR Tower

JNIEXPORT jint JNICALL 
Java_josx_rcxcomm_Tower_open(JNIEnv *env, jobject obj, jstring jport, jboolean fastMode)
{
  int result = 0;

#ifdef TRACE
  rcxSetDebug(1);
  printf("Entering open\n");
#endif

   // Get the port parameter 
  char* tty = (char*) env->GetStringUTFChars(jport, 0);

  // Get a handle for the tower device
  void* port = rcxOpen(tty, fastMode);
  if (port == NULL) 
  {
	  result = RCX_OPEN_FAIL;
  } 
  else if (!rcxIsUsb(port)) 
  {
    // Only serial tower needs wake-up
	  result = rcxWakeupTower(port, WAKEUP_TIME_OUT);
  }

  setError(env, obj, result != 0);
  setPort(env, obj, port);

  env->ReleaseStringUTFChars(jport, tty);

#ifdef TRACE
  printf("Exiting open\n");
#endif

  return (jint) result;
}

// close - Close the IR Tower

JNIEXPORT jint JNICALL 
Java_josx_rcxcomm_Tower_close(JNIEnv *env, jobject obj)
{
#ifdef TRACE
  printf("Entering close\n");
#endif

  // Close the handle
  void* port = getPort(env, obj);
  if (port == NULL) 
  {
#ifdef TRACE
      printf("File already closed\n");
#endif

      return (jint) RCX_ALREADY_CLOSED;
  }
  
  rcxClose(port);
  setPort(env, obj, NULL);

#ifdef TRACE
  printf("Exiting close\n");
#endif
}

// write - write bytes to IR Tower

JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_write(JNIEnv *env, jobject obj, jbyteArray arr, jint n)
{
#ifdef TRACE
    printf("Entering write\n");
#endif

    // Check that file is open
    void* port = getPort(env, obj);
    if (port == NULL) 
    {
#ifdef TRACE
        printf("File not open\n");
#endif

        return (jint) RCX_NOT_OPEN;
    }

    // Get the array
    jbyte* body = env->GetByteArrayElements(arr, 0);
#ifdef TRACE
    hexdump("tower writes", body, n);
#endif

    int result = rcxWrite(port, body, n);

    setError(env, obj, result < 0);

    env->ReleaseByteArrayElements(arr, body, 0);

#ifdef TRACE
    printf("Exiting write\n");
#endif

    return (jint) result;
}

// read - Read Bytes from IR Tower

JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_read(JNIEnv *env, jobject obj, jbyteArray arr)
{
#ifdef TRACE
    printf("Entering read\n");
#endif

    // Check that file is open
    void* port = getPort(env, obj);
    if (port == NULL) 
    {
#ifdef TRACE
        printf("File not open\n");
#endif

        return (jint) RCX_NOT_OPEN;
    }

    // Get the array
    int size = env->GetArrayLength(arr);
    jbyte* body = env->GetByteArrayElements(arr, 0);

    int result = rcxRead(port, body, size, TIME_OUT);

	 setError(env, obj, result < 0);

    env->ReleaseByteArrayElements(arr, body, 1);

#ifdef TRACE
    printf("Exiting read\n");
#endif  

    return (jint) result;
}

// send - send a message to IR Tower

JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_send(JNIEnv *env, jobject obj, jbyteArray arr, jint n)
{
#ifdef TRACE
    printf("Entering send\n");
#endif

    // Check that file is open
    void* port = getPort(env, obj);
    if (port == NULL) 
    {
#ifdef TRACE
        printf("File not open\n");
#endif

        return (jint) RCX_NOT_OPEN;
    }

    // Get the array
    jbyte* body = env->GetByteArrayElements(arr, 0);

    // Write the bytes
    int result = rcxSend(port, body, n);

    // Flush buffers
#if defined(_WIN32) || defined(__CYGWIN32__)
    FlushFileBuffers (((Port*) port)->fileHandle);
#endif

    setError(env, obj, result < 0);

    env->ReleaseByteArrayElements(arr, body, 0);

#ifdef TRACE
    printf("Exiting send\n");
#endif

    return (jint) result;
}

// read - Read Bytes from IR Tower

JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_receive(JNIEnv *env, jobject obj, jbyteArray arr)
{
#ifdef TRACE
    printf("Entering receive\n");
#endif

    // Check that file is open
    void* port = getPort(env, obj);
    if (port == NULL) 
    {
#ifdef TRACE
        printf("File not open\n");
#endif

        return (jint) RCX_NOT_OPEN;
    }

    // Get the array
    int size = env->GetArrayLength(arr);
    jbyte* body = env->GetByteArrayElements(arr, 0);

    // Receive a packet
    int actual = rcxReceive(port, body, size, TIME_OUT);

    setError(env, obj, actual < 0);

    env->ReleaseByteArrayElements(arr, body, 1);

#ifdef TRACE
    printf("Exiting receive\n");
#endif  

    return (jint) actual;
}

// isRCXAlive - test if RCX is alive

JNIEXPORT jboolean JNICALL
Java_josx_rcxcomm_Tower_isRCXAlive(JNIEnv *env, jobject obj)
{
#ifdef TRACE
    printf("Entering isRCXAlive\n");
#endif
  
    // Check if RCX is alive
    void* port = getPort(env, obj);
    bool result = rcxIsAlive(port) == 1;

#ifdef TRACE
    printf("Exiting isRCXAlive\n");
#endif

    return (jboolean) result;
}

// isUSB - test if IR Tower is an usb tower

JNIEXPORT jboolean JNICALL
Java_josx_rcxcomm_Tower_isUSB(JNIEnv *env, jobject obj)
{
#ifdef TRACE
    printf("Entering isUSB\n");
#endif
  
    // Check if RCX is alive
    void* port = getPort(env, obj);
    bool result = rcxIsUsb(port);

#ifdef TRACE
    printf("Exiting isUSB\n");
#endif

    return (jboolean) result;
}

//
// Java attribute interface
//

void setError (JNIEnv* env, jobject obj, bool error)
{
   jclass cls = env->GetObjectClass(obj);
   jfieldID fid = env->GetFieldID(cls, "_error", "I");
   if (fid != 0)
   {
      env->SetIntField(obj, fid, error? GetLastError() : 0);
   }
#ifdef TRACE
   else
   {
      printf("Could not get error code field.\n");
   }
#endif
}

void setPort (JNIEnv* env, jobject obj, void* port)
{
   jclass cls = env->GetObjectClass(obj);
   jfieldID fid = env->GetFieldID(cls, "_port", "J");
   if (fid != 0)
   {
      // the warning regarding this cast may be ignored
      env->SetLongField(obj,fid,(jlong) port);
   }
#ifdef TRACE
   else
   {
      printf("Could not get file handle field.\n");
   }
#endif
}

void* getPort (JNIEnv* env, jobject obj)
{
   jclass cls = env->GetObjectClass(obj);
   jfieldID fid = env->GetFieldID(cls, "_port", "J");
   if (fid == 0)
   {
#ifdef TRACE
      printf("Could not get file handle field.\n");
#endif

		return NULL;
   }

   // the warning regarding this cast may be ignored
   void* port = (void*) env->GetLongField(obj, fid);
   return port;
}
