package com.cydroid.note.encrypt;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.filecrypt.zyt.filesdk.FileCryptSDK;
import android.app.filecrypt.zyt.filesdk.FileCryptUtil;
import android.app.filecrypt.zyt.services.CryptWork;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import com.cydroid.note.common.Log;

import com.cydroid.note.R;
import com.cydroid.note.app.Config;
import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.span.PhotoImageSpan;
import com.cydroid.note.app.span.SoundImageSpan;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.DecodeUtils;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.ReflectionUtils;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.provider.NoteShareDataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by spc on 16-5-24.
 */
public class EncryptUtil {

    private static final String TAG = "EncryptUtil";
    public static final String ACTION_ENCRYPT_ENCRYPTION = "com.gionee.encryptspace.encryption";
    public static final String ACTION_ENTER_ENCRYPT_VERIFICATION = "com.gionee.encryptspace.verification";
    public static final String EXTRA_PENDINGINTENT = "extra_pendingintent";

    private static final String BASEINTENT_ENCRYPT_MAIN =
            "Intent { flg=0x10800000 cmp=com.cydroid.note/.encrypt.EncryptMainActivity }";
    private static final String BASEINTENT_SECURTTY_MAIN = "Intent { flg=0x10800000 cmp=com.cydroid.note/.encrypt.EncryptMainActivity (has extras) }";
    private static final String FROM_NOTIFICATION =
            "Intent { flg=0x10000000 cmp=com.cydroid.note/.encrypt.PasswordActivity (has extras) }";

    public static final int REQUEST_DIAL_SETTING_SUCCESS = 500;

    public static List<NoteItemAttachInfo> getAttachs(String noteContent) {
        List<NoteItemAttachInfo> attachs = new ArrayList<>();
        if (TextUtils.isEmpty(noteContent)) {
            return attachs;
        }
        try {
            JSONTokener jsonParser = new JSONTokener(noteContent);
            JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
            JSONArray spans = jsonObject.optJSONArray(DataConvert.JSON_SPANS_KEY);
            if (spans == null || spans.length() == 0) {
                return attachs;
            }
            int length = spans.length();
            String noteImageSpan = PhotoImageSpan.class.getName();
            String soundImageSpan = SoundImageSpan.class.getName();
            for (int i = 0; i < length; i++) {
                NoteItemAttachInfo attach = new NoteItemAttachInfo();
                JSONObject span = spans.getJSONObject(i);
                String type = span.getString(DataConvert.SPAN_ITEM_TYPE);
                if (type.equals(noteImageSpan)) {
                    String originUri = Uri.parse(span.getString(PhotoImageSpan.ORIGIN_URI)).getPath();
                    String thumbUri = Uri.parse(span.getString(PhotoImageSpan.THUMB_URI)).getPath();
                    attach.setOriginPicPath(originUri);
                    attach.setThumbPicPath(thumbUri);
                    attachs.add(attach);
                } else if (type.equals(soundImageSpan)) {
                    String originPath = Uri.parse(span.getString(SoundImageSpan.ORIGIN_PATH)).getPath();
                    attach.setSoundPath(originPath);
                    attachs.add(attach);
                }
            }
        } catch (JSONException e) {
        }
        return attachs;
    }

    public static int getAttachCount(List<NoteItem> items) {
        int attchcount = 0;
        if (items == null || items.size() == 0) {
            return attchcount;
        }
        try {
            for (NoteItem item : items) {
                String noteContent = item.content;
                JSONTokener jsonParser = new JSONTokener(noteContent);
                JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
                JSONArray spans = jsonObject.optJSONArray(DataConvert.JSON_SPANS_KEY);
                if (spans == null || spans.length() == 0) {
                    return attchcount;
                }
                int length = spans.length();
                String noteImageSpan = PhotoImageSpan.class.getName();
                String soundImageSpan = SoundImageSpan.class.getName();
                for (int i = 0; i < length; i++) {
                    JSONObject span = spans.getJSONObject(i);
                    String type = span.getString(DataConvert.SPAN_ITEM_TYPE);
                    if (type.equals(noteImageSpan) || type.equals(soundImageSpan)) {
                        attchcount++;
                    }
                }
            }
        } catch (JSONException e) {
        }
        return attchcount;
    }

