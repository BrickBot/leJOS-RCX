#ifndef IRTOWER_H_INCLUDED
#define IRTOWER_H_INCLUDED

void setError (JNIEnv* env, jobject obj, bool error);
void setUSB (JNIEnv* env, jobject obj, jboolean usb);
void setFileHandle (JNIEnv* env, jobject obj, FILEDESCR fh);
FILEDESCR getFileHandle (JNIEnv* env, jobject obj);

#endif
