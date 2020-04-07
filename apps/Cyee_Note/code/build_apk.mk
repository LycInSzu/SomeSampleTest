LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-v4 \
    android-support-v7-recyclerview

LOCAL_STATIC_JAVA_LIBRARIES :=  \
    photo-view\
    universal-image-loader\
    findbug_annotations\
    zytSdk\
    plugin\
    javabase64\
    queryWeather
   
LOCAL_CERTIFICATE := platform
LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-Iaidl-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, build_env_reliance)
LOCAL_AIDL_INCLUDES := $(call all-Iaidl-files-under, src)
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/src/app/src/main/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/src/app/cydroid/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/recyclerview/res
LOCAL_ASSET_DIR += $(LOCAL_PATH)/src/app/src/main/assets
# LOCAL_STATIC_JAVA_AAR_LIBRARIES := recyclerview-v7-aar
LOCAL_AAPT_FLAGS := --auto-add-overlay --extra-packages android.support.v7.recyclerview
LOCAL_PACKAGE_NAME := Cyee_Note
LOCAL_PROGUARD_ENABLED := disabled

#cljtest begin
#LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_PROGUARD_ENABLED := full
ifeq ($(APK_SOLUTION),fwvga)
LOCAL_AAPT_FLAGS += --preferred-density hdpi
LOCAL_PACKAGE_NAME := Cyee_Note_hdpi
else
ifeq ($(APK_SOLUTION),720p)
LOCAL_AAPT_FLAGS += --preferred-density xhdpi
LOCAL_PACKAGE_NAME := Cyee_Note_xhdpi
else
ifeq ($(APK_SOLUTION),1080p)
LOCAL_AAPT_FLAGS += --preferred-density xxhdpi
LOCAL_PACKAGE_NAME := Cyee_Note_xxhdpi
endif
endif
endif
#cljtest end
#LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    photo-view:build_env_reliance/libs/photo-view.jar \
    findbug_annotations:build_env_reliance/libs/annotations.jar \
    universal-image-loader:build_env_reliance/libs/universal-image-loader.jar \
    zytSdk:build_env_reliance/libs/zytSdk.jar \
    plugin:build_env_reliance/libs/plugin.jar \
    javabase64:build_env_reliance/libs/commons-codec-1.10.jar\
    queryWeather:src/app/libs/queryWeather_m.jar
