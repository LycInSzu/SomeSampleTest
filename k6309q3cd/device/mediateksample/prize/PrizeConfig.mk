#prize PrizeConfig.mk-lanwm 20180106
#=======================常用============================
PRIZE_PRODUCT_MODEL             = Allure X
PRIZE_TARGET_DEVICE             = Allure_X
PRIZE_PRODUCT_BRAND             = Condor
PRIZE_PRODUCT_MANUFACTURER      = SPA Condor Electronics
PRIZE_PRODUCT_BOARD             = Allure_X
PRIZE_TARGET_DEVICE_FOR_CONDOR  = SP646
#========================CTS============================
BUILD_GMS                       = yes
PRIZE_CURRENT_GMS               = gms_201903
#[NONE,FASTPASS,FASTPASS_PLUS,RUSSIA]
PRIZE_GMS_SEND_TEST_TYPE		= FASTPASS_PLUS
#是否是运营商
PRIZE_IS_CARRIERS               = no
#clientidbase
PRIZE_CLIENTIDBASE              = android-condor
#cts
PRIZE_CTS                       = yes

#PLATFORM_BASE_OS默认为空，SMR软件需要赋值为上一个全过Google认证版本的fingerprint
PLATFORM_BASE_OS		=

#========================EEA规范========================
#EEA项目需配置type4c
#[none,type4c]
PRIZE_GMS_SEND_TYPE_EEA =none

#针对go的版本且出货EEA的项目：在只有出货CY-塞浦路斯 LI-列支敦士登 MT-马耳他 这个三个欧洲国家的时候 这个宏要改为yes
PRIZE_EEA_YOUTUBE_GO_BUILD_IN = no  

#针对非go的版本且出货EEA的项目：在只有出货BG-保加利亚 LI-列支敦士登 这两个欧洲国家的时候 这个宏要改为yes
PRIZE_EEA_PLAYMOVIES_NOT_BUILD_IN = no 

#=======================设置============================
#------------网络和互联网------------------
#WIFI
PRIZE_WIFI                      = no
#蓝牙
PRIZE_BLUETOOTH                 = no
#wifi名称 若为空则与PRIZE_PRODUCT_MODEL一致
PRIZE_WIFI_NAME                 = 
#WLAN 直连名称 若为空则与PRIZE_PRODUCT_MODEL一致
PRIZE_WLAN_NAME                 = 
#数据连接默认开/闭状态
PRIZE_MOBILE_DATA_DEFAULT_ON    = yes

#-----------------电池--------------------
#电量百分比默认值[0,1]
PRIZE_DEFAULT_VALUE_SHOW_BATTERY_PERCENT = 0
#电池剩余可用时间字串是否显示[0,1]
PRIZE_SHOW_BATTERY_REMAININGLABEL = 0
#电池最大容量
PRIZE_BATTERY_CAPACITY          = 3400
#待机智能省电
PRIZE_BG_POWER_SAVING_ENABLE    = yes

#-----------------显示--------------------
#默认屏幕亮度
PRIZE_SCREEN_BRIGHTNESS         = 128
#自动调节亮度
PRIZE_SCREEN_BRIGHTNESS_MODE    = yes
#set values->0.85 | 1.0 | 1.06 | 1.13
PRIZE_FONT_SIZE                 =0.85
#自动旋转屏幕
PRIZE_ACCELEROMETER_ROTATION=yes
#是否显示字体风格
PRIZE_ISSHOW_FONTSTYLE=no
#休眠时间
PRIZE_SCREEN_OFF_TIMEOUT        =30000

