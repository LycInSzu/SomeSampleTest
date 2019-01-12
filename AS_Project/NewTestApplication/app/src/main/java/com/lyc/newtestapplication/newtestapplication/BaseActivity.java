package com.lyc.newtestapplication.newtestapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    public String TAG ="my"+ getCurrentActivityName().getSimpleName();


    public abstract Class getCurrentActivityName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 检查是否已被授权危险权限
     *
     * @param permissions
     * @return
     */
    public boolean checkDangerousPermissions(Activity ac, String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.shouldShowRequestPermissionRationale(ac, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否已被授权危险权限
     *
     * @param permission
     * @return
     */
    public boolean checkDangerousPermission(Activity ac, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.shouldShowRequestPermissionRationale(ac, permission)) {
            return false;
        }

        return true;
    }


    // 开始提交请求权限
    public void requestNeedPermissions(Activity ac, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(ac, permissions, requestCode);
    }


    // 提示用户去应用设置界面手动开启权限
    public void showDialogTipUserGoToAppSettting(final Context context) {

        new AlertDialog.Builder(context)
                .setTitle("存储权限不可用")
                .setMessage("请在-应用设置-权限-中，允许支付宝使用存储权限来保存用户数据")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        ((Activity) context).finish();
                    }
                }).setCancelable(false).show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 123);
    }
    public void startDetermindActivity(Class c,@Nullable Bundle data) {
        Intent intent = new Intent();
        if (data!=null){

        }
        intent.setClass(this, c);
        startActivity(intent);
    }


    public void startDetermindActivityForResult(Class c,@Nullable String flagName,@Nullable Bundle bundle, int requestCode) {
        Intent intent = new Intent();
        if (bundle!=null){
            intent.putExtra(flagName,bundle);
        }
        intent.setClass(this, c);
        startActivityForResult(intent,requestCode);
    }
}
