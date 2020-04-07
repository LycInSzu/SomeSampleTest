/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.qs;

import static android.app.StatusBarManager.DISABLE2_QUICK_SETTINGS;

import static com.android.systemui.util.InjectionInflationController.VIEW_CONTEXT;

import android.annotation.ColorInt;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;

import com.android.settingslib.Utils;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.DualToneHandler;
import com.android.systemui.R;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver;
import com.android.systemui.qs.QSDetail.Callback;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarIconController.TintedIconManager;
import com.android.systemui.statusbar.phone.StatusIconContainer;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.DateView;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.ZenModeController;

import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import com.android.systemui.SystemUIApplication;
import android.view.ViewGroup;
//add for TEWBW-1269 by liyuchong 20200319 begin
import android.view.Gravity;
import android.widget.LinearLayout;
import static com.android.systemui.ScreenDecorations.DisplayCutoutView.boundsFromDirection;
//add for TEWBW-1269 by liyuchong 20200319 end

/**
 * View that contains the top-most bits of the screen (primarily the status bar with date, time, and
 * battery) and also contains the {@link QuickQSPanel} along with some of the panel's inner
 * contents.
 */
public class QuickStatusBarHeader extends RelativeLayout implements
        View.OnClickListener, NextAlarmController.NextAlarmChangeCallback,
        ZenModeController.Callback {
    private static final String TAG = "QuickStatusBarHeader";
    private static final boolean DEBUG = false;

    /** Delay for auto fading out the long press tooltip after it's fully visible (in ms). */
    private static final long AUTO_FADE_OUT_DELAY_MS = DateUtils.SECOND_IN_MILLIS * 6;
    private static final int FADE_ANIMATION_DURATION_MS = 300;
    private static final int TOOLTIP_NOT_YET_SHOWN_COUNT = 0;
    public static final int MAX_TOOLTIP_SHOWN_COUNT = 2;

    private final Handler mHandler = new Handler();
    private final NextAlarmController mAlarmController;
    private final ZenModeController mZenController;
    private final StatusBarIconController mStatusBarIconController;
    private final ActivityStarter mActivityStarter;

    private QSPanel mQsPanel;

    private boolean mExpanded;
    private boolean mListening;
    private boolean mQsDisabled;

    private QSCarrierGroup mCarrierGroup;
    protected QuickQSPanel mHeaderQsPanel;
    protected QSTileHost mHost;
    private TintedIconManager mIconManager;
    private TouchAnimator mStatusIconsAlphaAnimator;
    private TouchAnimator mHeaderTextContainerAlphaAnimator;
    private DualToneHandler mDualToneHandler;

    private View mSystemIconsView;
    private View mQuickQsStatusIcons;
    private View mHeaderTextContainerView;

    private int mRingerMode = AudioManager.RINGER_MODE_NORMAL;
    private AlarmManager.AlarmClockInfo mNextAlarm;

    private ImageView mNextAlarmIcon;
    /** {@link TextView} containing the actual text indicating when the next alarm will go off. */
    private TextView mNextAlarmTextView;
    private View mNextAlarmContainer;
    private View mStatusSeparator;
    private ImageView mRingerModeIcon;
    private TextView mRingerModeTextView;
    private View mRingerContainer;
    private Clock mClockView;
    private DateView mDateView;
    private BatteryMeterView mBatteryRemainingIcon;
    //add for EJQQQ-80 by liyuchong 20191204 begin
    private boolean mPortraitOrientation = true;
    //add for EJQQQ-80 by liyuchong 20191204 end
    //add for TEWBW-1269 by liyuchong 20200319 begin
    private static final int LAYOUT_NONE = 0;
    private static final int LAYOUT_CUTOUT = 1;
    private static final int LAYOUT_NO_CUTOUT = 2;
    private int mLayoutState = LAYOUT_NONE;
    private View mCutoutSpace;
    private int mCutoutSideNudge = 0;
    //add for TEWBW-1269 by liyuchong 20200319 end
    private final BroadcastReceiver mRingerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mRingerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
            updateStatusText();
        }
    };
    private boolean mHasTopCutout = false;

    @Inject
    public QuickStatusBarHeader(@Named(VIEW_CONTEXT) Context context, AttributeSet attrs,
            NextAlarmController nextAlarmController, ZenModeController zenModeController,
            StatusBarIconController statusBarIconController,
            ActivityStarter activityStarter) {
        super(context, attrs);
        mAlarmController = nextAlarmController;
        mZenController = zenModeController;
        mStatusBarIconController = statusBarIconController;
        mActivityStarter = activityStarter;
        mDualToneHandler = new DualToneHandler(
                new ContextThemeWrapper(context, R.style.QSHeaderTheme));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHeaderQsPanel = findViewById(R.id.quick_qs_panel);
        mSystemIconsView = findViewById(R.id.quick_status_bar_system_icons);
        mQuickQsStatusIcons = findViewById(R.id.quick_qs_status_icons);
        StatusIconContainer iconContainer = findViewById(R.id.statusIcons);
        //modify for EJQQQ-80 by liyuchong 20191204 begin
        // Views corresponding to the header info section (e.g. ringer and next alarm).
        //mHeaderTextContainerView = findViewById(R.id.header_text_container);
        mHeaderTextContainerView = findViewById(R.id.header_text_container);
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI||SystemUIApplication.useBluCustomUI) {
            mQuickQsStatusIcons.setVisibility(View.GONE);
            mHeaderTextContainerView.setVisibility(View.GONE);
            iconContainer = mSystemIconsView.findViewById(R.id.statusIcons);
            //add for TEWBW-1269 by liyuchong 20200319 begin
            mCutoutSpace = mSystemIconsView.findViewById(R.id.cutout_space_view);
            mCutoutSideNudge = mContext.getResources().getDimensionPixelSize(
                    R.dimen.display_cutout_margin_consumption);
            //add for TEWBW-1269 by liyuchong 20200319 end
        }else {
            //add for TEWBW-1269 by liyuchong 20200319 begin
            ((ViewGroup)mSystemIconsView).removeView((View) mSystemIconsView.findViewById(R.id.statusIconsArea));
            //add for TEWBW-1269 by liyuchong 20200319 end
            ((ViewGroup)mSystemIconsView).removeView((View) mSystemIconsView.findViewById(R.id.statusIcons));
            ((ViewGroup)mSystemIconsView).removeView((View) mSystemIconsView.findViewById(R.id.batteryRemainingIcon));
        }
        iconContainer.setShouldRestrictIcons(false);
        mIconManager = new TintedIconManager(iconContainer);
        //modify for EJQQQ-80 by liyuchong 20191204 end
        mStatusSeparator = findViewById(R.id.status_separator);
        mNextAlarmIcon = findViewById(R.id.next_alarm_icon);
        mNextAlarmTextView = findViewById(R.id.next_alarm_text);
        mNextAlarmContainer = findViewById(R.id.alarm_container);
        mNextAlarmContainer.setOnClickListener(this::onClick);
        mRingerModeIcon = findViewById(R.id.ringer_mode_icon);
        mRingerModeTextView = findViewById(R.id.ringer_mode_text);
        mRingerContainer = findViewById(R.id.ringer_container);
        mCarrierGroup = findViewById(R.id.carrier_group);

        updateResources();

        Rect tintArea = new Rect(0, 0, 0, 0);
        int colorForeground = Utils.getColorAttrDefaultColor(getContext(),
                android.R.attr.colorForeground);
        float intensity = getColorIntensity(colorForeground);
        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            colorForeground = Color.WHITE;
            intensity = 0;
        }
        //add for EJQQQ-80 by liyuchong 20191204 end
        int fillColor = mDualToneHandler.getSingleColor(intensity);

        // Set light text on the header icons because they will always be on a black background
        applyDarkness(R.id.clock, tintArea, 0, DarkIconDispatcher.DEFAULT_ICON_TINT);

        // Set the correct tint for the status icons so they contrast
        mIconManager.setTint(fillColor);
        mNextAlarmIcon.setImageTintList(ColorStateList.valueOf(fillColor));
        mRingerModeIcon.setImageTintList(ColorStateList.valueOf(fillColor));

        mClockView = findViewById(R.id.clock);
        //add for TEJWQE-424 by liyuchong 20200402 begin
        mClockView.setTintColor(fillColor);
        //add for TEJWQE-424 by liyuchong 20200402 end
        mClockView.setOnClickListener(this);
        mDateView = findViewById(R.id.date);

        // Tint for the battery icons are handled in setupHost()
        mBatteryRemainingIcon = findViewById(R.id.batteryRemainingIcon);
        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            mBatteryRemainingIcon = mSystemIconsView.findViewById(R.id.batteryRemainingIcon);
            mBatteryRemainingIcon.setIsShowingInExpandedQuickStatusBarHeader(true);
        }else if (SystemUIApplication.useBluCustomUI){
            mBatteryRemainingIcon = mSystemIconsView.findViewById(R.id.batteryRemainingIcon);
        }
        //add for EJQQQ-80 by liyuchong 20191204 end
        // Don't need to worry about tuner settings for this icon
        mBatteryRemainingIcon.setIgnoreTunerUpdates(true);
        // QS will always show the estimate, and BatteryMeterView handles the case where
        // it's unavailable or charging
        mBatteryRemainingIcon.setPercentShowMode(BatteryMeterView.MODE_ESTIMATE);
        mRingerModeTextView.setSelected(true);
        mNextAlarmTextView.setSelected(true);
    }

    //add for TEWBW-1269 by liyuchong 20200319 begin
    private boolean updateSystemIconsViewLayoutParamsNoCutout() {
        if (mLayoutState == LAYOUT_NO_CUTOUT) {
            return false;
        }
        mLayoutState = LAYOUT_NO_CUTOUT;
        if (mCutoutSpace != null) {
            mCutoutSpace.setVisibility(View.GONE);
        }
        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) mClockView.getLayoutParams();
        llp.weight = 0f;
        llp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        return true;
    }

    private boolean updateSystemIconsViewLayoutParamsForCutout(DisplayCutout dc) {
        if (mLayoutState == LAYOUT_CUTOUT) {
            return false;
        }
        mLayoutState = LAYOUT_CUTOUT;

        if (mCutoutSpace == null) {
            updateSystemIconsViewLayoutParamsNoCutout();
        }

        Rect bounds = new Rect();
        boundsFromDirection(dc, Gravity.TOP, bounds);
        mCutoutSpace.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mCutoutSpace.getLayoutParams();
        bounds.left = bounds.left + mCutoutSideNudge;
        bounds.right = bounds.right - mCutoutSideNudge;
        lp.width = bounds.width();
        lp.height = bounds.height();
        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) mClockView.getLayoutParams();
        llp.width = 0;
        llp.weight = 1f;
        return true;
    }
    //add for TEWBW-1269 by liyuchong 20200319 end

    private void updateStatusText() {
        boolean changed = updateRingerStatus() || updateAlarmStatus();

        if (changed) {
            boolean alarmVisible = mNextAlarmTextView.getVisibility() == View.VISIBLE;
            boolean ringerVisible = mRingerModeTextView.getVisibility() == View.VISIBLE;
            mStatusSeparator.setVisibility(alarmVisible && ringerVisible ? View.VISIBLE
                    : View.GONE);
        }
    }

    private boolean updateRingerStatus() {
        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI||SystemUIApplication.useBluCustomUI) {
            return false;
        }
        //add for EJQQQ-80 by liyuchong 20191204 end
        boolean isOriginalVisible = mRingerModeTextView.getVisibility() == View.VISIBLE;
        CharSequence originalRingerText = mRingerModeTextView.getText();

        boolean ringerVisible = false;
        if (!ZenModeConfig.isZenOverridingRinger(mZenController.getZen(),
                mZenController.getConsolidatedPolicy())) {
            if (mRingerMode == AudioManager.RINGER_MODE_VIBRATE) {
                mRingerModeIcon.setImageResource(R.drawable.ic_volume_ringer_vibrate);
                mRingerModeTextView.setText(R.string.qs_status_phone_vibrate);
                ringerVisible = true;
            } else if (mRingerMode == AudioManager.RINGER_MODE_SILENT) {
                mRingerModeIcon.setImageResource(R.drawable.ic_volume_ringer_mute);
                mRingerModeTextView.setText(R.string.qs_status_phone_muted);
                ringerVisible = true;
            }
        }
        mRingerModeIcon.setVisibility(ringerVisible ? View.VISIBLE : View.GONE);
        mRingerModeTextView.setVisibility(ringerVisible ? View.VISIBLE : View.GONE);
        mRingerContainer.setVisibility(ringerVisible ? View.VISIBLE : View.GONE);

        return isOriginalVisible != ringerVisible ||
                !Objects.equals(originalRingerText, mRingerModeTextView.getText());
    }

    private boolean updateAlarmStatus() {
        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI||SystemUIApplication.useBluCustomUI) {
            return false;
        }
        //add for EJQQQ-80 by liyuchong 20191204 end
        boolean isOriginalVisible = mNextAlarmTextView.getVisibility() == View.VISIBLE;
        CharSequence originalAlarmText = mNextAlarmTextView.getText();

        boolean alarmVisible = false;
        if (mNextAlarm != null) {
            alarmVisible = true;
            mNextAlarmTextView.setText(formatNextAlarm(mNextAlarm));
        }
        mNextAlarmIcon.setVisibility(alarmVisible ? View.VISIBLE : View.GONE);
        mNextAlarmTextView.setVisibility(alarmVisible ? View.VISIBLE : View.GONE);
        mNextAlarmContainer.setVisibility(alarmVisible ? View.VISIBLE : View.GONE);

        return isOriginalVisible != alarmVisible ||
                !Objects.equals(originalAlarmText, mNextAlarmTextView.getText());
    }

    private void applyDarkness(int id, Rect tintArea, float intensity, int color) {
        View v = findViewById(id);
        if (v instanceof DarkReceiver) {
            ((DarkReceiver) v).onDarkChanged(tintArea, intensity, color);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //add for EJQQQ-80 by liyuchong 20191204 begin
        mPortraitOrientation = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        //add for EJQQQ-80 by liyuchong 20191204 end
        updateResources();

        // Update color schemes in landscape to use wallpaperTextColor
        boolean shouldUseWallpaperTextColor =
                newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        mClockView.useWallpaperTextColor(shouldUseWallpaperTextColor);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        updateResources();
    }

    /**
     * The height of QQS should always be the status bar height + 128dp. This is normally easy, but
     * when there is a notch involved the status bar can remain a fixed pixel size.
     */
    private void updateMinimumHeight() {
        int sbHeight = mContext.getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_height);
        int qqsHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.qs_quick_header_panel_height);

        setMinimumHeight(sbHeight + qqsHeight);
    }

    private void updateResources() {
        Resources resources = mContext.getResources();
        updateMinimumHeight();
        //modify for EJQQQ-80 by liyuchong 20191204 begin
//        // Update height for a few views, especially due to landscape mode restricting space.
//        mHeaderTextContainerView.getLayoutParams().height =
//                resources.getDimensionPixelSize(R.dimen.qs_header_tooltip_height);
//        mHeaderTextContainerView.setLayoutParams(mHeaderTextContainerView.getLayoutParams());
//
//        mSystemIconsView.getLayoutParams().height = resources.getDimensionPixelSize(
//                com.android.internal.R.dimen.quick_qs_offset_height);
//        mSystemIconsView.setLayoutParams(mSystemIconsView.getLayoutParams());

        if (SystemUIApplication.useDgCustomUI) {
            RelativeLayout.LayoutParams rLayoutParams = (RelativeLayout.LayoutParams) mSystemIconsView.getLayoutParams();
            rLayoutParams.height = resources.getDimensionPixelSize(
                    com.android.internal.R.dimen.quick_qs_offset_height);
            //add for TEWBW-993 by liyuchong 20200303 begin
            rLayoutParams.leftMargin = !mPortraitOrientation ? mContext.getResources().getDimensionPixelSize(R.dimen.notification_side_paddings) : 0;
            rLayoutParams.rightMargin = !mPortraitOrientation ? mContext.getResources().getDimensionPixelSize(R.dimen.notification_side_paddings) : 0;
            //add for TEWBW-993 by liyuchong 20200303 end
            mSystemIconsView.setLayoutParams(rLayoutParams);

            LayoutParams layoutParams = (LayoutParams) mHeaderQsPanel.getLayoutParams();
            layoutParams.topMargin =mContext.getResources().getDimensionPixelSize(R.dimen.qs_footer_height);
            mHeaderQsPanel.setLayoutParams(layoutParams);
            //add for TEWBW-1269 by liyuchong 20200319 begin
            mCutoutSideNudge = mContext.getResources().getDimensionPixelSize(
                    R.dimen.display_cutout_margin_consumption);
            //add for TEWBW-1269 by liyuchong 20200319 end
            //add for EWSWQ-275 by liyuchong 20200224 begin
        }else if(SystemUIApplication.useDkCustomUI||SystemUIApplication.useBluCustomUI){
            RelativeLayout.LayoutParams rLayoutParams = (RelativeLayout.LayoutParams) mSystemIconsView.getLayoutParams();
            rLayoutParams.height = resources.getDimensionPixelSize(
                    com.android.internal.R.dimen.quick_qs_offset_height);
            rLayoutParams.leftMargin = !mPortraitOrientation ? mContext.getResources().getDimensionPixelSize(R.dimen.notification_side_paddings) : 0;
            rLayoutParams.rightMargin = !mPortraitOrientation ? mContext.getResources().getDimensionPixelSize(R.dimen.notification_side_paddings) : 0;
            mSystemIconsView.setLayoutParams(rLayoutParams);

            LayoutParams layoutParams = (LayoutParams) mHeaderQsPanel.getLayoutParams();
            layoutParams.topMargin =mContext.getResources().getDimensionPixelSize(R.dimen.qs_tile_margin_top_bottom);
            mHeaderQsPanel.setLayoutParams(layoutParams);
            //add for EWSWQ-275 by liyuchong 20200224 end
            //add for TEWBW-1269 by liyuchong 20200319 begin
            mCutoutSideNudge = mContext.getResources().getDimensionPixelSize(
                    R.dimen.display_cutout_margin_consumption);
            //add for TEWBW-1269 by liyuchong 20200319 end
        } else {
            // Update height for a few views, especially due to landscape mode restricting space.
            mHeaderTextContainerView.getLayoutParams().height =
                    resources.getDimensionPixelSize(R.dimen.qs_header_tooltip_height);
            mHeaderTextContainerView.setLayoutParams(mHeaderTextContainerView.getLayoutParams());

            mSystemIconsView.getLayoutParams().height = resources.getDimensionPixelSize(
                    com.android.internal.R.dimen.quick_qs_offset_height);
            mSystemIconsView.setLayoutParams(mSystemIconsView.getLayoutParams());
        }
        //modify for EJQQQ-80 by liyuchong 20191204 end
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();
        if (mQsDisabled) {
            lp.height = resources.getDimensionPixelSize(
                    com.android.internal.R.dimen.quick_qs_offset_height);
        } else {
            //modify for TEWBW-993 by liyuchong 20200303 begin
            /*lp.height = Math.max(getMinimumHeight(),
                    resources.getDimensionPixelSize(
                            com.android.internal.R.dimen.quick_qs_total_height));*/
            if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useBluCustomUI){
                lp.height = Math.max(getMinimumHeight(),
                        resources.getDimensionPixelSize(
                                R.dimen.quick_qs_total_height_dg));
            }else {
                lp.height = Math.max(getMinimumHeight(),
                        resources.getDimensionPixelSize(
                                com.android.internal.R.dimen.quick_qs_total_height));
            }
            //modify for TEWBW-993 by liyuchong 20200303 end
        }
        setLayoutParams(lp);

        updateStatusIconAlphaAnimator();
        updateHeaderTextContainerAlphaAnimator();
    }

    private void updateStatusIconAlphaAnimator() {
        mStatusIconsAlphaAnimator = new TouchAnimator.Builder()
                .addFloat(mQuickQsStatusIcons, "alpha", 1, 0, 0)
                .build();
    }

    private void updateHeaderTextContainerAlphaAnimator() {
        mHeaderTextContainerAlphaAnimator = new TouchAnimator.Builder()
                .addFloat(mHeaderTextContainerView, "alpha", 0, 0, 1)
                .build();
    }

    public void setExpanded(boolean expanded) {
        if (mExpanded == expanded) return;
        mExpanded = expanded;
        mHeaderQsPanel.setExpanded(expanded);
        updateEverything();
    }

    /**
     * Animates the inner contents based on the given expansion details.
     *
     * @param isKeyguardShowing whether or not we're showing the keyguard (a.k.a. lockscreen)
     * @param expansionFraction how much the QS panel is expanded/pulled out (up to 1f)
     * @param panelTranslationY how much the panel has physically moved down vertically (required
     *                          for keyguard animations only)
     */
    public void setExpansion(boolean isKeyguardShowing, float expansionFraction,
                             float panelTranslationY) {
        final float keyguardExpansionFraction = isKeyguardShowing ? 1f : expansionFraction;
        if (mStatusIconsAlphaAnimator != null) {
            mStatusIconsAlphaAnimator.setPosition(keyguardExpansionFraction);
        }
        //modify for EJQQQ-80 by liyuchong 20191204 begin
        if (!SystemUIApplication.useDgCustomUI&&!SystemUIApplication.useDkCustomUI&&!SystemUIApplication.useBluCustomUI) {
            if (isKeyguardShowing) {
                // If the keyguard is showing, we want to offset the text so that it comes in at the
                // same time as the panel as it slides down.
                mHeaderTextContainerView.setTranslationY(panelTranslationY);
            } else {
                mHeaderTextContainerView.setTranslationY(0f);
            }

            if (mHeaderTextContainerAlphaAnimator != null) {
                mHeaderTextContainerAlphaAnimator.setPosition(keyguardExpansionFraction);
                if (keyguardExpansionFraction > 0) {
                    mHeaderTextContainerView.setVisibility(VISIBLE);
                } else {
                    mHeaderTextContainerView.setVisibility(INVISIBLE);
                }
            }
        }

    }
        //modify for EJQQQ-80 by liyuchong 20191204 end
    public void disable(int state1, int state2, boolean animate) {
        final boolean disabled = (state2 & DISABLE2_QUICK_SETTINGS) != 0;
        if (disabled == mQsDisabled) return;
        mQsDisabled = disabled;
        mHeaderQsPanel.setDisabledByPolicy(disabled);
        //modify for EJQQQ-80 by liyuchong 20191204 begin
        //mHeaderTextContainerView.setVisibility(mQsDisabled ? View.GONE : View.VISIBLE);
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI||SystemUIApplication.useBluCustomUI) {
            mHeaderTextContainerView.setVisibility(mQsDisabled ? View.GONE : View.GONE);
        } else {
            mHeaderTextContainerView.setVisibility(mQsDisabled ? View.GONE : View.VISIBLE);
        }
        //modify for EJQQQ-80 by liyuchong 20191204 end
        mQuickQsStatusIcons.setVisibility(mQsDisabled ? View.GONE : View.VISIBLE);
        updateResources();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mStatusBarIconController.addIconGroup(mIconManager);
        requestApplyInsets();
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        //add for TEWBW-1269 by liyuchong 20200319 begin
        mLayoutState = LAYOUT_NONE;
        //add for TEWBW-1269 by liyuchong 20200319 end
        DisplayCutout cutout = insets.getDisplayCutout();
        Pair<Integer, Integer> padding = PhoneStatusBarView.cornerCutoutMargins(
                cutout, getDisplay());
        if (padding == null) {
            mSystemIconsView.setPaddingRelative(
                    getResources().getDimensionPixelSize(R.dimen.status_bar_padding_start), 0,
                    getResources().getDimensionPixelSize(R.dimen.status_bar_padding_end), 0);
        } else {
            mSystemIconsView.setPadding(padding.first, 0, padding.second, 0);
        }
        //add for TEWBW-1269 by liyuchong 20200319 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI||SystemUIApplication.useBluCustomUI) {
            if (updateLayoutConsideringCutout(cutout, padding)) {
                requestLayout();
            }
        }
        //add for TEWBW-1269 by liyuchong 20200319 end
        return super.onApplyWindowInsets(insets);
    }
    //add for TEWBW-1269 by liyuchong 20200319 begin
    private boolean updateLayoutConsideringCutout(DisplayCutout cutout, Pair<Integer, Integer> padding) {
        if (cutout == null || padding != null) {
            return updateSystemIconsViewLayoutParamsNoCutout();
        } else {
            return updateSystemIconsViewLayoutParamsForCutout(cutout);
        }
    }
    //add for TEWBW-1269 by liyuchong 20200319 end

    @Override
    @VisibleForTesting
    public void onDetachedFromWindow() {
        setListening(false);
        mStatusBarIconController.removeIconGroup(mIconManager);
        super.onDetachedFromWindow();
    }

    public void setListening(boolean listening) {
        if (listening == mListening) {
            return;
        }
        mHeaderQsPanel.setListening(listening);
        mListening = listening;
        mCarrierGroup.setListening(mListening);

        if (listening) {
            mZenController.addCallback(this);
            mAlarmController.addCallback(this);
            mContext.registerReceiver(mRingerReceiver,
                    new IntentFilter(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION));
        } else {
            mZenController.removeCallback(this);
            mAlarmController.removeCallback(this);
            mContext.unregisterReceiver(mRingerReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mClockView) {
            mActivityStarter.postStartActivityDismissingKeyguard(new Intent(
                    AlarmClock.ACTION_SHOW_ALARMS), 0);
        } else if (v == mNextAlarmContainer && mNextAlarmContainer.isVisibleToUser()) {
            if (mNextAlarm.getShowIntent() != null) {
                mActivityStarter.postStartActivityDismissingKeyguard(
                        mNextAlarm.getShowIntent());
            } else {
                Log.d(TAG, "No PendingIntent for next alarm. Using default intent");
                mActivityStarter.postStartActivityDismissingKeyguard(new Intent(
                        AlarmClock.ACTION_SHOW_ALARMS), 0);
            }
        } else if (v == mRingerContainer && mRingerContainer.isVisibleToUser()) {
            mActivityStarter.postStartActivityDismissingKeyguard(new Intent(
                    Settings.ACTION_SOUND_SETTINGS), 0);
        }
    }

    @Override
    public void onNextAlarmChanged(AlarmManager.AlarmClockInfo nextAlarm) {
        mNextAlarm = nextAlarm;
        updateStatusText();
    }

    @Override
    public void onZenChanged(int zen) {
        updateStatusText();
    }

    @Override
    public void onConfigChanged(ZenModeConfig config) {
        updateStatusText();
    }

    public void updateEverything() {
        post(() -> setClickable(!mExpanded));
    }

    public void setQSPanel(final QSPanel qsPanel) {
        mQsPanel = qsPanel;
        setupHost(qsPanel.getHost());
    }

    public void setupHost(final QSTileHost host) {
        mHost = host;
        //host.setHeaderView(mExpandIndicator);
        mHeaderQsPanel.setQSPanelAndHeader(mQsPanel, this);
        mHeaderQsPanel.setHost(host, null /* No customization in header */);

        Rect tintArea = new Rect(0, 0, 0, 0);
        int colorForeground = Utils.getColorAttrDefaultColor(getContext(),
                android.R.attr.colorForeground);
        float intensity = getColorIntensity(colorForeground);
        //add for EJQQQ-80 by liyuchong 20191204 begin
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            colorForeground = Color.WHITE;
            intensity = 0;
        }
        //add for EJQQQ-80 by liyuchong 20191204 end
        int fillColor = mDualToneHandler.getSingleColor(intensity);
        mBatteryRemainingIcon.onDarkChanged(tintArea, intensity, fillColor);
    }

    public void setCallback(Callback qsPanelCallback) {
        mHeaderQsPanel.setCallback(qsPanelCallback);
    }

    private String formatNextAlarm(AlarmManager.AlarmClockInfo info) {
        if (info == null) {
            return "";
        }
        String skeleton = android.text.format.DateFormat
                .is24HourFormat(mContext, ActivityManager.getCurrentUser()) ? "EHm" : "Ehma";
        String pattern = android.text.format.DateFormat
                .getBestDateTimePattern(Locale.getDefault(), skeleton);
        return android.text.format.DateFormat.format(pattern, info.getTriggerTime()).toString();
    }

    public static float getColorIntensity(@ColorInt int color) {
        return color == Color.WHITE ? 0 : 1;
    }

    public void setMargins(int sideMargins) {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            // Prevents these views from getting set a margin.
            // The Icon views all have the same padding set in XML to be aligned.
            if (v == mSystemIconsView || v == mQuickQsStatusIcons || v == mHeaderQsPanel
                    || v == mHeaderTextContainerView) {
                continue;
            }
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v.getLayoutParams();
            lp.leftMargin = sideMargins;
            lp.rightMargin = sideMargins;
        }
    }
}
