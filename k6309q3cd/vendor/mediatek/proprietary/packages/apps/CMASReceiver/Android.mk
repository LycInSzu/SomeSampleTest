# Copyright 2011 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)

# Build CMASCommon.jar
# ====  the library  =========================================
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
   src/com/mediatek/cellbroadcastreceiver/Comparer.java \
   src/com/mediatek/cellbroadcastreceiver/CellBroadcastContentProvider.java \
   src/com/mediatek/cellbroadcastreceiver/CellBroadcastDatabaseHelper.java
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_CERTIFICATE := platform
#LOCAL_NO_STANDARD_LIBRARIES := true

LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += mediatek-common
LOCAL_JAVA_LIBRARIES += mediatek-telephony-base

LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE := CMASCommon

#include $(BUILD_JAVA_LIBRARY)
include $(BUILD_STATIC_JAVA_LIBRARY)

# Build CMASReceiver.apk
# ====  the library  =========================================
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)


LOCAL_PACKAGE_NAME := CMASReceiver
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PRIVATE_PLATFORM_APIS := true
#LOCAL_PROPRIETARY_MODULE := true
#LOCAL_MODULE_OWNER := mtk
#prize modified by liyuchong, config CMASReceiver fot TRA test,20190124-begin
LOCAL_OVERRIDES_PACKAGES := CellBroadcastReceiver MtkCellBroadcastReceiver
#prize modified by liyuchong, config CMASReceiver fot TRA test,20190124-end
LOCAL_CERTIFICATE := platform
LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += mediatek-common
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_JAVA_LIBRARIES += mediatek-telephony-base
LOCAL_JAVA_LIBRARIES += mediatek-telephony-common

LOCAL_STATIC_JAVA_LIBRARIES += CMASCommon
LOCAL_STATIC_JAVA_LIBRARIES := com.mediatek.cmas.ext

#ifeq ($(strip $(MTK_CIP_SUPPORT)),yes)
#LOCAL_MODULE_PATH := $(TARGET_CUSTOM_OUT)/app
#else
#LOCAL_MODULE_PATH := $(TARGET_OUT_VENDOR)/app
#endif

include $(LOCAL_PATH)/config.mk

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

# This finds and builds the test apk as well, so a single make does both.
include $(call all-makefiles-under,$(LOCAL_PATH))
