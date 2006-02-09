#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>

#include "rcx_comm.h"

#include "josx_rcxcomm_Tower.h"


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

/* Constants */

static jfieldID _fieldIDPort;
static jfieldID _fieldIDError;

/* Local prototypes */

static void set_error(JNIEnv *env, jobject obj, int error);
static void set_port(JNIEnv *env, jobject obj, rcx_dev_t *port);
static rcx_dev_t *get_port(JNIEnv *env, jobject obj);


/* Public interface */

/* init - Init class */
JNIEXPORT void JNICALL 
Java_josx_rcxcomm_Tower_init(JNIEnv *env, jclass clazz)
{
#ifdef TRACE
	printf("Entering init\n");
#endif	
	_fieldIDPort = (*env)->GetFieldID(env, clazz, "_port", "J");
	if (_fieldIDPort == NULL)
	{
#ifdef TRACE	
		printf("Could not get port field.\n");
#endif			
		return;
	}
	_fieldIDError = (*env)->GetFieldID(env, clazz, "_error", "I");
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

/* Open the IR Tower */
JNIEXPORT jint JNICALL 
Java_josx_rcxcomm_Tower_open(JNIEnv *env, jobject obj, jstring jport, jboolean fastMode)
{
	int result = 0;
	char *tty = NULL;
	rcx_dev_t *port = NULL;

#ifdef TRACE
	rcx_set_debug(1);
	printf("Entering open\n");
#endif

	/* Get the port parameter  */
	tty = (char*)(*env)->GetStringUTFChars(env, jport, 0);

	/* Get a handle for the tower device */
	port = rcx_open(tty, fastMode);
	if (!port) {
		result = RCX_OPEN_FAIL;
	}
	else if (!rcx_is_usb(port)) {
		/* Only serial tower needs wake-up */
		result = rcx_wakeup_tower(port, WAKEUP_TIME_OUT);
	}

	set_error(env, obj, result != 0);
	set_port(env, obj, port);

	(*env)->ReleaseStringUTFChars(env, jport, tty);

#ifdef TRACE
	printf("Exiting open\n");
#endif	
	return (jint)result;
}

/* close - Close the IR Tower */
JNIEXPORT jint JNICALL 
Java_josx_rcxcomm_Tower_close(JNIEnv *env, jobject obj)
{
	rcx_dev_t *port = NULL;
#ifdef TRACE
	printf("Entering close\n");
#endif	

	/* Close the handle */
	port = get_port(env, obj);
	if (port == NULL) {
#ifdef TRACE
		printf("File already closed\n");
#endif		
		return (jint) RCX_ALREADY_CLOSED;
	}
  
	rcx_close(port);
	set_port(env, obj, NULL);

 #ifdef TRACE
	printf("Exiting close\n");
#endif
	return RCX_OK;
}

/* write - write bytes to IR Tower */
JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_write(JNIEnv *env, jobject obj, jbyteArray arr, jint n)
{
#ifdef TRACE
	printf("Entering write\n");
#endif
	// Check that file is open
	rcx_dev_t *port = get_port(env, obj);
	if (!port) {
#ifdef TRACE
		printf("File not open\n");
#endif
		return (jint) RCX_NOT_OPEN;
	}
	jbyte *body = (*env)->GetByteArrayElements(env, arr, 0);
	int result = rcx_write(port, body, n);
	set_error(env, obj, result < 0);
	(*env)->ReleaseByteArrayElements(env, arr, body, 0);
#ifdef TRACE
	printf("Exiting write\n");
#endif
	return (jint)result;
}

/* read - Read Bytes from IR Tower */
JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_read(JNIEnv *env, jobject obj, jbyteArray arr, jint timeout)
{
#ifdef TRACE
	printf("Entering read\n");
#endif
	/* Check that file is open */
	rcx_dev_t *port = get_port(env, obj);
	if (!port) {
#ifdef TRACE	
		printf("File not open\n");
#endif
		return (jint)RCX_NOT_OPEN;
	}

	int size = (*env)->GetArrayLength(env, arr);
	jbyte *body = (*env)->GetByteArrayElements(env, arr, 0);
	int result = rcx_read(port, body, size, timeout);
	(*env)->ReleaseByteArrayElements(env, arr, body, 1);

	set_error(env, obj, result < 0);
#ifdef TRACE
	printf("Exiting read\n");
#endif
	return (jint) result;
}

/* send - send a message to IR Tower */
JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_send(JNIEnv *env, jobject obj, jbyteArray arr, jint n)
{
#ifdef TRACE
	printf("Entering send\n");
#endif
	/* Check that file is open */
	rcx_dev_t *port = get_port(env, obj);
	if (!port) {
#ifdef TRACE	
		printf("File not open\n");
#endif
		return (jint) RCX_NOT_OPEN;
	}
	jbyte* body = (*env)->GetByteArrayElements(env, arr, 0);
	int result = rcx_send(port, body, n);
	(*env)->ReleaseByteArrayElements(env, arr, body, 0);

	set_error(env, obj, result < 0);
#ifdef TRACE
	printf("Exiting send\n");
#endif
	return (jint) result;
}

/* read - Read Bytes from IR Tower */
JNIEXPORT jint JNICALL
Java_josx_rcxcomm_Tower_receive(JNIEnv *env, jobject obj, jbyteArray arr, jint timeout)
{
#ifdef TRACE
	printf("Entering receive\n");
#endif
	/* Check that file is open */
	rcx_dev_t *port = get_port(env, obj);
	if (!port) {
#ifdef TRACE	
		printf("File not open\n");
#endif
		return (jint) RCX_NOT_OPEN;
	}
	int size = (*env)->GetArrayLength(env, arr);
	jbyte* body = (*env)->GetByteArrayElements(env, arr, NULL);
	int actual = rcx_receive(port, body, size, timeout);
	(*env)->ReleaseByteArrayElements(env, arr, body, 1);

	set_error(env, obj, actual < 0);
#ifdef TRACE
	printf("Exiting receive\n");
#endif
	return (jint) actual;
}

/* isRCXAlive - test if RCX is alive */
JNIEXPORT jboolean JNICALL
Java_josx_rcxcomm_Tower_isRCXAlive(JNIEnv *env, jobject obj)
{
#ifdef TRACE
	printf("Entering isRCXAlive\n");
#endif  
	/* Check if RCX is alive */
	rcx_dev_t *port = get_port(env, obj);
	int result = rcx_is_alive(port) == 1;
#ifdef TRACE
	printf("Exiting isRCXAlive\n");
#endif
	return result? JNI_TRUE : JNI_FALSE;
}

/* isUSB - test if IR Tower is an usb tower */
JNIEXPORT jboolean JNICALL
Java_josx_rcxcomm_Tower_isUSB(JNIEnv *env, jobject obj)
{
#ifdef TRACE
	printf("Entering isUSB\n");
#endif  
	/* Check if RCX is alive */
	rcx_dev_t *port = get_port(env, obj);
	int result = rcx_is_usb(port);
#ifdef TRACE
	printf("Exiting isUSB\n");
#endif
	return result ? JNI_TRUE : JNI_FALSE;
}


/* Java attribute interface */

static void set_error(JNIEnv *env, jobject obj, int error)
{
	(*env)->SetIntField(env,
			    obj,
			    _fieldIDError,
			    error ? GetLastError() : 0);
}

static void set_port(JNIEnv *env, jobject obj, rcx_dev_t *port)
{
	/* the warning regarding this cast may be ignored */
	/* or it can be fixed ;-) */
	intptr_t port_as_int = (intptr_t)port;
	(*env)->SetLongField(env, obj, _fieldIDPort, (jlong)port_as_int);
}

static rcx_dev_t *get_port(JNIEnv *env, jobject obj)
{
	/* the warning regarding this cast may be ignored */
	/* or it can be fixed ;-) */
	intptr_t port_as_intptr = (intptr_t)(*env)->GetLongField(env, obj, _fieldIDPort);
	return (rcx_dev_t *)port_as_intptr;
}
