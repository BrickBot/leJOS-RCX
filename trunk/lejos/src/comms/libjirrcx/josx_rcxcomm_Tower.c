/***************************************************************
*                                                              *
* Tower.c                                                      *
*                                                              *
* Description:                                                 *
* Java Native Interface to RCX command communicator.           *
*                                                              *
* Author:    Lawrie Griffiths	                               *
*                                                              *
***************************************************************/

#include<stdio.h>
#include<string.h>
#include <jni.h>

#include "josx_rcxcomm_Tower.h"
#include "rcx.h"


JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_open
(JNIEnv * env, jobject object, jstring aString)
{
	return rcx_open();
}

JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_close
  (JNIEnv * env, jobject object)
{
	int result = 0;
	return rcx_close();
}

JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_send
  (JNIEnv * env, jobject object, jbyteArray aArray, jint aInt)
{
  	int result = 0;

	jbyte *cbuf;
	int i;

	int givenLength = ((int) aInt);

	cbuf = (*env)->GetByteArrayElements(env, aArray, 0);

	result = rcx_send(cbuf, givenLength);

	(*env)->ReleaseByteArrayElements(env, aArray, &cbuf[0], 0);

	return result;
}

JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_write
  (JNIEnv * env, jobject object, jbyteArray aArray, jint aInt)
{
  	int result = 0, i;

	jbyte *cbuf;

	int givenLength = ((int) aInt);

	cbuf = (*env)->GetByteArrayElements(env, aArray, 0);

	for (i=0; i < givenLength; i++)
	{
		result = rcx_send_byte(cbuf[i]);
	}

	(*env)->ReleaseByteArrayElements(env, aArray, &cbuf[0], 0);

	return (jint)givenLength;
}

JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_read
  (JNIEnv * env, jobject object, jbyteArray aArray)
{

	int result, len, i;

	jbyte *cbuf;

	cbuf = (*env)->GetByteArrayElements(env, aArray, 0);

	len = (*env)->GetArrayLength(env, aArray);

	// printf("Tower: Requesting %d bytes\n", len);

	for(i=0;i<len;i++) {
		result = rcx_receive_byte(&cbuf[i]);
		if (result != RCX_OK) break;
		// printf("Tower: Received %x\n", cbuf[i] & 0xff);
	}

	(*env)->ReleaseByteArrayElements(env, aArray, &cbuf[0], 0);

	// printf("Tower: Returning %d\n", i);

	return (jint) i;
}

JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_receive
  (JNIEnv * env, jobject object, jbyteArray aArray)
{

	jbyte *cbuf;
	int len, ret;
	cbuf = (*env)->GetByteArrayElements(env, aArray, 0);

	ret = rcx_receive(cbuf, (*env)->GetArrayLength(env, aArray), &len);

	(*env)->ReleaseByteArrayElements(env, aArray, cbuf, 0);

	if (ret == RCX_OK) return len;
	else return ret;
}

/*
 * Class:     jnilirc_JniRcxIr
 * Method:    message
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_message
  (JNIEnv * env, jobject obj, jstring str)
{
	printf("MESSAGE MESSAGE\n");
	return 0;
}

