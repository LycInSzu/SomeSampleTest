//Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 begin
package com.cydroid.note.common;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.UriPermission;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.MediaStore;
import com.cydroid.note.common.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import com.cydroid.note.app.NoteAppImpl;


/**
 * @author MTK81255
 *
 */
public class ExternalStorageFileManager {
    private static final String TAG = "SoundRecorder_ExternalStorageFileManager";
    private static boolean DEBUG = true;
    private static boolean usenew = true;
    private static final String PATH_TREE = "tree";

    public static final String AUTHORITY_EXTERNAL_STORAGE = "com.android.externalstorage.documents";
    public static final String FILE_PATH_INDEX = "storage/";
    public static ContentResolver sContentResolver = NoteAppImpl.getContext().getContentResolver();

    /**
     * @param file
     *            File
     * @return boolean
     */
    public static boolean delete(File file) {
        Log.i(TAG, "deleteFile(), file.getAbsolutePath() = " + file.getAbsolutePath());
        Uri uri = getUri(file);
        if (uri == null) {
            return false;
        }
        Log.i(TAG, "Uri: " + uri.toString());
        try {
            return DocumentsContract.deleteDocument(sContentResolver, uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        return false;
    }

    /**
     * @param file
     *            File
     * @return Uri
     *//*
    public static Uri getUri(File file) {
        // source : /storage/3558-110E/mtklog/netlog/NTLog_2018_0620_142338
        String filePath = file.getAbsolutePath();
        Log.d(TAG, "getUri filepath = " + filePath);
        int externalStroageIndex = filePath.indexOf(FILE_PATH_INDEX);
        if (externalStroageIndex == -1) {
            return null;
        }
        String documentId = filePath.substring(externalStroageIndex + FILE_PATH_INDEX.length());
        String rootId = filePath.substring(externalStroageIndex + FILE_PATH_INDEX.length());
        if (rootId.contains("/")){
            rootId = rootId.substring(0, rootId.indexOf("/"));
        }else {
            documentId = documentId + "/";
        }
        rootId = rootId + "/";
        rootId = rootId.replaceFirst("/", ":");
        documentId = documentId.replaceFirst("/", ":");
        Log.d(TAG, "getUri rootId  = " + rootId + ",   documentId = " + documentId);
        //return DocumentsContract.buildDocumentUriUsingTree(Uri.parse("content://" + AUTHORITY_EXTERNAL_STORAGE + "/tree/" + rootId), documentId);
        Uri docUri = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY_EXTERNAL_STORAGE).appendPath(PATH_TREE)
                .appendPath(rootId).appendPath(PATH_DOCUMENT)
                .appendPath(documentId).build();
        return DocumentsContract.buildDocumentUriUsingTree(docUri, documentId);
    }*/

    /**
     * @param file
     *            File
     * @return Uri
     */
    /*public static Uri getUri1(File file) {
        // source : /storage/3558-110E/mtklog/netlog/NTLog_2018_0620_142338
        if (EXTERNAL_STORAGE_URI == null){
            return  null;
        }
        String filePath = file.getAbsolutePath();
        int externalStroageIndex = filePath.indexOf(FILE_PATH_INDEX);
        if (externalStroageIndex == -1) {
            return null;
        }
        String documentId = filePath.substring(externalStroageIndex + FILE_PATH_INDEX.length());
        if (!documentId.contains("/")){
            documentId = documentId + "/";
        }
        documentId = documentId.replaceFirst("/", ":");
        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(EXTERNAL_STORAGE_URI, DocumentsContract.getTreeDocumentId(EXTERNAL_STORAGE_URI));
        return DocumentsContract.buildDocumentUriUsingTree(docUri, documentId);
    }*/

    public static Uri buildRootUri(File file){
        String filePath = file.getAbsolutePath();
        Log.d(TAG, "getUri filepath = " + filePath);
        int externalStroageIndex = filePath.indexOf(FILE_PATH_INDEX);
        if (externalStroageIndex == -1) {
            return null;
        }
        String documentId = filePath.substring(externalStroageIndex + FILE_PATH_INDEX.length());
        String rootId = filePath.substring(externalStroageIndex + FILE_PATH_INDEX.length());
        if (rootId.contains("/")){
            rootId = rootId.substring(0, rootId.indexOf("/"));
        }else {
            documentId = documentId + "/";
        }
        rootId = rootId + "/";
        rootId = rootId.replaceFirst("/", ":");
        documentId = documentId.replaceFirst("/", ":");
        Log.d(TAG, "getUri rootId  = " + rootId + ",   documentId = " + documentId);

        Uri docUri = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY_EXTERNAL_STORAGE).appendPath(PATH_TREE)
                .appendPath(rootId).build();
        return docUri;
    }

    /**
     * @param file
     *            File
     * @return Uri
     */
    public static Uri getUri(File file) {
        // source : /storage/3558-110E/mtklog/netlog/NTLog_2018_0620_142338
        String filePath = file.getAbsolutePath();
        Log.d(TAG, "getUri filepath = " + filePath);
        int externalStroageIndex = filePath.indexOf(FILE_PATH_INDEX);
        if (externalStroageIndex == -1) {
            return null;
        }
        String documentId = filePath.substring(externalStroageIndex + FILE_PATH_INDEX.length());
        String rootId = filePath.substring(externalStroageIndex + FILE_PATH_INDEX.length());
        if (rootId.contains("/")){
            rootId = rootId.substring(0, rootId.indexOf("/"));
        }else {
            documentId = documentId + "/";
        }
        rootId = rootId + "/";
        rootId = rootId.replaceFirst("/", ":");
        documentId = documentId.replaceFirst("/", ":");
        Log.d(TAG, "getUri rootId  = " + rootId + ",   documentId = " + documentId);

        Uri docUri = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY_EXTERNAL_STORAGE).appendPath(PATH_TREE)
                .appendPath(rootId).build();
        Log.d(TAG, "docUri = " + docUri);
        Uri childUri = DocumentsContract.buildDocumentUriUsingTree(docUri, documentId);
        Log.d(TAG, "childUri = " + childUri);
        return childUri;
    }

