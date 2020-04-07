package com.cydroid.note.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cydroid.note.app.NoteMainActivity;
import com.cydroid.note.common.Constants;

/**
 * Created by gaojt on 16-1-26.
 */
public class NoteShareDataManager {

    private static final String NOTE_PREFERENCE = "note_preference";
    private static final String NOTE_SIGNATURE = "note_signature";
    private static final String FIRST_LAUNCH = "first_launch";
    private static final String LABEL_INIT = "label_init";
    private static final String AI_SWITCH_OPEN = "ai_switch_open";
    private static final String SECRET_SWITCH_OPEN = "secret_switch_open";
    private static final String IMPORT_BACKUP_DATA_CONFIG = "import_backup_data_config";
    private static final String KEY_IMPORT_BACKUP_DATA_FINISH = "key_import_backup_data_finish";
    private static final String KEY_BACKUP_DATA_TO_TEMP_FINISH = "key_backup_data_to_temp_finish";
    private static final String KEY_BACKUP_DATA_TO_TEMP_FILE_PATH = "key_backup_data_to_temp_file_path";
    private static final String IS_SHOW_DATA_FLOW_HINE = "show_data_flow_hint";
    private static final String PASSWORD = "password";
    private static final String PASSWORD_PROTECT_QUESTION = "protect_question";
    private static final String PASSWORD_PROTECT_ANSWER = "protect_answer";
    private static final String SHOW_ENCRYPT_USER_GUIDE = "show_encrypt_user_guide";
    private static final String INPUT_PASSWORD_ERROR_EXCEED_FOUR_TIMES = "input_password_error_exceed_four_times";
    private static final String INPUT_PASSWORD_ERROR_EXCEED_FOUR_SYSTEM_TIME = "input_password_error_exceed_four_system_time";
    private static final String INPUT_PASSWORD_ERROR_COUNT = "input_password_error_count";
    private static final String ASSWORD_AND_QUESTIONS_SET_PSUCCESSFUL = "password_and_questions_set_successful";
    private static final String IS_ENCRYPT_USER_GUIDE_NORMAL_CLOSED = "is_encrypt_user_guide_normal_closed";
    private static final String ENCRYPT_NOT_COMPLETED_NOTE_IMAGES = "encryp_not_completed_note_images";
    private static final String DECRYPT_NOT_COMPLETED_NOTE_IMAGES = "decrypt_note_completed_note_images";
    private static final String ENCRYPT_NOT_COMPLETED_NOTE_SOUNDS = "encryp_not_completed_note_sounds";
    private static final String DECRYPT_NOT_COMPLETED_NOTE_SOUNDS = "decrypt_note_completed_note_sounds";
    private static final String IS_EXIT = "is_exit";
    private static final String TRASH_ALERT_KEY = "trash_alert_key";
    private static final String NOTE_DIPLAY_MODE = "display_mode";

    public static String getSignatureText(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(NOTE_PREFERENCE,
                Context.MODE_PRIVATE);
        return preferences.getString(NOTE_SIGNATURE, "");
    }

    public static void saveSignature(Context context, String signature) {
        SharedPreferences preferences = context.getSharedPreferences(NOTE_PREFERENCE,
                Context.MODE_PRIVATE);
        preferences.edit().putString(NOTE_SIGNATURE, signature).commit();
    }

    public static boolean getIsFirstLaunch(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(FIRST_LAUNCH, true);
    }

