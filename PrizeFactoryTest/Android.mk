LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

#LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES := $(call all-subdir-Iaidl-files)
LOCAL_SRC_FILES += $(call all-java-files-under, src/com) $(call all-java-files-under, src/hwbinder)

LOCAL_STATIC_JAVA_LIBRARIES := zxing

LOCAL_USE_AAPT2 := true

LOCAL_PACKAGE_NAME := PriFactoryTest

LOCAL_CERTIFICATE := platform

LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += mediatek-framework

ifeq ($(strip $(PRIZE_TEE)),1)
LOCAL_JNI_SHARED_LIBRARIES := libteeinfo
endif

#LOCAL_SDK_VERSION := current
LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_PRIVILEGED_MODULE := true

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_STATIC_JAVA_LIBRARIES += vendor.mediatek.hardware.nvram-V1.0-java

LOCAL_STATIC_JAVA_LIBRARIES += android.hidl.base-V1.0-java
LOCAL_STATIC_JAVA_LIBRARIES += vendor.goodix.hardware.biometrics.fingerprint-V2.1-java
LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-constraint-layout \
    android-support-v7-appcompat \

LOCAL_STATIC_JAVA_LIBRARIES += android-support-constraint-layout-solver
LOCAL_DEX_PREOPT := false
LOCAL_JNI_SHARED_LIBRARIES += libprifactory_sensor_jni
#LOCAL_STATIC_JAVA_AAR_LIBRARIES := huiding_fingerprint

#LOCAL_AAPT_FLAGS := \
#      --auto-add-overlay \
#      --extra-packages com.goodix.fingerprint.lib

include $(BUILD_PACKAGE)

#include $(CLEAR_VARS)
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
#    huiding_fingerprint:libs/gf_manager_lib-debug.aar
#include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
