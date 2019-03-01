#prize-public add 3rdparty apps to system-lanwm-20180830
LOCAL_PATH :=$(TOPDIR)packages/apps/PreBuildApps/project

#ifeq ($(strip $(PRIZE_XXXX_XXXX)), yes)
#    PRODUCT_PACKAGES += XXXX_XXXX.apk
#    PRODUCT_COPY_FILES += packages/apps/PreBuildApps/project/xxxx/xxxx:system/xxxx/xxxx
#endif

#ifeq ($(strip $(PRIZE_PRODUCT_BRAND)), Condor)
    PRODUCT_PACKAGES += CondorPassport
    PRODUCT_PACKAGES += CondorThemePark
#endif