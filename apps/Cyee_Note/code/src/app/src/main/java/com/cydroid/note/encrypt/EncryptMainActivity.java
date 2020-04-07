package com.cydroid.note.encrypt;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
//GIONEE wanghaiyan 2016-12-6 modify for 40498 for begin
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
//GIONEE wanghaiyan 2016-12-6 modify for 40498 for end

import com.cydroid.encryptspace.token.ITokenService;
import com.cydroid.note.R;
import com.cydroid.note.app.NoteMainFragment;
import com.cydroid.note.app.NoteSearchFragment;
import com.cydroid.note.app.NoteSelectionManager;
import com.cydroid.note.app.view.NoteSearchView;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.PlatformUtil;

import cyee.widget.CyeeTextView;

/**
 * Created by wuguangjie on 16-4-14.
 */
public class EncryptMainActivity extends StandardActivity implements View.OnClickListener {
    //    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String ACTION_ENTER_ENCRYPTSPACE = "com.gionee.encryptspace.enter";
    private static final String EXTRA_ENTER_PATH = "enter_path";
    private static final int ENCRYPT_MODULE_CALL = 1004;
    private static final String EXTRA_TOKEN = "extra_encrypt_token";
    private static final String ENTER_ENCRYPTSPACE = "com.gionee.encryptspace";
    private static final String ENTER_ENCRYPTSPACE_TOKEN_SERVICE = "com.gionee.encryptspace.token.TokenService";
    private static final String ACTION_PRIVATE_SPACE = "action.note.private.space";

    private RelativeLayout mFootActionsContainer;
    private TextView mFooterDecryptView;
    private TextView mFooterDelView;
    private static final int NORMAL = 0;
    private static final int DEDRYPT_OR_DELETE = 1;
    private int mActionsMode = NORMAL;
    private NoteSelectionManager mNoteSelectionManager;
    private TextView mSelectionTitleView;
    private TextView mSelectionAllView;
    private Drawable mEncrypIcon;
    private Drawable mDeleteIcon;
    private EncryptBroadcastReceiver mBroadcastReceiver = new EncryptBroadcastReceiver();
    private boolean mIsSecurityOS;
    private CyeeTextView mSecurityOSFooterSearchView;
    private CyeeTextView mSecurityOSFooterView;
    private ITokenService mTokenService;
    private ServiceConnection mTokenServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTokenService = ITokenService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTokenService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsSecurityOS = PlatformUtil.isSecurityOS();
        initView();
        initData();
        initFragment(savedInstanceState);
        registerBroadcastReceiver();
        if (mIsSecurityOS) {
            bindTokenService();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (ACTION_PRIVATE_SPACE.equals(getIntent().getAction())) {
            mBroadcastReceiver.setActivity(null);
        }
        unregisterReceiver(mBroadcastReceiver);
        if (mIsSecurityOS) {
            unbindService(mTokenServiceConn);
        }
        super.onDestroy();
    }