    public static void setIsFirstLaunch(Context context, boolean first) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(FIRST_LAUNCH, first).commit();
    }

    public static boolean getImportToTempFinishValue(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG,
                Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_BACKUP_DATA_TO_TEMP_FINISH, false);
    }

    public static String getTempFilePath(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG,
                Context.MODE_PRIVATE);
        return preferences.getString(KEY_BACKUP_DATA_TO_TEMP_FILE_PATH, null);
    }

    public static boolean writeFinishImportToTemp(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_BACKUP_DATA_TO_TEMP_FINISH, true);
        return editor.commit();
    }

    public static void writeTempFilePath(Context context, String filePath) {
        SharedPreferences preferences = context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_BACKUP_DATA_TO_TEMP_FILE_PATH, filePath);
        editor.commit();
    }

    public static boolean writeFinishImport(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_IMPORT_BACKUP_DATA_FINISH, true);
        return editor.commit();
    }

    public static boolean getImportBackupDataFinish(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG,
                Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_IMPORT_BACKUP_DATA_FINISH, false);
    }

    public static boolean getIsInitLabel(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(LABEL_INIT, false);
    }

    public static void setIsInitLabel(Context context, boolean init) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(LABEL_INIT, init).commit();
    }

    public static boolean isAISwitchOpen(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(AI_SWITCH_OPEN, true);
    }

    public static void setAISwitchValue(Context context, boolean isOpen) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(AI_SWITCH_OPEN, isOpen).commit();
    }

    public static boolean isSecretSwitchOpen(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(SECRET_SWITCH_OPEN, true);
    }

    public static void setAISecretSwitchValue(Context context, boolean isOpen) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(SECRET_SWITCH_OPEN, isOpen).commit();
    }

    public static void setShowDataFlowHint(Context context, boolean isShow) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(IS_SHOW_DATA_FLOW_HINE, isShow).commit();
    }

    public static boolean getHasShowDataFlowHint(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(IS_SHOW_DATA_FLOW_HINE, false);
    }

    public static void setPassword(Context context, String password) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(PASSWORD, password).commit();
    }

    public static String getPassword(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PASSWORD, "");
    }

    public static void setProtectQuestion(Context context, String password) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(PASSWORD_PROTECT_QUESTION, password).commit();
    }

    public static String getProtectQuestion(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PASSWORD_PROTECT_QUESTION, "");
    }

    public static void setPasswordProtectAnswer(Context context, String protectAnswer) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(PASSWORD_PROTECT_ANSWER, protectAnswer).commit();
    }

    public static String getPasswordProtectAnswer(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PASSWORD_PROTECT_ANSWER, "");
    }

    public static void setShowEncryptUserGuide(Context context, int showUserGuideType) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putInt(SHOW_ENCRYPT_USER_GUIDE, showUserGuideType).commit();
    }

    public static int isShowEncryptUserGuide(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(SHOW_ENCRYPT_USER_GUIDE, NoteMainActivity.ENCRYPT_USER_GUIDE_DEFAULT);
    }

    public static void setInputPasswordErrorExceedFourTimes(Context context, int times) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putInt(INPUT_PASSWORD_ERROR_EXCEED_FOUR_TIMES, times).commit();
    }

    public static int getInputPasswordErrorExceedFourTimes(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(INPUT_PASSWORD_ERROR_EXCEED_FOUR_TIMES, 0);
    }

    public static void setInputPasswordErrorExceedFourSystemTime(Context context, long systemTime) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putLong(INPUT_PASSWORD_ERROR_EXCEED_FOUR_SYSTEM_TIME, systemTime).commit();
    }

    public static long getInputPasswordErrorExceedFourSystemTime(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getLong(INPUT_PASSWORD_ERROR_EXCEED_FOUR_SYSTEM_TIME, System.currentTimeMillis());
    }

    public static void setInputPasswordErrorCount(Context context, int count) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putInt(INPUT_PASSWORD_ERROR_COUNT, count).commit();
    }

    public static int getInputPasswordErrorCount(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(INPUT_PASSWORD_ERROR_COUNT, 0);
    }

    public static void setPasswordAndQuestionsSetSuccessful(Context context, boolean success) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(ASSWORD_AND_QUESTIONS_SET_PSUCCESSFUL, success).commit();
    }

    public static boolean getPasswordAndQuestionsSetSuccessful(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(ASSWORD_AND_QUESTIONS_SET_PSUCCESSFUL, false);
    }

    public static void setIsEncryptUserGuideNormalClosed(Context context, boolean normalClosed) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(IS_ENCRYPT_USER_GUIDE_NORMAL_CLOSED, normalClosed).commit();
    }

    public static boolean isEncryptUserGuideNormalClosed(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(IS_ENCRYPT_USER_GUIDE_NORMAL_CLOSED, true);
    }

    public static void setEncryptNotCompleteImages(Context context, String imagePaths) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(ENCRYPT_NOT_COMPLETED_NOTE_IMAGES, imagePaths).commit();
    }

    public static String getEncryptNotNotCompleteImages(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(ENCRYPT_NOT_COMPLETED_NOTE_IMAGES, "");
    }

    public static void setDecryptNotCompleteImages(Context context, String imagePaths) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(DECRYPT_NOT_COMPLETED_NOTE_IMAGES, imagePaths).commit();
    }

    public static String getDecryptNotNotCompleteImages(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(DECRYPT_NOT_COMPLETED_NOTE_IMAGES, "");
    }

    public static void setEncryptNotCompletedSounds(Context context, String soundPaths) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(ENCRYPT_NOT_COMPLETED_NOTE_SOUNDS, soundPaths).commit();
    }

    public static String getEncryptNotNotCompleteSounds(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(ENCRYPT_NOT_COMPLETED_NOTE_SOUNDS, "");
    }

    public static void setDecryptNotCompleteSounds(Context context, String soundPaths) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putString(DECRYPT_NOT_COMPLETED_NOTE_SOUNDS, soundPaths).commit();
    }

    public static String getDecryptNotNotCompleteSounds(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(DECRYPT_NOT_COMPLETED_NOTE_SOUNDS, "");
    }

    public static boolean getShowTrashAlert(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(TRASH_ALERT_KEY, true);
    }

    public static void setShowTrashAlert(Context context, boolean show) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(TRASH_ALERT_KEY, show).commit();
    }

    public static void setNoteDisplayMode(Context context, int displayMode) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putInt(NOTE_DIPLAY_MODE, displayMode).commit();
    }

    public static int getNoteDisplayMode(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(NOTE_DIPLAY_MODE, Constants.NOTE_DISPLAY_NONE);
    }

}
