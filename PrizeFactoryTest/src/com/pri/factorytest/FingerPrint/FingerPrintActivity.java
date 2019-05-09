package com.pri.factorytest.FingerPrint;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.pri.factorytest.PrizeHwInfo;
import com.pri.factorytest.R;

import java.util.Optional;
import java.util.stream.Stream;

import static com.pri.factorytest.FactoryTestApplication.PRIZE_CUSTOMER;

public class FingerPrintActivity extends BaseActivity {

    private static final String TAG = "FingerPrintActivity";

    private static final long CANCEL_TIME_INTERVAL = 30000;
    private static final long RELEASE_TIME_INTERVAL = 100;

    private ImageView mPhoneImage;
    private TextView mTitleNoticeTxt;
    private TextView mSubInfoTxt;
    private TextView mSubInfoTxtOutside;

    private final int[] printImages = new int[]{R.drawable.b_20,
            R.drawable.b_10,
            R.drawable.b_01
    };
    private ImageView mGuideAnimationView;
    private RelativeLayout mGuideRl;
    private RelativeLayout mRegisterLl;
    private AnimationDrawable mGuideAnim;

    private FingerprintManager mFingerprintManager;
    private CancellationSignal mEnrollmentCancel;
    private int mEnrollmentSteps = -1;
    private int mEnrollSteps = 0;
    private Handler mHandler = new Handler();
    private Handler mThreadHandler = null;
    private int mUserId;
    private LockPatternUtils mLockUtils;
    private boolean mHasFingerTest = false;
    private static final String LOCK_PASSWORD = "1234";
    /**fingerprint module 1:duntai,other:other*/
    private final static int PRIZE_FINGERPRINT_MODULE = Optional.ofNullable(SystemProperties
            .get("ro.pri_fingerprint_module")).map(x -> {
        try {
            return Integer.parseInt(x.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }).orElse(-1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSubContentView(R.layout.activity_register);
        displayBackButton();
        setTitleHeaderText(getResources().getString(R.string.register_title));
        initView();
        mFingerprintManager = (FingerprintManager) this.getSystemService(
                Context.FINGERPRINT_SERVICE);

        mEnrollmentCancel = new CancellationSignal();
        mUserId = UserHandle.myUserId();
        if (mUserId == UserHandle.USER_NULL) {
            mUserId = UserHandle.USER_CURRENT;
        }
        mFingerprintManager.setActiveUser(mUserId);

        HandlerThread ht = new HandlerThread("removeScreenLock");
        ht.start();
        mThreadHandler = new Handler(ht.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Optional.ofNullable(mLockUtils).ifPresent(x -> {
                    x.clearLock(LOCK_PASSWORD, mUserId);
                    x.setLockScreenDisabled(false, mUserId);
                    x.setCredentialRequiredToDecrypt(false);
                    x.clearEncryptionPassword();
                    Log.d(TAG, "removeScreenLock success!");
                });
            }
        };

        String settingToken = Settings.System.getString(getContentResolver(),
                Settings.System.PRIZE_FINGERPRINT_TOKEN);

        if (settingToken != null && PRIZE_FINGERPRINT_MODULE == 1) {
            mHasFingerTest = true;
            byte[] token = Base64.decode(settingToken.getBytes(), Base64.DEFAULT);
            mFingerprintManager.enroll(token, mEnrollmentCancel, 0, mUserId, mEnrollmentCallback);
        } else {
            if ("ACTIVE".equals(SystemProperties.get("vendor.soter.teei.thh.init"))) {
                mTitleNoticeTxt.setText(getString(R.string.finger_active_disc));
            } else {
                mTitleNoticeTxt.setText(getString(R.string.finger_unactive_disc));
            }
            new Task().execute();
        }
    }

    private class Task extends AsyncTask<Void, Void, Intent> {
        @Override
        protected Intent doInBackground(Void... params) {
            initEnrollData();
            return null;
        }

        @Override
        protected void onPostExecute(Intent resultData) {
            // finish(resultData);
        }
    }

    private void initEnrollData() {
        mHandler.postDelayed(mStartRunnable, 50);
        if (PRIZE_FINGERPRINT_MODULE == 1) {
            enrollDunTai();
        } else {
            enrollOther();
        }
        mHandler.postDelayed(mEndRunnable, 50);
    }

    private void enrollDunTai() {
        mLockUtils = new LockPatternUtils(FingerPrintActivity.this);

        final long challenge = mFingerprintManager.preEnroll();
        Log.i(TAG, "---mToken challenge=" + Long.toHexString(challenge));
        if (mLockUtils.getActivePasswordQuality(mUserId) != DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED) {
            Log.d(TAG, "alreay set screen lock ,now remove screenLock when first in Fptest!");
            removeScreenLock();
        }
        byte[] tokenDunTai = null;
        try {
            startSavePassword(false, challenge, LOCK_PASSWORD, null,
                    DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
            tokenDunTai = mLockUtils.verifyPassword(LOCK_PASSWORD, challenge, mUserId);
        } catch (RequestThrottledException e) {
            e.printStackTrace();
            tokenDunTai = null;
        }
        if (tokenDunTai == null) {
            Log.e(TAG, "-------get the token duntai is error,return------");
            return;
        }

        mFingerprintManager.enroll(tokenDunTai, mEnrollmentCancel, 0, mUserId, mEnrollmentCallback);
    }

    private void enrollOther() {
        Optional.ofNullable(mFingerprintManager).ifPresent(x -> {
            long challenge = x.preEnroll();
            Log.i(TAG, "---mToken challenge=" + Long.toHexString(challenge));
            byte[] tokenXinWei = new byte[69];
            for (int i = 0; i < tokenXinWei.length; i++) {
                tokenXinWei[i] = (byte) 0xFF;
            }
            tokenXinWei[33] = 2;
            tokenXinWei[34] = 0;
            tokenXinWei[35] = 0;
            tokenXinWei[36] = 0;
            int challenge_h = (int) (challenge >> 32);
            int challenge_l = (int) (challenge & 0xFFFFFFFF);
            Log.i(TAG, "---mToken challenge_h=" + Integer.toHexString(challenge_h));
            Log.i(TAG, "---mToken challenge_l=" + Integer.toHexString(challenge_l));
            tokenXinWei[8] = (byte) (challenge_h >> 24);
            tokenXinWei[7] = (byte) ((challenge_h & 0xFF0000) >> 16);
            tokenXinWei[6] = (byte) ((challenge_h & 0xFF00) >> 8);
            tokenXinWei[5] = (byte) (challenge_h & 0xFF);
            tokenXinWei[4] = (byte) (challenge_l >> 24);
            tokenXinWei[3] = (byte) ((challenge_l & 0xFF0000) >> 16);
            tokenXinWei[2] = (byte) ((challenge_l & 0xFF00) >> 8);
            tokenXinWei[1] = (byte) (challenge_l & 0xFF);
            tokenXinWei[0] = (byte) 0;

            x.enroll(tokenXinWei, mEnrollmentCancel, 0, UserHandle.USER_CURRENT, mEnrollmentCallback);
        });
    }

    private void removeScreenLock() {
        mHandler.removeCallbacks(mStartRunnable);
        mHandler.removeCallbacks(mEndRunnable);
        mThreadHandler.sendEmptyMessage(0);
    }

    private void startSavePassword(boolean credentialRequired, long challenge,
                                   String chosenPassword, String currentPassword, int requestedQuality) {
        Optional.ofNullable(mLockUtils).ifPresent(x->{
            x.setCredentialRequiredToDecrypt(credentialRequired);
            x.saveLockPassword(chosenPassword, currentPassword,
                    requestedQuality, mUserId);
        });
    }

    private FingerprintManager.EnrollmentCallback mEnrollmentCallback
            = new FingerprintManager.EnrollmentCallback() {

        @Override
        public void onEnrollmentProgress(int remaining) {
            if (mEnrollmentSteps == -1) {
                mEnrollmentSteps = remaining;
            }
            Log.i(TAG, "----onEnrollment---Progress-----" + mEnrollmentSteps);
            fingerEnrollmentCall();
        }

        @Override
        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
            Log.i(TAG, "----onEnrollment---Help-----ID:" + helpMsgId + "--Msg:" + helpString);
            if (Stream.of(1001, 1003, 1004).noneMatch(x -> x == helpMsgId)) {
                fingerEnrollmentCall();
            }

        }

        @Override
        public void onEnrollmentError(int errMsgId, CharSequence errString) {
            Log.i(TAG, "----onEnrollment---Error--errId:" + errMsgId + "|errMsg:" + errString);
        }
    };

    private void fingerEnrollmentCall() {
        mEnrollSteps++;

        if (mEnrollSteps < 1 || mEnrollSteps > 3) {
            return;
        }
        mPhoneImage.setBackgroundResource(printImages[3 - mEnrollSteps]);
        if (mEnrollSteps == 3) {
            mEnrollmentCancel.cancel();
            mEnrollmentSteps = -1;
            SystemProperties.set("persist.sys.prize_fp_enable", "1");
            if(!mHasFingerTest){
                removeScreenLock();
            }

            finish();
        }
    }

    private final Runnable mStartRunnable = () -> Toast.makeText(this,
            getString(R.string.finger_env_init), Toast.LENGTH_SHORT).show();

    private final Runnable mEndRunnable = () -> Toast.makeText(this,
            getString(R.string.finger_env_ready_to_test), Toast.LENGTH_SHORT).show();

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEnrollmentCancel.cancel();
        mEnrollSteps = 0;
        mEnrollmentSteps = -1;
        removeScreenLock();
        Optional.ofNullable(mLockUtils).ifPresent(x -> x = null);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initView() {
        mGuideRl = (RelativeLayout) findViewById(R.id.guide_animation_rl);
        mRegisterLl = (RelativeLayout) findViewById(R.id.register_rl);

        mGuideAnimationView = (ImageView) findViewById(R.id.guide_animation_view);
        mGuideAnimationView.setBackgroundResource(R.drawable.guide_animation);
        mGuideAnim = (AnimationDrawable) mGuideAnimationView.getBackground();
        //mGuideAnim.start(); //removed by tangan-20170321

        mPhoneImage = (ImageView) findViewById(R.id.register_phone);
        mTitleNoticeTxt = (TextView) findViewById(R.id.title_notice_text);

        boolean isShowTitleNotice = Stream.of("koobee").anyMatch(x -> PRIZE_CUSTOMER.equals(x));
        mTitleNoticeTxt.setVisibility(isShowTitleNotice ? View.VISIBLE : View.GONE);

        mSubInfoTxt = (TextView) findViewById(R.id.register_sub_info);

        mSubInfoTxt.setText(getString(R.string.guide_notice));

        mSubInfoTxtOutside = (TextView) findViewById(R.id.register_sub_info_outside);
        //added by tangan-20170321-begin
        mGuideRl.setVisibility(View.GONE);
        mRegisterLl.setVisibility(View.VISIBLE);
        //added by tangan-20170321-end
    }

    @Override
    public void onBackPressed() {
    }

} 
