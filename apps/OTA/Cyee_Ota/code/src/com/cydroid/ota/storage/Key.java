package com.cydroid.ota.storage;

/**
 * Created by borney on 4/14/15.
 */
public final class Key {

    public static final class Setting {
        public static final String KEY_SETTING_CONTEXTSTATE = "key_setting_contextstate";
        public static final String KEY_SETTING_DOWNLOAD_FILE_NAME = "key_setting_download_file_name";
        public static final String KEY_SETTING_UPDATE_CURRENT_RELEASENOTE = "key_setting_update_current_releasenote";
        public static final String KEY_SETTING_UPDATE_CURRENT_RELEASEURL = "key_setting_update_current_releaseurl";
        public static final String KEY_SETTING_UPDATE_CURRENT_VERSION = "key_setting_update_current_version";
        public static final String KEY_SETTING_UPDATE_LAST_CHECK_TIME = "key_setting_update_last_check_time";
        public static final String KEY_SETTING_UPDATE_CURRENT_PID_FROM_USER = "key_setting_update_current_pid_from_user";
        public static final String KEY_SETTING_QUESTIONNAIRE_COUNT = "key_setting_questionnaire_count";
        public static final String KEY_UPGRADE_RECOVER_UPGRADE_FLAG = "key_upgrade_recover_upgrade_flag";
        public static final String KEY_UPGRADE_UPGRADE_ONLINE_FLAG = "key_upgrade_upgrade_online_flag";
        public static final String KEY_MOBILE_NET_ENABLE = "key_mobile_net_enable";
        public static final String KEY_SETTING_QUESTIONNAIRE_INFO = "key_setting_questionnaire_info";
        public static final String KEY_SETTING_QUESTIONNAIRE_SYS_VERSION = "key_setting_questionnaire_sys_version";
        public static final String KEY_SETTING_QUESTIONNAIRE_READ = "key_setting_questionnaire_read";
        public static final String KEY_SETTING_QUESTIONNAIRE_LAST_CHECK_TIME = "KEY_SETTING_QUESTIONNAIRE_LAST_CHECK_TIME";
        public static final String KEY_SETTING_UPDATE_SUCCESS_DATE = "key_setting_update_success_date";
        public static final String KEY_SETTING_UPDATE_LAST_VERSION = "key_setting_update_last_version";
        public static final String KEY_SETTING_UPDATE_LAST_DOWNLOAD_FLAG = "key_setting_update_last_download_flag";
        public static final String KEY_FIRST_START_SYSTEM_UPDATE_FLAG = "key_first_start_system_update_flag";
        public static final String KEY_SETTING_UPDATE_PACKAGE_NAME = "KEY_SETTING_UPDATE_PACKAGE_NAME";
        public static final String KEY_FIRST_BOOT_COMPLEPED_AFRET_UPGRADE = "KEY_FIRST_BOOT_COMPLEPED_AFRET_UPGRADE";
    }

    public static final class Push {
        public static final String KEY_PUSH_RECEIVER_NOTIFIER_ID = "key_push_receiver_notifier_id";
        public static final String KEY_PUSH_GPE_REGISTER_RESULT = "key_push_gpe_register_result";
        public static final String KEY_PUSH_RID = "key_push_rid";
        public static final String KEY_PUSH_RID_REGISTER_RESULT = "key_push_rid_register_result";
        public static final String KEY_PUSH_NEW_VERSION_NUM = "key_push_new_version_num";

    }

    public static final class WlanAuto {
        public static final String KEY_WLAN_AUTO_UPGRADE_SWITCH = "key_wlan_auto_upgrade_switch";
        public static final String KEY_AUTO_UPGRADE_LAST_NOTIFY_SYSTEM_VERSION = "key_auto_upgrade_last_notify_system_version";
        public static final String KEY_AUTO_UPGRADE_NEW_VERSION_NOTIFY_COUNT = "key_auto_upgrade_new_version_notify_count";
        public static final String KEY_AUTO_UPGRADE_DOWNLOAD_COMPLETE_NOTIFY_COUNT = "key_auto_upgrade_download_complete_notify_count";
    }

    public static final class SettingUpdateInfo {
        public static final String KEY_SETTING_UPDATE_INFO_RELEASE_NOTE = "key_setting_update_info_releasenote";
        public static final String KEY_SETTING_UPDATE_INFO_MD5 = "key_setting_update_info_md5";
        public static final String KEY_SETTING_UPDATE_INFO_DOWNLOAD_URL = "key_setting_update_info_downloadurl";
        public static final String KEY_SETTING_UPDATE_INFO_VERSION = "key_setting_update_info_version";
        public static final String KEY_SETTING_UPDATE_INFO_FILE_SIZE = "key_setting_update_info_file_size";
        public static final String KEY_SETTING_UPDATE_INFO_IS_PRE_RELEASE = "key_setting_update_info_is_pre_release";
        public static final String KEY_SETTING_UPDATE_INFO_VERSION_RELEASE_DATE = "key_setting_update_info_version_release_date";
        public static final String KEY_SETTING_UPDATE_INFO_RELEASE_NOTE_URL = "key_setting_update_info_release_note_url";
        public static final String KEY_SETTING_UPDATE_INFO_INTERNAL_VER = "key_setting_update_info_internal_ver";
        public static final String KEY_SETTING_UPDATE_INFO_DOWNLOAD_PEOPLE_NUM = "key_setting_update_info_download_people_num";
        public static final String KEY_SETTING_UPDATE_INFO_EXT_PKG = "key_setting_update_info_ext_pkg";
    }
}
