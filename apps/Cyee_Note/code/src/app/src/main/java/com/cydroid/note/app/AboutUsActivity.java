package com.cydroid.note.app;

import android.os.Bundle;
import android.view.View;

import com.cydroid.note.R;
import com.cydroid.note.app.view.LoadAppLayout;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.PlatformUtil;

import cyee.widget.CyeeTextView;

import static com.cydroid.note.app.utils.EditUtils.getVersionName;

public class AboutUsActivity extends StandardActivity implements StandardActivity.StandardAListener {

    private LoadAppLayout mLoadContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.about_us);
        setStandardAListener(this);
        setNoteContentView(R.layout.aboutus_content_layout);
        initView();
        setNoteRootViewBackgroundColor();
        if (!PlatformUtil.isGioneeDevice()) {
/*            mLoadContainer = (LoadAppLayout) findViewById(R.id.app_info_load_container);
            mLoadContainer.initLoadData("life_app.xml", this);*/
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!PlatformUtil.isGioneeDevice()) {
            mLoadContainer.updateImageState();
        }
    }

    private void initView() {
        CyeeTextView version = (CyeeTextView) findViewById(R.id.aboutus_version_text);
        version.setTextColor(ColorThemeHelper.getContentSmallTextColor(this, false));
        version.setText(versionData(R.string.aboutus_version_content_text));
        CyeeTextView noteText = (CyeeTextView) findViewById(R.id.aboutus_note_text);
        noteText.setTextColor(ColorThemeHelper.getContentNormalTextColor(this, false));
        if (PlatformUtil.isGioneeDevice()) {
            noteText.setText(R.string.app_name);
        } else {
            noteText.setText(R.string.app_name_outside);
        }

        CyeeTextView copyRightView = (CyeeTextView)findViewById(R.id.aboutus_copyright_text);
        copyRightView.setTextColor(ColorThemeHelper.getContentSmallTextColor(this, false));
        if (PlatformUtil.isGioneeDevice()) {
            copyRightView.setVisibility(View.VISIBLE);
        } else {
            copyRightView.setVisibility(View.GONE);
        }
    }

    private String versionData(int resId) {
        StringBuilder versionName = new StringBuilder();
        versionName = versionName.append(this.getString(resId)).append(getVersionName(this));
        return versionName.toString();
    }

    @Override
    public void onClickHomeBack() {
        finish();
    }

    @Override
    public void onClickRightView() {

    }
}