#-----------------声音--------------------
#prize-modify for fixbug: 67871 the ring and alarm volume by xiekui-20181113-start
PRIZE_STREAM_VOICE_CALL=4       #set value->1~7
PRIZE_STREAM_SYSTEM=15          #set value->0~15
PRIZE_STREAM_RING=12             #set value->0~15
PRIZE_STREAM_MUSIC=8            #set value->0~15
PRIZE_STREAM_ALARM=12            #set value->1~15
PRIZE_STREAM_NOTIFICATION=8     #set value->0~15
PRIZE_STREAM_BLUETOOTH_SCO=7    #set value->0~15
PRIZE_STREAM_SYSTEM_ENFORCED=15 #set value->0~15
PRIZE_STREAM_DTMF=11            #set value->0~15
PRIZE_STREAM_TTS=11             #set value->0~15
PRIZE_STREAM_ACCESSIBILITY=11   #set value->1~15
#prize-modify for fixbug: 67871 the ring and alarm volume by xiekui-20181113-start
#默认手机铃声
PRIZE_DEF_RINGTONE=prize_ring_default.ogg
#默认通知铃声
PRIZE_DEF_NOTIFICATION_SOUND=prize_noti_default.ogg
#默认闹钟铃声
PRIZE_DEF_ALARM=Alarm_Lights.ogg
#有来电时响铃并振动
PRIZE_VEBRATE_WHEN_RINGING      =yes
#拨号键盘提示音
PRIZE_DTMF_TONE_WHEN_DIALING=yes
#屏幕锁定提示音
PRIZE_LOCKSCREEN_SOUNDS_ENABLED=yes
#触摸提示音
PRIZE_TOUCH_SOUND_EFFECTS_ENABLED=no
#点按时振动
PRIZE_VIBRATE_ON_TOUCH=yes
#紧急提示音,setvalue->0/1/2
PRIZE_DEFAULT_EMERGENCY_TONE=0

#------------安全性和位置信息--------------
#是否删除设置安全里面的安全更新显示项
PRIZE_ISDELETE_SECURITY_UPDATE = no
#锁屏提示语字符限制字数
PRIZE_OWNERINFO_MAX_LENGTH=200

#-----------------系统--------------------
#默认语言
PRIZE_DEF_LANGUAGE=en_US
#开机语言是否随SIM卡自适配
PRIZE_IS_LANGUAGE_ADP_SIM=yes
#输入法
#PRIZE_KIKA_INPUTMETHOD          = no
DEFAULT_INPUT_METHOD            = 

#日期格式
PRIZE_TIME_12_24=24
#默认时区
PRIZE_TIMEZONE = Africa/Brazzaville
#自动确定时区
PRIZE_AUTO_TIME_ZONE=no
#自动更新网络时间
PRIZE_AUTO_TIME=yes
#版本号
PRIZE_KERNEL_VERSION    =
PRIZE_BASEBAND_VERSION  =

#PRIZE_SW_VERSION=K6309Q3CD.KBEE.HDJ.P0.$(PRIZE_FLASH_CODE).$(shell date +%m%d_%H%M)
PRIZE_SW_VERSION= SP646_V06_20190329
#自定义版本号
#PRIZE_CUSTOM_BUILD_VERSION      = SP646_V06_20190329
#自定义真实编译版本号 锁定版本查看	 		  
PRIZE_REAL_CUSTOM_BUILD_VERSION      = 
#版本号
PRIZE_BUILD_NUMBER_VERSION      =
#开发者选项,setvalue->0/1/2,0为不显示 1为显示默认关闭USB调试 2为显示默认打开USB调试
PRIZE_DEFAULT_DEVELOPMENT_SETTINGS     = 0

#prize-set default usb mode -peisaisai-20180507-start
#value : 
#1、null或不写
#2、mtp
#3、ptp
#4、midi
PRIZE_DEF_USB_FUNCTION = 
#prize-set default usb mode -peisaisai-20180507-end
#无线充电默认值[0,1]
PRIZE_DEFAULT_VALUE_WIRELESS_CHARGER = 1
#充电添加提示语
PRIZE_CHARGING_TIPS		=yes
#充电添加提示音
PRIZE_CHARGING_SOUND    =yes
#prize-设置存储显示可用存储空间-yanglvxiong-20180327-start
PRIZE_SETTING_STORAGE_AVAILABLE =no
#prize-设置存储显示可用存储空间-yanglvxiong-20180327-end