    public static String getSecuritySpacePath(String filePath) {
        return Constants.SECURITY_OS_ENCRYPT_PATH + filePath;
    }

    private static void initAllFileLists(List<NoteItemAttachInfo> attachInfos, List<String> imageFiles,
                                         List<String> soundFiles, boolean isCrypted) {
        for (int i = 0, len = attachInfos.size(); i < len; i++) {
            NoteItemAttachInfo info = attachInfos.get(i);
            if (!TextUtils.isEmpty(info.getThumbPicPath())) {
                imageFiles.add(isCrypted ? getSecuritySpacePath(info.getThumbPicPath()) : info.getThumbPicPath());
            } else if (!TextUtils.isEmpty(info.getOriginPicPath())) {
                imageFiles.add(isCrypted ? getSecuritySpacePath(info.getOriginPicPath()) : info.getOriginPicPath());
            } else if (!TextUtils.isEmpty(info.getSoundPath())) {
                soundFiles.add(isCrypted ? getSecuritySpacePath(info.getSoundPath()) : info.getSoundPath());
            }
        }
    }

    public static void encryptAttachFileForSecurityOS(List<NoteItemAttachInfo> attachInfos,
                                                      NoteActionProgressListener listener) {
        if (null == attachInfos || attachInfos.size() == 0) {
            return;
        }
        List<String> imageFiles = new ArrayList<>();
        List<String> soundFiles = new ArrayList<>();
        initAllFileLists(attachInfos, imageFiles, soundFiles, false);
        boolean isImage = true;
        for (int i = 0, len = attachInfos.size(); i < len; i++) {
            NoteItemAttachInfo info = attachInfos.get(i);
            String srcPath = "";
            String dstPath = "";
            if (!TextUtils.isEmpty(info.getThumbPicPath())) {
                srcPath = info.getThumbPicPath();
                dstPath = getSecuritySpacePath(srcPath);
                isImage = true;

            } else if (!TextUtils.isEmpty(info.getThumbPicPath())
                    && !info.getThumbPicPath().equals(info.getOriginPicPath())) {
                srcPath = info.getOriginPicPath();
                dstPath = getSecuritySpacePath(srcPath);
                isImage = true;

            } else if (!TextUtils.isEmpty(info.getSoundPath())) {
                srcPath = info.getSoundPath();
                dstPath = getSecuritySpacePath(srcPath);
                isImage = false;
            }
            if (!TextUtils.isEmpty(srcPath)) {
                CryptWork cryptWork = new CryptWork(srcPath, dstPath, true);
                FileCryptUtil.cryptFile(cryptWork, new ZYTProgressListener(srcPath, false, null));
                if (null != listener) {
                    listener.onOneComplete();
                }
                saveUnCompleteEncryptFiles(srcPath, imageFiles, soundFiles, isImage);
            }
        }
    }

    private static void saveUnCompleteEncryptFiles(String srcPath, List<String> imageFiles,
                                                   List<String> soundFiles, boolean isImage) {
        if (isImage) {
            imageFiles.remove(srcPath);
        } else {
            soundFiles.remove(srcPath);
        }
        if (imageFiles.size() >= 0) {
            String imagePaths = imageFiles.toString().replace("[", "").replace("]", "");
            NoteShareDataManager.setEncryptNotCompleteImages(NoteAppImpl.getContext(), imagePaths);
        }
        if (soundFiles.size() >= 0) {
            String soundPaths = soundFiles.toString().replace("[", "").replace("]", "");
            NoteShareDataManager.setEncryptNotCompletedSounds(NoteAppImpl.getContext(), soundPaths);
        }
    }

    public static void encryptFileForSecurityOS(String srcPath) {
        String dstPath = getSecuritySpacePath(srcPath);
        CryptWork cryptWork = new CryptWork(srcPath, dstPath, true);
        FileCryptSDK.setProgressCallback(cryptWork, new ZYTProgressListener(srcPath, true, null));
        FileCryptUtil.cryptFile(cryptWork, new ZYTProgressListener(srcPath, false, null));
    }

