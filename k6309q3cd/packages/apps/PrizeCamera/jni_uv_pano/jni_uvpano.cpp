#include <jni.h>
#include <string>
#include <android/log.h>
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG,"PrizePanoJni",__VA_ARGS__)
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR,"PrizePanoJni",__VA_ARGS__)


#include "uvBaseDefine.h"
#include "uvPanoInterface.h"

#ifdef Kb_REFOCUS
#include "ImageRefocusEngine.h"
#endif


#define UV_RUN 0



using namespace std;
#ifdef __cplusplus
extern "C" {
#endif

uvVoid *pPanoHandle = NULL;

jmethodID mJavaCallbackMethod = NULL;

jmethodID findJavaMethod(JNIEnv *env, jobject obj, const char* methodName, const char* methodSign) {

    if ((NULL == methodName) || (NULL == methodSign)) {
        LOG_E("findJavaMethod (NULL == methodName) || (NULL == methodSign) err");
        return NULL;
    }

    jclass clazz = env->GetObjectClass(obj);
    if (NULL == clazz) {
        LOG_E("findJavaMethod NULL == clazz err");
        return NULL;
    }

    jmethodID mID = env->GetMethodID(clazz, methodName, methodSign);
    if (NULL == mID) {
        LOG_E("findJavaMethod NULL == mID err");
        return NULL;
    }

    return mID;
}


JNIEXPORT jint JNICALL
Java_com_prize_camera_feature_mode_pano_UVPanoJni_nativeInit(JNIEnv *env, jobject instance, jint width, jint height, jint maxWidth, jint thumbnailWidth, jint thumbnailHeight){
	uvInt32 result = 0;
	
	mJavaCallbackMethod = findJavaMethod(env, instance, "callbackData", "([BII)V");
    if (NULL == mJavaCallbackMethod) {
        LOG_D("nativeInit NULL == mJavaCallbackMethod err");
        return result;
    }
	
	
	#if UV_RUN
	uvPanoInitParam initParam;
	memset(&initParam, 0, sizeof(uvPanoInitParam));
	initParam.s32Direction = UV_PANO_DIR_LEFT2RIGHT;                              // 拼接方向 eg. UV_PANO_DIR_LEFT2RIGHT;
	initParam.u32SrcFullImageFormat = UV_IMAGE_TYPE_NV21;             // UV_IMAGE_TYPE_NV21
	initParam.s32SrcFullImageWidth    = width;                       // 图像宽
	initParam.s32SrcFullImageHeight = height;                        // 图像高


	initParam.u32SrcSmallImageFormat = UV_IMAGE_TYPE_NV21;           // UV_IMAGE_TYPE_NV21
	initParam.s32SrcSmallImageWidth = width;                         // 图像宽
	initParam.s32SrcSmallImageHeight = height;                       // 图像高

	initParam.s32FullResultLength = maxWidth;        //拼接后结果图最大宽

	initParam.u32ThumbnailFormat = UV_IMAGE_TYPE_NV21;           //UV_IMAGE_TYPE_NV21
	initParam.s32ThumbnailResultWidthH = thumbnailWidth;   //水平拼接方向时的缩略图宽
	initParam.s32ThumbnailResultHeightH = thumbnailHeight; //水平拼接方向时的缩略图高
	initParam.s32ThumbnailResultWidthV = thumbnailWidth;   //垂直拼接方向时的缩略图宽
	initParam.s32ThumbnailResultHeightV = thumbnailHeight; //水平拼接方向时的缩略图高

	result = uvPanoInit(&pPanoHandle, &initParam);
	//sleep(2);
	LOG_D("start uvPanoInit result=%d", result);
	#endif
    return result;
}

JNIEXPORT jint JNICALL
Java_com_prize_camera_feature_mode_pano_UVPanoJni_nativeUnInit(JNIEnv *env, jobject instance){
	uvInt32 result = 0;
	#if UV_RUN
	if(pPanoHandle){
		result = uvPanoDeInit(&pPanoHandle);
		pPanoHandle = NULL;
		LOG_D("uvPanoUnInit result=%d", result);
	}
	#endif
	return result;
}

JNIEXPORT jint JNICALL
Java_com_prize_camera_feature_mode_pano_UVPanoJni_nativeProcess(JNIEnv *env, jobject instance, jint width, jint height, jbyteArray data){
	uvInt32 result = 0;
	#if UV_RUN
	uvImageInfo srcImgInfo;                         //单帧源图像
	uvImageInfo smallResultImage;                   //预览拼接结果图像
	uvImageInfo smallResultMask;                    //预览拼接结果mask
	uvRect rcUpdateSmallImage;                      //预览拼接更新区域，无效
	uvImageInfo fullResultImage;                    //拍照拼接结果图像
	uvPoint ptOutputOffset;                         //预览拼接offset
	uvInt32 s32Direction = UV_PANO_DIR_LEFT2RIGHT;  //预览方向
	uvInt32 s32Progress = 0;                        //预览进度
	
	
	unsigned char* buffer = (unsigned char*)env->GetByteArrayElements(data, 0);
	
	
	srcImgInfo.enImageType = UV_IMAGE_TYPE_NV21;
	srcImgInfo.s32Width = width;
	srcImgInfo.s32Height = height;
	srcImgInfo.as32Pitch[0] = width;
	srcImgInfo.as32Pitch[1] = width;
	//srcImgInfo.as32Pitch[2] = width / 2;
	//srcImgInfo.pau8Plane[0] = (uvUInt8 *)in->getBufVA(0);buffer
	//srcImgInfo.pau8Plane[1] = (uvUInt8 *)in->getBufVA(1);buffer + width*height
	//srcImgInfo.pau8Plane[2] = (uvUInt8 *)in->getBufVA(2);buffer + width*height + width*height*1/4

	srcImgInfo.pau8Plane[0] = buffer;//(MUInt8 *) malloc(mLeftImg.pi32Pitch[0] * mLeftImg.i32Height * 3 / 2);
    srcImgInfo.pau8Plane[1] = srcImgInfo.pau8Plane[0] + srcImgInfo.as32Pitch[0] * srcImgInfo.s32Height;
	
	result = uvPanoProcess(pPanoHandle,
					  &srcImgInfo,
					  &srcImgInfo,
					  UV_PANO_STATE_ATTATCH,
					  &smallResultImage,
					  &smallResultMask,
					  &rcUpdateSmallImage,
					  &fullResultImage,
					  &ptOutputOffset,
					  &s32Direction,
					  &s32Progress);

	
	env->ReleaseByteArrayElements(data, (signed char*)buffer, 0);
	
	
	LOG_D("nativeProcess result=%d progress=%d", result, s32Progress);
	#endif
	LOG_D("nativeProcess method");
	
	mJavaCallbackMethod = findJavaMethod(env, instance, "callbackData", "([BII)V");
	
	if(mJavaCallbackMethod){
		char d[5];
		d[0] = 1;
		d[1] = 2;
		d[2] = 3;
		d[3] = 4;
		d[4] = 5;
		
		
		jbyteArray intArrayToApp = env->NewByteArray(5);
        env->SetByteArrayRegion(intArrayToApp, 0, 5, (jbyte*)d);
		
	    env->CallVoidMethod(instance, mJavaCallbackMethod, intArrayToApp, width, height);
	}
	LOG_D("nativeProcess end");
	return result;
}


#ifdef __cplusplus
}
#endif
