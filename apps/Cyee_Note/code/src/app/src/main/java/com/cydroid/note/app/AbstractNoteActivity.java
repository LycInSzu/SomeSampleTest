package com.cydroid.note.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import com.cydroid.note.common.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cydroid.note.R;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;

public class AbstractNoteActivity extends CyeeActivity {

    protected FrameLayout mContentViewGroup;
    protected FrameLayout mFooterViewGroup;
    protected ViewGroup mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ComponentStack.obtain().addActivity(this);
        setContentView(R.layout.abstract_note_activity_layout);
        initViewGroups();
        super.onCreate(savedInstanceState);
        if (NoteUtils.checkHasSmartBar()) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatusBarColor();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ComponentStack.obtain().removeActivity(this);
        super.onDestroy();
    }

    protected Drawable getDrawable(Context context, int id, ColorStateList colorLists) {
        Drawable drawableIcon = ContextCompat.getDrawable(context, id);
        Drawable drawable = DrawableCompat.wrap(drawableIcon);
        DrawableCompat.setTintList(drawable, colorLists);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        return drawable;
    }


    private void initViewGroups() {
        mContentViewGroup = (FrameLayout) findViewById(R.id.abstract_note_activity_layout_content);
        mFooterViewGroup = (FrameLayout) findViewById(R.id.abstract_note_activity_layout_footer);
        mFooterViewGroup.setBackgroundColor(PlatformUtil.isBusinessStyle() ?
                ContextCompat.getColor(this, R.color.new_note_foot_bg_business_style) :
                ContextCompat.getColor(this, R.color.new_note_foot_bg));
        ViewGroup root = (ViewGroup)findViewById(R.id.abstract_note_activity_layout_root);
        mRootView = root;
        int flags = root.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        root.setSystemUiVisibility(flags);
    }

    protected void setActionBarBg() {
        View actionBar = getCyeeActionBar().getCustomView();
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false)
                || ("action.note.private.space".equals(getIntent().getAction()));
        actionBar.setBackgroundColor(ColorThemeHelper.getActionBarBgColor(this, isSecuritySpace));
    }

    public void setNoteContentView(int layoutResID) {
        mContentViewGroup.removeAllViews();
        if (layoutResID <= 0) {
            mContentViewGroup.setVisibility(View.GONE);
            return;
        }
        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(layoutResID, mContentViewGroup, true);
        mContentViewGroup.setVisibility(View.VISIBLE);

    }

    public void setNoteTitleView(int layoutResID) {
        CyeeActionBar actionBar = getCyeeActionBar();
        actionBar.setDisplayOptions(CyeeActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(layoutResID);
        setActionBarBg();
    }

    public void setNoteFooterView(int layoutResID) {
        mFooterViewGroup.removeAllViews();
        if (layoutResID <= 0) {
            mFooterViewGroup.setVisibility(View.GONE);
            return;
        }
        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(layoutResID, mFooterViewGroup, true);
        mFooterViewGroup.setVisibility(View.VISIBLE);
        setFooterViewBg();
    }

    public void setFooterViewBg() {
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        mFooterViewGroup.setBackgroundColor(ColorThemeHelper.getRootViewBgColor(this, isSecuritySpace));
    }

    public void setNoteRootViewBackgroundColor() {
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        mRootView.setBackgroundColor(ColorThemeHelper.getRootViewBgColor(this, isSecuritySpace));
    }

    public View getFooterView() {
        return mFooterViewGroup;
    }

    public void setStatusBarColor() {
        if (!PlatformUtil.isGioneeDevice()) {
            return;
        }
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false)
                || ("action.note.private.space".equals(getIntent().getAction()));
        getWindow().setStatusBarColor(ColorThemeHelper.getStatusBarColor(this, isSecuritySpace));
    }

    @Override
    public Resources getResources() {
        if (!PlatformUtil.isGioneeDevice()
                || !PlatformUtil.isBusinessStyle()) {
            return super.getResources();
        }
        Resources resources = super.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return resources;
    }
}
