ifeq ("$(CYEE_SCREENRECORDER_SUPPORT)","yes")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform
LOCAL_PACKAGE_NAME := Cyee_ScreenRecorder

#add by liyuchong begin
LOCAL_PRIVATE_PLATFORM_APIS := true
#add by liyuchong end

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v4

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := android-support-v4:./libs/android-support-v4.jar
include $(BUILD_MULTI_PREBUILT)
endif
