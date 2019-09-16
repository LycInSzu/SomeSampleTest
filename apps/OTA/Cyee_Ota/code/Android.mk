LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

#Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
LOCAL_STATIC_JAVA_LIBRARIES := \
    v4 \
    cyee_statistics
#Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end    

LOCAL_JAVA_LIBRARIES := org.apache.http.legacy

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := Cyee_Ota

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

#Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    v4:libs/android-support-v4.jar \
    cyee_statistics:libs/cyee_statistics.jar
#Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