#================锁屏/状态栏/导航栏=====================
#信号图标
PRIZE_SIGNAL_ICON               = yes
#导航栏
PRIZE_SUPPORT_NAVBAR            = yes
PRIZE_SUPPORT_HIDING_NAVBAR     = yes
#PRIZE_NAVBAR_RECENT_USED_MENU   = no
#虚拟按键风格，0为虚拟键风格,带back，home，rencent键; 1为默认手势键风格，上滑手势; 
PRIZE_NAVBAR_STYLE              = 1
#虚拟键风格 back 在左边
PRIZE_BACK_AT_LEFT_POSITION_DEFAULT   = yes
PRIZE_NAVBAR_HEIGHT             = 48
#提高导航栏灵敏度
PRIZE_INCREASE_NAVBAR_SENSITIVITY = no
#prize-bangs screen-lanwm-20180408-begin
PRIZE_BANGS_SCREEN =no
#prize-bangs screen-lanwm-20180408-end
#prize-set default theme-lanwm-20180419-begin
PRIZE_STATUSBAR_DEFAULT_LIGHT_THEME= yes
#prize-set default theme-lanwm-20180419-end

#========================桌面==========================
#单双层
PRIZE_LAUNCHER_SINGLEANDDOUBLELAYER = yes
#桌面主题
PRIZE_LAUNCHER_THEME_SUPPORT = no
#默认主题
#[0,default] [1,jinsenianhua] [2,shuiguopaidui] [3,xingguangcuican] [4,xingjizhiguang] [5,yuanzhicaiyun] [6,zhiruochujian]
PRIZE_LAUNCHER_DEFAULT_THEME = 0
#桌面图标上下间距[例如0.8即为原来的0.8倍]
PRIZE_APP_CELL_HEIGHT           = 1
#launcher半透
PRIZE_LAUNCHER_TRANSLUCENT = yes

#======================相机图库=========================
PRIZE_CAMERA_APP               = DC
GANGYUN_BOKEH_SUPPORT          = yes
#港云虚化种类[1:后摄有虚化  2:前后摄都有虚化  3：仅前摄有虚化 4：前后摄均没有虚化]
GANGYUN_BOKEH_TYPE             = 1
#单反 yuv camera 感光阈值
PRIZE_YUV_BACK_VALUE           = 150
PRIZE_YUV_FRONT_VALUE           = 150
PRIZE_ALS_VALUE                 = 3
PRIZE_SUPERZOOM_FROM_ARCSOFT   = yes
PRIZE_LOWLIGHT_FROM_ARCSOFT    = yes
PRIZE_HDR_FROM_ARCSOFT         = yes
PRIZE_PICSELFIE_FROM_ARCSOFT   = yes
PRIZE_FACEBEAUTY_FROM_ARCSOFT = yes
#人像
PRIZE_PORTRAIT_MODE			  = yes	
#水印相机
PRIZE_BRAND_WATERMARK          = yes
#AI场景识别
PRIZE_AI_SCENE         	       = yes
PRIZE_CAMERA_MAXBRIGHTNRSS     = no
#假对焦
PRIZE_CAMERA_FALSE_FOCUS	   = no	
#专业模式
PRIZE_PROFESSIONAL_MODE		   = yes
#前后摄18:9比例
PRIZE_CAMERA_SCALE_EIGHTEEN_TO_NINE = no
#前摄补光灯
PRIZE_FRONT_FLASH = yes
#更多拍照模式
PRIZE_PLUGIN_MODE = no
#大光圈模式
PRIZE_APERTURE_MODE = no
#假双摄副摄遮挡提示
PRIZE_SIMULATE_DUAL_CAMERA_TIP = no
#前后摄默认像素比例[1.3333,1.7778]
PRIZE_CAMERA_PRE_DEFAULT_ROTATION     = 1.3333
PRIZE_CAMERA_BACK_DEFAULT_ROTATION     = 1.7778
#前后摄默认像素大小[max,如3840x2160的像素值，需camera支持的]
PRIZE_CAMERA_PRE_DEFAULT_SIZE     = max
PRIZE_CAMERA_BACK_DEFAULT_SIZE     = max
#yes:normal no:facebeauty
PRIZE_DEFAULT_FRONT_MODE_NORMAL= yes
#yes:hide NUM no:show NUM
PRIZE_HIDE_PICTURE_NUM=no
#录制视频时音量键作用[no:开始/停止录制 yes:拍照]
PRIZE_VIDEO_TAKESNAPSHOT=no
#判断当前项目为代码做差分,请配置未使用的值,0：默认值,不代表任何工程 1：Blu KD项目 2:k6203s3vs 3:K6309Q2AW 4:K6309QCD
PRIZE_CURRENT_PROJECT = 4
#前摄是否是升降摄像头
PRIZE_LIFTCAMERA_SUPPORT = yes

