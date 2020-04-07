LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

############################
# Build Apk for fwvga
include $(CLEAR_VARS)
APK_SOLUTION := fwvga
include $(LOCAL_PATH)/build_apk.mk
############################

############################
# Build Apk for 720P
include $(CLEAR_VARS)
APK_SOLUTION := 720p
include $(LOCAL_PATH)/build_apk.mk
############################

############################
# Build Apk for 1080P
include $(CLEAR_VARS)
APK_SOLUTION := 1080p
include $(LOCAL_PATH)/build_apk.mk
############################

############################
# Build Apk for default solution
include $(CLEAR_VARS)
APK_SOLUTION := default
include $(LOCAL_PATH)/build_apk.mk
############################


include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
