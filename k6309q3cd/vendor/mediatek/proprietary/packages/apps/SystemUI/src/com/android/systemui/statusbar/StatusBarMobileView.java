/*
 * Copyright (C) 2018 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import static com.android.systemui.statusbar.StatusBarIconView.STATE_DOT;
import static com.android.systemui.statusbar.StatusBarIconView.STATE_HIDDEN;
import static com.android.systemui.statusbar.StatusBarIconView.STATE_ICON;
import static com.android.systemui.statusbar.policy.DarkIconDispatcher.getTint;
import static com.android.systemui.statusbar.policy.DarkIconDispatcher.isInArea;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
//prize add by liyuchong, add sim card number for condor ,20190124-begin
import android.widget.TextView;
//prize add by liyuchong, add sim card number for condor ,20190124-end
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.settingslib.graph.SignalDrawable;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy.MobileIconState;
import com.android.systemui.statusbar.policy.DarkIconDispatcher.DarkReceiver;

import com.mediatek.systemui.ext.ISystemUIStatusBarExt;
import com.mediatek.systemui.ext.OpSystemUICustomizationFactoryBase;
import com.mediatek.systemui.statusbar.util.FeatureOptions;
//PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-08-start
import com.android.systemui.PrizeSystemUIOption;
import com.android.systemui.statusbar.phone.LiuHaiStatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarIconControllerImpl;
//PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-08-end

//prize add by xiarui PRIZE_NETWORK_STYLE for bug66854 & bug67636 2018-11-03 start
import android.telephony.TelephonyManager;
import com.android.systemui.statusbar.policy.TelephonyIcons;
//prize add by xiarui PRIZE_NETWORK_STYLE for bug66854 & bug67636 2018-11-03 end

public class StatusBarMobileView extends FrameLayout implements DarkReceiver,
        StatusIconDisplayable {
    private static final String TAG = "StatusBarMobileView";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG)
            || FeatureOptions.LOG_ENABLE;

    /// Used to show etc dots
    private StatusBarIconView mDotView;
    /// The main icon view
    private LinearLayout mMobileGroup;
    private String mSlot;
    private MobileIconState mState;
    private SignalDrawable mMobileDrawable;
    private View mInoutContainer;
    private ImageView mIn;
    private ImageView mOut;
    //prize add by xiarui PRIZE_NETWORK_STYLE for bug66854 & bug67636 2018-11-03 start
    private ImageView mInOut;
    //prize add by xiarui PRIZE_NETWORK_STYLE for bug66854 & bug67636 2018-11-03 end
    private ImageView mMobile, mMobileType, mMobileRoaming;
    private int mVisibleState = -1;
    /// M: Add for new features @{
    // Add for [Network Type and volte on Statusbar]
    private ImageView mNetworkType;
    private ImageView mVolteType;
    /// @}
    /// M: for vowifi
    private boolean mIsWfcEnable;
    private boolean mIsWfcCase;
//prize add by liyuchong, add sim card number for condor ,20190124-begin
    private TextView mSlotIndicatorText;
//prize add by liyuchong, add sim card number for condor ,20190124-end
    /// M: Add for Plugin feature @ {
    private ISystemUIStatusBarExt mStatusBarExt;
    /// @ }

    public static StatusBarMobileView fromContext(Context context, String slot) {
        LayoutInflater inflater = LayoutInflater.from(context);
        StatusBarMobileView v = (StatusBarMobileView)
                inflater.inflate(R.layout.status_bar_mobile_signal_group, null);

        v.setSlot(slot);
        v.init();
        v.setVisibleState(STATE_ICON);
        return v;
    }

    public StatusBarMobileView(Context context) {
        super(context);
    }

    public StatusBarMobileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusBarMobileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StatusBarMobileView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void getDrawingRect(Rect outRect) {
        super.getDrawingRect(outRect);
        float translationX = getTranslationX();
        float translationY = getTranslationY();
        outRect.left += translationX;
        outRect.right += translationX;
        outRect.top += translationY;
        outRect.bottom += translationY;
    }

    private void init() {
	//prize add by liyuchong, add sim card number for condor ,20190124-begin
        mSlotIndicatorText = findViewById(R.id.mobile_slot_indicator_text);
	//prize add by liyuchong, add sim card number for condor ,20190124-end
        mMobileGroup = findViewById(R.id.mobile_group);
        mMobile = findViewById(R.id.mobile_signal);
        mMobileType = findViewById(R.id.mobile_type);
        mMobileRoaming = findViewById(R.id.mobile_roaming);
        mIn = findViewById(R.id.mobile_in);
        mOut = findViewById(R.id.mobile_out);
        //prize add by xiarui for bug66854 & bug67636 2018-11-03 start
        if (PrizeSystemUIOption.PRIZE_NETWORK_STYLE) {
            mInOut = findViewById(R.id.mobile_inout);
        }
        //prize add by xiarui for bug66854 & bug67636 2018-11-03 end
        mInoutContainer = findViewById(R.id.inout_container);
        /// M: Add for [Network Type and volte on Statusbar] @{
        mNetworkType    = findViewById(R.id.network_type);
        mVolteType      = findViewById(R.id.volte_indicator_ext);
        /// @}

        /*prize delete zhaojian 20180906 start*/
