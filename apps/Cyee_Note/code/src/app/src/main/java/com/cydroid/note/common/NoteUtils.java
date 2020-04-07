package com.cydroid.note.common;

import android.Manifest;
import android.app.ActivityManager;
//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin
import android.app.Activity;
//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import com.gionee.framework.log.Logger;
import com.cydroid.note.R;
import com.cydroid.note.app.Config;
import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.ImageCache;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.span.PhotoImageSpan;
import com.cydroid.note.app.span.SoundImageSpan;
import com.cydroid.note.data.LocalNoteItem;
import com.cydroid.note.data.LocalNoteSet;
import com.cydroid.note.data.NoteInfo;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.Path;
import com.cydroid.note.encrypt.DES;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteShareDataManager;
import com.cydroid.note.trash.data.TrashNoteItem;
import com.cydroid.note.widget.WidgetUtil;
import com.cydroid.note.common.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Gionee wanghaiyan 2015-11-12 add for CR01581714 begin
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import com.cydroid.note.common.Log;
import cyee.provider.CyeeSettings;
import com.cydroid.note.app.NoteAppImpl;
//Gionee wanghaiyan 2015-11-12 add for CR01581714 end
import java.util.Locale;
//GIONEE wanghaiyan 2017-2-10 modify for 66156 begin
import android.os.SystemProperties;
//GIONEE wanghaiyan 2017-2-10 modify for 66156 end

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "MS_CANNOT_BE_FINAL", justification = "seems no problem")
public class NoteUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "NoteUtils";
    private static final boolean AT_BEFORE_23 = Build.VERSION.SDK_INT < 23;
    //GIONEE wanghaiyan 2017-2-10 modify for 66156 begin
    public static final boolean gnKRFlag = SystemProperties.get("ro.cy.custom").equals("KOREA_BOE");
    //GIONEE wanghaiyan 2017-2-10 modify for 66156 end
    //GIONEE wanghaiyan 2017-3-2 modify for 77724 begin
    public static final boolean gnIPFlag = SystemProperties.get("ro.cy.custom").equals("ISRAEL_PELEPHONE");
    //GIONEE wanghaiyan 2017-3-2 modify for 77724 end
    //GIONEE wanghaiyan 2017-5-16 modify for 77724 begin
    public static final boolean gnBluFlag = SystemProperties.get("ro.cy.custom").equals("SOUTH_AMERICA_BLU");
    //GIONEE wanghaiyan 2017-5-16 modify for 77724 end
    //Chenyee wanghaiyan 2018-6-13 modify for CSW1703CX-1072 begin
    public static final boolean gnXLJFlag = SystemProperties.get("ro.cy.custom").equals("XiaoLaJiao");
    //Chenyee wanghaiyan 2018-6-13 modify for CSW1703CX-1072 end
    //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    public static final int REQUEST_CODE_ASK_STORAGE_PERMISSIONS = 125;
    //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end


    public static int sScreenWidth;//NOSONAR
    public static int sScreenHeight;//NOSONAR
    public static float sDensity;//NOSONAR

    public static void initScreenSize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        sScreenWidth = Math.min(metrics.widthPixels, metrics.heightPixels);
        sScreenHeight = Math.max(metrics.widthPixels, metrics.heightPixels);
        sDensity = metrics.density;
        if (DEBUG) {
            Logger.printLog(TAG, "sScreenWidth = " + sScreenWidth + ",sScreenHeight = " + sScreenHeight);
        }
    }

    public static Uri convertToFileUri(Context context, Uri uri) {
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            return uri;
        }

        if (Build.VERSION.SDK_INT >= 24 && ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            return uri;
        } else if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context, uri)) {
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                String filePath = getDataColumn(context, contentUri, selection, selectionArgs);
                if (filePath != null) {
                    return Uri.parse("file://" + filePath);
                }
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String filePath = getDataColumn(context, uri, null, null);
            if (filePath != null) {
                return Uri.parse("file://" + filePath);
            }
        }

        return uri;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(column));
            }
        } finally {
            closeSilently(cursor);
        }
        return null;
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String formatLeftDays(Context context, long startPointInMs, int keepDays) {
        long currTimeInMs = System.currentTimeMillis();
        long elapsedTimeInMs = currTimeInMs - startPointInMs;
        long leftTimeInMs = Math.max(0, TrashNoteItem.KEEP_DAYS_IN_MILLIS - elapsedTimeInMs);
        int leftDays;
        if (elapsedTimeInMs <= 0) {
            leftDays = keepDays;
        } else {
            leftDays = (int) Math.ceil(leftTimeInMs / (float) DateUtils.DAY_IN_MILLIS);
            leftDays = Math.min(keepDays, leftDays);
        }

        String formatDays = context.getResources().getQuantityString(R.plurals.trash_left_days, leftDays);
        return String.format(formatDays, leftDays);
    }

    public static String formatDateTime(long timeInMs, SimpleDateFormat formatter) {
        Date date = new Date(timeInMs);
        return formatter.format(date);
    }

    public static String formatTime(int elapse, String connectChar) {
        int hour = elapse / 3600;
        int minute = (elapse % 3600) / 60;
        int second = elapse % 60;
        StringBuilder builder = new StringBuilder();
        appendFormat(builder, hour, true, connectChar);
        appendFormat(builder, minute, true, connectChar);
        appendFormat(builder, second, false, connectChar);
        return builder.toString();
    }

    private static void appendFormat(StringBuilder builder, int digital, boolean connect, String connectChar) {
        if (digital < 10) {
            builder.append("0");
        }
        builder.append(digital);
        if (connect) {
            builder.append(connectChar);
        }
    }

    public static void deleteOriginMediaFile(String json, boolean isEncrypt) {
        if (TextUtils.isEmpty(json)) {
            return;
        }
        try {
            JSONTokener jsonParser = new JSONTokener(json);
            JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
            JSONArray spans = jsonObject.optJSONArray(DataConvert.JSON_SPANS_KEY);
            if (spans == null || spans.length() == 0) {
                return;
            }
            int length = spans.length();
            String noteImageSpan = PhotoImageSpan.class.getName();
            String soundImageSpan = SoundImageSpan.class.getName();
            for (int i = 0; i < length; i++) {
                JSONObject span = spans.getJSONObject(i);
                String type = span.getString(DataConvert.SPAN_ITEM_TYPE);
                if (type.equals(noteImageSpan)) {
                    String originUri = span.getString(PhotoImageSpan.ORIGIN_URI);
                    String thumbUri = span.getString(PhotoImageSpan.THUMB_URI);
                    deleteImageFile(Uri.parse(originUri), Uri.parse(thumbUri), isEncrypt);
                } else if (type.equals(soundImageSpan)) {
                    String originPath = span.getString(SoundImageSpan.ORIGIN_PATH);
                    deleteSoundPath(originPath, isEncrypt);
                }
            }
        } catch (Throwable e) {
            Logger.printLog(TAG, "getOriginData fail : " + e);
        }
    }

    private static void deleteSoundPath(String originPath, boolean isEncrypt) {
        if (PlatformUtil.isSecurityOS() && isEncrypt) {
            String deletePath = Uri.parse(originPath).getPath();
            deleteFile(EncryptUtil.getSecuritySpacePath(deletePath));
        } else if (isEncrypt) {
            String deletePath = Uri.parse(originPath).getPath();
            String deleteName = EncryptUtil.getFileName(deletePath);
            File deleteFile = new File(Constants.SOUND_ENCRYPT_PATH + File.separator + deleteName);
            if (deleteFile.exists()) {
                deleteFile.delete();//NOSONAR
            }
        } else {
            deleteFile(originPath);
        }
    }

    public static boolean deleteFile(String originPath) {
        File originFile = new File(originPath);
        if (originFile.exists()) {
            boolean success = originFile.delete();
            if (!success) {
                return false;
            }
        }
        return true;
    }

    public static boolean deleteImageFile(Uri originUri, Uri thumbUri, boolean isEncrypt) {
        String originPath = originUri.getPath();
        String thumbPath = thumbUri.getPath();
        boolean deleteOriginSuccess = false;
        boolean deleteThumbSuccess = false;

        if (PlatformUtil.isSecurityOS() && isEncrypt) {
            return deletFileFromSecuritySpace(originPath, thumbPath);
        }

        if (originPath.startsWith(Constants.NOTE_MEDIA_PHOTO_PATH.toString())) {
            File originFile = new File(originPath);
            if (originFile.exists()) {
                deleteOriginSuccess = originFile.delete();
            }
        }
        File thumbFile = new File(thumbPath);
        if (thumbFile.exists()) {
            deleteThumbSuccess = thumbFile.delete();
        }

        return deleteOriginSuccess && deleteThumbSuccess;
    }

    private static boolean deletFileFromSecuritySpace(String originPath, String thumbPath) {
        String deleteThumbPath = EncryptUtil.getSecuritySpacePath(thumbPath);
        boolean suceess = deleteFile(deleteThumbPath);
        if (!thumbPath.equals(originPath)) {
            String deleteOriginPath = EncryptUtil.getSecuritySpacePath(originPath);
            suceess = deleteFile(deleteOriginPath);
        }
        return suceess;
    }


    public static boolean saveBitmap(Bitmap bitmap, File file) {
        OutputStream os = null;
        boolean success = false;
        try {
            os = new FileOutputStream(file);
            success = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
            return success;
        } catch (Throwable e) {
            Logger.printLog(TAG, "error:" + e);
        } finally {
            if (!success) {
                Logger.printLog(TAG, "bitmap file delete");
                file.delete();  //NOSONAR
            }
            NoteUtils.closeSilently(os);
        }
        return false;
    }

    public static boolean saveBitmap(Bitmap bitmap, File file, Bitmap.CompressFormat format, int quality) {
        OutputStream os = null;
        boolean success = false;
        try {
            os = new FileOutputStream(file);
            success = bitmap.compress(format, quality, os);
            return success;
        } catch (Throwable e) {
            Logger.printLog(TAG, "error:" + e);
        } finally {
            if (!success) {
                Logger.printLog(TAG, "bitmap file delete "+quality);
                file.delete();  //NOSONAR
            }
            NoteUtils.closeSilently(os);
        }
        return false;
    }

    public static File getSaveImageFile(File fileDirectory) {
        File file = new File(fileDirectory, "/" + System.currentTimeMillis());
        if (!file.exists()) {
            return file;
        }

        Random random = new Random();
        do {
            long path = System.currentTimeMillis() + random.nextLong();
            file = new File(fileDirectory, "/" + path);
        } while (file.exists());
        return file;
    }

    public static boolean fileNotFound(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(context, R.string.file_note_found, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static String customName(Context context, String path) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (storageManager == null) {
            return path;
        }
        try {
            Class<?> property = Class.forName("android.os.storage.StorageVolume");
            Method method = StorageManager.class.getDeclaredMethod("getVolumeList");
            Object[] storageVolume = (Object[]) method.invoke(storageManager);
            if (storageVolume == null) {
                return path;
            }
            int length = storageVolume.length;
            method = property.getDeclaredMethod("getPath");
            for (int i = 0; i < length; i++) {
                String rootPath = (String) method.invoke(storageVolume[i]);
                if (path.startsWith(rootPath)) {
                    method = property.getDeclaredMethod("getDescription", Context.class);
                    String newRootSt = (String) method.invoke(storageVolume[i], context);
                    path = newRootSt + path.substring(rootPath.length(), path.length());
                    break;
                }
            }
        } catch (Exception e) {
            Logger.printLog(TAG, "customName fail:" + e);
        }

        return path;
    }

    // Throws AssertionError if the input is false.
    public static void assertTrue(boolean cond) {
        if (!cond) {
            //GIONEE wanghaiyan 2016-12-15 modify for 47296 begin
            //throw new AssertionError();
            //GIONEE wanghaiyan 2016-12-15 modify for 47296 end
        }
    }

    // Throws AssertionError if the input is false.
    public static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            //GIONEE wanghaiyan 2016-12-15 modify for 47296 begin
            //throw new AssertionError(msg);
            //GIONEE wanghaiyan 2016-12-15 modify for 47296 end
        }
    }

    public static void flushSilently(Flushable f) {
        if (f == null) {
            return;
        }
        try {
            f.flush();
        } catch (IOException t) {
            Logger.printLog(TAG, "flush fail :" + t);
        }
    }

    public static void closeSilently(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException t) {
            Logger.printLog(TAG, "close fail :" + t);
        }
    }

    public static void closeSilently(ParcelFileDescriptor fd) {
        try {
            if (fd != null) {
                fd.close();
            }
        } catch (Throwable t) {
            Logger.printLog(TAG, "fail to close:" + t);
        }
    }

    public static void closeSilently(Cursor cursor) {
        try {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable t) {
            Logger.printLog(TAG, "fail to close:" + t);
        }
    }

    public static void closeSilently(SQLiteDatabase database) {
        try {
            if (database != null) {
                database.close();
            }
        } catch (Throwable t) {
            Logger.printLog(TAG, "fail to close:" + t);
        }
    }

    // Returns the previous power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0
    public static int prevPowerOf2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }

    // Returns the input value x clamped to the range [min, max].
    public static int clamp(int x, int min, int max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }

    // Throws AssertionError with the message. We had a method having the form
    //   assertTrue(boolean cond, String message, Object ... args);
    // However a call to that method will cause memory allocation even if the
    // condition is false (due to autoboxing generated by "Object ... args"),
    // so we don't use that anymore.
    //Chenyee wanghaiyan 2017-11-3 modify for SW17W16A-912 begin
    public static void fail(String message, Object... args) {
        // throw new AssertionError(
        //        args.length == 0 ? message : String.format(message, args));//wanghaiyan
    }
    //Chenyee wanghaiyan 2017-11-3 modify for SW17W16A-912 end

    // Returns true if two input Object are both null or equal
    // to each other.
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a == null ? false : a.equals(b));
    }

    public static String createPlainTextJsonContent(String content) {
        try {
            JSONStringer jsonStringer = new JSONStringer();
            jsonStringer.object();
            jsonStringer.key(DataConvert.JSON_CONTENT_KEY).value(content);
            jsonStringer.endObject();
            return jsonStringer.toString();
        } catch (JSONException e) {
            Logger.printLog(TAG, "error:" + e);
            return null;
        }
    }

    public static ArrayList<Integer> indexofs(String content, String subStr) {
        if (!content.contains(subStr)) {
            return null;
        }
        int subStrLength = subStr.length();
        int fromIndex = 0;
        ArrayList<Integer> indexs = null;
        while (true) {
            int index = content.indexOf(subStr, fromIndex);
            if (index < 0) {
                break;
            }
            if (indexs == null) {
                indexs = new ArrayList(3);
            }
            indexs.add(Integer.valueOf(index));
            fromIndex = index + subStrLength;
        }
        return indexs;
    }


    public static boolean checkHasSmartBar() {
        boolean hasSmartBar = false;
        boolean isException = true;
        try {
            Method method = Class.forName("android.os.Build").getMethod("hasSmartBar");
            hasSmartBar = ((Boolean) method.invoke(null)).booleanValue();
            isException = false;
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (ClassNotFoundException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } finally {
            if (isException) {
                if (Build.DEVICE.equals("mx2")) {
                    hasSmartBar = true;
                } else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
                    hasSmartBar = false;
                }
            }
        }
        return hasSmartBar;
    }

    public static String lineSpaceFilter(String source) {
        String filterString = source;
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(filterString);
        filterString = m.replaceAll("");
        return filterString;
    }

    public static int getIdFromPath(String path, boolean isEncrypted) {
        int id = NoteItem.INVALID_ID;
        String pathString = path;

        if (TextUtils.isEmpty(pathString)) {
            return id;
        }

        String[] pathArray = Path.split(pathString);
        if (pathArray.length >= 1) {
            id = parseInt(pathArray[pathArray.length - 1]);
        }
        //fix bug begin
        //This is only a temporary solution.
        if (isExist(id, isEncrypted)) {
            return id;
        }
        return NoteItem.INVALID_ID;
        //fix bug end
    }

    private static boolean isExist(int id, boolean isEncrypted) {
        ContentResolver resolver = NoteAppImpl.getContext().getContentResolver();
        Cursor cursor = LocalNoteSet.getItemCursor(resolver,
                getContentUri(isEncrypted), null, id);
        try {
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
        } finally {
            closeSilently(cursor);
        }
        return true;
    }

    private static int parseInt(String id) {
        int parseId = NoteItem.INVALID_ID;
        try {
            parseId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
        }
        return parseId;
    }

    public static NoteInfo getNoteItemFromDB(int id, boolean isEncrypted) {
        NoteInfo item = null;
        ContentResolver resolver = NoteAppImpl.getContext().getContentResolver();
        Cursor cursor = LocalNoteSet.getItemCursor(resolver,
                getContentUri(isEncrypted),
                LocalNoteSet.NOTE_PROJECTION, id);
        if (null == cursor || cursor.getCount() == 0) {
            return item;
        }

        try {
            if (cursor.moveToFirst()) {
                item = new NoteInfo();
                item.mId = cursor.getInt(LocalNoteItem.INDEX_ID);
                item.mTitle = cursor.getString(LocalNoteItem.INDEX_TITLE);
                item.mContent = cursor.getString(LocalNoteItem.INDEX_CONTENT);
                String labels = cursor.getString(LocalNoteItem.INDEX_LABEL);
                item.mLabel = convertLabel(labels);
                item.mDateCreatedInMs = cursor.getLong(LocalNoteItem.INDEX_DATE_CREATED);
                item.mDateModifiedInMs = cursor.getLong(LocalNoteItem.INDEX_DATE_MODIFIED);
                item.mDateReminderInMs = cursor.getLong(LocalNoteItem.INDEX_REMINDER);
                item.mEncyptHintState = cursor.getInt(LocalNoteItem.INDEX_ENCRYPT_HINT_STATE);
                item.mEncrytRemindReadState = cursor.getInt(LocalNoteItem.INDEX_ENCRYPT_REMIND_READ_STATE);
            }
        } finally {
            closeSilently(cursor);
        }
        return item;
    }

    public static ArrayList<Integer> convertLabel(String labels) {
        ArrayList<Integer> label = new ArrayList<>();
        label.clear();
        if (labels == null) {
            return label;
        }
        String[] temps = labels.split(",");
        for (String temp : temps) {
            label.add(Integer.parseInt(temp));
        }
        return label;
    }

    public static int[] getToady() {
        Calendar curCalendar = Calendar.getInstance();
        curCalendar.setTimeInMillis(System.currentTimeMillis());
        int year = curCalendar.get(Calendar.YEAR);
        int month = curCalendar.get(Calendar.MONTH);
        int day = curCalendar.get(Calendar.DAY_OF_MONTH);
        int[] today = new int[3];
        today[0] = year;
        today[1] = month;
        today[2] = day;
        return today;
    }

    public static boolean isSomeDay(int[] day1, int[] day2) {
        if (day1[2] != day2[2]) {
            return false;
        }
        if (day1[1] != day2[1]) {
            return false;
        }
        return day1[0] == day2[0];
    }

    public static void updateNoteData(String title, String jsonContent, ContentResolver resolver,
                                      int noteId, long modifiedTime, long reminderInMs,
                                      ArrayList<Integer> label, int encryptHintState, int encrytRemindReadState,
                                      boolean isIsEncrypt) {
        DES des = new DES();
        ContentValues values = new ContentValues();
        if (isIsEncrypt) {
            title = des.authcode(title, DES.OPERATION_ENCODE, DES.DES_KEY);
            jsonContent = des.authcode(jsonContent, DES.OPERATION_ENCODE, DES.DES_KEY);
        }
        values.put(NoteContract.NoteContent.COLUMN_TITLE, title);
        values.put(NoteContract.NoteContent.COLUMN_CONTENT, jsonContent);
        values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, modifiedTime);
        values.put(NoteContract.NoteContent.COLUMN_REMINDER, reminderInMs);
        values.put(NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE, encryptHintState);
        values.put(NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE, encrytRemindReadState);
        values.put(NoteContract.NoteContent.CLOUMN_ITEM_SOURCE, NoteItem.USER_DATE_SOURCE);
        String labels = NoteItem.convertToStringLabel(label);
        values.put(NoteContract.NoteContent.COLUMN_LABEL, labels);
        String selection = NoteContract.NoteContent._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(noteId)};
        resolver.update(getContentUri(isIsEncrypt), values, selection, selectionArgs);
        if (!isIsEncrypt) {
            WidgetUtil.updateWidget(title, jsonContent, noteId, modifiedTime, reminderInMs);
        }
    }

    public static int addNoteData(String title, String jsonContent, ContentResolver resolver,
                                  long modifiedTime, long reminderInMs, ArrayList<Integer> label,
                                  int encryptHintstate, int encrytRemindReadState, boolean isIsEncrypt) {
        try{
            ContentValues values = new ContentValues();
            values.put(NoteContract.NoteContent.COLUMN_TITLE, title);
            values.put(NoteContract.NoteContent.COLUMN_CONTENT, jsonContent);
            values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, System.currentTimeMillis());
            values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, modifiedTime);
            values.put(NoteContract.NoteContent.COLUMN_REMINDER, reminderInMs);
            String labels = NoteItem.convertToStringLabel(label);
            values.put(NoteContract.NoteContent.COLUMN_LABEL, labels);
            values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, modifiedTime);
            values.put(NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE, encryptHintstate);
            values.put(NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE, encrytRemindReadState);
            values.put(NoteContract.NoteContent.CLOUMN_ITEM_SOURCE, NoteItem.USER_DATE_SOURCE);
            Uri uri = resolver.insert(getContentUri(isIsEncrypt), values);
            int id = (int) ContentUris.parseId(uri);
            return id;
            //Chenyee wanghaiyan 2018-10-19 modify for CSW1703A-3906 begin
        } catch(Exception e){
            Log.d(TAG, "e = " + e);
            Toast.makeText(NoteAppImpl.getContext(), R.string.low_memory, Toast.LENGTH_SHORT).show();
        }
        return -1;
        //Chenyee wanghaiyan 2018-10-19 modify for CSW1703A-3906 end

    }

    public static boolean checkEnoughFreeMemory() {
        boolean isEnough = true;
        long recommenFreedMemory = getRecommendFreeMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = (int) Runtime.getRuntime().totalMemory();

        long actualFreeMemory = maxMemory - totalMemory;
        if (actualFreeMemory - ImageCache.getInstance().getMemCacheSize()
                <= recommenFreedMemory) {
            isEnough = false;
        }
        return isEnough;
    }

    public static long getRecommendFreeMemory() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long recommendFreeMemory = (long) (maxMemory * 0.2);
        return recommendFreeMemory;
    }

    public static Bitmap getAddBitmapFromUri(Context context, Uri uri) {
        Config.EditPage config = Config.EditPage.get(context);
        ThumbnailDecodeProcess decodeProcess = new ThumbnailDecodeProcess(context, uri, config.mImageWidth,
                config.mImageHeight, ThumbnailDecodeProcess.ThumbnailDecodeMode.WIDTH_FIXED_HEIGHT_SCALE,
                false);
        return decodeProcess.getThumbnail();
    }

    public static File getSaveBitmapFile(Context context) {
        Config.EditPage page = Config.EditPage.get(context);
        File fileDirectory = StorageUtils.getAvailableFileDirectory(context,
                page.mImageWidth * page.mImageHeight * 4,
                Constants.NOTE_MEDIA_THUMBNAIL_PATH);
        if (fileDirectory == null) {
            fileDirectory = Constants.NOTE_MEDIA_THUMBNAIL_PATH;
        }
        if (!fileDirectory.exists()) {
            boolean success = fileDirectory.mkdirs();
            if (!success) {
                return null;
            }
        }
        return NoteUtils.getSaveImageFile(fileDirectory);
    }

    public static boolean isContentEmpty(Editable editable) {
        String content = editable.toString().trim();
        if (TextUtils.isEmpty(content)) {
            return true;
        }
        content = content.replaceAll(Constants.MEDIA_BILL, "");
        String[] lineTexts = content.split(Constants.STR_NEW_LINE);
        for (String text : lineTexts) {
            if (!TextUtils.isEmpty(text)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmojiCharacter(char codePoint) {
        return !((codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) ||
                (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000)
                && (codePoint <= 0x10FFFF)));
    }

    public static Uri getContentUri(boolean isIsEncrypt) {
        return isIsEncrypt ? NoteContract.NoteContent.SECRET_CONTENT_URI :
                NoteContract.NoteContent.CONTENT_URI;
    }

    //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin
    public static boolean checkNeededPermissionForRecord(Activity activity) {
        if (AT_BEFORE_23) {
            return true;
        }

        ArrayList<String> permissionsNeeded = new ArrayList<String>();
        permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ArrayList<String> permissionsNeedRequest = new ArrayList<String>();
        for (String permission : permissionsNeeded) {
            if (activity.checkCallingOrSelfPermission(permission)
                    == PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            permissionsNeedRequest.add(permission);
        }

        if (permissionsNeedRequest.size() == 0) {
            return true;
        } else {
            String[] permissions = new String[permissionsNeedRequest.size()];
            permissions = permissionsNeedRequest.toArray(permissions);
            try{
                Method m = Activity.class.getMethod("requestPermissions", String[].class, int.class);
                m.invoke(activity, permissions, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }catch(Exception e){
            }
            return false;
        }
    }

    public static boolean checkExternalStoragePermission(Activity activity) {
        if (AT_BEFORE_23) {
            return true;
        }

        if (activity.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            try{
                Method m = Activity.class.getMethod("requestPermissions", String[].class, int.class);
                m.invoke(activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
            }catch(Exception e){
            }
            return false;
        }

        if (activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            try{
                Method m = Activity.class.getMethod("requestPermissions", String[].class, int.class);
                m.invoke(activity, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
            }catch(Exception e){
            }
            return false;
        }
        return true;
    }

    public static boolean isAllPermissionsGranted(String[] permissions, int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end

    public static int getDisplayMode() {
        int displayMode = NoteShareDataManager.getNoteDisplayMode(NoteAppImpl.getContext());
        if (displayMode == Constants.NOTE_DISPLAY_NONE) {
            displayMode = Constants.NOTE_DISPLAY_GRID_MODE;
        }
        return displayMode;
    }

    public static void finishAndRemoveAllTask(Context context) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).finishAndRemoveTask();
        }
    }

    //Gionee wanghaiyan 2015-11-12 add for 40950 begin
    public static void requestAudioFocus() {
        //Log.d( "requestAudioFocus");
        ((AudioManager) NoteAppImpl.getContext().getSystemService(Context.AUDIO_SERVICE))
                .requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    public static void abandonAudioFocus() {
        //Log.d("abandonAudioFocus");
        ((AudioManager) NoteAppImpl.getContext().getSystemService(Context.AUDIO_SERVICE))
                .abandonAudioFocus(null);
    }
    //Gionee wanghaiyan 2015-11-12 add for 40950 end

    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
    public static void updateNoteData(ContentResolver resolver,int noteId,String saveImagePath) {
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteContent.COLUMN_NOTE_SAVEDAS_IMAGE_PATH, saveImagePath);
        String selection = NoteContract.NoteContent._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(noteId)};
        resolver.update(NoteContract.NoteContent.CONTENT_URI, values, selection, selectionArgs);
    }

    public static String getNoteSavedImagePathColumn(Context context, Uri uri, String selection,
                                                     String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "noteSavedAsImagePath";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(column));
            }
        } finally {
            closeSilently(cursor);
        }
        return null;
    }
    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end

    //Chenyee wanghaiyan 2017-11-17 modify SW17W16A-2093 begin
    public static final String LANGUAGE_ZH = "zh";
    public static final String LANGUAGE_EN = "en";
    public static String formateTime(String date,Context context){
        boolean is24HourFormat=android.text.format.DateFormat.is24HourFormat(context);
        if(is24HourFormat){
            return date;
        }
        String[] dateArr=date.split(" ");
        //Chenyee wanghaiyan 2018-1-25 modify for CSW1705A-1249 begin
        String mTime=dateArr[0];
        if(dateArr.length==2){
            mTime=dateArr[1];
        }
        else if(dateArr.length==3){
            mTime=dateArr[2];
        }
        else if(dateArr.length==4){
            mTime=dateArr[3];
        }
        else if(dateArr.length==5){
            mTime=dateArr[4];
        }
        //Chenyee wanghaiyan 2018-1-25 modify for CSW1705A-1249 end
        //Chenyee wanghaiyan 2017-12-04 modify for SW17W16A-2219 begin
        Log.d(TAG,"mTime" + mTime);
        try {
            String[] timeArr = mTime.split(":");
            String currentLanguage = context.getResources().getConfiguration().locale.getLanguage();
            if (Integer.parseInt(timeArr[0]) > 12) {
                if (LANGUAGE_ZH.equals(currentLanguage)) {
                    mTime = context.getResources().getString(R.string.time_pm) + (Integer.parseInt(timeArr[0]) - 12) + ":" + (timeArr[1]);
                } else {
                    mTime = (Integer.parseInt(timeArr[0]) - 12) + ":" + (timeArr[1]) + context.getResources().getString(R.string.time_pm);
                }
            } else if (Integer.parseInt(timeArr[0]) == 12) {
                mTime = (Integer.parseInt(timeArr[0])) + ":" + (timeArr[1]) + context.getResources().getString(R.string.time_pm);
            } else {
                if (LANGUAGE_ZH.equals(currentLanguage)) {
                    mTime = context.getResources().getString(R.string.time_am) + (Integer.parseInt(timeArr[0])) + ":" + (timeArr[1]);
                } else {
                    mTime = (Integer.parseInt(timeArr[0])) + ":" + (timeArr[1]) + context.getResources().getString(R.string.time_am);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        //Chenyee wanghaiyan 2017-12-04 modify for SW17W16A-2219 end
        //Chenyee wanghaiyan 2018-1-25 modify for CSW1705A-1249 begin
        String newDate=mTime;

        if(dateArr.length==5){
            newDate=dateArr[0]+" "+dateArr[1]+" "+dateArr[2]+dateArr[3]+" "+mTime;
        }
        else  if(dateArr.length==4){
            newDate=dateArr[0]+" "+dateArr[1]+" "+dateArr[2]+" "+mTime;
        }
        else if(dateArr.length==3){
            newDate=dateArr[0]+" "+dateArr[1]+" "+mTime;
        }
        else if(dateArr.length==2){
            newDate=dateArr[0]+" "+mTime;
        }
        //Chenyee wanghaiyan 2018-1-25 modify for CSW1705A-1249 end
        return newDate;
    }
    //Chenyee wanghaiyan 2017-11-17 modify SW17W16A-2093 end
}
