package com.mediatek.systemui.qs.tiles.ext;

import android.content.Intent;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.SignalState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;

import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import com.mediatek.systemui.ext.OpSystemUICustomizationFactoryBase;
import com.mediatek.systemui.statusbar.extcb.IconIdWrapper;
import com.mediatek.systemui.statusbar.util.SIMHelper;
import android.service.quicksettings.Tile;
//prize added by liufan, for bugid:72505 2019-3-11-start
import android.provider.Settings;
//prize added by liufan, for bugid:72505 2019-3-11-end

/**
 * M: Mobile Data Connection Tile.
 */
public class MobileDataTile extends QSTileImpl<SignalState> {
    private static final boolean DEBUG = true;

    //prize modify by xiarui for bug66644 2018-10-27 @{
    private static final int QS_MOBILE_DISABLE = R.drawable.prize_mobile_data_off;//ic_qs_mobile_off;
    private static final int QS_MOBILE_ENABLE = R.drawable.prize_mobile_data_on;//ic_qs_mobile_white;
    private static final int QS_MOBILE_NO_SIM = R.drawable.prize_mobile_no_sim;//prize added by liufan, for qs(blue)-2019-2-20
    //@}

    private static final int DATA_DISCONNECT = 0;
    private static final int DATA_CONNECT = 1;
    private static final int AIRPLANE_DATA_CONNECT = 2;
    private static final int DATA_CONNECT_DISABLE = 3;
    private static final int DATA_RADIO_OFF = 4;

    private final NetworkController mController;
    private final DataUsageController mDataController;
    private CharSequence mTileLabel;
    private boolean mEnabled;
    private boolean mConnected;

    private IQuickSettingsPlugin mQuickSettingsExt = null;
    private int mDataConnectionState = DATA_DISCONNECT;
    private int mDataStateIconId = QS_MOBILE_DISABLE;
    private final IconIdWrapper mEnableStateIconIdWrapper = new IconIdWrapper();
    private final IconIdWrapper mDisableStateIconIdWrapper = new IconIdWrapper();

    private final MobileDataSignalCallback mCallback = new MobileDataSignalCallback();

    /**
     * Constructs a new MobileDataTile instance with Host.
     * @param host A Host object.
     */
    public MobileDataTile(QSHost host) {
        super(host);
        mQuickSettingsExt = OpSystemUICustomizationFactoryBase
                .getOpFactory(mContext).makeQuickSettings(mContext);

        mController = Dependency.get(NetworkController.class);
        mDataController = mController.getMobileDataController();
        if (DEBUG) {
            Log.d(TAG, "create MobileDataTile");
        }
    }

    @Override
    public void handleSetListening(boolean listening) {
        if (DEBUG) {
            Log.d(TAG, "setListening = " + listening);
        }
        if (listening) {
            mController.addCallback(mCallback);
        } else {
            mController.removeCallback(mCallback);
        }
    }

    @Override
    public CharSequence getTileLabel() {
        //prize modify by xiarui for bug66644 2018-10-27 @{
        //mTileLabel = mQuickSettingsExt.getTileLabel("mobiledata");
        //return mTileLabel;
        return mContext.getString(R.string.mobile);
        //@}
    }

    //prize added by liufan, for bugid:72505 2019-3-11-start
    private static final Intent DATA_SETTINGS = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
    //prize added by liufan, for bugid:72505 2019-3-11-end

    @Override
    public Intent getLongClickIntent() {
        //prize modify by liufan, for bugid:72505 2019-3-11-start
        //return null;
        return DATA_SETTINGS;
        //prize modify by liufan, for bugid:72505 2019-3-11-end
    }

    @Override
    public SignalState newTileState() {
        return new SignalState();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_PANEL;
    }

    //prize delete by liufan, for bugid:72505 2019-3-11-start
    /*@Override
    protected void handleLongClick() {
        handleClick();
    }*/
    //prize delete by liufan, for bugid:72505 2019-3-11-end

    @Override
    protected void handleClick() {
        if (mDataController.isMobileDataSupported() && mEnabled) {
            /// M: if try to turn on the data connection.
            if (!mConnected) {
                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                /// M: if has not default data SIM set, ignore click.
                if (subId < 0) {
                    return;
                /// M: if the default data SIM radio off, ignore click.
                } else if (!SIMHelper.isRadioOn(subId)) {
                    return;
                }
            }
            mDataController.setMobileDataEnabled(!mConnected);
        }
    }

