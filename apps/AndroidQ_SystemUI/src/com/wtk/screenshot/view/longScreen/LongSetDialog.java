package com.wtk.screenshot.view.longScreen;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wtk.screenshot.util.SharePref;
import com.wtk.screenshot.util.ShotUtil;
import com.android.systemui.R;

public class LongSetDialog implements OnClickListener {
    /* Common */
    // Default
    public static final String SHARE_TITLE = "long_set_share";
    public static final String SHARE_TOLERANCE_VALUES = "tolerance_values";
    public static final String SHARE_TOLERANCE_AUTO = "tolerance_auto";
    public static final String SHARE_UPPER_STATUS_VALUES = "upper_status_values";
    public static final String SHARE_UPPER_STATUS_AUTO = "upper_status_auto";
    public static final String SHARE_DOWN_STATUS_VALUES = "down_status_values";
    public static final String SHARE_DOWN_STATUS_AUTO = "down_status_auto";
    public static final int TOLERANCE_MAX_VALUE = 20;

    // Util
    private Context mContext;
    private SharePref mSharePref;

    // Flag
    private int fullHeight = 100;

    /* View */
    private Dialog mDialog;
    private TextView toleranceValue;
    private SeekBar toleranceSeek;
    private TextView toleranceAuto;
    private TextView upperStatusValue;
    private SeekBar upperStatusSeek;
    private TextView upperStatusAuto;
    private TextView downStatusValue;
    private SeekBar downStatusSeek;
    private TextView downStatusAuto;
    // add BUG_ID:TQQB-127 liuzhijun 20181018 (start)
    private int mDefUpDownValue = 0;
    // add BUG_ID:TQQB-127 liuzhijun 20181018 (end)

    public LongSetDialog(Context context) {
        mContext = context;
        mDialog = createLoadingDialog(mContext, mContext.getResources()
                .getString(R.string.bitmap_montage_loading));
        mSharePref = SharePref.getInstance(mContext, SHARE_TITLE);
        // add BUG_ID:TQQB-127 liuzhijun 20181018 (start)
        mDefUpDownValue = context.getResources().getInteger(R.integer.def_up_down_value);
        // add BUG_ID:TQQB-127 liuzhijun 20181018 (end)
    }

