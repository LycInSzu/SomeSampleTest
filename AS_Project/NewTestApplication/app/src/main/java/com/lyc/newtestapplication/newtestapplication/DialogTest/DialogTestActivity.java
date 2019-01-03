package com.lyc.newtestapplication.newtestapplication.DialogTest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.lyc.newtestapplication.newtestapplication.R;


public class DialogTestActivity extends AppCompatActivity {
    private static final String TAG ="DialogTestActivity" ;
    private Dialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        mDialog=createAlertDialog(DialogTestActivity.this);
        mDialog.setCancelable(false);
        mDialog.show();



    }

    private AlertDialog createAlertDialog(Context context){
        //创建AlertDialog的构造器的对象
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        //设置构造器标题
        builder.setTitle("提示");
        //构造器对应的图标
        builder.setIcon(R.mipmap.ic_launcher);
        //构造器内容,为对话框设置文本项(之后还有列表项的例子)
        builder.setMessage("你是否要狠心离我而去？");
        //为构造器设置确定按钮,第一个参数为按钮显示的文本信息，第二个参数为点击后的监听事件，用匿名内部类实现
        builder.setPositiveButton("是呀", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //第一个参数dialog是点击的确定按钮所属的Dialog对象,第二个参数which是按钮的标示值
//                finish();//结束App
                dialog.dismiss();

            }
        });
        //为构造器设置取消按钮,若点击按钮后不需要做任何操作则直接为第二个参数赋值null
        builder.setNegativeButton("不呀",null);
        //为构造器设置一个比较中性的按钮，比如忽略、稍后提醒等
        builder.setNeutralButton("稍后提醒",null);
        //利用构造器创建AlertDialog的对象,实现实例化
        return builder.create();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG,"onConfigurationChanged");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.i(TAG,"onContentChanged");

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDialog.dismiss();
        Log.i(TAG,"onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDialog.show();
        Log.i(TAG,"onResume");
    }

    /**
     * Called after {@link #onStop} when the current activity is being
     * re-displayed to the user (the user has navigated back to it).  It will
     * be followed by {@link #onStart} and then {@link #onResume}.
     *
     * <p>For activities that are using raw  Cursor objects (instead of
     * creating them through
     * {@link #managedQuery(Uri, String[], String, String[], String)},
     * this is usually the place
     * where the cursor should be requeried (because you had deactivated it in
     * {@link #onStop}.
     *
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onStop
     * @see #onStart
     * @see #onResume
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG,"onRestart");
    }
}
