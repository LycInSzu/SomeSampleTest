/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <jni.h>
#include <utils/Log.h>
#include <kphproxy.h>
#include <pl.h>
#include <nativehelper/JNIHelp.h>
#include "android_runtime/AndroidRuntime.h"

#define MTK_LOG_ENABLE 1
#undef LOG_NDEBUG 
#undef NDEBUG

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "SENSOR-JNI"
#endif

#include <cutils/log.h>

static const char *TAG="TEE-KEY";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

using namespace android;

jint verify_status(void)
{
    int r;
    uint32_t verify, authorize;
    uint32_t __tmp0 = 2,__tmp1, __tmp2;
    
    r = pl_device_get_status(&__tmp0, &verify, &authorize, &__tmp1, &__tmp2, NULL);
    LOGI("pl_device_get_status r = %d verify = %d authorize = %d", r, verify, authorize);
    if (r) {
        return r;
    }
    if (verify != DEVICE_VERIFIED)
        return -1;
    if (authorize != DEVICE_AUTHORIZED)
        return -2;
    return 0;
}

jint verify_truststore(void)
{
    uint32_t truststore_version, truststore_type, truststore_status;
    if (pl_device_get_truststore(&truststore_type, &truststore_status, &truststore_version)) {
        LOGI("pl_device_get_truststore failed");
        return -1;
    }
    if (truststore_type == 0 && truststore_status == 1) {
        LOGI("user rpmb");
        return 0;
    } else if (truststore_type == 1 && truststore_status == 1) {
        LOGI("use others");
        return 1;
    } else {
        LOGI("truststore is INVALID");
    }
    return -1;
}

jint verify_tee_key(void)
{
    int ret = kph_verify_tee_all();
    LOGI("verify_tee_all ret = %d", ret);
    return ret;
}

jint verify_keybox_state()
{
    // int ret = remove_ta_data2(6,"keybox");
    int ret = kph_verify_ta_data2(6,"keybox");
    LOGI("verify_keybox_state ret = %d", ret);
    return ret;
}

//static const char *classPathNameRx = "com/pri/factorytest/PrizeHwInfo";

static JNINativeMethod methodsRx[] = {
    {"verifyStatus", "()I", (void*)verify_status},
    {"verifyTeeKey", "()I", (void*)verify_tee_key},
    {"verifyKeyboxState", "()I", (void*)verify_keybox_state}
    // {"verifyTruststore", "()I", (void*)verify_truststore}
};


/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static jint registerNatives(JNIEnv* env)
{
    return AndroidRuntime::registerNativeMethods(env, "com/pri/factorytest/PrizeHwInfo",
            methodsRx, NELEM(methodsRx));
}

// ----------------------------------------------------------------------------

/*
 * This is called by the VM when the shared library is first loaded.
 */

jint JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env = NULL;
    jint result = -1;

    ALOGD("Enter JNI_OnLoad()...\n");
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed\n");
        goto bail;
    }
    assert(env != NULL);

    if (registerNatives(env) < 0) {
        ALOGE("ERROR: native registration failed\n");
        goto bail;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

    ALOGD("Leave JNI_OnLoad()...\n");
    bail: return result;
}