    private void initView() {
        if (mIsSecurityOS) {
            setSecurityOSTitleAndFootView();
        } else {
            setTitleAndFootView();
        }
        setNoteContentView(R.layout.encrypt_main_activity_content_layout);
        setNoteRootViewBackgroundColor();
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mBroadcastReceiver, filter);
        if (ACTION_PRIVATE_SPACE.equals(getIntent().getAction())) {
            mBroadcastReceiver.setActivity(this);
        }
    }

    private void initData() {
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false)
                || ("action.note.private.space".equals(getIntent().getAction()));
        mEncrypIcon = getDrawable(this, R.drawable.icon_encrypt, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));
        mDeleteIcon = getDrawable(this, R.drawable.note_main_del_icon, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));
    }

    private void setFootCommonViewState() {
        mFooterDelView.setCompoundDrawables(null, mDeleteIcon, null, null);
        mFooterDecryptView.setCompoundDrawables(null, mEncrypIcon, null, null);
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            NoteMainFragment noteMainFragment = new NoteMainFragment();
            ft.add(R.id.encrypt_fragment_container, noteMainFragment, "EncryptMainFragment");
            ft.commit();
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            }
        }
    }

    private void setTitleAndFootView() {
        if (DEDRYPT_OR_DELETE == mActionsMode) {
            setNoteFooterView(R.layout.encrypt_main_activity_footer_layout);
            mFootActionsContainer = (RelativeLayout) findViewById(R.id.footer_encrypt_action_container);
            mFooterDecryptView = (TextView) findViewById(R.id.footer_decrypt_action);
            mFooterDelView = (TextView) findViewById(R.id.footer_delete_action);
            mFooterDecryptView.setOnClickListener(this);
            mFooterDelView.setOnClickListener(this);
            setFootCommonViewState();
        } else {
            if (mIsSecurityOS) {
                setSecurityOSTitleAndFootView();
                return;
            }
            setNoteTitleView(R.layout.encrypt_main_activity_title_layout);
            ((TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_text))
                    .setText(R.string.secret_room);
            setNoteFooterView(-1);
            findViewById(R.id.encrypt_main_activity_title_layout_setting).setOnClickListener(this);
            findViewById(R.id.encrypt_main_activity_title_layout_search).setOnClickListener(this);
            findViewById(R.id.encrypt_main_activity_title_layout_back).setOnClickListener(this);
        }
    }

    private void setSecurityOSTitleAndFootView() {
        setNoteTitleView(R.layout.encrypt_main_activity_security_os_title_layout);
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false)
                || ("action.note.private.space".equals(getIntent().getAction()));
        ((TextView) findViewById(R.id.title))
                .setTextColor(getTitleTextColor());
        setNoteFooterView(R.layout.encrypt_main_activity_security_os_footer_layout);
        mSecurityOSFooterView = (CyeeTextView) findViewById(R.id.encrypt_main_activity_security_os_footer_desktop);
        Drawable drawable = getDrawable(this, R.drawable.security_os,
                ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));
        mSecurityOSFooterView.setCompoundDrawables(null, drawable, null, null);
        mSecurityOSFooterView.setOnClickListener(this);
        mSecurityOSFooterSearchView = (CyeeTextView) findViewById(R.id.encrypt_main_activity_security_os_footer_search);
        Drawable searchDrawable = getDrawable(this, R.drawable.note_main_activity_title_dw_search,
                ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));
        mSecurityOSFooterSearchView.setCompoundDrawables(null, searchDrawable, null, null);
        mSecurityOSFooterSearchView.setOnClickListener(this);
        findViewById(R.id.encrypt_main_activity_security_os_footer_add).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.encrypt_main_activity_title_layout_back:
                finish();
                break;
            case R.id.note_main_activity_search_title_layout_back:
                onNoteSearchBack();
                break;
            case R.id.encrypt_main_activity_title_layout_setting:
                goToSetting();
                break;
            case R.id.footer_decrypt_action:
                decryptNote();
                break;
            case R.id.footer_delete_action:
                deleteNote();
                break;
            case R.id.note_main_activity_action_mode_title_layout_back:
                onBack();
                break;
            case R.id.encrypt_main_activity_title_layout_search:
            case R.id.encrypt_main_activity_security_os_footer_search:
                onNoteSearch();
                break;
            case R.id.encrypt_main_activity_security_os_footer_desktop:
                openPrivateLauncher();
                break;
            case R.id.encrypt_main_activity_security_os_footer_add:
                addEncryptNotes();
                break;
            default:
                break;
        }
    }

    private void openPrivateLauncher() {
        String token = null;
        try {
            if (mTokenService != null) {
                token = mTokenService.getToken();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(ACTION_ENTER_ENCRYPTSPACE);
        intent.putExtra(EXTRA_ENTER_PATH, ENCRYPT_MODULE_CALL);
        intent.putExtra(EXTRA_TOKEN, token);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getTaskId();
        if (PlatformUtil.isSecurityOS()) {
            EncryptUtil.removeCurrentTask(this);
        }
    }


    private void bindTokenService() {
        Intent intent = new Intent();
        intent.setClassName(ENTER_ENCRYPTSPACE, ENTER_ENCRYPTSPACE_TOKEN_SERVICE);
        bindService(intent, mTokenServiceConn, Service.BIND_AUTO_CREATE);
    }

    private void addEncryptNotes() {
        Intent intent = new Intent(this, EncryptSelectActivity.class);
        intent.putExtra(Constants.IS_SECURITY_SPACE, true);
        startActivity(intent);
    }

    private void onNoteSearchBack() {
        findViewById(R.id.note_search_view).clearFocus();
        getFragmentManager().popBackStack();
    }

    private void onNoteSearch() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        NoteSearchFragment noteSearchFragment = new NoteSearchFragment();
        ft.replace(R.id.encrypt_fragment_container, noteSearchFragment, "NoteSearchFragment");
        ft.addToBackStack(null);
        ft.commit();
        setNoteFooterView(-1);
    }

    public void endNoteSearch() {
        setTitleAndFootView();
    }

    public void beginNoteSearch(NoteSearchView.OnQueryTextListener listener) {
        setNoteTitleView(R.layout.note_main_activity_search_title_layout);
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false)
                || ("action.note.private.space".equals(getIntent().getAction()));
        tintImageViewDrawable(R.id.note_main_activity_search_title_layout_back,
                R.drawable.note_title_back_icon, ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));
        findViewById(R.id.note_main_activity_search_title_layout_back).setOnClickListener(this);
        NoteSearchView searchView = (NoteSearchView) findViewById(R.id.note_search_view);
        searchView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.searchview_zoom));
        searchView.setOnQueryTextListener(listener);
	    //GIONEE wanghaiyan 2016-12-6 modify for 40498 for begin
        EditText mInputMsgView = (EditText) searchView.findViewById(R.id.search_input_msg_edit_text);
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(mInputMsgView, 0);
        }
	    //GIONEE wanghaiyan 2016-12-6 modify for 40498 for end
    }

    private void deleteNote() {
        NoteMainFragment fragment = (NoteMainFragment) getFragmentManager().
                findFragmentById(R.id.encrypt_fragment_container);
        if (null != fragment) {
            fragment.onDel();
        }
    }

    private void decryptNote() {
        NoteMainFragment fragment = (NoteMainFragment) getFragmentManager().
                findFragmentById(R.id.encrypt_fragment_container);
        if (null != fragment) {
            fragment.onDecrypt();
        }
    }

    private void goToSetting() {
        try {
            Intent intent = new Intent();
            intent.setClass(this, EncryptSettingActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void onBack() {
        mActionsMode = NORMAL;
        setTitleAndFootView();
    }

    @Override
    public void onBackPressed() {
        if (mNoteSelectionManager != null && mNoteSelectionManager.inSelectionMode()) {
            mNoteSelectionManager.leaveSelectionMode();
            return;
        }
        super.onBackPressed();
    }

    public void startSelectionMode(NoteSelectionManager noteSelectionManager, View.OnClickListener listener) {
        mActionsMode = DEDRYPT_OR_DELETE;
        setTitleAndFootView();
        mNoteSelectionManager = noteSelectionManager;
        setNoteTitleView(R.layout.note_main_activity_action_mode_title_layout);
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false)
                || ("action.note.private.space".equals(getIntent().getAction()));
        tintImageViewDrawable(R.id.note_main_activity_action_mode_title_layout_back,
                R.drawable.note_title_back_icon, ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));
        ((TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_text))
                .setTextColor(getTitleTextColor());
        ((TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_select))
                .setTextColor(getTitleTextColor());
        findViewById(R.id.note_main_activity_action_mode_title_layout_back).setOnClickListener(listener);
        mSelectionAllView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_select);
        mSelectionAllView.setOnClickListener(listener);
        mSelectionTitleView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_text);
    }


    public void finishSelectionMode() {
        mActionsMode = NORMAL;
        setTitleAndFootView();
        mNoteSelectionManager = null;
        mSelectionTitleView = null;
        mSelectionAllView = null;
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
                mFooterDelView.setEnabled(false);
                mFooterDecryptView.setEnabled(false);
            } else {
                mFooterDelView.setEnabled(true);
                mFooterDecryptView.setEnabled(true);
            }
        }
    }

    public void updateSecurityOSFooterViewsState(boolean enable) {
        if (null != mSecurityOSFooterSearchView) {
            mSecurityOSFooterSearchView.setEnabled(enable);
        }
    }
}
