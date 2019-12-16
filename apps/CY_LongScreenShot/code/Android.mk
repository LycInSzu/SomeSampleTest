ifeq ("$(CYEE_LONGSCREENSHOT_SUPPORT)","yes")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform
LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v4 
LOCAL_PACKAGE_NAME := CY_LongScreenShot
# LOCAL_JAVA_LIBRARIES := YouJuAgent
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PROGUARD_ENABLED := disabled
include $(BUILD_PACKAGE)
endif