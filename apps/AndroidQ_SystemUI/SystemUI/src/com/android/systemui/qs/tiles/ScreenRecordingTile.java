package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.app.StatusBarManager;
import android.content.IntentFilter;
import android.content.Intent;
import com.android.systemui.qs.QSHost;
import com.android.systemui.plugins.qs.QSTile.SignalState;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import android.util.Log;
// A: Bug_id:TWJL-4360    chenchunyong 20180622 {
import android.service.quicksettings.Tile;
// A: }
import android.app.KeyguardManager;

public class ScreenRecordingTile extends QSTileImpl<SignalState>{
    private final String TAG = "CaptiveTitle";
    //modify BUG_ID:EJWJ-704 mengna.liu 20190816 start
    //private int mIconState = R.drawable.screen_record_normal;
    private static int mIconState = R.drawable.screen_record_normal;
    //modify BUG_ID:EJWJ-704 mengna.liu 20190816 end
    protected final Context mContext;
    private StatusBarManager mStatusBarManager;
    private RecordingStatusReceiver mReceiver ;
    private static boolean mRecording = false;
    private Boolean isEnable = true;

    public ScreenRecordingTile(QSHost host) {
        super(host);
        mContext = host.getContext();
        mStatusBarManager = (StatusBarManager) mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        mReceiver = new RecordingStatusReceiver();
        mReceiver.register(true);
        //remove BUG_ID:EJWJ-704 mengna.liu 20190816 start
        //mRecording = false;
        //remove BUG_ID:EJWJ-704 mengna.liu 20190816 end
    }

    @Override
    protected void handleClick() {
        //add BUG_ID:EWWQ-81 sunshiwei 20190125 start
        if (!isEnable) {
            return;
        }
        //add BUG_ID:EWWQ-81 sunshiwei 20190125 end
        mStatusBarManager.collapsePanels();
        Log.d(TAG, "handleTitleClick  run ? "+mRecording);
        if(!mRecording){
            Intent intent = new Intent(mContext,ScreenRecordingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }else{
            Intent service = new Intent(mContext,RecordService.class);
            service.putExtra("status", false);
            mContext.startService(service);
        }
    }
    
    @Override
    protected void handleUpdateState(SignalState state, Object arg) {
        state.icon = ResourceIcon.get(mIconState);
        state.label = mContext.getString(R.string.screen_record_title);
        //add BUG_ID:EWWQ-81 sunshiwei 20190125 start
        KeyguardManager mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
        if(flag){
            state.state = Tile.STATE_UNAVAILABLE;
            isEnable = false;
            return;
        }
        // A: Bug_id:TWJL-4360    chenchunyong 20180622 {
        state.state = mRecording ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
        isEnable = true;
        // A: }
        //add BUG_ID:EWWQ-81 sunshiwei 20190125 end
    }
    
    @Override
     public SignalState newTileState() {
        return new SignalState();
    }
    
    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.screen_record_title);
    }

    @Override
    public void handleSetListening(boolean listening) {
    }
    
    @Override
    public Intent getLongClickIntent() {
        return new Intent("com.android.screen.shot");
    }
    
    @Override
    public int getMetricsCategory(){
        return MetricsEvent.QS_PANEL;
    }

    public class RecordingStatusReceiver extends BroadcastReceiver {
        private boolean mRegistered = false;
        // Add by zhiheng.huang on 2019/3/11 for YWSW-404 start ^_^
        private final String ACTION_REBOOT = "android.intent.action.REBOOT";
        private final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
        // Add by zhiheng.huang on 2019/3/11 for YWSW-404 end ^_^

        public void register(boolean register) {
            if (mRegistered == register) return;
            if (register) {
                final IntentFilter filter = new IntentFilter();
                filter.addAction("com.android.screen.recording");
                // Add by zhiheng.huang on 2019/3/11 for YWSW-404 start ^_^
                filter.addAction(ACTION_REBOOT);
                filter.addAction(ACTION_SHUTDOWN);
                // Add by zhiheng.huang on 2019/3/11 for YWSW-404 end ^_^
                mContext.registerReceiver(this, filter);
            } else {
                mContext.unregisterReceiver(this);
            }
            mRegistered = register;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mRecording = intent.getBooleanExtra("status", false);
            Log.d(TAG, "onReceive -- run ? " + mRecording);
            // Add by zhiheng.huang on 2019/3/11 for YWSW-404 start ^_^
            String action = intent.getAction();
            if (ACTION_REBOOT.equals(action) || ACTION_SHUTDOWN.equals(action)) {
                Intent service = new Intent(mContext,RecordService.class);
                service.putExtra("status", false);
                mContext.startService(service);
                mIconState = R.drawable.screen_record_normal;
                return;
            }
            // Add by zhiheng.huang on 2019/3/11 for YWSW-404 end ^_^
            if (mRecording) {
                mIconState = R.drawable.screen_record_select;
            } else {
                mIconState = R.drawable.screen_record_normal;
            }
            refreshState();
        }
    }
}