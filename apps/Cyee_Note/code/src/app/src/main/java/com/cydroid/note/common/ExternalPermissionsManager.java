//Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 begin
package com.cydroid.note.common;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Environment;
//import com.cydroid.note.common.Log;

import com.cydroid.note.common.ExternalStorageFileManager;

import java.io.File;
import java.util.List;

/**
 * Created by zhaocaili on 18-10-11.
 */

public class ExternalPermissionsManager {
    private static final String TAG = "ExternalPermissionsManager";

    public static boolean hasExternalSDCardFilePermission(Context context, File file){
        boolean isInExternalStorage = Environment.isExternalStorageRemovable(file);
        if (isInExternalStorage){
            Uri fileUri = ExternalStorageFileManager.buildRootUri(file);
            Log.d(TAG, "fileUri = " + fileUri);
            List<UriPermission> permissionList = context.getContentResolver().getPersistedUriPermissions();
            for (UriPermission permission : permissionList){
                Log.d(TAG, "permissionUri String= " + permission.getUri().toString());
                Log.d(TAG, "fileUri String = " + fileUri.toString());
                if (permission.getUri().toString().contains(fileUri.toString())){
                    if(isHasReadPermission(permissionList) && isHasWritePermission(permissionList)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasExternalSDCardPermission(Context context){
        List<UriPermission> permissionList = context.getContentResolver().getPersistedUriPermissions();
        if(isHasReadPermission(permissionList)&&isHasWritePermission(permissionList)){
            return true;
        }
        return false;
    }

    private static boolean isHasReadPermission(List<UriPermission> permissionList){
        for(UriPermission uriPermission : permissionList){
            if(uriPermission.isReadPermission())
                return true;
        }
        return false;
    }

    private static boolean isHasWritePermission(List<UriPermission> permissionList){
        for(UriPermission uriPermission : permissionList){
            if(uriPermission.isWritePermission())
                return true;
        }
        return false;
    }
}
//Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 end
