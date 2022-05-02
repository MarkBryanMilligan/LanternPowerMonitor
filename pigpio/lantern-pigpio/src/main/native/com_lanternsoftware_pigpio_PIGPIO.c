#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <pigpio.h>
#include "com_lanternsoftware_pigpio_PIGPIO.h"


JavaVM *callback_jvm;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	JNIEnv *env;
    if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_2))
    {
        return JNI_ERR;
    }
    callback_jvm = jvm;
	return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved)
{
	return;
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_gpioInitialise
  (JNIEnv *env, jclass class)
{
    gpioCfgSetInternals (gpioCfgGetInternals () | PI_CFG_NOSIGHANDLER);
    return gpioInitialise();
}

JNIEXPORT void JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_gpioTerminate(JNIEnv *env, jclass class)
{
    return gpioTerminate();
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_gpioSetMode
  (JNIEnv *env, jclass class, jint gpio, jint mode)
{
    return gpioSetMode((unsigned)gpio, (unsigned)mode);
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_gpioGetMode
  (JNIEnv *env, jclass class, jint gpio)
{
    return gpioGetMode((unsigned)gpio);
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_gpioSetPullUpDown
  (JNIEnv *env, jclass class, jint gpio, jint pud)
{
    return gpioSetPullUpDown((unsigned)gpio, (unsigned)pud);
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_gpioRead
  (JNIEnv *env, jclass class, jint gpio)
{
    return gpioRead((unsigned)gpio);
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_gpioWrite
  (JNIEnv *env, jclass class, jint gpio, jint level)
{
    return gpioWrite((unsigned)gpio, (unsigned)level);
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_spiOpen
  (JNIEnv *env, jclass class, jint spiChan, jint baud, jint spiFlags)
{
    return spiOpen((unsigned)spiChan, (unsigned)baud, (unsigned)spiFlags);
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_spiClose
  (JNIEnv *env, jclass class, jint handle)
{
    return spiClose((unsigned)handle);
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_spiRead
  (JNIEnv *env, jclass class, jint handle, jbyteArray data, jint offset, jint count)
{
    jbyte *buffer = (*env)->GetByteArrayElements(env, data, 0);
    jsize max_length = (*env)->GetArrayLength(env, data) - offset;
    int length = (count > max_length) ? max_length : count;
    jbyte *offsetBuffer = buffer + offset;
	jint result = spiRead((unsigned)handle, (char *)offsetBuffer, (unsigned)length);
	(*env)->ReleaseByteArrayElements(env, data, buffer, 0);
	return result;
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_spiWrite
  (JNIEnv *env, jclass class, jint handle, jbyteArray data, jint offset, jint count)
{
    jbyte *buffer = (*env)->GetByteArrayElements(env, data, 0);
    jsize max_length = (*env)->GetArrayLength(env, data) - offset;
    int length = (count > max_length) ? max_length : count;
    jbyte *offsetBuffer = buffer + offset;
	jint result = spiWrite((unsigned)handle, (char *)offsetBuffer, (unsigned)length);
	(*env)->ReleaseByteArrayElements(env, data, buffer, JNI_ABORT);
	return result;
}

JNIEXPORT jint JNICALL Java_com_lanternsoftware_pigpio_PIGPIO_spiXfer
  (JNIEnv *env, jclass class, jint handle, jbyteArray writeData, jint writeOffset, jbyteArray readData, jint readOffset, jint count)
{
    jbyte *writeBuffer = (*env)->GetByteArrayElements(env, writeData, 0);
    jbyte *readBuffer = (*env)->GetByteArrayElements(env, readData, 0);
    jsize max_length = (*env)->GetArrayLength(env, writeData) - writeOffset;
    int length = (count > max_length) ? max_length : count;
    jbyte *offsetWriteBuffer = writeBuffer + writeOffset;
    jbyte *offsetReadBuffer = readBuffer + readOffset;
    jint result = spiXfer((unsigned)handle, (char *)offsetWriteBuffer, (char *)offsetReadBuffer, (unsigned)length);
	(*env)->ReleaseByteArrayElements(env, writeData, writeBuffer, JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env, readData, readBuffer, 0);
	return result;
}