#prize-custom camera f_number info-tangan-20181101-begin
PRIZE_BACK_CAMERA_FNUMBER = 
PRIZE_FRONT_CAMERA_FNUMBER = 
#prize-custom camera f_number info-tangan-20181101-end

#手势-快速打开相机
PRIZE_GESTER_CAMERA	= no
#电量低于5%仍然可以使用相机
PRIZE_USE_CAMERA_LOW_BATTERY = no
#下拉手电筒不能使用的电量
PRIZE_FLASHLIGHT_DISABLE_LEVEL = 0
#短按侧键进入相机
PRIZE_SHORT_CAMERA_KEY		=yes
#长按侧键进入相机
PRIZE_LONG_CAMERA_KEY		=no
#PRIZE-NXP-tangan-20161122-start
PRIZE_NXP_CAL =no
#PRIZE-NXP-tangan-20161122-end
#图库图片双击最大放大倍数
PRIZE_GALLERY_MAGNIFY                 = 2

#prize-camera  add for Insertion resolution by zhuzhengjiang 20190222-begin
PRIZE_CAMERA_INSERT_RESOLUTION_SUPPORT =yes
#prize-camera  add for Insertion resolution by zhuzhengjiang 20190222-end

#双摄标定
ARCSOFT_DUALCAMERA=yes

PRIZE_APERTURE_MODE = yes
#=====================电话/联系人=======================
#号码匹配位数
PRIZE_NUMBER_MIN_MATCH                          = 9
#拨号盘默认打开
PRIZE_DIALPAD_ALWAYS_SHOW      = yes
#拨号盘默认TAB界面(0,1,2)
PRIZE_DIALER_TAB_INDEX		   =0
#是否删除状态栏SimProcessor通知
PRIZR_IS_DEL_SIMPROCESSOR_NOTIFY =yes
#是否删除勿扰模式中的仅限优先打扰模式
PRIZE_IS_DEL_ZENMODE=no
#prize-Using the gravity sensor to achieve the call light off screen function -yanglvxiong-20180420-start
PRIZE_GSENSOR_CALL_LF_SCREEN =no
#prize-Using the gravity sensor to achieve the call light off screen function -yanglvxiong-20180420-end

#======================信息=============================
#短信输入模式设置,三个选项为：GSM alphabet,Unicode,Automatic
PRIZE_MMS_INPUT_TYPE_SETTING	=no
#信息输入文字默认3个短信长度时自动转化为彩信，N个短信长度自动转换彩信。#set value->3 4 5 6
PRIZE_SMS_TO_MMS_THRESHOLD	=3
#来短信是否亮屏
PRIZE_MMSRECEIVER_WAKEUP = yes

#=============浏览器/日历/邮件/文件管理/录音机============
#浏览器主页
PRIZE_BROWSER_HOMEPAGE          = http://www.condor.dz/
#浏览器是否支持预置本地书签
PRIZE_PRELOAD_LOCAL_BOOKMARK_SUPPORT             = yes
#是否支持本地预置雅虎书签
PRIZE_BROWSER_YAHOO_BOOKMARKS_SUPPORT            = no
#周一排在一周的第一天
PRIZE_CALENDAR_MONDAY_SORT     = no
#邮件
PRIZE_EMAIL_SIGNATURE                           = null
#prize-def_mail_storage_permissions-tangan-20170929-begin
#yes:open  no:close
PRIZE_DEFFAULT_MAIL_STORAGE_PERMISSIONS=yes
#prize-def_mail_storage_permissions-tangan-20170929-end
#prize-Add image video thumbnail in fileManager-yanglvxiong-20180510-start
PRIZE_ADD_IMAGEV_THUMB_IN_FILEMANAGER=no
#prize-Add image video thumbnail in fileManager-yanglvxiong-20180510-end

