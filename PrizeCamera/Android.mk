#ifneq ($(MTK_BSP_PACKAGE) , yes)
ifeq ($(strip $(PRIZE_CAMERA_APP)),DC)
ifeq ($(MTK_CAMERA_APP_VERSION), 3)
LOCAL_ROOT_PATH:= $(call my-dir)

include $(LOCAL_ROOT_PATH)/host/Android.mk
include $(LOCAL_ROOT_PATH)/portability/Android.mk
include $(LOCAL_ROOT_PATH)/tests/Android.mk
include $(LOCAL_ROOT_PATH)/feature/setting/matrixdisplay/matrixdisplay_ext/Android.mk
include $(LOCAL_ROOT_PATH)/testscat/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/pluginroot/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/mode/panorama/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/mode/pip/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/mode/vsdof/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/mode/slowmotion/Android.mk
include $(LOCAL_ROOT_PATH)/jni_uv_filter/Android.mk
include $(LOCAL_ROOT_PATH)/jni_uv_pano/Android.mk
endif
endif
#endif
