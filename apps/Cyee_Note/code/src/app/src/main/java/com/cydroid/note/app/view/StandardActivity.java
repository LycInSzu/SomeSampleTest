package com.cydroid.note.app.view;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.AbstractNoteActivity;
import com.cydroid.note.app.dialog.CyeeDeterminateProgressDialog;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.PlatformUtil;

import cyee.app.CyeeActionBar;


public class StandardActivity extends AbstractNoteActivity implements View.OnClickListener {

    private StandardAListener mListener;
    protected CyeeDeterminateProgressDialog mProgressDialog;
    protected int mProgress;
    protected int mAddPicNum;
    protected Handler mMainHandler;

    private void initHandler() {
        mMainHandler = new Handler(getMainLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandler();
    }

    public interface StandardAListener {
        void onClickHomeBack();

        void onClickRightView();
    }

    public void setStandardAListener(StandardAListener listener) {
        mListener = listener;
    }

    public void setTitle(int titleTextId) {
        setTitle(getString(titleTextId));
    }

    public void setTitle(String title) {
        CyeeActionBar actionBar = getCyeeActionBar();
        actionBar.setDisplayOptions(CyeeActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.setting_title_layout);
        ((TextView) actionBar.getCustomView().findViewById(R.id.title))
                .setText(title);
        setActionBarBg();
        setTitleViewColor();
        findViewById(R.id.action_bar_setting_custom_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickHomeBack();
                }
            }
        });
    }

    private void setTitleViewColor() {
        ImageView back = (ImageView) findViewById(R.id.action_bar_setting_custom_back);
        TextView title = (TextView) findViewById(R.id.title);
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        Drawable drawable = getDrawable(this, R.drawable.note_title_back_icon, ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));
        title.setTextColor(ColorThemeHelper.getActionBarTextColor(this, isSecuritySpace));
        back.setImageDrawable(drawable);
    }

    @Override
    public void onClick(View v) {
    }

    protected void tintImageViewDrawable(int imageViewId, int iconId, ColorStateList colorsLists) {
        Drawable icon = ContextCompat.getDrawable(this, iconId);
        Drawable tintIcon = DrawableCompat.wrap(icon);
        DrawableCompat.setTintList(tintIcon, colorsLists);
        ((ImageView) findViewById(imageViewId)).setImageDrawable(tintIcon);
    }

    protected int getTitleTextColor() {
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false)
                || ("action.note.private.space".equals(getIntent().getAction()));
        if (isSecuritySpace) {
            return ContextCompat.getColor(this, R.color.setting_title_bg);
        } else {
            return ContextCompat.getColor(this, R.color.note_main_activity_title_layout_title_text_color);
        }
    }

    protected void showProgressDialog(int maxProgress, int textMax, String title) {
        CyeeDeterminateProgressDialog progressDialog = mProgressDialog;
        if (progressDialog == null) {
            progressDialog = new CyeeDeterminateProgressDialog(this, false);
            mProgressDialog = progressDialog;
        }
        progressDialog.setMessage(title);
        progressDialog.setMax(maxProgress, textMax);
        mProgress = 0;
        progressDialog.setProgress(mProgress);
        progressDialog.show();
    }

}
