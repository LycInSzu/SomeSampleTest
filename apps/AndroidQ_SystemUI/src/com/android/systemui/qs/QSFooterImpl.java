/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.qs;

import static android.app.StatusBarManager.DISABLE2_QUICK_SETTINGS;

import static com.android.systemui.util.InjectionInflationController.VIEW_CONTEXT;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.Utils;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.settingslib.drawable.UserIconDrawable;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.R.dimen;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.TouchAnimator.Builder;
import com.android.systemui.statusbar.phone.MultiUserSwitch;
import com.android.systemui.statusbar.phone.SettingsButton;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener;
import com.android.systemui.tuner.TunerService;

import javax.inject.Inject;
import javax.inject.Named;
//add for EJQQQ-80 by liyuchong 20191204 begin
import java.util.Locale;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.text.format.DateFormat;
import android.widget.TextView;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.qs.TouchAnimator.ListenerAdapter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import com.android.systemui.SystemUIApplication;
//add for EJQQQ-80 by liyuchong 20191204 end
//modify for EJQQQ-80 by liyuchong 20191204 begin
//public class QSFooterImpl extends FrameLayout implements QSFooter,
//        OnClickListener, OnUserInfoChangedListener {
public class QSFooterImpl extends FrameLayout implements QSFooter,
        OnClickListener, OnUserInfoChangedListener, NextAlarmController.NextAlarmChangeCallback {
//modify for EJQQQ-80 by liyuchong 20191204 end

    private static final String TAG = "QSFooterImpl";

    //add for EJQQQ-80 by liyuchong 20191204 begin
    private NextAlarmController mNextAlarmController;
    private TextView mAlarmStatus;
    private View mAlarmStatusCollapsed;
    private View mDate;
    private View mDateTimeGroup;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private TouchAnimator mAlarmAnimator;
    private boolean mAlarmShowing;
    private boolean mKeyguardShowing;
    //add for EJQQQ-80 by liyuchong 20191204 end

    private final ActivityStarter mActivityStarter;
    private final UserInfoController mUserInfoController;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private SettingsButton mSettingsButton;
    protected View mSettingsContainer;
    private PageIndicator mPageIndicator;

    private boolean mQsDisabled;
    private QSPanel mQsPanel;

    private boolean mExpanded;

    private boolean mListening;

    protected MultiUserSwitch mMultiUserSwitch;
    private ImageView mMultiUserAvatar;

    protected TouchAnimator mFooterAnimator;
    private float mExpansionAmount;

    protected View mEdit;
    protected View mEditContainer;
    private TouchAnimator mSettingsCogAnimator;

    private View mActionsContainer;
    private View mDragHandle;

    private OnClickListener mExpandClickListener;
// delete for EJQQ-1123 by liyuchong 20191029 begin
//    private final ContentObserver mDeveloperSettingsObserver = new ContentObserver(
//            new Handler(mContext.getMainLooper())) {
//        @Override
//        public void onChange(boolean selfChange, Uri uri) {
//            super.onChange(selfChange, uri);
//            setBuildText();
//        }
//    };
// delete for EJQQ-1123 by liyuchong 20191029 end
    @Inject
    public QSFooterImpl(@Named(VIEW_CONTEXT) Context context, AttributeSet attrs,
            ActivityStarter activityStarter, UserInfoController userInfoController,
            DeviceProvisionedController deviceProvisionedController) {
        super(context, attrs);
        mActivityStarter = activityStarter;
        mUserInfoController = userInfoController;
        mDeviceProvisionedController = deviceProvisionedController;
    }

    @VisibleForTesting
    public QSFooterImpl(Context context, AttributeSet attrs) {
        this(context, attrs,
                Dependency.get(ActivityStarter.class),
                Dependency.get(UserInfoController.class),
                Dependency.get(DeviceProvisionedController.class));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEdit = findViewById(android.R.id.edit);
        mEdit.setOnClickListener(view ->
                mActivityStarter.postQSRunnableDismissingKeyguard(() ->
                        mQsPanel.showEdit(view)));

        mPageIndicator = findViewById(R.id.footer_page_indicator);

        mSettingsButton = findViewById(R.id.settings_button);
        mSettingsContainer = findViewById(R.id.settings_button_container);
        mSettingsButton.setOnClickListener(this);

        mMultiUserSwitch = findViewById(R.id.multi_user_switch);
        mMultiUserAvatar = mMultiUserSwitch.findViewById(R.id.multi_user_avatar);

        mDragHandle = findViewById(R.id.qs_drag_handle_view);
        mActionsContainer = findViewById(R.id.qs_footer_actions_container);
        mEditContainer = findViewById(R.id.qs_footer_actions_edit_container);

        // RenderThread is doing more harm than good when touching the header (to expand quick
        // settings), so disable it for this view
        ((RippleDrawable) mSettingsButton.getBackground()).setForceSoftware(true);
        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            ((ImageView) mEdit).setImageDrawable(mContext.getDrawable(R.drawable.ic_mode_edit));
            ((ImageView) mEdit).setImageTintList(ColorStateList.valueOf(Color.WHITE));
            mDateTimeGroup = findViewById(R.id.date_time_alarm_group);
            mDate = findViewById(R.id.date);
            mAlarmStatusCollapsed = findViewById(R.id.alarm_status_collapsed);
            mAlarmStatus = findViewById(R.id.alarm_status);
            mNextAlarmController = Dependency.get(NextAlarmController.class);
            ((ImageView) mSettingsButton).setImageDrawable(mContext.getDrawable(R.drawable.ic_settings_dg));
            ((ImageView) mSettingsButton).setImageTintList(ColorStateList.valueOf(Color.WHITE));
        }
        //add for EJQQQ-80 by liyuchong 20191204 end
        updateResources();

        addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight,
                oldBottom) -> updateAnimator(right - left));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        updateEverything();
        setBuildText();
    }

    private void setBuildText() {
        TextView v = findViewById(R.id.build);
        if (v == null) return;
//        modify for EJQQ-1123 by liyuchong 20191029 begin
//        if (DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(mContext)) {
//            v.setText(mContext.getString(
//                    com.android.internal.R.string.bugreport_status,
//                    Build.VERSION.RELEASE,
//                    Build.ID));
//            v.setVisibility(View.VISIBLE);
//        } else {
//            v.setVisibility(View.GONE);
//        }
        v.setVisibility(View.GONE);
//        modify for EJQQ-1123 by liyuchong 20191029 end
    }

    private void updateAnimator(int width) {
        int numTiles = QuickQSPanel.getNumQuickTiles(mContext);
        int size = mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_size)
                - mContext.getResources().getDimensionPixelSize(dimen.qs_quick_tile_padding);
        int remaining = (width - numTiles * size) / (numTiles - 1);
        int defSpace = mContext.getResources().getDimensionPixelOffset(R.dimen.default_gear_space);

        mSettingsCogAnimator = new Builder()
                .addFloat(mSettingsContainer, "translationX",
                        isLayoutRtl() ? (remaining - defSpace) : -(remaining - defSpace), 0)
                .addFloat(mSettingsButton, "rotation", -120, 0)
                .build();
        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            if (mAlarmShowing) {
                int translate = isLayoutRtl() ? mDate.getWidth() : -mDate.getWidth();
                mAlarmAnimator = new Builder().addFloat(mDate, "alpha", 1, 0)
                        .addFloat(mDateTimeGroup, "translationX", 0, translate)
                        .addFloat(mAlarmStatus, "alpha", 0, 1)
                        .setListener(new ListenerAdapter() {
                            @Override
                            public void onAnimationAtStart() {
                                mAlarmStatus.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationStarted() {
                                mAlarmStatus.setVisibility(View.VISIBLE);
                            }
                        }).build();
            } else {
                mAlarmAnimator = null;
                mAlarmStatus.setVisibility(View.GONE);
                mDate.setAlpha(1);
                mDateTimeGroup.setTranslationX(0);
            }
        }
        //add for EJQQQ-80 by liyuchong 20191204 end

        setExpansion(mExpansionAmount);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateResources();
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        updateResources();
    }

    private void updateResources() {
        updateFooterAnimator();
    }

    private void updateFooterAnimator() {
        mFooterAnimator = createFooterAnimator();
    }

    @Nullable
    private TouchAnimator createFooterAnimator() {
        //modify for EJQQQ-80 by liyuchong 20191205 begin
        /*return new TouchAnimator.Builder()
                .addFloat(mActionsContainer, "alpha", 0, 1)
                .addFloat(mEditContainer, "alpha", 0, 1)
                .addFloat(mDragHandle, "alpha", 1, 0, 0)
                .addFloat(mPageIndicator, "alpha", 0, 1)
                .setStartDelay(0.15f)
                .build();*/
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            return new TouchAnimator.Builder()
                    .addFloat(mActionsContainer, "alpha", 0, 1)
                    .addFloat(mEditContainer, "alpha", 0, 1)
                    .addFloat(mDragHandle, "alpha", 0, 0, 0)
                    .addFloat(mPageIndicator, "alpha", 0, 1)
                    .setStartDelay(0.15f)
                    .build();
        }else {
            return new TouchAnimator.Builder()
                    .addFloat(mActionsContainer, "alpha", 0, 1)
                    .addFloat(mEditContainer, "alpha", 0, 1)
                    .addFloat(mDragHandle, "alpha", 1, 0, 0)
                    .addFloat(mPageIndicator, "alpha", 0, 1)
                    .setStartDelay(0.15f)
                    .build();
        }
       //modify for EJQQQ-80 by liyuchong 20191205 end
    }

    @Override
    public void setKeyguardShowing(boolean keyguardShowing) {
        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            mKeyguardShowing = keyguardShowing;
        }
        //add for EJQQQ-80 by liyuchong 20191204 end
        setExpansion(mExpansionAmount);
    }

    @Override
    public void setExpandClickListener(OnClickListener onClickListener) {
        mExpandClickListener = onClickListener;
    }

    @Override
    public void setExpanded(boolean expanded) {
        if (mExpanded == expanded) return;
        mExpanded = expanded;
        updateEverything();
    }

    @Override
    public void setExpansion(float headerExpansionFraction) {
        mExpansionAmount = headerExpansionFraction;
        if (mSettingsCogAnimator != null) mSettingsCogAnimator.setPosition(headerExpansionFraction);

        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            if (mAlarmAnimator != null)
                mAlarmAnimator.setPosition(mKeyguardShowing ? 0 : headerExpansionFraction);
            updateAlarmVisibilities();
        }
        //add for EJQQQ-80 by liyuchong 20191204 end
        if (mFooterAnimator != null) {
            mFooterAnimator.setPosition(headerExpansionFraction);
        }
    }
    //add for EJQQQ-80 by liyuchong 20191204 begin
    @Override
    public void onNextAlarmChanged(AlarmManager.AlarmClockInfo nextAlarm) {
        mNextAlarm = nextAlarm;
        if (nextAlarm != null) {
            String alarmString = formatNextAlarm(getContext(), nextAlarm);
            mAlarmStatus.setText(alarmString);
            mAlarmStatus.setContentDescription(mContext.getString(
                    R.string.accessibility_quick_settings_alarm, alarmString));
            mAlarmStatusCollapsed.setContentDescription(mContext.getString(
                    R.string.accessibility_quick_settings_alarm, alarmString));
        }
        if (mAlarmShowing != (nextAlarm != null)) {
            mAlarmShowing = nextAlarm != null;
            updateAnimator(getWidth());
            updateEverything();
        }
    }

    private void updateAlarmVisibilities() {
        mAlarmStatusCollapsed.setVisibility(mAlarmShowing ? View.VISIBLE : View.GONE);
    }

    private String formatNextAlarm(Context context, AlarmManager.AlarmClockInfo info) {
        if (info == null) {
            return "";
        }
        String skeleton = DateFormat.is24HourFormat(context, ActivityManager.getCurrentUser())
                ? "EHm"
                : "Ehma";
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
        return DateFormat.format(pattern, info.getTriggerTime()).toString();
    }
    //add for EJQQQ-80 by liyuchong 20191204 end
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // delete for EJQQ-1123 by liyuchong 20191029 begin
        /*mContext.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED), false,
                mDeveloperSettingsObserver, UserHandle.USER_ALL);*/
        // delete for EJQQ-1123 by liyuchong 20191029 end
    }

    @Override
    @VisibleForTesting
    public void onDetachedFromWindow() {
        setListening(false);
        // delete for EJQQ-1123 by liyuchong 20191029 begin
//        mContext.getContentResolver().unregisterContentObserver(mDeveloperSettingsObserver);
        // delete for EJQQ-1123 by liyuchong 20191029 end
        super.onDetachedFromWindow();
    }

    @Override
    public void setListening(boolean listening) {
        if (listening == mListening) {
            return;
        }
        mListening = listening;
        updateListeners();
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (action == AccessibilityNodeInfo.ACTION_EXPAND) {
            if (mExpandClickListener != null) {
                mExpandClickListener.onClick(null);
                return true;
            }
        }
        return super.performAccessibilityAction(action, arguments);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
    }

    @Override
    public void disable(int state1, int state2, boolean animate) {
        final boolean disabled = (state2 & DISABLE2_QUICK_SETTINGS) != 0;
        if (disabled == mQsDisabled) return;
        mQsDisabled = disabled;
        updateEverything();
    }

    public void updateEverything() {
        post(() -> {
            updateVisibilities();
            updateClickabilities();
            setClickable(false);
        });
    }

    private void updateClickabilities() {
        mMultiUserSwitch.setClickable(mMultiUserSwitch.getVisibility() == View.VISIBLE);
        mEdit.setClickable(mEdit.getVisibility() == View.VISIBLE);
        mSettingsButton.setClickable(mSettingsButton.getVisibility() == View.VISIBLE);
    }

    private void updateVisibilities() {
        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            updateAlarmVisibilities();
        }
        //add for EJQQQ-80 by liyuchong 20191204 end
        mSettingsContainer.setVisibility(mQsDisabled ? View.GONE : View.VISIBLE);
        mSettingsContainer.findViewById(R.id.tuner_icon).setVisibility(
                TunerService.isTunerEnabled(mContext) ? View.VISIBLE : View.INVISIBLE);
        final boolean isDemo = UserManager.isDeviceInDemoMode(mContext);
        mMultiUserSwitch.setVisibility(showUserSwitcher() ? View.VISIBLE : View.INVISIBLE);
        mEditContainer.setVisibility(isDemo || !mExpanded ? View.INVISIBLE : View.VISIBLE);
        mSettingsButton.setVisibility(isDemo || !mExpanded ? View.INVISIBLE : View.VISIBLE);
    }

    private boolean showUserSwitcher() {
        return mExpanded && mMultiUserSwitch.isMultiUserEnabled();
    }

    private void updateListeners() {
        //modify for EJQQQ-80 by liyuchong 20191204 begin
        /*if (mListening) {
            mUserInfoController.addCallback(this);
        } else {
            mUserInfoController.removeCallback(this);
        }*/
        if (mListening) {
            if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
                mNextAlarmController.addCallback(this);
            }

            mUserInfoController.addCallback(this);
        } else {
            if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
                mNextAlarmController.removeCallback(this);
            }
            mUserInfoController.removeCallback(this);

        }
        //modify for EJQQQ-80 by liyuchong 20191204 end
    }

    @Override
    public void setQSPanel(final QSPanel qsPanel) {
        mQsPanel = qsPanel;
        if (mQsPanel != null) {
            mMultiUserSwitch.setQsPanel(qsPanel);
            mQsPanel.setFooterPageIndicator(mPageIndicator);
        }
    }

    @Override
    public void onClick(View v) {
        // Don't do anything until view are unhidden
        if (!mExpanded) {
            return;
        }

        if (v == mSettingsButton) {
            if (!mDeviceProvisionedController.isCurrentUserSetup()) {
                // If user isn't setup just unlock the device and dump them back at SUW.
                mActivityStarter.postQSRunnableDismissingKeyguard(() -> {
                });
                return;
            }
            MetricsLogger.action(mContext,
                    mExpanded ? MetricsProto.MetricsEvent.ACTION_QS_EXPANDED_SETTINGS_LAUNCH
                            : MetricsProto.MetricsEvent.ACTION_QS_COLLAPSED_SETTINGS_LAUNCH);
            if (mSettingsButton.isTunerClick()) {
                mActivityStarter.postQSRunnableDismissingKeyguard(() -> {
                    if (TunerService.isTunerEnabled(mContext)) {
                        TunerService.showResetRequest(mContext, () -> {
                            // Relaunch settings so that the tuner disappears.
                            startSettingsActivity();
                        });
                    } else {
                        Toast.makeText(getContext(), R.string.tuner_toast,
                                Toast.LENGTH_LONG).show();
                        TunerService.setTunerEnabled(mContext, true);
                    }
                    startSettingsActivity();

                });
            } else {
                startSettingsActivity();
            }
        }
    }

    private void startSettingsActivity() {
        mActivityStarter.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS),
                true /* dismissShade */);
    }

    @Override
    public void onUserInfoChanged(String name, Drawable picture, String userAccount) {
        if (picture != null &&
                UserManager.get(mContext).isGuestUser(KeyguardUpdateMonitor.getCurrentUser()) &&
                !(picture instanceof UserIconDrawable)) {
            picture = picture.getConstantState().newDrawable(mContext.getResources()).mutate();
            picture.setColorFilter(
                    Utils.getColorAttrDefaultColor(mContext, android.R.attr.colorForeground),
                    Mode.SRC_IN);
        }
        mMultiUserAvatar.setImageDrawable(picture);
    }
}
