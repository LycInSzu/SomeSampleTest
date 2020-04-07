package com.cydroid.note.common;

import android.app.filecrypt.zyt.filesdk.FileCryptUtil;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import com.gionee.framework.log.Logger;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.FileConfuseSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class DecodeUtils {
    private static final String TAG = "DecodeUtils";

    public static Bitmap decodeRawBitmap(Context context, String rawFileName) {
        InputStream is = null;
        try {
            is = context.getAssets().open(rawFileName);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            return bitmap;
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeRawBitmap rawFileName fail" + e);
        } finally {
            NoteUtils.closeSilently(is);
        }
        return null;
    }

    public static Bitmap decodeSystemBitmap(String fileName) {
        File file = new File(Constants.SYSTEM_ETC_DIR + fileName);
        if (!file.exists()) {
            return null;
        }
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            return bitmap;
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeRawBitmap rawFileName fail" + e);
        } finally {
            NoteUtils.closeSilently(is);
        }
        return null;
    }

    public static Bitmap decodeThumbnail(Context context, Uri uri, int targetW, int targetH,
                                         int rotation, boolean isCropped, boolean isEncrypt) {
        boolean decodeFromSecuritySpace = PlatformUtil.isSecurityOS() && isEncrypt;
        int[] size = loadBitmapSize(context, uri, isEncrypt);
        if (size == null) {
            return null;
        }
        InputStream is = null;
        try {
            if (decodeFromSecuritySpace) {
                is = FileCryptUtil.getDecryptInputStream(EncryptUtil.getSecuritySpacePath(uri.getPath()));
            } else if (isEncrypt) {
                is = FileConfuseSession.open().backupConfuse(uri.getPath());
            } else {
                is = context.getContentResolver().openInputStream(uri);
            }
            Bitmap bitmap = decodeThumbnail(is, size[0], size[1], targetW, targetH, rotation, isCropped);
            return bitmap;
        } catch (Throwable e) {
            Logger.printLog(TAG, e.toString());
        } finally {
            NoteUtils.closeSilently(is);
        }
        return null;
    }

    public static int decodeImageRotate(Uri uri) {
        String scheme = uri.getScheme();
        if (!ContentResolver.SCHEME_FILE.equals(scheme)) {
            return 0;
        }
        return getExifOrientation(uri.getPath());
    }

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            Logger.printLog(TAG, "cannot read exif" + ex);
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        break;
                }
            }
        }
        return degree;
    }

    public static Bitmap decodeThumbnail(InputStream is, int originW, int originH,
                                         int targetW, int targetH, int rotation, boolean isCropped) {
        float scale;
        if (rotation == 90 || rotation == 270) {
            scale = Math.max((float) targetW / originH, (float) targetH / originW);
        } else {
            scale = Math.max((float) targetW / originW, (float) targetH / originH);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        options.inMutable = true;
        Bitmap result = BitmapFactory.decodeStream(is, null, options);
        if (result == null) {
            return null;
        }
        if (rotation != 0) {
            result = rotate(result, rotation);
        }
        return BitmapUtils.resizeAndCropCenter(result, targetW, targetH, true, isCropped);
    }

    public static Bitmap rotate(Bitmap bitmap, int rotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap rotationBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotationBitmap;
    }

    public static int[] loadBitmapSize(Context context, Uri uri, boolean isEncrypt) {
        InputStream is = null;
        try {
            boolean decodeFromSecuritySpace = PlatformUtil.isSecurityOS() && isEncrypt;
            if (decodeFromSecuritySpace) {
                is = FileCryptUtil.getDecryptInputStream(EncryptUtil.getSecuritySpacePath(uri.getPath()));
            } else if (isEncrypt) {
                is = FileConfuseSession.open().backupConfuse(uri.getPath());
            } else {
                is = context.getContentResolver().openInputStream(uri);
            }
            if (is == null) {
                return null;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            int[] size = new int[2];
            size[0] = options.outWidth;
            size[1] = options.outHeight;
            return size;
        } catch (FileNotFoundException e) {
            Logger.printLog(TAG, "loadBitmapSize error:" + e);
        } finally {
            NoteUtils.closeSilently(is);
        }
        return null;
    }

    public static Bitmap decodeBitmap(String filePath, boolean isEncrypted) {
        InputStream is = null;
        try {
            boolean decodeFromSecuritySpace = PlatformUtil.isSecurityOS() && isEncrypted;
            if (decodeFromSecuritySpace) {
                is = FileCryptUtil.getDecryptInputStream(EncryptUtil.getSecuritySpacePath(filePath));
            } else if (isEncrypted) {
                is = FileConfuseSession.open().backupConfuse(filePath);
            } else {
                is = new FileInputStream(filePath);
            }
            return decodeBitmap(is);
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeBitmap filePath error: " + e);
            return null;
        } finally {
            NoteUtils.closeSilently(is);
        }

    }

    public static Bitmap decodeBitmap(Context context, Uri uri) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (is != null) {
                return decodeBitmap(is);
            }

        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeBitmap error:" + e);
        } finally {
            NoteUtils.closeSilently(is);
        }
        return null;
    }

    public static Bitmap decodeBitmap(InputStream is) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeBitmap error111 :" + e);
            return null;
        }
    }

}
