
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libUvPanorama
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := mtk
LOCAL_SRC_FILES_arm := libs/armeabi-v7a/$(LOCAL_MODULE).so
LOCAL_SRC_FILES_arm64 := libs/arm64-v8a/$(LOCAL_MODULE).so
LOCAL_MODULE_STEM := $(LOCAL_MODULE)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MULTILIB := both
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libjni_uvpanorama
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := mtk
LOCAL_SRC_FILES_arm := libs/armeabi-v7a/$(LOCAL_MODULE).so
LOCAL_SRC_FILES_arm64 := libs/arm64-v8a/$(LOCAL_MODULE).so
LOCAL_MODULE_STEM := $(LOCAL_MODULE)
LOCAL_MODULE_SUFFIX := .so
LOCAL_MULTILIB := both
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_SHARED_LIBRARIES := libUvPanorama
include $(BUILD_PREBUILT)