    @Override
    protected void handleUpdateState(SignalState state, Object arg) {
        if (DEBUG) {
            Log.d(TAG, "handleUpdateState arg=" + arg);
        }

        CallbackInfo cb = (CallbackInfo) arg;

        if (cb == null) {
            cb = mCallback.mInfo;
        }

        final boolean enabled = mDataController.isMobileDataSupported()
                && !cb.noSim && !cb.airplaneModeEnabled && isDefaultDataSimRadioOn();
        final boolean dataConnected = enabled && mDataController.isMobileDataEnabled()
                && (cb.mobileSignalIconId > 0);
        final boolean dataNotConnected = (cb.mobileSignalIconId > 0) && (cb.enabledDesc == null);

        mEnabled = enabled;
        mConnected = dataConnected;
        state.activityIn = cb.enabled && cb.activityIn;
        state.activityOut = cb.enabled && cb.activityOut;

        if (!enabled) {
            state.state = Tile.STATE_UNAVAILABLE; //prize add by xiarui PRIZE_QS_THEME 2019-01-24
            mDataConnectionState = DATA_CONNECT_DISABLE;
            //prize modify by xiarui 20190326 start
			//prize added by liufan, for qs(blue)-2019-2-20-start
            //mDataStateIconId = QS_MOBILE_NO_SIM;//QS_MOBILE_DISABLE;
			//prize added by liufan, for qs(blue)-2019-2-20-end
            if (cb.noSim) {
                mDataStateIconId = QS_MOBILE_NO_SIM;
            } else {
                mDataStateIconId = QS_MOBILE_DISABLE;
            }
            //prize modify by xiarui 20190326 end
            mDisableStateIconIdWrapper.setResources(mContext.getResources());
            mDisableStateIconIdWrapper.setIconId(mDataStateIconId);
            state.label = mQuickSettingsExt
                    .customizeDataConnectionTile(mDataConnectionState, mDisableStateIconIdWrapper,
                            mContext.getString(R.string.mobile));
            state.icon = QsIconWrapper.get(
                    mDisableStateIconIdWrapper.getIconId(), mDisableStateIconIdWrapper);
        } else if (dataConnected) {
            state.state = Tile.STATE_ACTIVE; //prize add by xiarui PRIZE_QS_THEME 2019-01-24
            mDataConnectionState = DATA_CONNECT;
            mDataStateIconId = QS_MOBILE_ENABLE;
            mEnableStateIconIdWrapper.setResources(mContext.getResources());
            mEnableStateIconIdWrapper.setIconId(mDataStateIconId);
            state.label = mQuickSettingsExt
                    .customizeDataConnectionTile(mDataConnectionState, mEnableStateIconIdWrapper,
                            mContext.getString(R.string.mobile));
            state.icon = QsIconWrapper.get(
                    mEnableStateIconIdWrapper.getIconId(), mEnableStateIconIdWrapper);
        } else if (dataNotConnected) {
            state.state = Tile.STATE_INACTIVE; //prize add by xiarui PRIZE_QS_THEME 2019-01-24
            mDataConnectionState = DATA_DISCONNECT;
            mDataStateIconId = QS_MOBILE_DISABLE;
            mDisableStateIconIdWrapper.setResources(mContext.getResources());
            mDisableStateIconIdWrapper.setIconId(mDataStateIconId);
            state.label = mQuickSettingsExt
                    .customizeDataConnectionTile(mDataConnectionState, mDisableStateIconIdWrapper,
                            mContext.getString(R.string.mobile));
            state.icon = QsIconWrapper.get(
                    mDisableStateIconIdWrapper.getIconId(), mDisableStateIconIdWrapper);
        } else {
            state.state = Tile.STATE_INACTIVE; //prize add by xiarui PRIZE_QS_THEME 2019-01-24
            mDataConnectionState = DATA_DISCONNECT;
            mDataStateIconId = QS_MOBILE_DISABLE;
            mDisableStateIconIdWrapper.setResources(mContext.getResources());
            mDisableStateIconIdWrapper.setIconId(mDataStateIconId);
            state.label = mQuickSettingsExt
                    .customizeDataConnectionTile(mDataConnectionState, mDisableStateIconIdWrapper,
                            mContext.getString(R.string.mobile));
            state.icon = QsIconWrapper.get(
                    mDisableStateIconIdWrapper.getIconId(), mDisableStateIconIdWrapper);
        }

        mTileLabel = state.label;

        if (DEBUG) {
            Log.d(TAG, "handleUpdateState state=" + state);
        }
    }

