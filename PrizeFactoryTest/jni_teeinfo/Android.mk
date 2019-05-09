ifeq ($(strip $(PRIZE_TEE)),1)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES:=teeinfo.cpp
LOCAL_MULTILIB := both
LOCAL_MODULE := libteeinfo
LOCAL_SHARED_LIBRARIES := libutils \
                          libnativehelper \
                          libandroid_runtime \
                          libui \
                          libhwm_mtk \
                          libcutils \
                          libpl_system \
                          libkphproxy_system \
                          liblog
LOCAL_C_INCLUDES += \
    $(MTK_ROOT)/external/sensor-tools \
    frameworks/base/core/jni \
    $(PV_INCLUDES) \
    $(JNI_H_INCLUDE) \
    $(call include-path-for, corecg graphics)
LOCAL_MODULE_TAGS :=optional
include $(BUILD_SHARED_LIBRARY)
include $(call all-makefiles-under, $(LOCAL_PATH))
endif
