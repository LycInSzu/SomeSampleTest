package com.cydroid.note.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.cydroid.note.common.Log;
//GIONEE wanghaiyan 2016 -12-13 modify for 45337 begin
import com.cydroid.note.common.FileUtils;
//GIONEE wanghaiyan 2016 -12-13 modify for 45337 end
import com.cydroid.note.common.Log;
import com.cydroid.note.R;
import com.cydroid.note.app.dataupgrade.DataUpgrade;
import com.cydroid.note.app.inputbackup.ImportBackupManager;
import com.cydroid.note.app.utils.PackageUtils;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.app.view.NoteSearchView;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteShareDataManager;

import cyee.app.CyeeAlertDialog;
import cyee.app.CyeeProgressDialog;
import cyee.widget.CyeeCheckBox;
import cyee.widget.CyeeTextView;
//GIONEE wanghaiyan 2016-12-08 modify for 40315 begin
import cyee.changecolors.ChameleonColorManager;
//GIONEE wanghaiyan 2016-12-08 modify for 40315 end

public class NoteMainActivity extends StandardActivity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION_AND_INIT_DATA = 1;
    private static final boolean DEBUG = true;
    private static final String NOTE_ACTION_FROM_SEARCH = "com.cydroid.note.intent.action.PRESSURE_SEARCH";
    private static final String TAG = "NoteMainActivity";
    private static final String USER_GUIDE_SHOW_STATE = "user_guide_show_state";
    private static final String AMI_MARK = "com.gionee.amimark";
    private NoteSelectionManager mNoteSelectionManager;
    private TextView mSelectionTitleView;
    private TextView mSelectionAllView;
    private TextView mFootCommonView;
    private TextView mFootDeleteOrSearchView;
    private ImageView mTitleListOrCardView;
    private ImageView mAmiMarkView;
    private TextView mFootSettingView;

    private RelativeLayout mEncryptUserGuide;
    private ImageView mCover;
    private Drawable mEditIcon;
    private Drawable mDeleteIcon;
    private Drawable mEncryptIcon;
    private String mEditString;
    private String mDeleteString;
    private CyeeProgressDialog mProgressDialog;
    private ImportBackupManager mImportBackupManager;
    private int mEditOrEncryptActionMode = EDIT_ACTION_MODE;
    private static final int COVER_DISAPPEAR = 0;
    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final int EDIT_ACTION_MODE = 2;
    private static final int ENCRYPT_ACTION_MODE = 3;
    private static final int COVER_APPEAR_TIME = 2000;
    private DataUpgrade mDataUpgrade;
    private Dialog mDataFlowHintDialog;
    private boolean mUserGuideShowing;
    private View.OnClickListener mSelectListener;
    private String mEncryptString;
    private String mSearchString;
    private String mSettingString;
    private Drawable mListViewIcon;
    private Drawable mCardViewIcon;
    private Drawable mSearchIcon;
    private Drawable mSettingIcon;
    private boolean isFootChoiceDelete;
    public static final int ENCRYPT_USER_GUIDE_DEFAULT = 0;
    public static final int ENCRYPT_USER_GUIDE_SHOW = 1;
    public static final int ENCRYPT_USER_GUIDE_CLICKED = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COVER_DISAPPEAR:
                    disappearCover();
					//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin
                    try {
                        if (!NoteUtils.checkNeededPermissionForRecord(NoteMainActivity.this)) {
                        
                            return;
                        }
                    }catch (Exception ex) {
                        Log.e(TAG, "ex.", ex);
                    }
					//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end
                    break;
                case MSG_UPDATE_PROGRESS:
                    checkUpgradeProgress();
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFavor();
        initView();
        initData();
        setFootCommonViewState();
        initListener();
        initFragment(savedInstanceState);
        boolean isFromPressure = NOTE_ACTION_FROM_SEARCH.equals(getIntent().getAction());
        if (isFromPressure) {
            onNoteSearch();
        }
        if (!PlatformUtil.isSecurityOS() && !NoteShareDataManager.isEncryptUserGuideNormalClosed(this)) {
            checkShowEncryptUserGuide();
        }
        checkShowDataFlowHint();
    }

    private void initFavor() {
        initImportBackupManager();
        //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin
        if(!NoteUtils.checkNeededPermissionForRecord(this)){
		//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end
            return;
        }
        initDataUpgrade();
        if (!PlatformUtil.isGioneeDevice()) {
            startCover();
        }

    }
	 //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin
     /*
    private String[] getPermissions() {
        String phoneStatePermission = null;
        if (!NoteUtils.checkPhoneStatePermission()) {
            phoneStatePermission = Manifest.permission.READ_PHONE_STATE;
        }
        String storagePermission = null;
        String storageWritePermission = null;
        Log.d(TAG,"wanghaiyan_NoteUtils.checkExternalStoragePermission()" + NoteUtils.checkExternalStoragePermission());
        if (!NoteUtils.checkExternalStoragePermission()) {
            storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
            storageWritePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }
		//GIONEE wanghaiyan 2016-11-24 modify for 28368 begin
		String recordAudioPermission = null;
		if (!NoteUtils.checkRecordAudioPermission()) {
			recordAudioPermission = Manifest.permission.RECORD_AUDIO;
		}
        String[] permissions = null;
        //Chenyee wanghaiyan 2018-
        if (phoneStatePermission != null && storagePermission != null && storageWritePermission !=null && recordAudioPermission != null) {
            permissions = new String[]{phoneStatePermission, storagePermission,storageWritePermission,recordAudioPermission};
        } else if (phoneStatePermission != null) {
            permissions = new String[]{phoneStatePermission};
        } else if (storagePermission != null) {
            permissions = new String[]{storagePermission};
        } else if (storageWritePermission != null) {
            permissions = new String[]{storageWritePermission};
        }else if (recordAudioPermission != null) {
            permissions = new String[]{recordAudioPermission};
        }
        //GIONEE wanghaiyan 2016-11-24 modify for 28368 end
        return permissions;
    }
    */
	//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end

    private void initImportBackupManager() {
        mImportBackupManager = new ImportBackupManager(NoteMainActivity.this);
    }

    private void startCover() {
        mCover = new ImageView(this);
        mCover.setClickable(true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.cover);
        mCover.setBackground(drawable);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRootView.addView(mCover, params);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(COVER_DISAPPEAR), COVER_APPEAR_TIME);
    }

    private void disappearCover() {
        mRootView.removeView(mCover);
        mCover.setClickable(false);
        mCover = null;
    }

    private void setFootCommonViewState() {
        if (EDIT_ACTION_MODE == mEditOrEncryptActionMode) {
            mFootCommonView.setCompoundDrawables(null, mEditIcon, null, null);
            mFootCommonView.setTextColor(ColorThemeHelper.setFooterBarTextColor(this, false));
            mFootCommonView.setText(mEditString);
	        //GIONEE wanghaiyan 2016 -12-13 modify for 45337 begin
	        if(!FileUtils.gnEncryptionSpaceSupport){
	        mFootCommonView.setVisibility(View.VISIBLE);
	        }
	        //GIONEE wanghaiyan 2016 -12-13 modify for 45337 begin
            mFootDeleteOrSearchView.setCompoundDrawables(null, mSearchIcon, null, null);
            mFootDeleteOrSearchView.setTextColor(ColorThemeHelper.setFooterBarTextColor(this, false));
            mFootDeleteOrSearchView.setText(mSearchString);
            mFootSettingView.setCompoundDrawables(null, mSettingIcon, null, null);
            mFootSettingView.setText(mSettingString);
            mFootSettingView.setTextColor(ColorThemeHelper.setFooterBarTextColor(this, false));
            mFootSettingView.setVisibility(View.VISIBLE);
            } else {
            //GIONEE wanghaiyan 2016 -12-13 modify for 45337 begin
            if(!FileUtils.gnEncryptionSpaceSupport){
	           mFootCommonView.setVisibility(View.GONE);
             }else{
               mFootCommonView.setCompoundDrawables(null, mEncryptIcon, null, null);
               mFootCommonView.setTextColor(ColorThemeHelper.setFooterBarTextColor(this, false));
               mFootCommonView.setText(mEncryptString);
            }		
	        //GIONEE wanghaiyan 2016 -12-13 modify for 45337 end
            mFootDeleteOrSearchView.setCompoundDrawables(null, mDeleteIcon, null, null);
            mFootDeleteOrSearchView.setTextColor(ColorThemeHelper.setFooterBarTextColor(this, false));
            mFootDeleteOrSearchView.setText(mDeleteString);
            mFootSettingView.setVisibility(View.GONE);
        }
    }

    private void initView() {
        setNoteMainNormalTilte();
        setNoteContentView(R.layout.note_main_activity_content_layout);
        setNoteFooterView(R.layout.note_main_activity_action_mode_footer_layout);
        setNoteRootViewBackgroundColor();
        mTitleListOrCardView = (ImageView) findViewById(R.id.note_main_activity_title_layout_choice);
        mFootCommonView = (TextView) findViewById(R.id.footer_edit_or_encrypt_action);
        mFootDeleteOrSearchView = (TextView) findViewById(R.id.footer_delete_or_search_action);
        mFootSettingView = (TextView) findViewById(R.id.note_main_activity_foot_layout_setting);
    }

    private void setAmiMarkView() {
        mAmiMarkView = (ImageView) findViewById(R.id.note_main_activity_foot_layout_ami_mark);
        boolean isAmiMarkInstall = PackageUtils.isPackageInstalled(this, AMI_MARK);
        if (isAmiMarkInstall) {
            tintImageViewDrawable(R.id.note_main_activity_foot_layout_ami_mark, R.drawable.ami_mark,
                    ColorThemeHelper.getActionBarIconColor(this, false));
            mAmiMarkView.setOnClickListener(this);
            mAmiMarkView.setVisibility(View.VISIBLE);
        } else {
            mAmiMarkView.setVisibility(View.GONE);
        }
    }


    private void initData() {
        mEditString = getResources().getString(R.string.note_action_edit_string);
        mDeleteString = getResources().getString(R.string.note_action_del_string);
        mEncryptString = getResources().getString(R.string.note_action_encrypt_string);
        mSearchString = getResources().getString(R.string.note_action_search_string);
        mSettingString = getResources().getString(R.string.note_action_setting_string);

        mEditIcon = getDrawable(this, R.drawable.note_main_activity_title_dw_edit, ColorThemeHelper.getFooterBarIconColor(this, false));
        mSearchIcon = getDrawable(this, R.drawable.note_main_activity_title_dw_search, ColorThemeHelper.getFooterBarIconColor(this, false));
        mDeleteIcon = getDrawable(this, R.drawable.note_main_del_icon, ColorThemeHelper.getFooterBarIconColor(this, false));
        mEncryptIcon = getDrawable(this, R.drawable.icon_encrypt, ColorThemeHelper.getFooterBarIconColor(this, false));
        mListViewIcon = getDrawable(this, R.drawable.note_main_activity_title_dw_list_view,
                ColorThemeHelper.getActionBarIconColor(this, false));
        mCardViewIcon = getDrawable(this, R.drawable.note_main_activity_title_dw_card_view,
                ColorThemeHelper.getActionBarIconColor(this, false));
        mSettingIcon = getDrawable(this, R.drawable.note_main_activity_title_dw_setting, ColorThemeHelper.getFooterBarIconColor(this, false));
        setTitleListOrCardViewDrawable(NoteShareDataManager.getNoteDisplayMode(this));
    }

    private void refreshIcon() {
        mSearchIcon = getDrawable(this, R.drawable.note_main_activity_title_dw_search, ColorThemeHelper.getFooterBarIconColor(this, false));
    }

    private void initListener() {
        mTitleListOrCardView.setOnClickListener(this);
        mFootCommonView.setOnClickListener(this);
        mFootDeleteOrSearchView.setOnClickListener(this);
        mFootSettingView.setOnClickListener(this);
    }

    private void initDataUpgrade() {
        if (!DataUpgrade.isExistOldDB(this)) {
            BuiltInNote.insertBuildInNoteAsync();
            startImportBackupCheck();
            return;
        }
        DataUpgrade dataUpgrade = new DataUpgrade();
        if (dataUpgrade.isUpgradeFinish()) {
            startImportBackupCheck();
            return;
        }
        mDataUpgrade = dataUpgrade;
        CyeeProgressDialog progressDialog = new CyeeProgressDialog(this);
        mProgressDialog = progressDialog;
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        progressDialog.setMessage(this.getString(R.string.date_upgrade_message));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 100);
    }

    private void checkUpgradeProgress() {
        DataUpgrade dataUpgrade = mDataUpgrade;
        if (dataUpgrade.isUpgradeFinish()) {
            dismissProgressDialog();
            startImportBackupCheck();
            return;
        }
        if (dataUpgrade.isUpgradeFail()) {
            dismissProgressDialog();
            startImportBackupCheck();
            String tip = "error code = " + dataUpgrade.getFailCode()
                    + ",total = " + dataUpgrade.getUpgradeTotalCount()
                    + ",success = " + dataUpgrade.getUpgradeSuccessCount()
                    + ",failCount = " + dataUpgrade.getUpgradeFailCount();
            new ToastManager(this).showToast(tip);
            return;
        }
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 100);
    }

    private void startImportBackupCheck() {
        mImportBackupManager.startCheck();
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            NoteMainFragment noteMainFragment = new NoteMainFragment();
            ft.add(R.id.fragment_container, noteMainFragment, "NoteMainFragment");
            ft.commit();
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (PlatformUtil.isGioneeDevice() && NOTE_ACTION_FROM_SEARCH.equals(intent.getAction())) {
            if (mNoteSelectionManager != null) {
                mNoteSelectionManager.leaveSelectionMode();
            }
            onNoteSearch();
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshIcon();
        checkEncryptUserGuide();
        if (PlatformUtil.isGioneeDevice()) {
            mImportBackupManager.resume();
        }
    }

    public void checkEncryptUserGuide() {
        if (PlatformUtil.isSecurityOS()) {
            setGestureDetectEnable();
        } else {
            checkShowEncryptUserGuide();
        }
    }

    @Override
    protected void onPause() {
        if (PlatformUtil.isGioneeDevice()) {
            mImportBackupManager.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (PlatformUtil.isGioneeDevice()) {
            mImportBackupManager.destroy();
        }
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(USER_GUIDE_SHOW_STATE, mUserGuideShowing);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        boolean userGuideShowing = savedInstanceState.getBoolean(USER_GUIDE_SHOW_STATE, false);
        mUserGuideShowing = userGuideShowing;
        if (userGuideShowing) {
            showEncryptUserGuide();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
            return;
        }
        switch (requestCode) {
            case EncryptUtil.REQUEST_DIAL_SETTING_SUCCESS:
                setGestureDetectEnable();
                encrypt();
                break;
            default:
                break;
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (mNoteSelectionManager != null && mNoteSelectionManager.inSelectionMode()) {
            mNoteSelectionManager.leaveSelectionMode();
            return;
        }

        if (!NoteShareDataManager.getHasShowDataFlowHint(getApplicationContext())) {
            finish();
            return;
        }

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            return;
        }

        if (PlatformUtil.isGioneeDevice()) {
            boolean backSuccess = moveTaskToBack(true);
            if (!backSuccess) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.note_main_activity_title_layout_choice:
                changeDisplayMode();
                break;
            case R.id.note_main_activity_foot_layout_setting:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.note_main_activity_search_title_layout_back:
                onNoteSearchBack();
                break;

            case R.id.footer_edit_or_encrypt_action:
                editOrEncryptActionResponse();
                break;
            case R.id.footer_delete_or_search_action:
                deleteOrSearchAction();
                break;
            case R.id.btn_exit:
                hideEncryptUserGuide();
                break;
            case R.id.note_main_activity_foot_layout_ami_mark:
                enterAmiMatk();
                break;
            default:
                break;
        }
    }

    private void enterAmiMatk() {
        try {
            Intent intent = PackageUtils.getAppLaunchIntent(this, AMI_MARK);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void setTitleListOrCardViewDrawable(int displayType) {
		//Gionee wanghaiyan 2017-6-15 modify for 157925 begin
        if (displayType == Constants.NOTE_DISPLAY_LIST_MODE) {
            mTitleListOrCardView.setImageDrawable(mListViewIcon);
        } else {
            mTitleListOrCardView.setImageDrawable(mCardViewIcon);
        }
		//Gionee wanghaiyan 2017-6-15 modify for 157925 end
    }

    private void deleteOrSearchAction() {
        if (isFootChoiceDelete) {
            trashResponse();
        } else {
            onNoteSearch();
        }
    }

    private void setGestureDetectEnable() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        if (null != fragment && fragment instanceof NoteMainFragment) {
            ((NoteMainFragment) fragment).setGestureDetectEnable();
        }
    }

    private void encrypt() {
        NoteMainFragment fragment = (NoteMainFragment) getFragmentManager().
                findFragmentById(R.id.fragment_container);
        if (null != fragment) {
            fragment.onEncrpt();
        }
    }

    private void hideEncryptUserGuide() {
        if (null != mEncryptUserGuide) {
            mUserGuideShowing = false;
            fullScreen(false);
            mEncryptUserGuide.setVisibility(View.GONE);
        }
        NoteShareDataManager.setShowEncryptUserGuide(getApplicationContext(), ENCRYPT_USER_GUIDE_CLICKED);
        NoteShareDataManager.setIsEncryptUserGuideNormalClosed(this, true);
    }

    private void editOrEncryptActionResponse() {
        if (mEditOrEncryptActionMode == EDIT_ACTION_MODE) {
            editResponse();
        } else {
            if (PlatformUtil.isSecurityOS() && !EncryptUtil.isDialcodeOpen(getContentResolver())) {
                EncryptUtil.startDialSettingInterface(NoteMainActivity.this);
            } else {
                encrypt();
            }
        }
    }

    private void fullScreen(boolean fullScreen) {
        if (fullScreen) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void editResponse() {
        produceNewNote();
    }

    private void changeDisplayMode() {
        NoteMainFragment fragment = (NoteMainFragment) getFragmentManager().
                findFragmentById(R.id.fragment_container);
        if (null != fragment) {
            if (fragment.getDisplayMode() == Constants.NOTE_DISPLAY_GRID_MODE) {
                fragment.setDisplayMode(Constants.NOTE_DISPLAY_LIST_MODE, true);
            } else {
                fragment.setDisplayMode(Constants.NOTE_DISPLAY_GRID_MODE, true);

            }
            fragment.loadDataForChangeMode();
            setTitleListOrCardViewDrawable(fragment.getDisplayMode());
        }
    }

    private void trashResponse() {
        NoteMainFragment fragment = (NoteMainFragment) getFragmentManager().
                findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.onThrowIntoTrash();
        }
    }

    private void produceNewNote() {
        Intent intent = new Intent(this, NewNoteActivity.class);
        intent.putExtra(NewNoteActivity.ENABLE_EDIT_MODE, true);
        intent.putExtra(NewNoteActivity.FROM_INNER_CONTEXT, true);
        startActivityForResult(intent, 0);
    }

    private void onNoteSearch() {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                NoteSearchFragment noteSearchFragment = new NoteSearchFragment();
                ft.replace(R.id.fragment_container, noteSearchFragment, "NoteSearchFragment");
                ft.addToBackStack(null);
                //Chenyee wanghaiyan 2018-4-27 modify for SWW1618OTA-400 begin
                ft.commitAllowingStateLoss();
                setNoteFooterView(-1);
                //Chenyee wanghaiyan 2018-4-27 modify for SWW1618OTA-400 end
    }

    protected void beginNoteSearch(NoteSearchView.OnQueryTextListener listener) {
        if (DEBUG) {
            Log.d(TAG, "beginNoteSearch");
        }
        setNoteTitleView(R.layout.note_main_activity_search_title_layout);
        tintImageViewDrawable(R.id.note_main_activity_search_title_layout_back,
                R.drawable.note_title_back_icon, ColorThemeHelper.getActionBarIconColor(this, false));
        findViewById(R.id.note_main_activity_search_title_layout_back).setOnClickListener(this);
        NoteSearchView searchView = (NoteSearchView) findViewById(R.id.note_search_view);
        searchView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.searchview_zoom));
        searchView.setOnQueryTextListener(listener);
	    //GIONEE wanghaiyan 2016-12-3 modify for 28315 for begin
        EditText mInputMsgView = (EditText) searchView.findViewById(R.id.search_input_msg_edit_text);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(mInputMsgView, 0);
        }
	    //GIONEE wanghaiyan 2016-12-3 modify for 28315 for end

    }

    protected void endNoteSearch() {
        if (DEBUG) {
          
			 Log.d(TAG, "endNoteSearch");
        }
        if (ENCRYPT_ACTION_MODE == mEditOrEncryptActionMode && null != mNoteSelectionManager) {
            setNoteTitleView(R.layout.note_main_activity_action_mode_title_layout);
            findViewById(R.id.note_main_activity_action_mode_title_layout_back).setOnClickListener(mSelectListener);
            mSelectionAllView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_select);
            mSelectionAllView.setOnClickListener(mSelectListener);
            mSelectionTitleView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_text);

            int count = mNoteSelectionManager.getSelectedCount();
            String format = getResources().getQuantityString(R.plurals.number_of_items_selected, count);
            setSelectionModeTitle(String.format(format, count));
            updateSelectionViewsState();
        } else {
            setNoteMainNormalTilte();
            mTitleListOrCardView = (ImageView) findViewById(R.id.note_main_activity_title_layout_choice);
            setTitleListOrCardViewDrawable(NoteShareDataManager.getNoteDisplayMode(this));
            mTitleListOrCardView.setOnClickListener(this);
        }
        setNoteFooterView(R.layout.note_main_activity_action_mode_footer_layout);
        mFootCommonView = (TextView) findViewById(R.id.footer_edit_or_encrypt_action);
        mFootCommonView.setOnClickListener(this);
        mFootDeleteOrSearchView = (TextView) findViewById(R.id.footer_delete_or_search_action);
        mFootDeleteOrSearchView.setOnClickListener(this);
        mFootSettingView = (TextView) findViewById(R.id.note_main_activity_foot_layout_setting);
        mFootSettingView.setOnClickListener(this);
        setFootCommonViewState();
    }

    private void setNoteMainNormalTilte() {
        setNoteTitleView(R.layout.note_main_activity_title_layout);
        ((TextView) findViewById(R.id.note_mian_actionbar_title)).setTextColor(
                ColorThemeHelper.getActionBarTextColor(this, false));
        setAmiMarkView();
    }

    private void onNoteSearchBack() {
        findViewById(R.id.note_search_view).clearFocus();
        //Chenyee wanghaiyan 2018-5-14 modify for CSW1707A-992 begin
        if(!isFinishing()) {
            getFragmentManager().popBackStack();
        }
        //Chenyee wanghaiyan 2018-5-14 modify for CSW1707A-992 ebd
    }

    public void startSelectionMode(NoteSelectionManager noteSelectionManager, View.OnClickListener listener) {
        mEditOrEncryptActionMode = ENCRYPT_ACTION_MODE;
        isFootChoiceDelete = true;
        setFootCommonViewState();
        mNoteSelectionManager = noteSelectionManager;
        mSelectListener = listener;
        setNoteTitleView(R.layout.note_main_activity_action_mode_title_layout);

        tintImageViewDrawable(R.id.note_main_activity_action_mode_title_layout_back,
                R.drawable.note_title_back_icon, ColorThemeHelper.getActionBarIconColor(this, false));
        ((TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_text))
                .setTextColor(ColorThemeHelper.getActionBarTextColor(this, false));
        ((TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_select))
                .setTextColor(ColorThemeHelper.getActionBarTextColor(this, false));

        findViewById(R.id.note_main_activity_action_mode_title_layout_back).setOnClickListener(listener);
        mSelectionAllView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_select);
        mSelectionAllView.setOnClickListener(listener);
        mSelectionTitleView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_text);
    }

    public void finishSelectionMode() {
        mEditOrEncryptActionMode = EDIT_ACTION_MODE;
        isFootChoiceDelete = false;
        setFootCommonViewState();
        setNoteMainNormalTilte();
        mTitleListOrCardView = (ImageView) findViewById(R.id.note_main_activity_title_layout_choice);
        mTitleListOrCardView.setOnClickListener(this);
        setTitleListOrCardViewDrawable(NoteShareDataManager.getNoteDisplayMode(this));
        mNoteSelectionManager = null;
        mSelectionTitleView = null;
        mSelectionAllView = null;
        mFootCommonView.setEnabled(true);
        mFootDeleteOrSearchView.setEnabled(true);
    }

    public void setSelectionModeTitle(String title) {
        if (mSelectionTitleView != null) {
            mSelectionTitleView.setText(title);
        }
    }

    public void updateSelectionViewsState() {
        if (mSelectionAllView != null && mNoteSelectionManager != null) {
            int strId = mNoteSelectionManager.inSelectAllMode() ? R.string.unselect_all : R.string.select_all;
            mSelectionAllView.setText(strId);
            if (mNoteSelectionManager.getSelectedCount() == 0) {
                mFootCommonView.setEnabled(false);
                mFootDeleteOrSearchView.setEnabled(false);
            } else {
                mFootCommonView.setEnabled(true);
                mFootDeleteOrSearchView.setEnabled(true);
            }
        }
    }

    private void statistics(int stringId) {
        if (DEBUG) {
        }
    }

    public void checkShowEncryptUserGuide() {
        if (NoteShareDataManager.isShowEncryptUserGuide(this) == ENCRYPT_USER_GUIDE_SHOW) {
            showEncryptUserGuide();
        }
    }

    private void showEncryptUserGuide() {
        fullScreen(true);
        NoteShareDataManager.setIsEncryptUserGuideNormalClosed(this, false);
        mUserGuideShowing = true;
        mEncryptUserGuide = (RelativeLayout) findViewById(R.id.encrypt_user_guide);
        mEncryptUserGuide.setVisibility(View.VISIBLE);
        mEncryptUserGuide.setClickable(true);
        findViewById(R.id.btn_exit).setOnClickListener(this);
    }
    
    //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case NoteUtils.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                boolean isOk = true;
                for (int i = 0, length = grantResults.length; i < length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        isOk = false;
                        break;
                    }
                }
                Log.d(TAG, "wanghaiyan_isOk" + isOk);
                if (isOk) {
                    getContentResolver().notifyChange(NoteContract.NoteContent.CONTENT_URI, null);
                    if (PlatformUtil.isGioneeDevice()) {
                        initDataUpgrade();
                    } else {
                        BuiltInNote.insertBuildInNoteAsync();
                    }
                    return;
                }
                showRemind();
                finish();
            }
        }
            
    }
    //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end

    private void showRemind() {
        Toast.makeText(this, R.string.authorization_failed, Toast.LENGTH_SHORT).show();
    }

    private void showDataFlowDialog(Context context) {
        if (mDataFlowHintDialog == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.data_flow_dialog, null);
            final CyeeCheckBox checkBox = (CyeeCheckBox) view.findViewById(R.id.authority_alert_user_checkBox);
	        //GIONEE wanghaiyan 2016-12-08 modify for 40315 begin
	        if(ChameleonColorManager.isNeedChangeColor()){
               checkBox.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
	        }else{
               checkBox.setTextColor(ColorThemeHelper.getContentColorSecondaryOnBackgroud_C2(this));
	        }	
	        //GIONEE wanghaiyan 2016-12-08 modify for 40315 end
            CyeeTextView title = (CyeeTextView) view.findViewById(R.id.authority_alert_user_dialog_title);
            title.setVisibility(View.VISIBLE);
            title.setText(R.string.data_flow_alert_user_title);
            CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(context);
            ((CyeeTextView) view.findViewById(R.id.authority_alert_user_title)).setMovementMethod(new ScrollingMovementMethod());
            builder.setView(view);
            builder.setPositiveButton(R.string.alert_user_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (checkBox.isChecked()) {
                        NoteShareDataManager.setShowDataFlowHint(getApplicationContext(), true);
                    }
                }
            });
            builder.setNegativeButton(R.string.alert_user_cancle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            mDataFlowHintDialog = builder.create();
            mDataFlowHintDialog.setCanceledOnTouchOutside(false);
	        //Gionee wanghaiyan 2017-3-23 modify for 90801 begin
            mDataFlowHintDialog.setCancelable(true);
	        //Gionee wanghaiyan 2017-3-23 modify for 90801 end
            mDataFlowHintDialog.show();
        } else if (!mDataFlowHintDialog.isShowing()) {
            CheckBox checkBox = (CheckBox) mDataFlowHintDialog.getWindow().getDecorView().
                    findViewById(R.id.authority_alert_user_checkBox);
	        //GIONEE wanghaiyan 2016-12-08 modify for 40315 begin
	        if(ChameleonColorManager.isNeedChangeColor()){
               checkBox.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
	        }
	        //GIONEE wanghaiyan 2016-12-08 modify for 40315 end
            checkBox.setChecked(true);
            mDataFlowHintDialog.show();
        }
    }

    private void checkShowDataFlowHint() {
        if (!PlatformUtil.isGioneeDevice()) {
            return;
        }
        if (PlatformUtil.authorizeToPermisson()) {
            NoteShareDataManager.setShowDataFlowHint(this, true);
            return;
        }
        if (!NoteShareDataManager.getHasShowDataFlowHint(getApplicationContext())) {
            showDataFlowDialog(NoteMainActivity.this);
        }
    }
}
