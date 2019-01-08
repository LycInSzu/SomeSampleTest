package com.lyc.newtestapplication.newtestapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.MemoryFile;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.lyc.newtestapplication.newtestapplication.AboutSurfaceView.SurfaceViewTestActivity;
import com.lyc.newtestapplication.newtestapplication.DialogTest.DialogTestActivity;
import com.lyc.newtestapplication.newtestapplication.VibrateDemo.VibratorDemoActivity;
import com.lyc.newtestapplication.newtestapplication.ViewModelTest.ViewModelTestActivity;


import java.util.Locale;
import java.util.TimeZone;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends BaseActivity {
    private static final String TAG = "FullscreenActivity";
    private String[] permissions = {"android.permission.CAMERA", "android.hardware.Camera"};
    //    /**
//     * Whether or not the system UI should be auto-hidden after
//     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
//     */
//    private static final boolean AUTO_HIDE = true;
//
//    /**
//     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
//     * user interaction before hiding the system UI.
//     */
//    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
//
//    /**
//     * Some older devices needs a small delay between UI widget updates
//     * and a change of the status and navigation bar.
//     */
//    private static final int UI_ANIMATION_DELAY = 300;
//    private final Handler mHideHandler = new Handler();
//    private View mContentView;
//    private static final long[] sVibratePattern = { 0, 2000, 500, 1000, 500, 1000, 500,
//            2000, 500, 1000, 500, 1000};
//    private final Runnable mHidePart2Runnable = new Runnable() {
//        @SuppressLint("InlinedApi")
//        @Override
//        public void run() {
//            // Delayed removal of status and navigation bar
//
//            // Note that some of these constants are new as of API 16 (Jelly Bean)
//            // and API 19 (KitKat). It is safe to use them, as they are inlined
//            // at compile-time and do nothing on earlier devices.
////            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
////                    | View.SYSTEM_UI_FLAG_FULLSCREEN
////                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
////                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
////                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
////                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }
//    };
    private LinearLayout mControlsView;
//    private final Runnable mShowPart2Runnable = new Runnable() {
//        @Override
//        public void run() {
//            // Delayed display of UI elements
//            ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.show();
//            }
////            mControlsView.setVisibility(View.VISIBLE);
//        }
//    };
//    private boolean mVisible;
//    private final Runnable mHideRunnable = new Runnable() {
//        @Override
//        public void run() {
////            hide();
//        }
//    };

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
////            if (AUTO_HIDE) {
////                delayedHide(AUTO_HIDE_DELAY_MILLIS);
////            }
//            return false;
//        }
//    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_fullscreen);

//        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
//        mContentView=findViewById(R.id.fullscreen_content);
//        TextView mTextview = (TextView) findViewById(R.id.fullscreen_content);
//        mTextview.setText("\u202D" + "+185 7664 0037" + "\u202C");

//        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) mControlsView.getLayoutParams();
//
//        params.width=params.width+280;
////        params.leftMargin=params.leftMargin+;
////        params.topMargin=params.topMargin+;
//        mControlsView.setLayoutParams(params);
//        WifiManager wifiManager = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE));


        ContentResolver contentResolver = getContentResolver();



        int[] position = new int[2];
        mControlsView.getLocationOnScreen(position);

        position[1] = position[1] + mControlsView.getHeight() / 2;
        final String able = Locale.getDefault().toString();
        // Locale locale=Locale.getDefault();
        System.out.println("--------lyc--------- able is  " + able);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        System.out.println("--------lyc----{----- screenWidth is}  " + screenWidth);
        System.out.println("--------lyc--------- screenHeight is  " + screenHeight);

        TimeZone zone = TimeZone.getDefault();

        System.out.println("--------lyc--------- zone is : " + zone + " \\n    -----------zone display name is :" + zone.getDisplayName() + "\n     ------------ zone ID is : " + zone.getID());
//        startActivity();

        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);


//        Vibrator vibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        vibrator.vibrate(sVibratePattern,-1);


//        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(1,new Notification());


//        TranslateAnimation translateAnimation = new TranslateAnimation(0, 100, 0, 100);


        final TextView mTestTextview = findViewById(R.id.shutdown_summary);

