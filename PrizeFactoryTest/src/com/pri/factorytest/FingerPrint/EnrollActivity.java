package com.pri.factorytest.FingerPrint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodix.fingerprint.ShenzhenConstants;
import com.goodix.fingerprint.service.GoodixFingerprintManager;
import com.goodix.fingerprint.setting.util.DataPref;
import com.goodix.fingerprint.setting.util.DataPrefDefault;
import com.pri.factorytest.FingerPrint.huiding.Util;
import com.pri.factorytest.R;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class EnrollActivity extends BaseActivity implements View.OnClickListener, View.OnKeyListener {

    private final static String TAG = "EnrollActivity";

    private final static String KEY_USER_DESC= "user_desc";
    private final static String KEY_ENV_DESC= "env_desc";

    private GoodixFingerprintManager mGoodixFingerprintManager;

    private CancellationSignal mCancellationSignal;

    private Interpolator mFastOutSlowInInterpolator;
    private Interpolator mLinearOutSlowInInterpolator;
    private Interpolator mFastOutLinearInInterpolator;

    private int mIndicatorBackgroundRestingColor;
    private int mIndicatorBackgroundActivatedColor;

    private ObjectAnimator mProgressAnim;
    private AnimatedVectorDrawable mIconAnimationDrawable;

    private Animation mUpAnimation;
    private Animation mDownAnimation;
    private Vibrator mVibrator;

    ImageView mFingerprintAnimator;
    RingProgressBar mProgressBar;

    private Timer mTimer;
    private TimerTask mTimeTask;

    private static final int PROGRESS_BAR_MAX = 100;
    private static final int FINISH_DELAY = 250;
    private static int MAX_ENROLL_TEMPLATE = 20;

    private Handler mHandler;
    private Context mContext;
    private Activity mActivity;
    private int mFingerIndex;
    private ImageView mFingerImageView;
    private boolean firstEnrollMsg = true;
    private TextView errorInfo;

    private TextView mUserTv,mEnvTv;
    private String mUserEnv = "";
    private volatile int mEnrollCount = 0;
    private volatile int mEnrollHelp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        displayBackButton();
        setTitleHeaderText(getResources().getString(R.string.register_title));
        DataPref.getInstance().init(this,"fingerprint_list");

        initFingerList();
        mFingerprintAnimator = (ImageView) findViewById(R.id.fingerprint_animator);
        mProgressBar = (RingProgressBar) findViewById(R.id.fingerprint_progress_bar);
        mGoodixFingerprintManager = GoodixFingerprintManager.getFingerprintManager(this);
        //mGoodixFingerprintManager.registerTestCmdCallback(mTestCmdCallback);
        mGoodixFingerprintManager.testCmd(ShenzhenConstants.CMD_TEST_SZ_SET_GROUP_ID);

        mFingerImageView = (ImageView) findViewById(R.id.finger_img);
        errorInfo = (TextView) findViewById(R.id.error_code);
        mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                this, android.R.interpolator.fast_out_slow_in);
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                this, android.R.interpolator.linear_out_slow_in);
        mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(
                this, android.R.interpolator.fast_out_linear_in);
        mIndicatorBackgroundRestingColor
                = getResources().getColor(R.color.fingerprint_indicator_background_resting);
        mIndicatorBackgroundActivatedColor
                = getResources().getColor(R.color.fingerprint_indicator_background_activated);
        mIconAnimationDrawable = (AnimatedVectorDrawable) mFingerprintAnimator.getDrawable();

        mUserTv = (TextView) findViewById(R.id.user_name_tv);
        mEnvTv = (TextView) findViewById(R.id.env_dsc_tv);
        mUserTv.setOnClickListener(this);
        mEnvTv.setOnClickListener(this);

        mUserTv.setOnKeyListener(this);
        mEnvTv.setOnKeyListener(this);

        mHandler = new Handler();
        mContext = this;
        mActivity = this;

        initAnimation();
        loopStartIconAnimaion();

        initUserEnv();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ");

        mGoodixFingerprintManager.showSensorViewWindow(true);
        mGoodixFingerprintManager.setHBMMode(true);
        Util.setAccesibility(this,true);
        //showCustomizeDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoodixFingerprintManager.showSensorViewWindow(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoodixFingerprintManager.showSensorViewWindow(false);
        mGoodixFingerprintManager.setHBMMode(false);
        cancel();
        Util.setAccesibility(this,false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.setAccesibility(this,false);
    }

    @Override
    public void finish() {
        super.finish();
        setResult(1);
    }

    private void initUserEnv(){

        String user = DataPrefDefault.getInstance(this).getString(KEY_USER_DESC,"");
        String env = DataPrefDefault.getInstance(this).getString(KEY_ENV_DESC,"");

        mUserTv.setText(user);
        mEnvTv.setText(env);

        if(user != null && env != null){
            if(user.length() == 0 && env.length() != 0){
                mUserEnv = env;
            } else if(user.length() != 0 && env.length() == 0){
                mUserEnv = user;
            } else if (user.length() != 0 && env.length() != 0){
                mUserEnv = user + "@"+env;
            } else {
                mUserEnv = "";
            }
        }

        Log.d(TAG, "initUserEnv mGoodixFingerprintManager.untrustedEnroll2");
        mCancellationSignal = new CancellationSignal();
        mGoodixFingerprintManager.untrustedEnroll2(mUserEnv,mCancellationSignal, mUntrustEnrollCallback);
    }


    private void showCustomizeDialog() {
//
//        AlertDialog.Builder customizeDialog =
//                new AlertDialog.Builder(this);
//        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_layout,null);
//
//        EditText userTx = (EditText) dialogView.findViewById(R.id.user_name_et);
//        EditText envTx= (EditText) dialogView.findViewById(R.id.env_dsc_et);
//
//        userTx.setText(DataPrefDefault.getInstance(this).getString(KEY_USER_DESC,""));
//        envTx.setText(DataPrefDefault.getInstance(this).getString(KEY_ENV_DESC,""));
//
//        mGoodixFingerprintManager.showSensorViewWindow(false);
//
//        customizeDialog.setTitle(mContext.getString(R.string.user_add));
//        customizeDialog.setView(dialogView);
//        customizeDialog.setPositiveButton(mContext.getString(R.string.ok),
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // 获取EditView中的输入内容
//                        EditText userEt = (EditText) dialogView.findViewById(R.id.user_name_et);
//                        EditText envEt = (EditText) dialogView.findViewById(R.id.env_dsc_et);
//
//                        String user = userEt.getText().toString();
//                        String env = envEt.getText().toString();
//                        Log.d(TAG, "user =  " + user + ",env =" + env);
//                        if(user != null && env != null){
//                            if(user.length() == 0 && env.length() != 0){
//                                mUserEnv = env;
//                            } else if(user.length() != 0 && env.length() == 0){
//                                mUserEnv = user;
//                            } else if (user.length() != 0 && env.length() != 0){
//                                mUserEnv = user + "@"+env;
//                            } else {
//                                mUserEnv = "";
//                            }
//
//                        }
//                        Log.d(TAG, "mGoodixFingerprintManager.untrustedEnroll2 ");
//                        mCancellationSignal = new CancellationSignal();
//                        mGoodixFingerprintManager.untrustedEnroll2(mUserEnv,mCancellationSignal, mUntrustEnrollCallback);
//
//                        DataPrefDefault.getInstance(mContext).putString(KEY_USER_DESC,user);
//                        DataPrefDefault.getInstance(mContext).putString(KEY_ENV_DESC,env);
//
//                        mUserTv.setText(user);
//                        mEnvTv.setText(env);
//                        mGoodixFingerprintManager.showSensorViewWindow(true);
//                    }
//                });
//        customizeDialog.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                mGoodixFingerprintManager.showSensorViewWindow(true);
//                 finish();
//            }
//        });
//        customizeDialog.show();
    }

    private void initFingerList(){
        Map<String,?> map = DataPref.getInstance().dataItems();
        mFingerIndex = map.size();

    }

    private void cancel(){
        if(mCancellationSignal != null && !mCancellationSignal.isCanceled()){
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    private void loopStartIconAnimaion() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startIconAnimation();
            }
        };
        mTimer = new Timer();
        mTimeTask = new TimerTask() {
            @Override
            public void run() {
                handler.postDelayed(runnable, 500);
            }
        };
        mTimer.schedule(mTimeTask, 500, 800);
    }


    private void startIconAnimation() {
        mIconAnimationDrawable.start();
    }

    private void initAnimation() {
/*        mUpAnimation = AnimationUtils.loadAnimation(this, R.anim.push_up_in);
        mUpAnimation.setFillAfter(true);
        mUpAnimation.setAnimationListener(new UpAnimationOnListener());

        mDownAnimation = AnimationUtils.loadAnimation(this, R.anim.push_up_out);
        mDownAnimation.setFillAfter(true);
        mDownAnimation.setAnimationListener(new DownAnimationOnListener());*/

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void animateFlash() {
        ValueAnimator anim = ValueAnimator.ofArgb(mIndicatorBackgroundRestingColor,
                mIndicatorBackgroundActivatedColor);
        final ValueAnimator.AnimatorUpdateListener listener =
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mFingerprintAnimator.setBackgroundTintList(ColorStateList.valueOf(
                                (Integer) animation.getAnimatedValue()));
                    }
                };

        final ValueAnimator.AnimatorListener animListerner = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startIconAnimation();

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        anim.addUpdateListener(listener);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ValueAnimator anim = ValueAnimator.ofArgb(mIndicatorBackgroundActivatedColor,
                        mIndicatorBackgroundRestingColor);
                anim.addUpdateListener(listener);
                anim.addListener(animListerner);
                anim.setDuration(300);
                anim.setInterpolator(mLinearOutSlowInInterpolator);
                anim.start();
            }
        });
        anim.setInterpolator(mFastOutSlowInInterpolator);
        anim.setDuration(300);
        anim.start();
    }

    private final Animator.AnimatorListener mProgressAnimationListener
            = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {
            Log.d(TAG, "progress onAnimationStart ");
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mProgressBar.getProgress() >= PROGRESS_BAR_MAX) {
                mProgressBar.postDelayed(mDelayedFinishRunnable, FINISH_DELAY);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }
    };

    private final Runnable mDelayedFinishRunnable = new Runnable() {
        @Override
        public void run() {
            //launchFinish();
        }
    };

    private void updateProgress(boolean animate, int progress) {

        if (animate) {
            animateProgress(progress);
        } else {
            mProgressBar.setProgress(progress);
        }
    }

    private void animateProgress(int progress) {
        if (mProgressAnim != null) {
            mProgressAnim.cancel();
        }
        ObjectAnimator anim = ObjectAnimator.ofInt(mProgressBar, "progress",
                mProgressBar.getProgress(), progress);
        anim.addListener(mProgressAnimationListener);
        anim.setInterpolator(mFastOutSlowInInterpolator);
        anim.setDuration(250);
        anim.start();
        mProgressAnim = anim;
    }


    private void stopIconAnimation() {
        mIconAnimationDrawable.stop();
    }

    private GoodixFingerprintManager.UntrustedEnrollmentCallback mUntrustEnrollCallback = new
            GoodixFingerprintManager.UntrustedEnrollmentCallback() {
        @Override
        public void onEnrollmentError(int errMsgId, final CharSequence errString) {
            Log.d(TAG, "onEnrollmentError");
            super.onEnrollmentError(errMsgId, errString);
            EnrollActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    errorInfo.setText(errString);
                }
            });
        }

        @Override
        public void onEnrollmentHelp(int helpMsgId, final CharSequence helpString) {
            Log.d(TAG, "onEnrollmentHelp");
            super.onEnrollmentHelp(helpMsgId, helpString);
            mEnrollHelp++;
            /*int a = mEnrollHelp;
            if (a % 2 == 0 && a % 4 != 0) {
                updateProgress(true, a / 2 * 100 / 3);
                stopIconAnimation();
                animateFlash();
                if (a == 10) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEnrollHelp = 0;
                            mActivity.finish();
                            setResult(1);
                        }
                    }, 1000);
                }
            }*/
            EnrollActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    errorInfo.setText(helpString);
                }
            });
        }

        @Override
        public void onEnrollImage(byte[] bmpData, int width, int height) {
            Log.d(TAG, "onEnrollImage");

            super.onEnrollImage(bmpData,width,height);
            if(width > 0 && height > 0){
                Bitmap bitmap = Util.convertToBitmap(bmpData,width,height);
                mFingerImageView.setImageBitmap(bitmap);
            }
        }

        @Override
        public void onEnrollmentProgress(int fingerId, int remaining) {
            if (firstEnrollMsg){
                MAX_ENROLL_TEMPLATE = remaining + 1;
                firstEnrollMsg = false;
                Log.d(TAG,"onEnrollmentProgress firstEnrollMsg = " + MAX_ENROLL_TEMPLATE);
            }
            super.onEnrollmentProgress(fingerId, remaining);
            Log.d(TAG,"onEnrollmentProgress remaining = " + remaining);
            mEnrollCount++;
            updateProgress(true, mEnrollCount * 100 / 3);
            stopIconAnimation();
            animateFlash();
            if (mEnrollCount == 3) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEnrollCount = 0;
                        setResult(1);
                        mActivity.finish();
                    }
                }, 200);
            }

            /*updateProgress(true,(MAX_ENROLL_TEMPLATE -remaining) * 100 / MAX_ENROLL_TEMPLATE);
            stopIconAnimation();
            animateFlash();

            if(remaining == 0){
                //DataPref.getInstance().putString("" + fingerId,getString(R.string.fingerprint_name,mFingerIndex++));
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.finish();
                    }
                },1000);
            }*/
        }

        @Override
        public void onEnrollmentAcquired(int acquireInfo) {
            Log.d(TAG, "onEnrollmentAcquired  acquireInfo = " + acquireInfo);
            super.onEnrollmentAcquired(acquireInfo);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_name_tv:
            case R.id.env_dsc_tv:
                Log.d(TAG, "showCustomizeDialog  onClick " );
                showCustomizeDialog();
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.d(TAG, "onKey  keyCode =  " + keyCode + " event =" + event.getAction() );
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return  false;
        }
        return true;
    }
}
