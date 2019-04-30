//
// Created by Abel on 2018/6/25/0025.
//

#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>

#include <fcntl.h>
#include <dlfcn.h>

#include <android/log.h>

#include "uvBaseDefine.h"
#include "uvResize.h"
#include "nv21rgb.h"
#include "uvBitmapFilter.h"

#define LOG_TAG "JNIUVFilter"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

#ifdef __cplusplus
extern "C" {
#endif
//--------------------------------------------------------------------------------------------------
jboolean dumpDataToFile(char* fileName, unsigned char* data, int dataLen, int count) {

    char filePath[256];
LOGE("dumpDataToFile NULL == fileName start");
    if ((NULL == fileName) || (NULL == data) || (0 == dataLen)) {
        LOGE("dumpDataToFile NULL == fileName err");
        return JNI_FALSE;
    }

    memset(filePath, 0x0, 256 * sizeof(char));
    snprintf(filePath, sizeof(filePath), "/sdcard/DCIM/Camera/uvjni_%s_%d.yuv", fileName, count);
    FILE* pFile = fopen(filePath, "wb");
    if (NULL != pFile) {
        fwrite(data, 1, dataLen, pFile);
        fclose(pFile);
    } else {
        LOGE("dumpDataToFile NULL == pFile err");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}



//--------------------------------------------------------------------------------------------------
const char* libResize  = "libuv_resize.so";
const char* libTrans   = "libuv_trans.so";
const char* libAlgo    = "libuv_bitmapFilter.so";

void* handleResize     = NULL;
void* handleTrans      = NULL;
void* handleAlgo       = NULL;

const char* fnUVResize = "uv_resize_nv21";
const char* fnUVTrans  = "trans_NV21_argb8888";
const char* fnUVAglo   = "uvBitmapFilterPreviewProcess";

typedef uvVoid  (*FuncResizeNV21)(const uvImageInfo*, uvImageInfo*);
typedef uvInt32 (*FuncTransNV21toARGB)(const uvImageInfo*, unsigned char*, uvInt32);
typedef uvInt32 (*FuncAlgoPrcss)(uvImageInfo*, EMUVCAMFILTER);

FuncResizeNV21      mFuncResizeNV21      = NULL;
FuncTransNV21toARGB mFuncTransNV21toARGB = NULL;
FuncAlgoPrcss       mFuncAlgoPrcss       = NULL;


jmethodID mJavaCallbackMethod = NULL;

bool mIsStopProcess           = false;
bool mIsFrameProcessing       = false;

int mDestWidth                = 0;
int mDestHeight               = 0;

unsigned char* mBuffTrans     = NULL;
unsigned char* mBuffResize    = NULL;
unsigned char* mBuffAlgo      = NULL;

//#define DUMP_DATA

//--------------------------------------------------------------------------------------------------
jmethodID findJavaMethod(JNIEnv *env, jobject obj, const char* methodName, const char* methodSign) {

    if ((NULL == methodName) || (NULL == methodSign)) {
        LOGE("findJavaMethod (NULL == methodName) || (NULL == methodSign) err");
        return NULL;
    }

    jclass clazz = env->GetObjectClass(obj);
    if (NULL == clazz) {
        LOGE("findJavaMethod NULL == clazz err");
        return NULL;
    }

    jmethodID mID = env->GetMethodID(clazz, methodName, methodSign);
    if (NULL == mID) {
        LOGE("findJavaMethod NULL == mID err");
        return NULL;
    }

    return mID;
}



//--------------------------------------------------------------------------------------------------
void destroyCallback() {

    mIsFrameProcessing = false;

    if (NULL != handleResize) {
        dlclose(handleResize);
        handleResize = NULL;
    }

    if (NULL != handleTrans) {
        dlclose(handleTrans);
        handleTrans = NULL;
    }

    if (NULL != handleAlgo) {
        dlclose(handleAlgo);
        handleAlgo = NULL;
    }

    if (NULL != mBuffResize) {
        free(mBuffResize);
        mBuffResize = NULL;
    }

    if (NULL != mBuffTrans) {
        free(mBuffTrans);
        mBuffTrans = NULL;
    }

    if (NULL != mBuffAlgo) {
        free(mBuffAlgo);
        mBuffAlgo = NULL;
    }

    mDestWidth           = 0;
    mDestHeight          = 0;

    mFuncResizeNV21      = NULL;
    mFuncTransNV21toARGB = NULL;
    mFuncAlgoPrcss       = NULL;
}


void jni_init222(JNIEnv *env, jobject obj, jint width, jint height, jintArray spItems){
    
    
}

jboolean jni_init(JNIEnv *env, jobject obj, jint width, jint height, jintArray spItems) {

    LOGD("jni_init");

    mIsStopProcess      = false;
    mIsFrameProcessing  = false;

    if ((width <= 0) || (height <= 0)) {
        LOGD("jni_init (width <= 0) || (height <= 0) err");
        goto ERR_OUT;
    }

    mJavaCallbackMethod = findJavaMethod(env, obj, "callbackData", "([IIII)V");
    if (NULL == mJavaCallbackMethod) {
        LOGD("jni_init NULL == mJavaCallbackMethod err");
        goto ERR_OUT;
    }

    handleResize = dlopen(libResize, RTLD_LAZY);
    if (NULL == handleResize) {
        LOGE("jni_init NULL == handleResize");
        goto ERR_OUT;
    }

    handleTrans = dlopen(libTrans, RTLD_LAZY);
    if (NULL == handleTrans) {
        LOGE("jni_init NULL == handleTrans");
        goto ERR_OUT;
    }

    handleAlgo = dlopen(libAlgo, RTLD_LAZY);
    if (NULL == handleAlgo) {
        LOGE("jni_init NULL == handleAlgo");
        goto ERR_OUT;
    }

    mFuncResizeNV21 = (FuncResizeNV21)dlsym(handleResize, fnUVResize);
    if (NULL == mFuncResizeNV21) {
        LOGE("jni_init NULL == mFuncResizeNV21");
        goto ERR_OUT;
    }

    mFuncTransNV21toARGB = (FuncTransNV21toARGB)dlsym(handleTrans, fnUVTrans);
    if (NULL == mFuncTransNV21toARGB) {
        LOGE("jni_init NULL == mFuncTransNV21toARGB");
        goto ERR_OUT;
    }

    mFuncAlgoPrcss = (FuncAlgoPrcss)dlsym(handleAlgo, fnUVAglo);
    if (NULL == mFuncAlgoPrcss) {
        LOGE("jni_init NULL == mFuncAlgoPrcss");
        goto ERR_OUT;
    }

    mDestWidth  = width;
    mDestHeight = height;

    mBuffResize = (unsigned char*)malloc(width * height * 3 / 2 * 2);
    mBuffTrans  = (unsigned char*)malloc(width * height * 4     * 2);
    mBuffAlgo   = (unsigned char*)malloc(width * height * 4     * 2);

    return JNI_TRUE;

ERR_OUT:
    destroyCallback();
    return JNI_FALSE;
}

void jni_unInit(JNIEnv *env, jobject obj) {
    LOGD("jni_unInit");
    mIsStopProcess = true;
}

void jni_run(JNIEnv *env, jobject obj, jbyteArray data, jint width, jint height, jint format
        , jint mainId, jint count) {

    LOGD("jni_run begin");

#ifdef DUMP_DATA
    int inDataLen = env->GetArrayLength(data);
#endif

    if (mIsStopProcess) {
        LOGE("jni_run mIsFramePrcssing:%d", mIsStopProcess ? 1 : 0);
        destroyCallback();
        return;
    }

    if (mIsFrameProcessing) {
        LOGE("jni_run mIsFramePrcssing:%d", mIsFrameProcessing ? 1 : 0);
        return;
    }

    if (NULL == data || width <= 0 || height <= 0 || (mainId < 0 || mainId > 10)) {
        LOGE("jni_run input para null err");
        return;
    }

    int dataLen = env->GetArrayLength(data);
    if (dataLen <= 0) {
        LOGE("jni_run dataLen <= 0 err");
        return;
    }

    mIsFrameProcessing = true;

    LOGE("jni_run width:%d, height:%d", width, height);

    mDestWidth = (int)(mDestHeight * 1.0f * (width * 1.0f / height));
    if (0 != mDestWidth % 2) {
        mDestWidth -= 1;
    }

    LOGE("jni_run mDestWidth:%d, mDestHeight:%d", mDestWidth, mDestHeight);

    jbyte *inData = env->GetByteArrayElements(data, 0);

    uvImageInfo src_buf;
    uvImageInfo des_buf;
    uvImageInfo algo_buf;

    memset(&src_buf,  sizeof(uvImageInfo), 0);
    memset(&des_buf,  sizeof(uvImageInfo), 0);
    memset(&algo_buf, sizeof(uvImageInfo), 0);

    src_buf.enImageType = UV_IMAGE_TYPE_NV21;
    src_buf.s32Width    = width;
    src_buf.s32Height   = height;
    src_buf.pau8Plane[0]= (unsigned char*)inData;
    src_buf.pau8Plane[1]= (unsigned char*)inData + (width * height);
    src_buf.as32Pitch[0]= width;
    src_buf.as32Pitch[1]= width;

    memset(mBuffResize, 0xFF, mDestWidth * mDestHeight * 3 / 2);
    des_buf.enImageType = UV_IMAGE_TYPE_NV21;
    des_buf.s32Width    = mDestWidth;
    des_buf.s32Height   = mDestHeight;
    des_buf.pau8Plane[0]= mBuffResize;
    des_buf.pau8Plane[1]= mBuffResize + (mDestWidth * mDestHeight);
    des_buf.as32Pitch[0]= mDestWidth;
    des_buf.as32Pitch[1]= mDestWidth;
    
#ifdef DUMP_DATA
    dumpDataToFile((char*)"uv_1", (unsigned char*)inData, inDataLen, 0);
#endif

    mFuncResizeNV21(&src_buf, &des_buf);
    
#ifdef DUMP_DATA
        dumpDataToFile((char*)"uv_2", mBuffResize, mDestWidth * mDestHeight * 3 / 2, 0);
#endif

    env->ReleaseByteArrayElements(data, inData, 0);

    uvInt32 argbStride = mDestWidth * 4;
    mFuncTransNV21toARGB(&des_buf, mBuffAlgo, argbStride);

#ifdef DUMP_DATA
        dumpDataToFile((char*)"uv_3", mBuffAlgo, mDestWidth * mDestHeight * 4, 0);
#endif

    memcpy(mBuffTrans, mBuffAlgo, mDestWidth * mDestHeight * 4);

    algo_buf.enImageType = UV_IMAGE_TYPE_NULL;
    algo_buf.s32Width    = mDestWidth;
    algo_buf.s32Height   = mDestHeight;
    algo_buf.pau8Plane[0]= mBuffAlgo;
    algo_buf.as32Pitch[0]= mDestWidth * 4;

    int currMainId = mainId * 10;
    for (int i = 0; i < count; i++) {
        if (i >= 1) {
            memcpy(mBuffAlgo, mBuffTrans, mDestWidth * mDestHeight * 4);
            mFuncAlgoPrcss(&algo_buf, (EMUVCAMFILTER)(currMainId + i - 1));

#ifdef DUMP_DATA
                dumpDataToFile((char*)"uv_4", mBuffAlgo, mDestWidth * mDestHeight * 4, i);
#endif
        }

        jintArray intArrayToApp = env->NewIntArray(mDestWidth * mDestHeight);
        env->SetIntArrayRegion(intArrayToApp, 0, mDestWidth * mDestHeight, (jint*)mBuffAlgo);
        env->CallVoidMethod(obj, mJavaCallbackMethod, intArrayToApp, mDestWidth, mDestHeight, i);
    }

    mIsFrameProcessing = false;

    LOGD("jni_run end");
}

void jni_run_once(JNIEnv *env, jobject obj, jbyteArray data, jint width, jint height, jint format
        , jint mainId, jint count) {

    LOGD("jni_run_once begin");

    if (NULL == data || width <= 0 || height <= 0 || 17 != format || (mainId < 0 || mainId > 10)) {
        LOGE("jni_run_once input para null err");
        return;
    }

    int inDataLen = env->GetArrayLength(data);
    if (inDataLen <= 0) {
        LOGE("jni_run_once dataLen <= 0 err");
        return;
    }

    mDestWidth = (int)(mDestHeight * 1.0f * (width * 1.0f / height));
    if (0 != mDestWidth % 2) {
        mDestWidth += 1;
    }

	

    jbyte *inData = env->GetByteArrayElements(data, 0);

    uvImageInfo src_buf;
    uvImageInfo des_buf;
    uvImageInfo algo_buf;

    memset(&src_buf,  sizeof(uvImageInfo), 0);
    memset(&des_buf,  sizeof(uvImageInfo), 0);
    memset(&algo_buf, sizeof(uvImageInfo), 0);

    src_buf.enImageType = UV_IMAGE_TYPE_NV21;
    src_buf.s32Width    = width;
    src_buf.s32Height   = height;
    src_buf.pau8Plane[0]= (unsigned char*)inData;
    src_buf.pau8Plane[1]= (unsigned char*)inData + (width * height);
    src_buf.as32Pitch[0]= width;
    src_buf.as32Pitch[1]= width;

    memset(mBuffResize, 0xFF, mDestWidth * mDestHeight * 3 / 2);
    des_buf.enImageType = UV_IMAGE_TYPE_NV21;
    des_buf.s32Width    = mDestWidth;
    des_buf.s32Height   = mDestHeight;
    des_buf.pau8Plane[0]= mBuffResize;
    des_buf.pau8Plane[1]= mBuffResize + (mDestWidth * mDestHeight);
    des_buf.as32Pitch[0]= mDestWidth;
    des_buf.as32Pitch[1]= mDestWidth;

    dumpDataToFile((char*)"uv_1", (unsigned char*)inData, inDataLen, 0);
    mFuncResizeNV21(&src_buf, &des_buf);
    dumpDataToFile((char*)"uv_2", mBuffResize, mDestWidth * mDestHeight * 3 / 2, 0);

    env->ReleaseByteArrayElements(data, inData, 0);

    uvInt32 argbStride = mDestWidth * 4;

    mFuncTransNV21toARGB(&des_buf, mBuffAlgo, argbStride);
    dumpDataToFile((char*)"uv_3", mBuffAlgo, mDestWidth * mDestHeight * 4, 0);
    memcpy(mBuffTrans, mBuffAlgo, mDestWidth * mDestHeight * 4);

    algo_buf.enImageType = UV_IMAGE_TYPE_NULL;
    algo_buf.s32Width    = mDestWidth;
    algo_buf.s32Height   = mDestHeight;
    algo_buf.pau8Plane[0]= mBuffAlgo;
    algo_buf.as32Pitch[0]= mDestWidth * 4;

    int currMainId = mainId * 10;
    for (int i = 0; i < count; i++) {
        if (i >= 1) {
            memcpy(mBuffAlgo, mBuffTrans, mDestWidth * mDestHeight * 4);
            mFuncAlgoPrcss(&algo_buf, (EMUVCAMFILTER)(currMainId + i - 1));
        }

        dumpDataToFile((char*)"uv_4", mBuffAlgo, mDestWidth * mDestHeight * 4, i);

        jintArray intArrayToApp = env->NewIntArray(mDestWidth * mDestHeight);
        env->SetIntArrayRegion(intArrayToApp, 0, mDestWidth * mDestHeight, (jint*)mBuffAlgo);
        env->CallVoidMethod(obj, mJavaCallbackMethod, intArrayToApp, mDestWidth, mDestHeight, i);
    }

    LOGD("jni_run_once end");
}


//--------------------------------------------------------------------------------------------------
const char* classPathName = "com/android/camera/uvfilter/UVFilterJni";

JNINativeMethod jniNtvMethod[] = {
    { "initNative",    "(II[I)Z",       (void*) jni_init     },
    { "destroyNative",  "()V",         (void*) jni_unInit   },
    { "runNative",     "([BIIIII)V",  (void*) jni_run      }
    //{ "runNativeOnce", "([BIIIII)V",  (void*) jni_run_once }
};

jint registerNativesMethods(JNIEnv *env, const char* className, JNINativeMethod* gMethods, int methodNum) {
    jclass clazz = env->FindClass(className);
    if (NULL == clazz) {
        LOGE("ERROR:registerNativesMethods env->FindClass failed");
        return JNI_FALSE;
    }

    if (0 > env->RegisterNatives(clazz, gMethods, methodNum)) {
        LOGE("ERROR:registerNativesMethods env->RegisterNatives failed");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv *env = NULL;

    LOGD("JNI_OnLoad begin");
    if (JNI_OK != vm->GetEnv((void**)&env, JNI_VERSION_1_6)) {
        LOGE("ERROR:JNI_OnLoad vm->GetEnv failed");
        return JNI_ERR;
    }

    if (!registerNativesMethods(env, classPathName, jniNtvMethod,
                    sizeof(jniNtvMethod) / sizeof(jniNtvMethod[0]))) {
        LOGE("ERROR:JNI_OnLoad registerNativesMethods failed");
        return JNI_ERR;
    }

    LOGD("JNI_OnLoad end success");
    return JNI_VERSION_1_6;
}

#ifdef __cplusplus
}
#endif