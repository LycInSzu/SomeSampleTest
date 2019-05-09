LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    com_android_factory_sensor.cpp

LOCAL_SHARED_LIBRARIES := \
     libnativehelper \
      libandroid_runtime \
      libutils \
      libui \
      libhwm_mtk \
      libcutils \
      liblog

LOCAL_STATIC_LIBRARIES := 

LOCAL_PRELINK_MODULE :=false

LOCAL_C_INCLUDES += \
    $(MTK_ROOT)/external/sensor-tools \
    frameworks/base/core/jni \
    $(PV_INCLUDES) \
    $(JNI_H_INCLUDE) \
    $(call include-path-for, corecg graphics)

LOCAL_CFLAGS +=

#LOCAL_LDLIBS := -lpthread

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE:= libprifactory_sensor_jni

include $(BUILD_SHARED_LIBRARY)