#指南针
PRIZE_COMPASS   =no

#=======================功能============================
#人脸识别
MICROTRUST_TEE_TFACE =no
#prize-add prize faceid-tangan-20180502-begin
PRIZE_FACE_ID=no
#prize-add prize faceid-tangan-20180502-end

#护眼模式
PRIZE_HUYANMODE			=yes

#百变锁屏
PRIZE_CHANGED_WALLPAPER                   	= yes
#百变锁屏默认值[0,1]
PRIZE_DEFAULT_VALUE_CHANGED_WALLPAPER       = 0

#信号灯
#设置中增加LED灯开关 ：0为不支持led 1为支持led  2为支持led且为单色灯 (设置里的菜单只控制通知栏未读信息提醒，不控制充电)
PRIZE_LED_SWITCH_SUPPORT        = 0

#FOTA
ADUPS_FOTA_SUPPORT              = no
ADUPS_FOTA_WITH_ICON            = no
ADUPS_FOTA_WITHOUT_MENU         = no
ADUPS_FOTA_SUPPORT_OVERLAY      = no

#IMEI
PRIZE_IMEI                      = no
PRIZE_AUTO_IMEI                 = no
PRIZE_CUSTOM_IMEI_DISPLAY       = no
#imei length [14,15]
PRIZE_IMEI_LENGTH		= 14

#长截屏
PRIZE_LONG_SHOTSCREEN           = yes
#三指截屏
PRIZE_SLIDE_SCREENSHOT		    = yes

#指纹
#PRIZE_FINGERPRINT_FUNCTION     = yes
#指纹位置[back,front,right]
PRIZE_FINGERPRINT_LOCATION      = front
PRIZE_FP_SUCCESS_NO_VIBRATE     = no
#指纹触控长按[0,1]
PRIZE_FP_ANSER_CALL		=0
PRIZE_FP_TAKE_PHOTO		=0
PRIZE_FP_SCREEN_CAPTURE		=0
PRIZE_FP_RETURN_HOME		=0
PRIZE_HIDE_FP_FUNC_MENU         =0
#指纹触控单击[0,1]	 
PRIZE_FP_RETURN_BACK		=0
PRIZE_FP_SLIDE_LAUNCHER		=0
PRIZE_FP_MUSIC_PLAYER		=0
PRIZE_FP_VIDEO_PLAYER		=0

#智能唤醒
PRIZE_GENE_APP                                   = yes
#翻转静音
PRIZE_FLIP_SILENT                                = yes
#翻转静音默认值[0,1]
PRIZE_DEFAULT_VALUE_FLIP_SILENT                  = 0
#prize-modify for open the smart answer call by xiekui-20181218-start
#智能接听
PRIZE_SMART_ANSWER_CALL                          = yes
#prize-modify for open the smart answer call by xiekui-20181218-end
#智能接听[0,1]
PRIZE_DEFAULT_VALUE_SMART_ANSWER_CALL            = 0
#智能拨号
PRIZE_SMART_DIALING                              = no
#智能拨号默认值[0,1]
PRIZE_DEFAULT_VALUE_SMART_DIALING                = 0
#口袋模式
PRIZE_POCKET_MODE                                = no
#口袋模式默认值[0,1]
PRIZE_DEFAULT_VALUE_POCKET_MODE                  = 0
#隔空操作
PRIZE_NON_TOUCH_OPERATION                        = no
#隔空操作默认值[0,1]
PRIZE_DEFAULT_VALUE_NON_TOUCH_OPERATION          = 0
#隔空解锁默认值[0,1]
PRIZE_DEFAULT_VALUE_NON_TOUCH_PERATION_UNLOCK    = 0
#隔空操作相册默认值[0,1]
PRIZE_DEFAULT_VALUE_NON_TOUCH_PERATION_GALLERY   = 0
#隔空切换待机界面默认值[0,1]
PRIZE_DEFAULT_VALUE_NON_TOUCH_PERATION_LAUNCHER  = 0
#隔空操作视频默认值[0,1]
PRIZE_DEFAULT_VALUE_NON_TOUCH_PERATION_VIDEO     = 0
#隔空操作音乐默认值[0,1]
PRIZE_DEFAULT_VALUE_NON_TOUCH_PERATION_MUSIC     = 0
#防误触模式
PRIZE_ANTIFAKE_TOUCH                             = no
#防误触模式默认值[0,1]
PRIZE_DEFAULT_VALUE_ANTIFAKE_TOUCH               = 0