    public void showDialog() {
        Bitmap mBitmap = ShotUtil.getInstance(mContext).getFullScreenBitmap();
        if (mBitmap != null) {
            fullHeight = mBitmap.getHeight() - getStatusBarHeight();
        }

        setDefault();

        mDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        mDialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mDialog.show();
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void setDefault() {
        // Tolerance
        boolean autoEnable = mSharePref.getBoolean(SHARE_TOLERANCE_AUTO, true);
        int values = mSharePref.getInt(SHARE_TOLERANCE_VALUES, 0);
        toleranceAuto.setSelected(autoEnable);
        toleranceValue.setText(String.format(
                mContext.getResources().getString(
                        R.string.long_set_tolerance_unit), values * TOLERANCE_MAX_VALUE / 100.0
                        + ""));
        toleranceSeek.setEnabled(!autoEnable);
        toleranceSeek.setProgress(values);
        toleranceValue.setTextColor(autoEnable ? Color.GRAY : Color.BLACK);

        // Upper status
        // add BUG_ID:TQQB-127 liuzhijun 20181018 (start)
        autoEnable = mSharePref.getBoolean(SHARE_UPPER_STATUS_AUTO, false);
        values = mSharePref.getInt(SHARE_UPPER_STATUS_VALUES, mDefUpDownValue);
        // add BUG_ID:TQQB-127 liuzhijun 20181018 (end)
        upperStatusAuto.setSelected(autoEnable);
        upperStatusValue.setText(String.format(mContext.getResources()
                .getString(R.string.long_set_status_unit), values * fullHeight
                / 100.0 + ""));
        upperStatusSeek.setEnabled(!autoEnable);
        upperStatusSeek.setProgress(values);
        upperStatusValue.setTextColor(autoEnable ? Color.GRAY : Color.BLACK);

        // Down status
        // add BUG_ID:TQQB-127 liuzhijun 20181018 (start)
        autoEnable = mSharePref.getBoolean(SHARE_DOWN_STATUS_AUTO, false);
        values = mSharePref.getInt(SHARE_DOWN_STATUS_VALUES, mDefUpDownValue);
        // add BUG_ID:TQQB-127 liuzhijun 20181018 (end)
        downStatusAuto.setSelected(autoEnable);
        downStatusValue.setText(String.format(mContext.getResources()
                .getString(R.string.long_set_status_unit), values * fullHeight
                / 100.0 + ""));
        downStatusSeek.setEnabled(!autoEnable);
        downStatusSeek.setProgress(values);
        downStatusValue.setTextColor(autoEnable ? Color.GRAY : Color.BLACK);
    }

    public void cancelDialog() {
        mDialog.cancel();
    }

    private Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.long_set_dialog, null);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);

        toleranceValue = (TextView) v.findViewById(R.id.tolerance_value);
        toleranceSeek = (SeekBar) v.findViewById(R.id.tolerance_seek);
        toleranceAuto = (TextView) v.findViewById(R.id.tolerance_auto);
        upperStatusValue = (TextView) v.findViewById(R.id.upper_status_value);
        upperStatusSeek = (SeekBar) v.findViewById(R.id.upper_status_seek);
        upperStatusAuto = (TextView) v.findViewById(R.id.upper_status_auto);

        downStatusValue = (TextView) v.findViewById(R.id.down_status_value);
        downStatusSeek = (SeekBar) v.findViewById(R.id.down_status_seek);
        downStatusAuto = (TextView) v.findViewById(R.id.down_status_auto);

        toleranceAuto.setOnClickListener(this);
        toleranceSeek.setOnSeekBarChangeListener(seekListener);
        upperStatusAuto.setOnClickListener(this);
        upperStatusSeek.setOnSeekBarChangeListener(seekListener);
        downStatusAuto.setOnClickListener(this);
        downStatusSeek.setOnSeekBarChangeListener(seekListener);

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));
        loadingDialog.getWindow().setWindowAnimations(R.style.loaddialogAnim);
        loadingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        return loadingDialog;
    }

    @Override
    public void onClick(View v) {
        boolean isSelected = false;
        int id = v.getId();
        if (id == R.id.tolerance_auto) {
            isSelected = toleranceAuto.isSelected();
            toleranceAuto.setSelected(!isSelected);
            mSharePref.setBoolean(SHARE_TOLERANCE_AUTO, !isSelected);
            toleranceSeek.setEnabled(isSelected);
            toleranceValue.setTextColor(!isSelected ? Color.GRAY : Color.BLACK);
        } else if (id == R.id.upper_status_auto) {
            isSelected = upperStatusAuto.isSelected();
            upperStatusAuto.setSelected(!isSelected);
            mSharePref.setBoolean(SHARE_UPPER_STATUS_AUTO, !isSelected);
            upperStatusSeek.setEnabled(isSelected);
            upperStatusValue.setTextColor(!isSelected ? Color.GRAY
                    : Color.BLACK);
        } else if (id == R.id.down_status_auto) {
            isSelected = downStatusAuto.isSelected();
            downStatusAuto.setSelected(!isSelected);
            mSharePref.setBoolean(SHARE_DOWN_STATUS_AUTO, !isSelected);
            downStatusSeek.setEnabled(isSelected);
            downStatusValue
                    .setTextColor(!isSelected ? Color.GRAY : Color.BLACK);
        }
    }

    private SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int id = seekBar.getId();
            if (id == R.id.tolerance_seek) {
                mSharePref
                        .setInt(SHARE_TOLERANCE_VALUES, seekBar.getProgress());
            } else if (id == R.id.upper_status_seek) {
                mSharePref.setInt(SHARE_UPPER_STATUS_VALUES,
                        seekBar.getProgress());
            } else if (id == R.id.down_status_seek) {
                mSharePref.setInt(SHARE_DOWN_STATUS_VALUES,
                        seekBar.getProgress());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            int id = seekBar.getId();
            if (id == R.id.tolerance_seek) {
                toleranceValue.setText(String.format(mContext.getResources()
                        .getString(R.string.long_set_tolerance_unit), progress
                        * 20 / 100.0 + ""));
            } else if (id == R.id.upper_status_seek) {
                upperStatusValue.setText(String.format(mContext.getResources()
                        .getString(R.string.long_set_status_unit), progress
                        * fullHeight / 100.0 + ""));
            } else if (id == R.id.down_status_seek) {
                downStatusValue.setText(String.format(mContext.getResources()
                        .getString(R.string.long_set_status_unit), progress
                        * fullHeight / 100.0 + ""));
            }
        }
    };

}