//        mMobileDrawable = new SignalDrawable(getContext());
//        mMobile.setImageDrawable(mMobileDrawable);
        /*prize delete zhaojian 20180906 end*/

        initDotView();

        mIsWfcEnable = SystemProperties.get("persist.vendor.mtk_wfc_support").equals("1");

        /// M: Add for Plugin feature @ {
        mStatusBarExt = OpSystemUICustomizationFactoryBase.getOpFactory(mContext)
                                     .makeSystemUIStatusBar(mContext);
        /// @ }
        //PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-08-start
		if(PrizeSystemUIOption.OPEN_LIUHAI_SCREEN){
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mMobile.getLayoutParams();
			params.gravity = Gravity.BOTTOM;
			mMobile.setLayoutParams(params);

			//int liuhaiSpace = mContext.getResources().getDimensionPixelSize(R.dimen.liuhai_status_bar_space);
		}
        //PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-08-end
    }

    private void initDotView() {
        mDotView = new StatusBarIconView(mContext, mSlot, null);
        mDotView.setVisibleState(STATE_DOT);

        int width = mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_size);
        LayoutParams lp = new LayoutParams(width, width);
        lp.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        addView(mDotView, lp);
    }

    public void applyMobileState(MobileIconState state) {
        if (DEBUG) {
            Log.d(getMobileTag(), "[" + this.hashCode() + "][visibility=" + getVisibility()
                + "] applyMobileState: state = " + state);
        }
        if (state == null) {
            setVisibility(View.GONE);
            mState = null;
            return;
        }

        if (mState == null) {
            mState = state.copy();
            initViewState();
            return;
        }

        if (!mState.equals(state)) {
            updateState(state.copy());
        }
		//prize add by liyuchong, add sim card number for condor ,20190124-begin
        updateSlotIndicatorVisibility(state);
		//prize add by liyuchong, add sim card number for condor ,20190124-end
    }

    private void initViewState() {
        setContentDescription(mState.contentDescription);
        if (!mState.visible) {
            mMobileGroup.setVisibility(View.GONE);
        } else {
            mMobileGroup.setVisibility(View.VISIBLE);
        }
        /*prize modify zhaojian 20180906 start*/
        //mMobileDrawable.setLevel(mState.strengthId);
        if (mState.strengthId != 0) {
            mMobile.setImageDrawable(getResources().getDrawable(mState.strengthId));
        }
        /*prize modify zhaojian 20180906 end*/
        if (mState.typeId > 0) {
            if (!mStatusBarExt.disableHostFunction()) {
                mMobileType.setContentDescription(mState.typeContentDescription);
                mMobileType.setImageResource(mState.typeId);
            }
            //prize add by xiarui for bug66854 & bug67636 2018-11-03 start
            if (PrizeSystemUIOption.PRIZE_NETWORK_STYLE) {
                mMobileType.setVisibility(View.GONE);
            } else {
                mMobileType.setVisibility(View.VISIBLE);
            }
            //prize add by xiarui for bug66854 & bug67636 2018-11-03 end
        } else {
                mMobileType.setVisibility(View.GONE);
        }

        mMobileRoaming.setVisibility(mState.roaming ? View.VISIBLE : View.GONE);

        //prize add by xiarui for bug66854 & bug67636 2018-11-03 start
        if (PrizeSystemUIOption.PRIZE_NETWORK_STYLE) {
            mIn.setVisibility(View.GONE);
            mOut.setVisibility(View.GONE);
            mInoutContainer.setVisibility(View.GONE);
            //prize add by xiarui for bug68143 bug68121 2018-11-14 start
            //mInOut.setVisibility((mState.activityIn || mState.activityOut)
            //        ? View.VISIBLE : View.GONE);
            mInOut.setVisibility(mState.showInOutIcon ? View.VISIBLE : View.GONE);
            //prize add by xiarui for bug68143 bug68121 2018-11-14 end
            int index = 0;
            if (mState.activityIn) {
                index |= TelephonyManager.DATA_ACTIVITY_IN;
            }
            if (mState.activityOut) {
                index |= TelephonyManager.DATA_ACTIVITY_OUT;
            }
            mInOut.setImageResource(TelephonyIcons.DATA_ACTIVITY[index]);
        } else {
            mIn.setVisibility(mState.activityIn ? View.VISIBLE : View.GONE);
            mOut.setVisibility(mState.activityIn ? View.VISIBLE : View.GONE);
            mInoutContainer.setVisibility((mState.activityIn || mState.activityOut)
                    ? View.VISIBLE : View.GONE);
        }
        //prize add by xiarui for bug66854 & bug67636 2018-11-03 end

        /// M: Add for [Network Type and volte on Statusbar] @{
        setCustomizeViewProperty();
        /// @}

        showWfcIfAirplaneMode();

        /// M: Add data group for plugin feature. @ {
        mStatusBarExt.addCustomizedView(mState.subId, mContext, mMobileGroup);
        setCustomizedOpViews();
        /// @ }
    }

    private void updateState(MobileIconState state) {
        setContentDescription(state.contentDescription);
        if (mState.visible != state.visible) {
            mMobileGroup.setVisibility(state.visible ? View.VISIBLE : View.GONE);
            // To avoid StatusBarMobileView will not show in extreme case,
            // force request layout once if visible state changed.
            requestLayout();
        }
        if (mState.strengthId != state.strengthId) {
            /*prize modify zhaojian 20180906 start*/
            //mMobileDrawable.setLevel(state.strengthId);
            if (state.strengthId != 0) {
                mMobile.setImageDrawable(getResources().getDrawable(state.strengthId));
            }
            /*prize modify zhaojian 20180906 end*/
        }
        if (mState.typeId != state.typeId) {
            if (state.typeId != 0) {
                if (!mStatusBarExt.disableHostFunction()) {
                    mMobileType.setContentDescription(state.typeContentDescription);
                    mMobileType.setImageResource(state.typeId);
                }
                //prize add by xiarui for bug66854 & bug67636 2018-11-03 start
                if (PrizeSystemUIOption.PRIZE_NETWORK_STYLE) {
                    mMobileType.setVisibility(View.GONE);
                } else {
                    mMobileType.setVisibility(View.VISIBLE);
                }
                //prize add by xiarui for bug66854 & bug67636 2018-11-03 end
            } else {
                mMobileType.setVisibility(View.GONE);
            }
        }

        mMobileRoaming.setVisibility(state.roaming ? View.VISIBLE : View.GONE);

        //prize add by xiarui for bug66854 & bug67636 2018-11-03 start
        if (PrizeSystemUIOption.PRIZE_NETWORK_STYLE) {
            mIn.setVisibility(View.GONE);
            mOut.setVisibility(View.GONE);
            mInoutContainer.setVisibility(View.GONE);
            //prize add by xiarui for bug68143 bug68121 2018-11-14 start
            //mInOut.setVisibility((state.activityIn || state.activityOut)
            //        ? View.VISIBLE : View.GONE);
            mInOut.setVisibility(state.showInOutIcon ? View.VISIBLE : View.GONE);
            //prize add by xiarui for bug68143 bug68121 2018-11-14 end
            int index = 0;
            if (state.activityIn) {
                index |= TelephonyManager.DATA_ACTIVITY_IN;
            }
            if (state.activityOut) {
                index |= TelephonyManager.DATA_ACTIVITY_OUT;
            }
            mInOut.setImageResource(TelephonyIcons.DATA_ACTIVITY[index]);
        } else {
            mIn.setVisibility(state.activityIn ? View.VISIBLE : View.GONE);
            mOut.setVisibility(state.activityIn ? View.VISIBLE : View.GONE);
            mInoutContainer.setVisibility((state.activityIn || state.activityOut)
                    ? View.VISIBLE : View.GONE);
        }
        //prize add by xiarui for bug66854 & bug67636 2018-11-03 end

        /// M: Add for [Network Type and volte on Statusbar] @{
        if (mState.networkIcon != state.networkIcon) {
            setNetworkIcon(state.networkIcon);
            // if network icon change to LTE, need to update dis volte icon.
            mStatusBarExt.setDisVolteView(mState.subId, state.volteIcon, mVolteType);
        }
        //PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-10-start
		if(PrizeSystemUIOption.OPEN_LIUHAI_SCREEN){
			setVolteIcon(state.volteIcon);
		} else
        //PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-10-end
        if (mState.volteIcon != state.volteIcon) {
            setVolteIcon(state.volteIcon);
        }
        /// @}

        mState = state;
        // should added after set mState
        showWfcIfAirplaneMode();
        setCustomizedOpViews();

        //PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-08-start
		if(PrizeSystemUIOption.OPEN_LIUHAI_SCREEN){
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mMobile.getLayoutParams();
			params.gravity = Gravity.BOTTOM;
			mMobile.setLayoutParams(params);

			//int liuhaiSpace = mContext.getResources().getDimensionPixelSize(R.dimen.liuhai_status_bar_space);
		}
        //PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-08-end
    }

    @Override
    public void onDarkChanged(Rect area, float darkIntensity, int tint) {
        /*prize delete by liufan, for bugid:67680 20181120 start*/
        /*if (!isInArea(area, this)) {
            return;
        }*/
        /*prize delete by liufan, for bugid:67680 20181120 end*/
        /*prize delete zhaojian 20180906 start*/
//        mMobileDrawable.setDarkIntensity(darkIntensity);
        /*prize delete zhaojian 20180906 end*/


        ColorStateList color = ColorStateList.valueOf(getTint(area, this, tint));
        mIn.setImageTintList(color);
        mOut.setImageTintList(color);
        //prize add by xiarui for bug66854 & bug67636 2018-11-03 start
        if (PrizeSystemUIOption.PRIZE_NETWORK_STYLE) {
            mInOut.setImageTintList(color);
        }
        //prize add by xiarui for bug66854 & bug67636 2018-11-03 end
        mMobileType.setImageTintList(color);
        mMobileRoaming.setImageTintList(color);
        mNetworkType.setImageTintList(color);
        mVolteType.setImageTintList(color);
        mDotView.setDecorColor(tint);
        mDotView.setIconColor(tint, false);
        mMobile.setImageTintList(color);
        /// M: Add for plugin items tint handling. @{
        mStatusBarExt.setCustomizedPlmnTextTint(tint);
        mStatusBarExt.setIconTint(color);
		//prize add by liyuchong, add sim card number for condor ,20190124-begin
        mSlotIndicatorText.setTextColor(color);
		//prize add by liyuchong, add sim card number for condor ,20190124-end
        /// @}
    }

    @Override
    public String getSlot() {
        return mSlot;
    }

    public void setSlot(String slot) {
        mSlot = slot;
    }

    @Override
    public void setStaticDrawableColor(int color) {
        ColorStateList list = ColorStateList.valueOf(color);
        float intensity = color == Color.WHITE ? 0 : 1;
        /*prize delete zhaojian 20180906 start*/
//        mMobileDrawable.setDarkIntensity(intensity);
        /*prize delete zhaojian 20180906 end*/
		//prize add by liyuchong, change mobile data inout icon color  ,20190213-begin
        if (PrizeSystemUIOption.PRIZE_NETWORK_STYLE) {
            mInOut.setImageTintList(list);
        }
		//prize add by liyuchong, change mobile data inout icon color  ,20190213-end
        mIn.setImageTintList(list);
        mOut.setImageTintList(list);
        mMobileType.setImageTintList(list);
        mMobileRoaming.setImageTintList(list);
        mNetworkType.setImageTintList(list);
        mVolteType.setImageTintList(list);
        mDotView.setDecorColor(color);
        mMobile.setImageTintList(list);
        /// M: Add for plugin items tint handling. @{
        mStatusBarExt.setCustomizedPlmnTextTint(color);
        mStatusBarExt.setIconTint(list);
        /// @}
		//prize add by liyuchong, add sim card number for condor ,20190213-begin
        mSlotIndicatorText.setTextColor(color);
        //prize add by liyuchong, add sim card number for condor ,20190213-end
    }

    @Override
    public void setDecorColor(int color) {
        mDotView.setDecorColor(color);
    }

    @Override
    public boolean isIconVisible() {
        return mState.visible || needShowWfcInAirplaneMode();
    }

    @Override
    public void setVisibleState(int state) {
        if (state == mVisibleState) {
            return;
        }

        mVisibleState = state;
        switch (state) {
            case STATE_ICON:
                mMobileGroup.setVisibility(View.VISIBLE);
                mDotView.setVisibility(View.GONE);
                break;
            case STATE_DOT:
                mMobileGroup.setVisibility(View.INVISIBLE);
                mDotView.setVisibility(View.VISIBLE);
                break;
            case STATE_HIDDEN:
            default:
                mMobileGroup.setVisibility(View.INVISIBLE);
                mDotView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public int getVisibleState() {
        return mVisibleState;
    }

    @VisibleForTesting
    public MobileIconState getState() {
        return mState;
    }

    @Override
    public String toString() {
        return "StatusBarMobileView(slot=" + mSlot + ", hash=" + this.hashCode() + ", state=" + mState + ")";
    }

    /// M: Set all added or customised view. @ {
    private void setCustomizeViewProperty() {
        // Add for [Network Type on Statusbar], the place to set network type icon.
        setNetworkIcon(mState.networkIcon);
        /// M: Add for volte icon.
        setVolteIcon(mState.volteIcon);
    }

	//PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-09-start
    public LiuHaiStatusBarIconController mLiuHaiStatusBarIconController;
    private LiuHaiStatusBarIconController.ShowOnLeftListenerImpI showOnLeftListener = null;
	private int mSubSize = -1;
	private int mIconId = -1;
    public void setLiuHaiStatusBarIconController(LiuHaiStatusBarIconController controller){
		mLiuHaiStatusBarIconController = controller;
		if(mLiuHaiStatusBarIconController != null){
			showOnLeftListener = mLiuHaiStatusBarIconController.getShowOnLeftListenerImpI();
        	if(mState != null) setVolteIcon(mState.volteIcon);
		}
	}

    private void refreshVolteIcon(int iconId){
		int subSize = showOnLeftListener != null ? showOnLeftListener.getSubSlotSize() : 0;
		if(subSize == mSubSize && mIconId == iconId){
			return ;
		}
		mSubSize = subSize;
		mIconId = iconId;
        if(mIconId > 0){
            if(showOnLeftListener != null){
                //showOnLeftListener.refreshVolteIcon(true, iconId);
				showOnLeftListener.showVolteIcon(mState.subId, mIconId, mSubSize);
            }
        } else {
            if(showOnLeftListener != null){
                //showOnLeftListener.refreshVolteIcon(false, iconId);
				showOnLeftListener.hideVolteIcon(mState.subId, mSubSize);
            }
        }
    }
	//PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-09-end

    /// M: Add for volte icon on Statusbar @{
    private void setVolteIcon(int volteIcon) {
        //PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-08-start
        if(PrizeSystemUIOption.OPEN_LIUHAI_SCREEN){
            mVolteType.setVisibility(View.GONE);
            refreshVolteIcon(volteIcon);
            return ;
        }
        //PRIZE-add for liuhai screen, OPEN_LIUHAI_SCREEN-liufan-2018-10-08-end
        if (volteIcon > 0) {
            mVolteType.setImageResource(volteIcon);
            mVolteType.setVisibility(View.VISIBLE);
        } else {
            mVolteType.setVisibility(View.GONE);
        }
        /// M: customize VoLTE icon. @{
        mStatusBarExt.setCustomizedVolteView(volteIcon, mVolteType);
        mStatusBarExt.setDisVolteView(mState.subId, volteIcon, mVolteType);
        /// M: customize VoLTE icon. @}
    }
    ///@}

    /// M : Add for [Network Type on Statusbar] @{
    private void setNetworkIcon(int networkIcon) {
        //PRIZE-delete for bugid 69646-liufan-2018-12-11-start
        // Network type is CTA feature, so non CTA project should not set this.
        //if ((!FeatureOptions.MTK_CTA_SET) /* TODO || mIsWfcCase*/) {
        //    return;
        //}
        //PRIZE-delete for bugid 69646-liufan-2018-12-11-end
        if (networkIcon > 0) {
            if (!mStatusBarExt.disableHostFunction()) {
                mNetworkType.setImageResource(networkIcon);
            }
            mNetworkType.setVisibility(View.VISIBLE);
        } else {
            mNetworkType.setVisibility(View.GONE);
        }
    }
    ///@}

    /// M: Add for plugin features. @{
    private void setCustomizedOpViews() {
        mStatusBarExt.SetHostViewInvisible(mMobileRoaming);
        mStatusBarExt.SetHostViewInvisible(mIn);
        mStatusBarExt.SetHostViewInvisible(mOut);
        if (mState.visible) {
            mStatusBarExt.getServiceStateForCustomizedView(mState.subId);
            mStatusBarExt.setCustomizedNetworkTypeView(
                    mState.subId, mState.networkIcon, mNetworkType);
            mStatusBarExt.setCustomizedDataTypeView(
                mState.subId, mState.typeId, mState.mDataActivityIn, mState.mDataActivityOut);
            mStatusBarExt.setCustomizedSignalStrengthView(
                mState.subId, mState.strengthId, mMobile);
            mStatusBarExt.setCustomizedMobileTypeView(
                mState.subId, mMobileType);
            mStatusBarExt.setCustomizedView(mState.subId);
        }
    }
    /// @}

    /**
     * If in airplane mode, and in wifi calling state, should show wfc icon and
     * hide other icons
     */
    private void showWfcIfAirplaneMode() {
        if (needShowWfcInAirplaneMode()) {
            if (DEBUG) {
                Log.d(getMobileTag(), "showWfcIfAirplaneMode: show wfc in airplane mode");
            }
            mMobileGroup.setVisibility(View.VISIBLE);
            mMobile.setVisibility(View.GONE);
            mMobileType.setVisibility(View.GONE);
            mNetworkType.setVisibility(View.GONE);
            mMobileRoaming.setVisibility(View.GONE);
            mIsWfcCase = true;
            requestLayout();
        } else {
            if (mIsWfcCase) {
                if (DEBUG) {
                    Log.d(getMobileTag(), "showWfcIfAirplaneMode: recover to show mobile view");
                }
                mMobile.setVisibility(View.VISIBLE);
                mIsWfcCase = false;
                requestLayout();
            }
        }
    }

    private boolean needShowWfcInAirplaneMode() {
        return mIsWfcEnable && !mState.visible && mState.volteIcon != 0;
    }

    /**
     * get tag with subscription id
     * @return tag for logcat
     */
    private String getMobileTag() {
        return String.format(TAG + "(%d)", mState != null ? mState.subId : -1);
    }
//prize add by liyuchong, add sim card number for condor ,20190124-begin
    private void updateSlotIndicatorVisibility(MobileIconState state){
		//Log.d(TAG, "-----------updateSlotIndicatorVisibility ------  state is "+state);
        int mSlotId=state.phoneId;
        //Log.d(TAG, "TelephonyManager.getDefault().getPhoneCount()  is  "+TelephonyManager.getDefault().getPhoneCount());
        final int slotIndicatorIconId = TelephonyManager.getDefault().getPhoneCount() < 2 ? 0 :mSlotId == 0?1:mSlotId == 1?2:0;
        //Log.d(TAG, "slotIndicatorIconId  is  "+slotIndicatorIconId);
        boolean shouldHideSimIndicator = slotIndicatorIconId==0;
        //Log.d(TAG, "shouldHideSimIndicator  is  "+shouldHideSimIndicator);
        if(shouldHideSimIndicator){
            mSlotIndicatorText.setVisibility(View.GONE);
        }else{
            mSlotIndicatorText.setText(String.valueOf(slotIndicatorIconId));
            mSlotIndicatorText.setVisibility(View.VISIBLE);
        }
    }
//prize add by liyuchong, add sim card number for condor ,20190124-end
}