#多套开关机动画（最多2套）
PRIZE_SWITCH_BOOTANIMA_SUPPORT  = no

#prize-关机动画时间-luolaigang-20181103-start
PRIZE_SHUTDOWN_ANIMATION_PLAY_TIME = 12
#prize-关机动画时间-luolaigang-20181103-end

#======================OS APP===========================
#prize add by zhouerlong -20180814 deskclock -start
PRIZE_DESKCLOCK=yes
PRIZE_NOTEPAD=yes
PRIZE_FILE_MANAGER=yes
PRIZE_CALCULATOR=yes
#prize add by zhouerlong -20180814 deskclock -end
#prize add by tianhuiju soundrecorder 20180911 start
PRIZE_SOUNDRECORDER_APP = yes
#prize add by tianhuiju soundrecorder 20180911 end
#prize add qiaohu oversea ui 20180914
PRIZE_CONTACTS = yes
#prize add qiaohu oversea ui 20180914
# prize add the electronic manual by houjian 20180913 -start
PRIZE_SHOW_USER_GUIDE = no
# prize add the electronic manual by houjian 20180913 -end
# prize add for remove last full charge time by houjian 20180914 -start
PRIZE_RM_BAT_LAST_FULL_CHATGE = yes
# prize add the remove last full charge time by houjian 20180914 -end
# prize add for wifi auto mac by houjian 20180927 -start
PRIZE_WIFI_AUTO_MAC = yes
# prize add the wifi auto mac by houjian 20180927 -end
#prize-add-by-lijimeng-delete MTK FM-20180927-start
PRIZE_MTK_FM = no
#prize-add-by-lijimeng-delete MTK FM-20180927-end
#prize add power key snooze by houjian 20181009 start
PRIZE_POWERKEY_SNOOZE = yes
#prize add power key snooze by houjian 20181009 end
#prize add by lishilun,fmradio,for fmradio switch 2018-09-25 begin
PRIZE_FMRADIO_APP = V7
#prize add by lishilun,fmradio,for fmradio switch  2018-09-25 end
#prize add by longzhongping, PrizeInCallUI, PrizeDialer , 2018.09.26-start
PRIZE_DIALER = yes
#prize add by longzhongping, PrizeInCallUI, PrizeDialer , 2018.09.26-end

#prize-modify-Dualcard ringtone/notificationSound- longzhongping-2018.06.12-start
PRIZE_DUALCARD_RINGTONE = yes
#prize-modify-Dualcard ringtone/notificationSound- longzhongping-2018.06.12-end

#状态栏下拉面板主题[0=白色主题,1=黑色主题]
PRIZE_QS_THEME = 0

#上滑手势
PRIZE_SWIPE_UP_GESTURE_NAVIGATION = yes
#设置关于手机-特色功能开关
PRIZE_SETTINGS_SPECIAL_FUNCYION = no
#====================================================================