//        TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f, -42, 225);
//        if(orientationInLand){
//            translateAnimation = new TranslateAnimation(0f, 225, -15, 0f);
//        }
//        ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f, 1f, 0.8f, 1f,
//                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

//        ObjectAnimator translationX = new ObjectAnimator().ofFloat(mTestTextview,"translationX",0,100);

        AnimatorSet animatorSet = new AnimatorSet();  //组合动画
        ObjectAnimator translationY = new ObjectAnimator().ofFloat(mTestTextview, "translationY", -42, 255);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mTestTextview, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mTestTextview, "scaleY", 0.8f, 1f);
        animatorSet.playTogether(scaleX, scaleY, translationY); //设置动画
        animatorSet.setDuration(3000);  //设置动画时间
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                Log.d("动画开始", "");
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.d("动画结束", "");
                //动画结束跳转

            }

            @Override
            public void onAnimationCancel(Animator animator) {
                Log.d("动画取消", "");
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                Log.d("动画重复", "");
            }
        });


//
//        TranslateAnimation translateAnimation = new TranslateAnimation(0, 100, 0, 100);
//        translateAnimation.setDuration(1000);
//        translateAnimation.setFillAfter(true);
//        mTestTextview.startAnimation(translateAnimation);
//        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                //清除动画
////                mTestTextview.clearAnimation();
//                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mTestTextview.getLayoutParams();
//                params.leftMargin = params.leftMargin + 100;
//                params.topMargin = params.topMargin + 100;
//                mTestTextview.setLayoutParams(params);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });


    }

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//
//        // Trigger the initial hide() shortly after the activity has been
//        // created, to briefly hint to the user that UI controls
//        // are available.
//        delayedHide(100);
//    }

//    private void toggle() {
//        if (mVisible) {
//            hide();
//        } else {
//            show();
//        }
//    }

//    private void hide() {
//        // Hide UI first
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
//        mControlsView.setVisibility(View.GONE);
//        mVisible = false;
//
//        // Schedule a runnable to remove the status and navigation bar after a delay
//        mHideHandler.removeCallbacks(mShowPart2Runnable);
//        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
//    }
//
//    @SuppressLint("InlinedApi")
//    private void show() {
//        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        mVisible = true;
//
//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
//    }

//    /**
//     * Schedules a call to hide() in delay milliseconds, canceling any
//     * previously scheduled calls.
//     */
//    private void delayedHide(int delayMillis) {
//        mHideHandler.removeCallbacks(mHideRunnable);
//        mHideHandler.postDelayed(mHideRunnable, delayMillis);
//    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.i(TAG, "onContentChanged");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    /**
     * Called after {@link #onStop} when the current activity is being
     * re-displayed to the user (the user has navigated back to it).  It will
     * be followed by {@link #onStart} and then {@link #onResume}.
     *
     * <p>For activities that are using raw Cursor objects (instead of
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
        Log.i(TAG, "onRestart");
    }

    public void onClick(View view) {
        switch (Utils.jumpToNewActivity) {
            case Utils.SurfaceViewTestActivity:

                if (checkDangerousPermissions(this, permissions)) {
                    startDetermindActivity(SurfaceViewTestActivity.class);
                } else {

                    requestNeedPermissions(this, permissions, 342);
                }
                break;
            case Utils.DialogTestActivity:
                Intent intent2 = new Intent();
                intent2.setClass(this, DialogTestActivity.class);
                startActivity(intent2);
                break;
            case Utils.VibratorDemoActivity:
                startDetermindActivity(VibratorDemoActivity.class);
                break;
            case Utils.ViewModelTestActivity:
                startDetermindActivity(ViewModelTestActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 342:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting(this);
                    } else
                        finish();
                } else {
                    startDetermindActivity(SurfaceViewTestActivity.class);
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 检查该权限是否已经获取
                int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 提示用户应该去应用设置界面手动开启权限
                    showDialogTipUserGoToAppSettting(this);

                } else {
//                     if (dialog != null && dialog.isShowing()) {
//                         dialog.dismiss();
//                    }
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startDetermindActivity(Class c) {
        Intent intent = new Intent();
        intent.setClass(this, c);
        startActivity(intent);
    }
}