    /**
     * @param file
     *            File
     * @return boolean
     */
    public static boolean createNewFile(File file) {
        Uri parentDocumentUri = getUri(file.getParentFile());
        if (parentDocumentUri == null){
            return false;
        }
        Log.i(TAG, "createNewFile(), parentDocumentUri: " + parentDocumentUri.toString()
                + ", file.getName() = " + file.getName());
        try {
            Uri fileUri = DocumentsContract
                    .createDocument(sContentResolver, parentDocumentUri, "", file.getName());
            return fileUri != null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param file
     *            File
     * @return boolean
     */
    public static boolean mkdir(File file) {
        Uri parentDocumentUri = getUri(file.getParentFile());
        if (parentDocumentUri == null){
            return false;
        }
        Log.i(TAG, "mkdir(), parentDocumentUri: " + parentDocumentUri.toString()
                + ", file.getName() = " + file.getName());
        try {
            Uri fileUri = DocumentsContract.createDocument(sContentResolver, parentDocumentUri,
                    Document.MIME_TYPE_DIR, file.getName());
            return fileUri != null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param file
     *            File
     * @return boolean
     */
    public static boolean mkdirs(File file) {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            mkdirs(parentFile);
        }
        return mkdir(file);
    }

    /**
     * @param sourceFile
     *            File
     * @param targetFile
     *            File
     * @return boolean
     */
    public static boolean move(File sourceFile, File targetFile) {
        Uri sourceDocumentUri = getUri(sourceFile);
        Uri sourceParentDocumentUri = getUri(sourceFile.getParentFile());
        Uri targetParentDocumentUri = getUri(targetFile.getParentFile());
        if (sourceDocumentUri == null || sourceParentDocumentUri == null || targetParentDocumentUri == null){
            return false;
        }

        if (!targetFile.getParentFile().exists()) {
            mkdirs(targetFile.getParentFile());
        }
        Log.i(TAG,
                "move(), sourceDocumentUri = " + sourceDocumentUri.toString()
                        + ", \nsourceParentDocumentUri = " + sourceParentDocumentUri.toString()
                        + ", \ntargetParentDocumentUri = " + targetParentDocumentUri.toString());
        try {
            Uri fileUri = DocumentsContract.moveDocument(sContentResolver, sourceDocumentUri,
                    sourceParentDocumentUri, targetParentDocumentUri);
            return fileUri != null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SQLiteConstraintException exception){
            Log.e(TAG, "file move failed");
        }
        return false;
    }

    /**
     * @param sourceFile
     *            File
     * @param targetFile
     *            File
     * @return boolean
     */
    public static boolean rename(File sourceFile, File targetFile) {
        Log.i(TAG, "rename() sourceFile = " + sourceFile.getAbsolutePath()
                + ", targetFile = " + targetFile.getAbsolutePath());
        Uri sourceDocumentUri = getUri(sourceFile);
        if (sourceDocumentUri == null){
            return false;
        }
        try {
            Uri fileUri = DocumentsContract.renameDocument(sContentResolver, sourceDocumentUri,
                    targetFile.getName());
            return fileUri != null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param file
     *            File
     * @return FileOutputStream
     */
    public static FileOutputStream getFileOutputStream(File file) {
        Log.i(TAG, "getFileOutputStream(), file = " + file.getAbsolutePath());
        if (!file.exists()) {
            if (!createNewFile(file)) {
                Log.w(TAG, "getFileOutputStream createFile failed, just return null!");
                return null;
            }
        }
        Uri targetUri = getUri(file);
        if (targetUri == null){
            return null;
        }
        try {
            ParcelFileDescriptor pfd = sContentResolver.openFileDescriptor(targetUri, "w");
            return new FileOutputStream(pfd.getFileDescriptor());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param file
     *            FileDescriptor
     * @return FileOutputStream
     */
    public static FileDescriptor getFileDescriptor(File file) {
        Log.i(TAG, "getFileOutputStream(), file = " + file.getAbsolutePath());
        if (!file.exists()) {
            if (!createNewFile(file)) {
                Log.w(TAG, "getFileOutputStream createFile failed, just return null!");
                return null;
            }
        }
        Uri targetUri = getUri(file);
        if (targetUri == null){
            return null;
        }
        try {
            ParcelFileDescriptor pfd = sContentResolver.openFileDescriptor(targetUri, "w");
            return pfd.getFileDescriptor();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFilePathByUri(Context context, Uri uri) {
        String path = null;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        }
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
            return path;
        }
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
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
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param file
     *            File
     * @return boolean
     */
    public static boolean isInExternalStorage(File file) {
        boolean isInExternalStorage = Environment.isExternalStorageRemovable(file);
        Log.i(TAG, "isInExternalStorage(), file.getAbsolutePath() = " + file.getAbsolutePath()
                + ", externalStoragePath = "  + ", isInExternalStorage = " + isInExternalStorage);
        return isInExternalStorage;
    }
}
//Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 end