#设置
#设置关于手机菜单下添加Regaltory监管菜单
PRIZE_REGALTORY_LEGAL           = no
#设置关于手机菜单下显示客户logo菜单
#同时需要替换图标res/drawable/prize_customer_logo_icon.png
PRIZE_CUSTOMER_LOGO_ICON        = no
#设置关于手机菜单下显示机身存储
PRIZE_SHOW_DEVICE_STORAGE       = no
#设置关于手机菜单下显示运行内存
PRIZE_SHOW_DEVICE_MEMORY        = no
#设置关于手机菜单下显示屏幕尺寸
PRIZE_SHOW_SCREEN_SIZE          = no
#设置关于手机菜单下显示屏幕分辩率寸
PRIZE_SHOW_SCREEN_RESOLUTION    = no
#设置关于手机菜单下显示主摄像头
PRIZE_SHOW_MAIN_CAMERA          = no
#设置关于手机菜单下显示副摄像头
PRIZE_SHOW_SUB_CAMERA           = no
#prize-Modify Arabic layout from left to right-yanglvxiong-20180507-start
PRIZE_AR_LAYOUT_LTR =no
#prize-Modify Arabic layout from left to right-yanglvxiong-20180507-end
#prize-apn order default "name DESC" -yanglvxiong-20180418-start
PRIZE_APN_ORDER_NAME_DESC =no
#prize-apn order default "name DESC" -yanglvxiong-20180418-end
#prize-delete settings gestures function-yanglvxiong-20180420-start
PRIZE_DEL_SETTINGS_GESTURES_DC =no
#prize-delete settings gestures function-yanglvxiong-20180420-end
#prize add by zhaojian-20180510 for SystemUI style -start
PRIZE_LIGHT_STYLE = yes
#prize add by zhaojian-20180510 for SystemUI style -end
#prize add by lihuangyuan,PRIZE_SPLIT_SCREEN,2018-11-16-start
PRIZE_SPLIT_SCREEN = yes
#prize add by lihuangyuan,PRIZE_SPLIT_SCREEN,2018-11-16-end

#prize add by liufan,PRIZE_GOODIX_FINGER_SWITCH,屏下指纹开关,2019-1-15-start
PRIZE_GOODIX_FINGER_SWITCH = yes
#prize add by liufan,PRIZE_GOODIX_FINGER_SWITCH,屏下指纹开关,2018-1-15-end

#prize added by lihuangyuan, open apps by fingerprint, 20190315-start
PRIZE_FINGERUNLOCK_OPENAPP = yes
#prize added by lihuangyuan, open apps by fingerprint, 20190315-end

#pcba 海外客户宏
PCBA_OVERSEA_CUSTOMER =none

#prize add for preferred sim by xiekui-20181210-start
#Add for remove the sim dialog when checking the preferred sim for sms and data
#and sets the only sim to default preferred sim of sms and data
#the scen of the operation: if the phone has two sim cards, user take out one.
PRIZE_SIM_PREFERRED = yes
#prize add for preferred sim by xiekui-20181210-end

#prize add by peisaisai for fingerprint-nubmer info -20181211-begin
BUILD_NUMBER := $(shell date +%s)
#prize add by peisaisai for fingerprint-nubmer info -20181211-end

#prize-add for language change by xiekui-20190123-start
# 切换语言加载界面
PRIZE_LANGUAGE_CHANGE_PROGRESS = no
#prize-add for language change by xiekui-20190123-start

#prize-add by xiekui, screen zoom,20190302-start
#设置中显示大小
PRIZE_SCREEN_ZOOM = yes
#prize-add by xiekui, screen zoom,20190302-end

#prize added by xiekui, configuration of wifi call(settings menu), 20190308-start
#设置中WLAN通话菜单
PRIZE_WIFI_CALL = yes
#prize added by xiekui, configuration of wifi call(settings menu), 20190308-end

#prize added by xiekui, configuration of elastic effect, 20190321-start
#设置下拉回弹效果
PRIZE_SETTINGS_ELASTIC = yes
#prize added by xiekui, configuration of elastic effect, 20190321-end

#===========================增减APP==============================
#普通版本去掉 google messages CarrierServices 简化宏
PRIZE_MESSAGES_AND_CS_NOT_BUILD_IN = no
#增加APP
PRODUCT_PACKAGES += \
    CalendarGoogle \
    CarrierServices \
    Messages

#删除APP
PRODUCT_PACKAGES_DELETE = \
	messaging \
	Mms \
	MtkMms \
	Calendar \
	GoogleCalendarSyncAdapter \
	MtkCalendar

	 
#prize add by liyuchong,MTK_CMASReceiver apk, 2019.01.22-begin
MTK_CMAS_SUPPORT = yes
#prize add by liyuchong,MTK_CMASReceiver apk, 2019.01.22-end

