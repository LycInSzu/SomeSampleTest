#define MTK_LOG_ENABLE 1
#include "jni.h"
#include <nativehelper/JNIHelp.h>
#include "android_runtime/AndroidRuntime.h"
#undef LOG_NDEBUG 
#undef NDEBUG

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "SENSOR-JNI"
#endif

#include <cutils/log.h>
extern "C" {
#include "libhwm.h"
}

using namespace android;

static jint getPsensorData(JNIEnv *, jclass) {
    ALOGD("Enter getPsensorData()\n");
    int ret = get_psensor_data();
    ALOGD("getPsensorData() returned %d\n", ret);

    return ret;
}

/*static jint getPsensorThreshold(JNIEnv *env, jclass, jintArray result) {
    int values[2] = { 0 };
    ALOGD("Enter getPsensorThreshold()\n");
    values[0] = get_psensor_threshold(0);
    values[1] = get_psensor_threshold(1);
    ALOGD("getPsensorThreshold() returned %d, %d\n", values[0], values[1]);

    env->SetIntArrayRegion(result, 0, 2, (jint*) values);
    return 1;
}

static jint setPsensorThreshold(JNIEnv *, jclass, jint high, jint low) {
    ALOGD("Enter getPsensorThreshold()\n");
    int ret = set_psensor_threshold(high, low);
    ALOGD("getPsensorThreshold() returned %d\n", ret);

    return ret;
}

static jint calculatePsensorMinValue(JNIEnv *, jclass) {
    ALOGD("Enter calculatePsensorMinValue()\n");
    int ret = calculate_psensor_min_value();
    ALOGD("calculatePsensorMinValue() returned %d\n", ret);

    return ret;
}

static jint getPsensorMinValue(JNIEnv *, jclass) {
    ALOGD("Enter getPsensorMinValue()\n");
    int ret = get_psensor_min_value();
    ALOGD("getPsensorMinValue() returned %d\n", ret);

    return ret;
}

static jint calculatePsensorMaxValue(JNIEnv *, jclass) {
    ALOGD("Enter calculatePsensorMaxValue()\n");
    int ret = calculate_psensor_max_value();
    ALOGD("calculatePsensorMaxValue() returned %d\n", ret);

    return ret;
}

static jint getPsensorMaxValue(JNIEnv *, jclass) {
    ALOGD("Enter getPsensorMaxValue()\n");
    int ret = get_psensor_max_value();
    ALOGD("getPsensorMaxValue() returned %d\n", ret);

    return ret;
}

static jint doPsensorCalibration(JNIEnv *, jclass, jint min, jint max) {
    ALOGD("Enter doPsensorCalibration()\n");
    int ret = do_calibration(min, max);
    ALOGD("doPsensorCalibration() returned %d\n", ret);

    return ret;
}

static jint clearPsensorCalibration(JNIEnv *, jclass) {
    ALOGD("Enter clear_psensor_calibration()\n");
    int ret = clear_psensor_calibration();
    ALOGD("clear_psensor_calibration() returned %d\n", ret);
    return ret;
}

static jint startGsensorCalibration(JNIEnv *, jclass) {
    ALOGD("Enter gsensor_start_static_calibration()\n");
    int ret = gsensor_start_static_calibration();
    ALOGD("gsensor_start_static_calibration() returned %d\n", ret);

    return ret;
}

static jint getGsensorStaticCalibration(JNIEnv *env, jclass, jfloatArray result) {
    ALOGD("Enter gsensor_get_static_calibration()\n");
    struct caliData caliData;
    int ret = gsensor_get_static_calibration(&caliData);
    ALOGD("get_gyroscope_calibration() returned %d, %f, %f, %f\n", ret,
        caliData.data[0], caliData.data[1] ,caliData.data[2]);

    env->SetFloatArrayRegion(result, 0, 3, (jfloat*) caliData.data);
    return ret;
}

static jint startGyroscopeCalibration(JNIEnv *, jclass) {
    ALOGD("Enter gyroscope_start_static_calibration()\n");
    int ret = gyroscope_start_static_calibration();
    ALOGD("gyroscope_start_static_calibration() returned %d\n", ret);
    return ret;
}

static jint getGyroscopeStaticCalibration(JNIEnv *env, jclass, jfloatArray result) {
    ALOGD("Enter gyroscope_get_static_calibration()\n");
    struct caliData caliData;
    int ret = gyroscope_get_static_calibration(&caliData);
    ALOGD("gyroscope_get_static_calibration() returned %d, %f, %f, %f\n", ret,
        caliData.data[0], caliData.data[1] ,caliData.data[2]);

    env->SetFloatArrayRegion(result, 0, 3, (jfloat*) caliData.data);
    return ret;
}

static jint startLightCalibration(JNIEnv *, jclass) {
    ALOGD("Enter als_start_static_calibration()\n");
    int ret = als_start_static_calibration();
    ALOGD("als_start_static_calibration() returned %d\n", ret);

    return ret;
}

static jint getLightStaticCalibration(JNIEnv *env, jclass, jfloatArray result) {
    ALOGD("Enter als_get_static_calibration()\n");
    struct caliData caliData;
    int ret = als_get_static_calibration(&caliData);
    ALOGD("als_get_static_calibration() returned %d, %f\n", ret, caliData.data[0]);

    env->SetFloatArrayRegion(result, 0, 3, (jfloat*) caliData.data);
    return ret;
}*/