    private final boolean isDefaultDataSimRadioOn() {
        final int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        final boolean isRadioOn = subId >= 0 && SIMHelper.isRadioOn(subId);
        if (DEBUG) {
            Log.d(TAG, "isDefaultDataSimRadioOn subId=" + subId + ", isRadioOn=" + isRadioOn);
        }
        return isRadioOn;
    }

    /**
     * NetworkSignalChanged Callback Info.
     */
    private static final class CallbackInfo {
        public boolean enabled;
        public boolean wifiEnabled;
        public boolean wifiConnected;
        public boolean airplaneModeEnabled;
        public int mobileSignalIconId;
        public int dataTypeIconId;
        public boolean activityIn;
        public boolean activityOut;
        public String enabledDesc;
        public boolean noSim;

        @Override
        public String toString() {
            return new StringBuilder("CallbackInfo[")
                    .append("enabled=").append(enabled)
                    .append(",wifiEnabled=").append(wifiEnabled)
                    .append(",wifiConnected=").append(wifiConnected)
                    .append(",airplaneModeEnabled=").append(airplaneModeEnabled)
                    .append(",mobileSignalIconId=").append(mobileSignalIconId)
                    .append(",dataTypeIconId=").append(dataTypeIconId)
                    .append(",activityIn=").append(activityIn)
                    .append(",activityOut=").append(activityOut)
                    .append(",enabledDesc=").append(enabledDesc)
                    .append(",noSim=").append(noSim)
                    .append(']').toString();
        }
    }

    private final class MobileDataSignalCallback implements SignalCallback {
        final CallbackInfo mInfo = new CallbackInfo();

        @Override
        public void setWifiIndicators(boolean enabled, IconState statusIcon, IconState qsIcon,
                boolean activityIn, boolean activityOut, String description, boolean isTransient,
                String statusLabel) {
            mInfo.wifiEnabled = enabled;
            mInfo.wifiConnected = qsIcon.visible;
            refreshState(mInfo);
        }

        @Override
        public void setMobileDataIndicators(IconState statusIcon, IconState qsIcon, int statusType,
                int networkIcon, int volteIcon, int qsType, boolean activityIn, boolean activityOut,
                String typeContentDescription,String description, boolean isWide, int subId,
                boolean roaming, boolean isDefaultData, int customizedState) {
            if (qsIcon == null) {
                // Not data sim, don't display.
                return;
            }
            mInfo.enabled = qsIcon.visible;
            mInfo.mobileSignalIconId = qsIcon.icon;
            mInfo.dataTypeIconId = qsType;
            mInfo.activityIn = activityIn;
            mInfo.activityOut = activityOut;
            mInfo.enabledDesc = description;
            if (DEBUG) {
                Log.d(TAG, "setMobileDataIndicators mInfo=" + mInfo);
            }
            refreshState(mInfo);
        }

        @Override
        public void setNoSims(boolean show, boolean simDetected) {
            mInfo.noSim = show;
            if (mInfo.noSim) {
                // Make sure signal gets cleared out when no sims.
                mInfo.mobileSignalIconId = 0;
                mInfo.dataTypeIconId = 0;
                mInfo.enabled = false;

                if (DEBUG) {
                    Log.d(TAG, "setNoSims noSim=" + show);
                }
            }
            refreshState(mInfo);
        }

        @Override
        public void setIsAirplaneMode(IconState icon) {
            mInfo.airplaneModeEnabled = icon.visible;
            if (mInfo.airplaneModeEnabled) {
                mInfo.mobileSignalIconId = 0;
                mInfo.dataTypeIconId = 0;
                mInfo.enabled = false;
            }
            refreshState(mInfo);
        }

        @Override
        public void setMobileDataEnabled(boolean enabled) {
            refreshState(mInfo);
        }
    };
}
