/*
*  09/23/2002 david <david@csse.uwa.edu.au> modified to support linux usb tower
*/

#include <jni.h>
#include "josx_rcxcomm_Tower.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>

#include "rcx_comm.h"
#include "Tower.h"

#define WAKEUP_TIME_OUT 4000

#if defined(_WIN32)
#include <windows.h>
#else
#include <errno.h>

int GetLastError() 
{
  return errno;
}
#endif

//
// Constants
//

static jfieldID _fieldIDPort;
static jfieldID _fieldIDError;

//
// public interface
//

// open - Open the IR Tower
JNIEXPORT jint JNICALL 
Java_josx_rcxcomm_Tower_open(JNIEnv *env, jobject obj, jstring jport, jboolean fastMode)
{
  int result = 0;

#ifdef TRACE
  rcxSetDebug(true);
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

  return RCX_OK;
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

    jbyte* body = env->GetByteArrayElements(arr, 0);
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
Java_josx_rcxcomm_Tower_read(JNIEnv *env, jobject obj, jbyteArray arr, jint timeout)
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

    int size = env->GetArrayLength(arr);
    jbyte* body = env->GetByteArrayElements(arr, 0);
    int result = rcxRead(port, body, size, timeout);
    env->ReleaseByteArrayElements(arr, body, 1);

	 setError(env, obj, result < 0);

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

    jbyte* body = env->GetByteArrayElements(arr, 0);
    int result = rcxSend(port, body, n);
    env->ReleaseByteArrayElements(arr, body, 0);

    setError(env, obj, result < 0);

#ifdef TRACE
    printf("Exiting send\n");
#endif
    return (jint) result;
}

// read - Read Bytes from IR Tower
JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_receive(JNIEnv *env, jobject obj, jbyteArray arr, jint timeout)
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

    int size = env->GetArrayLength(arr);
    jbyte* body = env->GetByteArrayElements(arr, NULL);
    int actual = rcxReceive(port, body, size, timeout);
    env->ReleaseByteArrayElements(arr, body, 1);

    setError(env, obj, actual < 0);

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
    return result? JNI_TRUE : JNI_FALSE;
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
    return result? JNI_TRUE : JNI_FALSE;
}

//
// Java attribute interface
//

void setError (JNIEnv* env, jobject obj, bool error)
{
   env->SetIntField(obj, _fieldIDError, error? GetLastError() : 0);
}

void setPort (JNIEnv* env, jobject obj, void* port)
{
   // the warning regarding this cast may be ignored
   intptr_t int_port = (intptr_t) port;
   env->SetLongField(obj, _fieldIDPort, (jlong) int_port);
}

void* getPort (JNIEnv* env, jobject obj)
{
   // the warning regarding this cast may be ignored
   intptr_t int_port = (intptr_t) env->GetLongField(obj, _fieldIDPort);
   return (void*) int_port;
}

// init - Init class
JNIEXPORT void JNICALL 
Java_josx_rcxcomm_Tower_init(JNIEnv *env, jclass clazz)
{
#ifdef TRACE
  printf("Entering init\n");
#endif
   _fieldIDPort = env->GetFieldID(clazz, "_port", "J");
   if (_fieldIDPort == NULL)
   {
#ifdef TRACE
      printf("Could not get port field.\n");
#endif
   	return;
   }
   _fieldIDError = env->GetFieldID(clazz, "_error", "I");
   if (_fieldIDError == NULL)
   {
#ifdef TRACE
      printf("Could not get error code field.\n");
#endif
   	return;
   }
#ifdef TRACE
  printf("Exiting init\n");
#endif
}
