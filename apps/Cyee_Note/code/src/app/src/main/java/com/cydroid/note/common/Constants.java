package com.cydroid.note.common;

import android.os.Environment;
import android.os.Build;
import com.cydroid.note.common.Log;

import com.cydroid.note.app.NoteAppImpl;

import java.io.File;

public class Constants {
    private static final String TAG = "Constants";
    public static final String STR_NEW_LINE = "\n";
    public static final char CHAR_NEW_LINE = '\n';

    //media story path
    public static final File ROOT_FILE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    public static final File NOTE_MEDIA_PATH;
    public static final File NOTE_MEDIA_THUMBNAIL_PATH;
    public static final File NOTE_MEDIA_SOUND_PATH;
    public static final File NOTE_MEDIA_PHOTO_PATH;
	//GIONEE wanghaiyan 2018-2-6 modify for CSW1705A-1501 begin
    //public static final File NOTE_MEDIA_IMAGE_TEMP_SHARE_PATH;
	//GIONEE wanghaiyan 2018-2-6 modify for CSW1705A-1501 end

    public static final int ENCRYPT_HINT_ABLE = 0;
    public static final int ENCRYPT_HINT_DISABLE = 1;
    public static final int ENCRYPT_REMIND_NOT_READ = 0;
    public static final int ENCRYPT_REMIND_READED = 1;

    public static final int NOTE_DISPLAY_NONE = 0;
    public static final int NOTE_DISPLAY_LIST_MODE = 1;
    public static final int NOTE_DISPLAY_GRID_MODE = 2;

    //media type
    //Gionee wanghaiyan 2017-8-9 modify for 181997 begin
    public static final String MEDIA_PHOTO = "<photo:/>";
    public static final String MEDIA_SOUND = "<sound:/>";
    public static final String MEDIA_BILL = "<bills:/>";
    //Gionee wanghaiyan 2017-8-9 modify for 181997 end

    public static final int ONE_M = 1024 * 1024;

    public static final String NOTE = "/Notes";
    public static final String SYSTEM_ETC_DIR = "/system/etc/Cyee_Note/";

    public static final String NOTE_IS_CRYPTED = "is_encrpyted";

    public static final String SOUND_ENCRYPT_PATH = NoteAppImpl.getContext().getFilesDir().getPath();
    /**
     * 安全OS加密后文件存放路径
     */
     //Chenyee wanghaiyan 2017-12-28 modify for CSW1702A-2107 begin
     public static final String SECURITY_OS_ENCRYPT_PATH;
     static {
	 	if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O){
        	SECURITY_OS_ENCRYPT_PATH = "/data/misc/gionee/secret";
		}else{
			SECURITY_OS_ENCRYPT_PATH = "/data/misc/msdata/secret";
 		}
	 }
	 //Chenyee wanghaiyan 2017-12-28 modify for CSW1702A-2107 end

    public static final String IS_SECURITY_SPACE = "is_security_space";

    static {
        if (PlatformUtil.isGioneeDevice()) {
            NOTE_MEDIA_PATH = new File(ROOT_FILE, "/.NoteMedia");
        } else {
            NOTE_MEDIA_PATH = new File(ROOT_FILE, "/.Media");
        }
        NOTE_MEDIA_THUMBNAIL_PATH = new File(NOTE_MEDIA_PATH, "thumbnail");
        NOTE_MEDIA_SOUND_PATH = new File(NOTE_MEDIA_PATH, "sound");
	 //GIONEE wanghaiyan 2016-12-21 modify for 48774 begin
        NOTE_MEDIA_PHOTO_PATH = new File(NOTE_MEDIA_PATH, "Photo");
	 //GIONEE wanghaiyan 2016-12-21 modify for 48774 end
	   //GIONEE wanghaiyan 2018-2-6 modify for CSW1705A-1501 begin
       //NOTE_MEDIA_IMAGE_TEMP_SHARE_PATH = new File(NOTE_MEDIA_PATH, "temp_share");
	   //GIONEE wanghaiyan 2018-2-6 modify for CSW1705A-1501 end
    }
    //GIONEE wanghaiyan 2018-2-6 modify for CSW1705A-1501 begin
    public static final File NOTE_MEDIA_IMAGE_TEMP_SHARE_PATH = new File(ROOT_FILE, "/NoteMedia/temp_share");
	//GIONEE wanghaiyan 2018-2-6 modify for CSW1705A-1501 end
}
