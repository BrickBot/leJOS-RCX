#ifndef IRTOWER_H_INCLUDED
#define IRTOWER_H_INCLUDED

void setError (JNIEnv* env, jobject obj, bool error);
void setPort (JNIEnv* env, jobject obj, void* port);
void* getPort (JNIEnv* env, jobject obj);

#endif