    public static void decrpyAttachFileForSecurityOS(List<NoteItemAttachInfo> attachInfos,
                                                     NoteActionProgressListener listener) {
        if (null == attachInfos || 0 == attachInfos.size()) {
            return;
        }
        List<String> imageFiles = new ArrayList<>();
        List<String> soundFiles = new ArrayList<>();
        initAllFileLists(attachInfos, imageFiles, soundFiles, true);
        boolean isImage = true;
        for (int i = 0, len = attachInfos.size(); i < len; i++) {
            NoteItemAttachInfo info = attachInfos.get(i);
            String srcPath = "";
            String dstPath = "";
            if (!TextUtils.isEmpty(info.getThumbPicPath())) {
                srcPath = getSecuritySpacePath(info.getThumbPicPath());
                dstPath = info.getThumbPicPath();
                isImage = true;

            } else if (!TextUtils.isEmpty(info.getThumbPicPath())
                    && !info.getThumbPicPath().equals(info.getOriginPicPath())) {
                srcPath = getSecuritySpacePath(info.getOriginPicPath());
                dstPath = info.getOriginPicPath();
                isImage = true;

            } else if (!TextUtils.isEmpty(info.getSoundPath())) {
                srcPath = getSecuritySpacePath(info.getSoundPath());
                dstPath = info.getSoundPath();
                isImage = false;
            }

            if (!TextUtils.isEmpty(srcPath)) {
                CryptWork cryptWork = new CryptWork(srcPath, dstPath, false);
                FileCryptUtil.cryptFile(cryptWork, new ZYTProgressListener(srcPath, false, null));
                if (null != listener) {
                    listener.onOneComplete();
                }
                saveUnCompleteDecryptFiles(srcPath, imageFiles, soundFiles, isImage);
            }
        }
    }

    private static void saveUnCompleteDecryptFiles(String srcPath, List<String> imageFiles,
                                                   List<String> soundFiles, boolean isImage) {
        if (isImage) {
            imageFiles.remove(srcPath);
        } else {
            soundFiles.remove(srcPath);
        }
        if (imageFiles.size() >= 0) {
            String imagePaths = imageFiles.toString().replace("[", "").replace("]", "");
            NoteShareDataManager.setDecryptNotCompleteImages(NoteAppImpl.getContext(), imagePaths);
        }
        if (soundFiles.size() >= 0) {
            String soundPaths = soundFiles.toString().replace("[", "").replace("]", "");
            NoteShareDataManager.setDecryptNotCompleteSounds(NoteAppImpl.getContext(), soundPaths);
        }
    }

    public static void encryptAttachFile(List<NoteItemAttachInfo> attachInfos, NoteActionProgressListener listener) {
        if (null == attachInfos || attachInfos.size() == 0) {
            return;
        }
        List<String> imageFiles = new ArrayList<>();
        List<String> soundFiles = new ArrayList<>();
        initAllFileLists(attachInfos, imageFiles, soundFiles, false);
        boolean isImage = true;
        for (int i = 0, len = attachInfos.size(); i < len; i++) {
            String srcPath = "";
            NoteItemAttachInfo info = attachInfos.get(i);
            if (!TextUtils.isEmpty(info.getThumbPicPath())) {
                isImage = true;
                srcPath = info.getThumbPicPath();
                encryptImageFile(srcPath);
            }
            if (!TextUtils.isEmpty(info.getThumbPicPath())
                    && !info.getThumbPicPath().equals(info.getOriginPicPath())) {
                isImage = true;
                srcPath = info.getOriginPicPath();
                encryptImageFile(srcPath);
            }
            if (!TextUtils.isEmpty(info.getSoundPath())) {
                isImage = false;
                srcPath = info.getSoundPath();
                encryptSoundFile(srcPath);
            }

            saveUnCompleteEncryptFiles(srcPath, imageFiles, soundFiles, isImage);

            if (null != listener) {
                listener.onOneComplete();
            }
        }
    }

