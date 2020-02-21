package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.service.quicksettings.Tile;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.ScreenshotHelper;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.SignalState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

public class ScreenshotTile extends QSTileImpl<SignalState> {
    private final String TAG = "ScreenshotTile";
    private int mIconState = R.drawable.ic_screenshot;
    protected Context mContext;
    private QSHost mHost;
    private final ScreenshotHelper mScreenshotHelper;

    public ScreenshotTile(QSHost host) {
        super(host);
        mContext = host.getContext();
        mHost = host;
        mScreenshotHelper = new ScreenshotHelper(mContext);
    }

    @Override
    protected void handleClick() {
        mHost.collapsePanels();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScreenshotHelper.takeScreenshot(1, true, true, mHandler);
            }
        }, 500);
    }

    @Override
    protected void handleUpdateState(SignalState state, Object arg) {
        state.state = Tile.STATE_INACTIVE;
        state.icon = ResourceIcon.get(mIconState);
        state.label = mContext.getString(com.android.internal.R.string.global_action_screenshot);
    }

    @Override
    public SignalState newTileState() {
        return new SignalState();
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(com.android.internal.R.string.global_action_screenshot);
    }

    @Override
    public void handleSetListening(boolean listening) {
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    protected void handleLongClick() {
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_PANEL;
    }

}