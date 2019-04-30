
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libuv_resize
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
LOCAL_MODULE := libuv_trans
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
LOCAL_MODULE := libuv_bitmapFilter
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
LOCAL_MODULE := libjni_uvfilter
LOCAL_SRC_FILES := jni_uvfilter.cpp

LOCAL_MULTILIB := both
LOCAL_LDLIBS :=-llog
LOCAL_MODULE_TAGS := optional
LOCAL_CFLAGS += -Wall -Wno-unused-parameter -Wno-user-defined-warnings
LOCAL_SHARED_LIBRARIES := libuv_resize \
						  libuv_trans \
						  libuv_bitmapFilter
include $(BUILD_SHARED_LIBRARY)