    public static void decryptAttachFile(List<NoteItemAttachInfo> attachInfos, NoteActionProgressListener listener) {
        if (null == attachInfos || attachInfos.size() == 0) {
            return;
        }
        List<String> imageFiles = new ArrayList<>();
        List<String> soundFiles = new ArrayList<>();
        initAllFileLists(attachInfos, imageFiles, soundFiles, false);
        boolean isImage = true;
        String srcPath = "";
        for (int i = 0, len = attachInfos.size(); i < len; i++) {
            NoteItemAttachInfo info = attachInfos.get(i);
            if (!TextUtils.isEmpty(info.getThumbPicPath())) {
                isImage = true;
                srcPath = info.getThumbPicPath();
                decryptImageFile(srcPath);
            }
            if (!TextUtils.isEmpty(info.getThumbPicPath())
                    && !info.getThumbPicPath().equals(info.getOriginPicPath())) {
                isImage = true;
                srcPath = info.getOriginPicPath();
                decryptImageFile(srcPath);
            }
            if (!TextUtils.isEmpty(info.getSoundPath())) {
                isImage = false;
                srcPath = info.getSoundPath();
                decryptSoundFile(srcPath);
            }
            if (null != listener) {
                listener.onOneComplete();
            }
            saveUnCompleteDecryptFiles(srcPath, imageFiles, soundFiles, isImage);
        }
    }

    public static void encryptImageFile(String filePath) {
        FileConfuseSession session = FileConfuseSession.open();
        session.confuse(filePath);
    }

    public static void decryptImageFile(String filePath) {
        InputStream inputStream = null;
        File tmpFile = new File(filePath + ".tmp");
        if (tmpFile.exists()) {
            tmpFile = new File(filePath + System.currentTimeMillis() + ".tmp");
        }
        try {
            FileConfuseSession session = FileConfuseSession.open();
            inputStream = session.backupConfuse(filePath);
            Bitmap bitmap = DecodeUtils.decodeBitmap(inputStream);
            if (null == bitmap) {
                return;
            }
            boolean saveSucess = NoteUtils.saveBitmap(bitmap, tmpFile);
            bitmap.recycle();
            if (saveSucess) {
                File srcFile = new File(filePath);
                if (srcFile.exists()) {
                    srcFile.delete();//NOSONAR
                    tmpFile.renameTo(srcFile);//NOSONAR
                }
            }
        } catch (Exception e) {
            tmpFile.delete();//NOSONAR
        } finally {
            NoteUtils.closeSilently(inputStream);
        }
    }

