/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class josx_rcxcomm_Tower */

#ifndef _Included_josx_rcxcomm_Tower
#define _Included_josx_rcxcomm_Tower
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     josx_rcxcomm_Tower
 * Method:    close
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_setFast
  (JNIEnv *, jobject, jint);

/*
 * Class:     josx_rcxcomm_Tower
 * Method:    close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_close
  (JNIEnv *, jobject);

/*
 * Class:     josx_rcxcomm_Tower
 * Method:    hexdump
 * Signature: (Ljava/lang/String;[BI)V
 */
JNIEXPORT void JNICALL Java_josx_rcxcomm_Tower_hexdump
  (JNIEnv *, jobject, jstring, jbyteArray, jint);

/*
 * Class:     josx_rcxcomm_Tower
 * Method:    isAlive
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_isAlive
  (JNIEnv *, jobject);

/*
 * Class:     josx_rcxcomm_Tower
 * Method:    open
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_open
  (JNIEnv *, jobject, jstring);

/*
 * Class:     josx_rcxcomm_Tower
 * Method:    read
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_read
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     josx_rcxcomm_Tower
 * Method:    receive
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_receive
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     josx_rcxcomm_Tower
 * Method:    send
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_send
  (JNIEnv *, jobject, jbyteArray, jint);

/*
 * Class:     josx_rcxcomm_Tower
 * Method:    write
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_josx_rcxcomm_Tower_write
  (JNIEnv *, jobject, jbyteArray, jint);

#ifdef __cplusplus
}
#endif
#endif