static JNINativeMethod mehods[] = {
//	{ "doGsensorCalibration", "(I)I",(void *) doGsensorCalibration },
//	{ "getGsensorCalibration", "([F)I",(void *) getGsensorCalibration },
//	{ "clearGsensorCalibration", "()I", (void *) clearGsensorCalibration },
//	{ "doGyroscopeCalibration", "(I)I",(void *) doGyroscopeCalibration },
//	{ "getGyroscopeCalibration", "([F)I",(void *) getGyroscopeCalibration },
//	{ "clearGyroscopeCalibration", "()I",(void *) clearGyroscopeCalibration },
	{ "getPsensorData", "()I",(void *) getPsensorData },
//	{ "getPsensorThreshold", "([I)I",(void *) getPsensorThreshold },
//	{ "setPsensorThreshold", "(II)I",(void *) setPsensorThreshold },
//	{ "calculatePsensorMinValue", "()I",(void *) calculatePsensorMinValue },
//	{ "getPsensorMinValue", "()I",(void *) getPsensorMinValue },
//	{ "calculatePsensorMaxValue", "()I",(void *) calculatePsensorMaxValue },
//	{ "getPsensorMaxValue", "()I",(void *) getPsensorMaxValue },
//	{ "doPsensorCalibration", "(II)I",(void *) doPsensorCalibration },
//	{ "clearPsensorCalibration", "()I",(void *) clearPsensorCalibration },
//	{ "startGsensorCalibration", "()I",(void *) startGsensorCalibration },
//	{ "getGsensorStaticCalibration", "([F)I",(void *) getGsensorStaticCalibration },
//	{ "startGyroscopeCalibration", "()I",(void *) startGyroscopeCalibration },
//	{ "getGyroscopeStaticCalibration", "([F)I",(void *) getGyroscopeStaticCalibration },
//	{ "startLightCalibration", "()I",(void *) startLightCalibration },
//	{ "getLightStaticCalibration", "([F)I",(void *) getLightStaticCalibration },
};

// This function only registers the native methods
static int register_com_mediatek_sensor(JNIEnv *env) {
    ALOGE("Register: register_com_HorCali_sensor()...\n");
    return AndroidRuntime::registerNativeMethods(env, "com/pri/factorytest/sensorlib/SensorLib",
            mehods, NELEM(mehods));
}

jint JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env = NULL;
    jint result = -1;

    ALOGD("Enter JNI_OnLoad()...\n");
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed\n");
        goto bail;
    }
    assert(env != NULL);

    if (register_com_mediatek_sensor(env) < 0) {
        ALOGE("ERROR: Sensor native registration failed\n");
        goto bail;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

    ALOGD("Leave JNI_OnLoad()...\n");
    bail: return result;
}