    public static void encryptSoundFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        String fileName = getFileName(filePath);
        if (!new File(Constants.NOTE_MEDIA_PATH, "sound").exists()) {
            Constants.NOTE_MEDIA_SOUND_PATH.mkdir();
        }
        File sourceFile = new File(filePath);
        File targetFile = new File(Constants.SOUND_ENCRYPT_PATH + File.separator + fileName);
        copyFile(sourceFile, targetFile);

    }

    public static void decryptSoundFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        String fileName = getFileName(filePath);
        File sourceFile = new File(Constants.SOUND_ENCRYPT_PATH + File.separator + fileName);
        if (!new File(Constants.NOTE_MEDIA_PATH, "sound").exists()) {
            Constants.NOTE_MEDIA_SOUND_PATH.mkdir();
        }
        File targetFile = new File(filePath);
        copyFile(sourceFile, targetFile);
    }

    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        String[] array = filePath.split("/");
        int len = array.length;
        if (len >= 1) {
            return array[len - 1];
        }
        return null;
    }

    private static void copyFile(File sourceFile, File targetFile) {
        FileInputStream input = null;
        BufferedInputStream inBuff = null;
        FileOutputStream output = null;
        BufferedOutputStream outBuff = null;
        try {
            input = new FileInputStream(sourceFile);
            inBuff = new BufferedInputStream(input);

            output = new FileOutputStream(targetFile);
            outBuff = new BufferedOutputStream(output);

            byte[] b = new byte[1024];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            outBuff.flush();
            sourceFile.delete();//NOSONAR
        } catch (IOException e) {
            if (targetFile.exists()) {
                targetFile.delete();//NOSONAR
            }
        } finally {
            NoteUtils.closeSilently(input);
            NoteUtils.closeSilently(inBuff);
            NoteUtils.closeSilently(output);
            NoteUtils.closeSilently(outBuff);
        }
    }

    public static boolean isDialcodeOpen(ContentResolver contentResolver) {
        String ENCRYPT_DIALCODE_FLAG = "encrypt_dialcode_exist";
        int exist = 0;
        Class<?> encryptions = null;
        try {
            encryptions = Class.forName("android.provider.Encryptions$Secure");
            Method getInt = ReflectionUtils.findMethod(encryptions, "getInt", ContentResolver.class, String.class);
            exist = (int) ReflectionUtils.invokeMethod(getInt, null, contentResolver, ENCRYPT_DIALCODE_FLAG);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "ClassNotFoundException error", e);
        } catch (Exception e) {
            Log.w(TAG, "Exception error", e);
        }
        return exist == 1;
    }

    public static boolean isGestureOpen(ContentResolver contentResolver) {
        String ENCRYPT_GESTURE_FLAG = "encrypt_gesture_exist";
        int exist = 0;
        Class<?> encryptions = null;
        try {
            encryptions = Class.forName("android.provider.Encryptions$Secure");
            Method getInt = ReflectionUtils.findMethod(encryptions, "getInt", ContentResolver.class, String.class);
            exist = (int) ReflectionUtils.invokeMethod(getInt, null, contentResolver, ENCRYPT_GESTURE_FLAG);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "ClassNotFoundException error", e);
        } catch (Exception e) {
            Log.w(TAG, "Exception error", e);
        }
        if (exist == 1) {
            Log.d(TAG, "gesture is open");
            return true;
        } else {
            Log.d(TAG, "gesture is close");
            return false;
        }
    }

    public static boolean startDialSettingInterface(Activity activity) {
        try {
            Intent intent = new Intent(ACTION_ENCRYPT_ENCRYPTION);
            activity.startActivityForResult(intent, REQUEST_DIAL_SETTING_SUCCESS);
            return true;
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "error", e);
        }
        return false;
    }

    public static void recoveryEncryptUnCompleteNote(final Context context) {
        final String encryptUnCompleteImages = NoteShareDataManager.getEncryptNotNotCompleteImages(context);
        final String decryptUnCompleteImges = NoteShareDataManager.getDecryptNotNotCompleteImages(context);
        final String encryptUnCompleteSounds = NoteShareDataManager.getEncryptNotNotCompleteSounds(context);
        final String decryptUnCompleteSounds = NoteShareDataManager.getDecryptNotNotCompleteSounds(context);
        recoveryUnCompleteImages(encryptUnCompleteImages, decryptUnCompleteImges);
        recoveryUnCompleteSounds(encryptUnCompleteSounds, decryptUnCompleteSounds);
    }

    private static void recoveryUnCompleteImages(String encryptUnCompleteImages, String decryptUnCompleteImges) {
        if (PlatformUtil.isSecurityOS()) {
            recoveryImagesForSecurityOS(encryptUnCompleteImages, decryptUnCompleteImges);
        } else {
            recoveryImages(encryptUnCompleteImages, decryptUnCompleteImges);
        }
    }

    private static void recoveryImagesForSecurityOS(String encryptUnCompleteImages, String decryptUnCompleteImges) {
        if (!TextUtils.isEmpty(encryptUnCompleteImages)) {
            String[] paths = getPathArray(encryptUnCompleteImages);
            for (int i = 0; i < paths.length; i++) {
                String srcPath = paths[i].replace(" ", "");
                String dstPath = getSecuritySpacePath(srcPath);
                CryptWork cryptWork = new CryptWork(srcPath, dstPath, true);
                FileCryptUtil.cryptFile(cryptWork, new ZYTProgressListener(srcPath, false, null));
            }
            NoteShareDataManager.setEncryptNotCompleteImages(NoteAppImpl.getContext(), "");
        }

        if (!TextUtils.isEmpty(decryptUnCompleteImges)) {
            String[] paths = getPathArray(decryptUnCompleteImges);
            for (int i = 0; i < paths.length; i++) {
                String srcPath = paths[i].replace(" ", "");
                String dstPath = srcPath.replace(Constants.SECURITY_OS_ENCRYPT_PATH, "");
                CryptWork cryptWork = new CryptWork(srcPath, dstPath, false);
                FileCryptUtil.cryptFile(cryptWork, new ZYTProgressListener(srcPath, false, null));
            }
            NoteShareDataManager.setDecryptNotCompleteImages(NoteAppImpl.getContext(), "");
        }
    }

    private static void recoveryImages(String encryptUnCompleteImages, String decryptUnCompleteImges) {
        if (!TextUtils.isEmpty(encryptUnCompleteImages)) {
            String[] paths = getPathArray(encryptUnCompleteImages);
            for (int i = 0; i < paths.length; i++) {
                encryptImageFile(paths[i].replace(" ", ""));
            }
            NoteShareDataManager.setEncryptNotCompleteImages(NoteAppImpl.getContext(), "");
        }

        if (!TextUtils.isEmpty(decryptUnCompleteImges)) {
            String[] paths = getPathArray(decryptUnCompleteImges);
            for (int i = 0; i < paths.length; i++) {
                decryptImageFile(paths[i].replace(" ", ""));
            }
            NoteShareDataManager.setDecryptNotCompleteImages(NoteAppImpl.getContext(), "");
        }
    }

    private static String[] getPathArray(String paths) {
        String[] pathArrays = paths.split(",");
        return pathArrays;
    }

    private static void recoveryUnCompleteSounds(String encryptUnCompleteSounds, String decryptUnCompleteSounds) {
        if (PlatformUtil.isSecurityOS()) {
            recoverySoundsForSecurityOS(encryptUnCompleteSounds, decryptUnCompleteSounds);
        } else {
            recoverySounds(encryptUnCompleteSounds, decryptUnCompleteSounds);
        }
    }

    private static void recoverySoundsForSecurityOS(String encryptUnCompleteSounds, String decryptUnCompleteSounds) {
        if (!TextUtils.isEmpty(encryptUnCompleteSounds)) {
            String[] paths = getPathArray(encryptUnCompleteSounds);
            for (int i = 0; i < paths.length; i++) {
                String srcPath = paths[i].replace(" ", "");
                String dstPath = getSecuritySpacePath(srcPath);
                CryptWork cryptWork = new CryptWork(srcPath, dstPath, true);
                FileCryptUtil.cryptFile(cryptWork, new ZYTProgressListener(srcPath, false, null));
            }
            NoteShareDataManager.setEncryptNotCompletedSounds(NoteAppImpl.getContext(), "");
        }

        if (!TextUtils.isEmpty(decryptUnCompleteSounds)) {
            String[] paths = getPathArray(decryptUnCompleteSounds);
            for (int i = 0; i < paths.length; i++) {
                String srcPath = paths[i].replace(" ", "");
                String dstPath = srcPath.replace(Constants.SECURITY_OS_ENCRYPT_PATH, "");
                CryptWork cryptWork = new CryptWork(srcPath, dstPath, false);
                FileCryptUtil.cryptFile(cryptWork, new ZYTProgressListener(srcPath, false, null));
            }
            NoteShareDataManager.setDecryptNotCompleteSounds(NoteAppImpl.getContext(), "");
        }
    }

    private static void recoverySounds(String encryptUnCompleteSounds, String decryptUnCompleteSounds) {
        if (!TextUtils.isEmpty(encryptUnCompleteSounds)) {
            String[] paths = getPathArray(encryptUnCompleteSounds);
            for (int i = 0; i < paths.length; i++) {
                encryptSoundFile(paths[i].replace(" ", ""));
            }
            NoteShareDataManager.setEncryptNotCompletedSounds(NoteAppImpl.getContext(), "");
        }

        if (!TextUtils.isEmpty(decryptUnCompleteSounds)) {
            String[] paths = getPathArray(decryptUnCompleteSounds);
            for (int i = 0; i < paths.length; i++) {
                decryptSoundFile(paths[i].replace(" ", ""));
            }
            NoteShareDataManager.setDecryptNotCompleteSounds(NoteAppImpl.getContext(), "");
        }
    }

    public static void removeCurrentTask(Context context) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
        for (int i = 0; i < tasks.size(); i++) {
            ActivityManager.RecentTaskInfo recent = tasks.get(i).getTaskInfo();
            String baseIntent = recent.baseIntent.toString();
            if (BASEINTENT_ENCRYPT_MAIN.equals(baseIntent)
                    || FROM_NOTIFICATION.equals(baseIntent)
                    || BASEINTENT_SECURTTY_MAIN.equals(baseIntent)) {
                tasks.get(i).finishAndRemoveTask();
                break;
            }
        }
    }

    public static void checkPhotoSpanHasSize(String jsonConent) {
        try {
            JSONTokener jsonParser = new JSONTokener(jsonConent);
            JSONObject jsonObject = (JSONObject) jsonParser.nextValue();

            JSONArray spans = jsonObject.optJSONArray(DataConvert.JSON_SPANS_KEY);
            if (spans == null) {
                return;
            }
            int length = spans.length();
            for (int i = 0; i < length; i++) {
                JSONObject json = spans.getJSONObject(i);
                if ("com.cydroid.note.app.span.PhotoImageSpan"
                        .equals(json.getString(DataConvert.SPAN_ITEM_TYPE))) {
                    if (!json.has(PhotoImageSpan.PIC_WIDTH)) {
                        String thumbUri = json.getString(PhotoImageSpan.THUMB_URI);
                        String originUri = json.getString(PhotoImageSpan.ORIGIN_URI);
                        int[] originSize = DecodeUtils.loadBitmapSize(NoteAppImpl.getContext(),
                                Uri.parse(originUri), false);
                        if (isInValidSize(originSize)) {
                            int[] thumbSize = DecodeUtils.loadBitmapSize(NoteAppImpl.getContext(),
                                    Uri.parse(thumbUri), false);
                            setScalePhotoSize(json, thumbSize[0], thumbSize[1], Uri.parse(originUri));
                        } else {
                            setScalePhotoSize(json, originSize[0], originSize[1], Uri.parse(originUri));
                        }
                    }
                }
            }
        } catch (JSONException e) {
        }
    }

    public static void setScalePhotoSize(JSONObject json, int picWidth, int picHeight, Uri originUri) {
        Config.EditPage page = Config.EditPage.get(NoteAppImpl.getContext());
        int imageWidth = page.mImageWidth;
        int imageHeight = 0;
        int rotate = DecodeUtils.decodeImageRotate(originUri);
        float origWidth = picWidth;
        float origHeight = picHeight;
        float scale = 0;
        if (rotate == 90 || rotate == 270) {
            scale = origHeight / imageWidth;
            imageHeight = (int) ((1 / scale) * origWidth);
        } else {
            scale = origWidth / imageWidth;
            imageHeight = (int) ((1 / scale) * origHeight);
        }
        try {
            json.put(PhotoImageSpan.PIC_WIDTH, imageWidth);
            json.put(PhotoImageSpan.PIC_HEIGHT, imageHeight);
        } catch (JSONException e) {
        }
    }

    public static boolean isInValidSize(int[] size) {
        return size == null || size.length == 0 || size[0] == 0;
    }

    public static String getHint(boolean encrypt, int successNum, int failNum) {
        Resources resources = NoteAppImpl.getContext().getResources();
        if (successNum == 0) {
            return encrypt ? resources.getString(R.string.encrypt_fail) :
                    resources.getString(R.string.decrypt_fail);
        }
        String hint = "";
        if (failNum == 0) {
            if (PlatformUtil.isSecurityOS()) {
                hint = encrypt ? resources.getString(R.string.encrypt_finish_hint_for_security_os) :
                        resources.getString(R.string.decrypt_finish_hint);
            } else {
                hint = encrypt ? resources.getString(R.string.encrypt_success) :
                        resources.getString(R.string.decrypt_finish_hint);
            }
        } else {
            hint = encrypt ? resources.getString(R.string.partial_encrypt, successNum, failNum) :
                    resources.getString(R.string.partial_decrypt, successNum, failNum);
        }
        return hint;
    }

}
