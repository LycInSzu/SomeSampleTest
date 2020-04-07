package com.cydroid.note.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import com.cydroid.note.common.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//GIONEE wanghaiyan 2016-11-25 modify for 32782 begin
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
//GIONEE wanghaiyan 2016-11-25 modify for 32782 end

import com.cydroid.note.R;
import com.cydroid.note.ai.AIActivity;
//GIONEE wanghaiyan 2016-12-02 modify for 37596 begin
//import com.cydroid.note.ai.AITipView;
//GIONEE wanghaiyan 2016-12-02 modify for 37596 end
import com.cydroid.note.app.LabelSelector.OnLabelChangedListener;
import com.cydroid.note.app.attachment.AttachmentSelector;
import com.cydroid.note.app.attachment.SoundRecorder;
import com.cydroid.note.app.dialog.DateTimeDialog;
import com.cydroid.note.app.effect.DrawableManager;
import com.cydroid.note.app.effect.EffectUtil;
import com.cydroid.note.app.reminder.ReminderManager;
import com.cydroid.note.app.span.JsonableSpan;
import com.cydroid.note.app.span.SoundImageSpan;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.app.view.NoteContentEditText;
import com.cydroid.note.app.view.NoteTitleEditText;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.common.UpdateHelper;
import com.cydroid.note.data.LocalNoteItem;
import com.cydroid.note.data.NoteInfo;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.encrypt.EncryptHintManager;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.NoteActionProgressListener;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteShareDataManager;
import com.cydroid.note.widget.WidgetUtil;
//GIONEE wanghaiyan 2016 -12-13 modify for 45337 begin
import com.cydroid.note.common.FileUtils;
//GIONEE wanghaiyan 2016 -12-13 modify for 45337 end
//Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 begin
import com.cydroid.note.common.ExternalPermissionsManager;
import com.cydroid.note.common.StorageManagerHelper;
import android.os.storage.StorageVolume;
//Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 end

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeCheckBox;
//GIONEE wanghaiyan 2016-12-08 modify for 40315 begin
import cyee.changecolors.ChameleonColorManager;
//GIONEE wanghaiyan 2016-12-08 modify for 40315 end
//Gionee wanghaiyan 20170307 add for 77568 begin
import android.database.Cursor;
import com.Legal.Java.*;;
//Gionee wanghaiyan 20170307 add for 77568 end
public class NewNoteActivity extends StandardActivity implements OnClickListener, IYouJuCallback,
        NoteDbInitCompleteNotify {
    private static final String TAG = "NewNoteActivity";
    public static final int REQUEST_PICK_IMAGE = 1;
    public static final int REQUEST_TAKE_PHOTO = 2;
    public static final int REQUEST_CUSTOM_LABEL = 3;
    //Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 begin
    public static final int REQUEST_RECORDER = 4;
    //Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 end
    public static final String ENABLE_EDIT_MODE = "enable_edit_mode";
    private static final String KEY_TITLE = "note_title";
    private static final String KEY_CONTENT = "note_content";
    private static final String KEY_PIC_URI = "note_pic_uri";
    private static final String CAMERA_ACTION = "note.intent.action.from.camera";

    public static final String NOTE_ITEM_PATH = "path";
    private static final String SAVE_INSTANCE_ID = "id";
    private static final String SAVE_TAKE_PHOTO_URI = "take_photo_uri";
    public static final String FROM_INNER_CONTEXT = "from_inner_context";

    private static final long NOTE_SAVE_DURATION = 10000L;
    private ContentResolver mResolver;
    private LabelManager mLabelManager;
    private boolean mActive = false;
    private boolean mIsOnResumed;
    private LinearLayout mLabelView;
    private ViewGroup mLabelContent;
    private NoteContentEditText mContentEditText;
    private NoteTitleEditText mTitleEditText;
    private View mSelectLabel;
    private View mSelectBill;
    private View mSelectReminder;
    private View mSelectRecord;
    private View mSelectGallery;
    private View mSelectCamera;
    //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 begin
    //private View mSelectOnlineImage;
    //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 end
    private ImageView mShare;
    private ImageView mEncryptAndDelete;
    private Uri mTakePhotoOutputUri;
    private AttachmentSelector mAttachmentSelector;
    private LabelSelector mLabelSelector;
    private NoteInfo mPreNoteInfo = new NoteInfo();
    private NoteInfo mCurrNoteInfo = new NoteInfo();
    private final long mModifiedTime = System.currentTimeMillis();
    private LabelManager.LabelDataChangeListener mLabelDataChangeListener;

    private boolean mIsEditMode;
    private boolean mNoteInfoInitSuccess;
    private NoteActionExecutor mExecutor;
    private long mOldModifyTimeMillis;
    private PreviewPictureMakeProxy mPreviewPictureMakeProxy;
    private int[] mCurDate;
    //GIONEE wanghaiyan 2016-12-02 modify for 37596 begin
    //private AITipView mAITipView;
    //GIONEE wanghaiyan 2016-12-02 modify for 37596 end
    private boolean mIsAiSwitchOpen;
    public boolean mIsEncrypted = false;
    private EncryptHintManager mEncryptHintManager;
    private CyeeAlertDialog mTitleMoreDialog;
    private Dialog mDateTimeDialog;
    private volatile boolean mCancelBgTask;
    private boolean mIsFromCamera;
    private ToastManager mToastManager;
    protected Handler mBackgroundHandler;
    protected HandlerThread mHandlerThread;
    private boolean mRecording;
    private PowerManager.WakeLock mWakeLock;
    //GIONEE wanghaiyan 2016-11-25 modify for 32782 begin
    private static final int REQUEST_PERMISSION_TAKE_PHOTOS= 1;
    private static final int REQUEST_PERMISSION_RECORD_AUDIO= 2;
    private static final int REQUEST_PERMISSION_SELECT_IMAGE= 3;
    //GIONEE wanghaiyan 2016-11-25 modify for 32782 end

    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
    private boolean previewBtnClicked=false;
    private boolean isNoteChanged=false;
    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883)end

    //wanghaiyan 2017-9-18 modify for 202128 begin
    private Bitmap bgBitmap;
    //wanghaiyan 2017-9-28 modify for 226343 begin
    private boolean mShouldSaveRecord;
    //wanghaiyan 2017-9-28 modify for 226343 end
    //Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 begin
    private StorageVolume mExternalStorageVolume;
	//Chenyee wanghaiyan 2018-10-25 modify for CSW1805A-1159 begin
    private String rootPath;
	//Chenyee wanghaiyan 2018-10-25 modify for CSW1805A-1159 end
    //Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 end
    private Runnable mSaveRunnable = new Runnable() {
        @Override
        public void run() {
            //Gionee wanghaiyan 2017-9-14 modify for 205814 begin
            saveNote();
            //Gionee wanghaiyan 2017-9-14 modify for 205814 end
            if (mActive) {
                mMainHandler.postDelayed(mSaveRunnable, NOTE_SAVE_DURATION);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NoteAppImpl.getContext().registerNoteDbInitCompleteNotify(this);
        initContentView();
        initView();
        initData(savedInstanceState);
        initListener();
        ckeckShowDataFlowHint();
        //Chenyee wanghaiyan 2018-10-26 modify for CSW1805A-780 begin
        if (!NoteUtils.checkNeededPermissionForRecord(this)) {
            return;
        }
        //Chenyee wanghaiyan 2018-10-26 modify for CSW1805A-780 end

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!mIsOnResumed) {
            return;
        }
        //wanghaiyan 2017-9-28 modify for 226343 begin
        mShouldSaveRecord = false;
        //wanghaiyan 2017-9-28 modify for 226343 end
        reset(intent);
        String action = intent.getAction();
        if (CAMERA_ACTION.equals(action)) {
            initFromCamera(intent);
        } else if (Intent.ACTION_SEND.equals(action)) {
            initFromShare(intent);
        } else {
            initNoteInfo(intent, null);
        }
        super.onNewIntent(intent);
    }

    private void reset(Intent intent) {
        if (isShouldDelete()) {
            deleteEmptyNote();
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mCancelBgTask = true;
        } else {
            mCancelBgTask = false;
        }
        mBackgroundHandler.removeCallbacks(null);
        mMainHandler.removeCallbacks(null);
        clearSpans();
        dismissShowingDialogs(intent);
        mLabelContent.removeAllViews();
        mTitleEditText.getText().clear();
        mContentEditText.setText("");
    }

    private void dismissShowingDialogs(Intent intent) {

        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
        if (null != mPreviewPictureMakeProxy) {
            mPreviewPictureMakeProxy.cancel();
        }
        if (null != mLabelSelector && !isTheSameNote(intent)) {
            mLabelSelector.cancel();
        }
        if (null != mDateTimeDialog && mDateTimeDialog.isShowing() && !isTheSameNote(intent)) {
            mDateTimeDialog.dismiss();
        }
        if (null != mAttachmentSelector && !isTheSameNote(intent)) {
            mAttachmentSelector.cancel();
            mAttachmentSelector.dismissPicSelectDialog();
        }
        if (null != mTitleMoreDialog && mTitleMoreDialog.isShowing() && !isTheSameNote(intent)) {
            mTitleMoreDialog.dismiss();
        }
        if (null != mEncryptHintManager && !isTheSameNote(intent)) {
            mEncryptHintManager.dismissDialog();
        }
    }

    private boolean isTheSameNote(Intent intent) {
        String path = intent.getStringExtra(NOTE_ITEM_PATH);
        int id = NoteUtils.getIdFromPath(path, mIsEncrypted);
        if (id == mCurrNoteInfo.mId) {
            return true;
        }
        return false;
    }

    private void initContentView() {
        setNoteTitleView(R.layout.new_note_activity_title_layout);
        setNoteContentView(R.layout.new_note_activity_content_layout);
        setNoteFooterView(R.layout.new_note_activity_footer_layout);
    }

    private void initListener() {
        mSelectBill.setOnClickListener(this);
        mSelectLabel.setOnClickListener(this);
        mSelectRecord.setOnClickListener(this);
        mSelectReminder.setOnClickListener(this);
        mSelectCamera.setOnClickListener(this);
        mSelectGallery.setOnClickListener(this);
        //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 begin
        //mSelectOnlineImage.setOnClickListener(this);
        //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 end
        mShare.setOnClickListener(this);
        mEncryptAndDelete.setOnClickListener(this);

        ImageView back = (ImageView) findViewById(R.id.new_note_activity_title_layout_back);
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        tintImageViewDrawable(R.id.new_note_activity_title_layout_back, R.drawable.note_title_back_icon,
                ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));
        back.setOnClickListener(this);
        mTitleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setFootActionEnable(false);
                    mTitleEditText.setHint(R.string.title_focus_hint);
                } else {
                    mTitleEditText.setHint(R.string.title_hint);
                }
            }
        });
        mContentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setFootActionEnable(true);
                }
            }
        });
    }

    private void initView() {
        mLabelView = (LinearLayout) findViewById(R.id.new_note_label);
        mLabelContent = (ViewGroup) mLabelView.findViewById(R.id.new_note_label_content);
        mContentEditText = (NoteContentEditText) findViewById(R.id.new_note_content);
        mContentEditText.setMovementMethod(new EditMovementMethod(this));
        mTitleEditText = (NoteTitleEditText) findViewById(R.id.new_note_title);
        mTitleEditText.setMovementMethod(new EditMovementMethod(this));
        mSelectBill = findViewById(R.id.action_bill);
        mSelectLabel = findViewById(R.id.action_label);
        mSelectRecord = findViewById(R.id.action_recorde);
        mSelectReminder = findViewById(R.id.action_reminder);
        mSelectCamera = findViewById(R.id.action_camera);
        mSelectGallery = findViewById(R.id.action_gallery);
        //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 begin
        //mSelectOnlineImage = findViewById(R.id.action_online_image);
        //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 end
        mShare = (ImageView) findViewById(R.id.new_note_activity_title_layout_share);
        mEncryptAndDelete = (ImageView) findViewById(R.id.new_note_activity_title_layout_delete);

        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        tintImageViewDrawableList(R.id.new_note_activity_title_layout_share,
                R.drawable.action_preview_enable, ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));

        tintImageViewDrawableList(R.id.new_note_activity_title_layout_delete,
                R.drawable.title_more, ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));

        tintImageViewDrawableList(R.id.action_label,
                R.drawable.action_label_icon, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));

        tintImageViewDrawableList(R.id.action_bill,
                R.drawable.action_bill_icon, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));

        tintImageViewDrawableList(R.id.action_reminder,
                R.drawable.action_alert_icon, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));

        tintImageViewDrawableList(R.id.action_recorde,
                R.drawable.attachment_sound_recorder, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));

        tintImageViewDrawableList(R.id.action_gallery,
                R.drawable.attachment_select_image, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));

        tintImageViewDrawableList(R.id.action_camera,
                R.drawable.attachment_take_photos, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));

        //gionee wanghaiyan add on 2016-08-03 for CR01739902 begin
        mContentEditText.setInputContentMaxSize();//最大输入值为10000
        //gionee wanghaiyan add on 2016-08-03 for CR01739902 end

        //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 begin
        //tintImageViewDrawableList(R.id.action_online_image,
        //      R.drawable.attachment_online_image, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));
        //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 end
        ////GIONEE wanghaiyan 2016-12-02 modify for 37596 begin
        /*
        if (NoteShareDataManager.isAISwitchOpen(this)) {
            ViewStub stub = (ViewStub) findViewById(R.id.ai_tip_view_stub);
            stub.inflate();
            AITipView aiTipView = (AITipView) findViewById(R.id.ai_tip_image);
            mAITipView = aiTipView;
            aiTipView.setAICallback(new AITipView.AITipCallback() {
                @Override
                public String requestContent() {
                    if (mContentEditText != null) {
                        return mContentEditText.getText().toString();
                    }
                    return null;
                }

                @Override
                public void resultKeyWords(ArrayList<String> keywords) {
                    startAIActivity(keywords);
                }
            });
        }
	   */
        //GIONEE wanghaiyan 2016-12-02 modify for 37596 end

    }

    private void startAIActivity(ArrayList<String> keywords) {
        try {
            Intent intent = new Intent();
            intent.setClass(this, AIActivity.class);
            intent.putExtra(AIActivity.KEY_AMI_Recommend, keywords);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void tintImageViewDrawableList(int imageViewId, int iconId, ColorStateList colorLists) {
        Drawable icon = ContextCompat.getDrawable(this, iconId);
        Drawable tintIcon = DrawableCompat.wrap(icon);
        DrawableCompat.setTintList(tintIcon, colorLists);
        ((ImageView) findViewById(imageViewId)).setImageDrawable(tintIcon);
    }

    private void refreshLabel(ArrayList<Integer> ids) {
        ArrayList<Integer> currLabels = mCurrNoteInfo.mLabel;
        currLabels.clear();
        currLabels.addAll(ids);
    }

    private void showLabel() {
        mLabelContent.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        ArrayList<Integer> labels = mCurrNoteInfo.mLabel;
        ArrayList<Integer> invalidLabels = new ArrayList<>();
        for (Integer id : labels) {
            String label = mLabelSelector.getLabelContentById(id);
            if (TextUtils.isEmpty(label)) {
                invalidLabels.add(id);
                continue;
            }
            TextView view = (TextView) inflater.inflate(R.layout.edit_page_label_item, null);
            view.setText(label);
            mLabelContent.addView(view);
        }
        if (invalidLabels.size() > 0) {
            labels.removeAll(invalidLabels);
        }
        int childCount = mLabelContent.getChildCount();
        if (childCount > 0) {
            mLabelView.setVisibility(View.VISIBLE);
        } else {
            mLabelView.setVisibility(View.GONE);
        }
    }

    private int getSaveNoteId(Bundle savedInstanceState) {
        int saveId = NoteItem.INVALID_ID;
        if (savedInstanceState != null) {
            saveId = savedInstanceState.getInt(SAVE_INSTANCE_ID, NoteItem.INVALID_ID);
        }
        return saveId;
    }

    private void initNoteInfo(NoteInfo noteInfo) {
        mPreNoteInfo.mId = noteInfo.mId;
        mCurrNoteInfo.mId = mPreNoteInfo.mId;
        mPreNoteInfo.mDateReminderInMs = noteInfo.mDateReminderInMs;
        mCurrNoteInfo.mDateReminderInMs = mPreNoteInfo.mDateReminderInMs;
        mPreNoteInfo.mEncyptHintState = noteInfo.mEncyptHintState;
        mCurrNoteInfo.mEncyptHintState = mPreNoteInfo.mEncyptHintState;
        mCurrNoteInfo.mDateModifiedInMs = noteInfo.mDateModifiedInMs;
        ArrayList<Integer> labels = noteInfo.mLabel;
        mPreNoteInfo.mLabel.clear();
        mCurrNoteInfo.mLabel.clear();
        mPreNoteInfo.mLabel.addAll(labels);
        mCurrNoteInfo.mLabel.addAll(labels);
        String title = noteInfo.mTitle;
        String jsonContent = noteInfo.mContent;

        if (!TextUtils.isEmpty(jsonContent)) {
            DataConvert.applySpanToEditableFromJson(this, jsonContent, mContentEditText, false);
        }

        mTitleEditText.getText().append(title);
        mContentEditText.setNoteTime(noteInfo.mDateModifiedInMs);
        mContentEditText.setHint(R.string.content_hint);
        mOldModifyTimeMillis = noteInfo.mDateModifiedInMs;
        mContentEditText.setReminderTime(mCurrNoteInfo.mDateReminderInMs);
        if (mCurrNoteInfo.mDateReminderInMs != NoteItem.INVALID_REMINDER) {
            ReminderManager.cancelAlarmAndNotification(this, mCurrNoteInfo.mId, false);
        }
        showLabel();
        //Chenyee wanghaiyan add on 2018-1-27 for SW17W16A-2093 begin
        mNoteTime=noteInfo.mDateModifiedInMs;
        //Chenyee wanghaiyan add on 2018-1-27 for SW17W16A-2093 end
    }

    private void initNoteInfoFromDB(final int id) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final NoteInfo noteInfo = NoteUtils.getNoteItemFromDB(id, mIsEncrypted);
                Runnable update = new Runnable() {
                    @Override
                    public void run() {
                        mNoteInfoInitSuccess = (null != noteInfo);
                        if (!mNoteInfoInitSuccess) {
                            insertEmptyNoteInfo();
                            return;
                        }
                        initNoteInfo(noteInfo);
                        initWatcher();
                        initCurDate();
                        updateActivityWindowBackground();
                        checkSetEmptyState();
                    }
                };
                mMainHandler.post(update);
            }
        };
        mBackgroundHandler.post(runnable);
    }

    private void checkSetEmptyState() {
        boolean enable = !isShouldDelete();
        mShare.setEnabled(enable);
        mEncryptAndDelete.setEnabled(enable);
    }

    private void initCurDate() {
        mCurDate = NoteUtils.getToady();
    }

    //Gionee wanghaiyan 2017-9-18 modify for 202128 begin
    private void recyclebgBitmap(){
        if (bgBitmap != null && !bgBitmap.isRecycled()){
            bgBitmap.isRecycled();
            bgBitmap = null;
            System.gc();
        }
    }
    private void recycleBitmap(Bitmap bitmap){
        if (bitmap != null && !bitmap.isRecycled()){
            bitmap.isRecycled();
            bitmap = null;
            System.gc();
        }
    }
    private void updateActivityWindowBackground() {
        EffectUtil effectUtil = new EffectUtil(System.currentTimeMillis());
        int effect = effectUtil.getEffect(mOldModifyTimeMillis);
        //Gionee wanghaiyan 2017-9-18 modify for 202128 begin
        /*Bitmap bgBitmap = DrawableManager.getEffectBitmap(NewNoteActivity.this, effect,
                NoteUtils.sScreenWidth, NoteUtils.sScreenHeight);*/
        recyclebgBitmap();
        bgBitmap = DrawableManager.getEffectBitmap(NewNoteActivity.this, effect,
                NoteUtils.sScreenWidth, NoteUtils.sScreenHeight);
        Drawable bgDrawable = null;
        if (bgBitmap != null) {
            bgDrawable = new BitmapDrawable(getResources(), bgBitmap);
        }
        NewNoteActivity.this.getWindow().setBackgroundDrawable(bgDrawable);
    }

    private void insertEmptyNoteInfo() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final String title = "";
                final String jsonContent = "";
                mOldModifyTimeMillis = mModifiedTime;
                final NoteInfo info = new NoteInfo();
                info.mDateModifiedInMs = mModifiedTime;
                final int id = NoteUtils.addNoteData(title, jsonContent, mResolver,
                        info.mDateModifiedInMs, info.mDateReminderInMs, info.mLabel,
                        Constants.ENCRYPT_HINT_ABLE, info.mEncrytRemindReadState, mIsEncrypted);
                //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
                mNoteId=id;
                //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
                Runnable update = new Runnable() {
                    @Override
                    public void run() {
                        updateActivityWindowBackground();
                        mPreNoteInfo.mId = id;
                        mCurrNoteInfo.mId = id;
                        mCurrNoteInfo.mDateModifiedInMs = mModifiedTime;
                        initCurDate();
                        initWatcher();
                        mContentEditText.setNoteTime(mModifiedTime);
                        //Chenyee wanghaiyan add on 2018-1-27 for SW17W16A-2093 begin
                        mNoteTime=mModifiedTime;
                        //Chenyee wanghaiyan add on 2018-1-27 for SW17W16A-2093 end
                        checkSetEmptyState();
                    }
                };
                mMainHandler.post(update);
            }
        };
        mBackgroundHandler.post(runnable);
    }

    private void initNoteInfo(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String action = intent.getAction();
        if (CAMERA_ACTION.equals(action)) {
            initFromCamera(intent);
        } else if (Intent.ACTION_SEND.equals(action)) {
            initFromShare(intent);
        } else {
            initNoteInfo(intent, savedInstanceState);
        }
    }

    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
    private int mNoteId;
    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
    private void initNoteInfo(Intent intent, Bundle savedInstanceState) {
        String path = intent.getStringExtra(NOTE_ITEM_PATH);
        int id = NoteUtils.getIdFromPath(path, mIsEncrypted);
        //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
        mNoteId=id;
        //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
        if (NoteItem.INVALID_ID == id) {
            id = getSaveNoteId(savedInstanceState);
            //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
            mNoteId=id;
            //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
            if (NoteItem.INVALID_ID != id) {
                initNoteInfoFromDB(id);
            } else {
                insertEmptyNoteInfo();
            }
        } else {
            initNoteInfoFromDB(id);
        }
    }

    private void initFromCamera(final Intent intent) {
        String title = intent.getStringExtra(KEY_TITLE);
        String content = intent.getStringExtra(KEY_CONTENT);
        String picPath = intent.getStringExtra(KEY_PIC_URI);
        Uri uri = null;
        if (!TextUtils.isEmpty(picPath)) {
            uri = Uri.parse(picPath);
        }
        initNoteInfo(title, content, uri);
        mIsFromCamera = true;
    }

    private void initFromShare(final Intent intent) {
        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        String content = intent.getStringExtra(Intent.EXTRA_TEXT);
        Uri picUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        initNoteInfo(title, content, picUri);
    }

    private void initNoteInfo(final String title, final String content, final Uri uri) {
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                insertEmptyNoteInfo();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mTitleEditText.setText(title);
                        mContentEditText.setText(content);
                        String type = getIntent().getType();
                        if (null != uri && !TextUtils.isEmpty(type) && "text/plain".equals(type)) {
                            new ToastManager(NoteAppImpl.getContext()).showToast(R.string.add_attach_fail);
                        } else if (null != uri) {
                            asynAddImage(uri, true);
                        }
                    }
                };
                mMainHandler.post(runnable);
            }
        });
    }

    private void initData(Bundle savedInstanceState) {
        mHandlerThread = new HandlerThread("new_note_bg_thread");
        mHandlerThread.start();
        mBackgroundHandler = new Handler(mHandlerThread.getLooper());
        mToastManager = new ToastManager(this);
        mAttachmentSelector = new AttachmentSelector(this, new AttachmentSelector.OnTakePhotoListener() {
            @Override
            public void onStartTakePhoto(Uri outputUri) {
                mTakePhotoOutputUri = outputUri;
            }
        }, mOnSelectPicToAddListener);
        mResolver = getContentResolver();
        mLabelSelector = new LabelSelector(this, new OnLabelChangedListener() {
            @Override
            public void onLabelChanged(ArrayList<Integer> ids) {
                refreshLabel(ids);
            }

            @Override
            public void onUpdate() {
                showLabel();
                checkSetEmptyState();
            }
        });
        mLabelSelector.setYouJuCb(this);
        NoteAppImpl app = (NoteAppImpl) getApplication();
        mLabelManager = app.getLabelManager();
        mLabelSelector.setLabels(mLabelManager.getLabelList());
        mLabelDataChangeListener = new LabelManager.LabelDataChangeListener() {
            @Override
            public void onDataChange() {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLabelSelector.setLabels(mLabelManager.getLabelList());
                    }
                });
            }
        };
        mLabelManager.addLabelDataChangeListener(mLabelDataChangeListener);
        initEditMode(savedInstanceState);
        initNoteInfo(savedInstanceState);

        mExecutor = new NoteActionExecutor(this);
        mEncryptHintManager = new EncryptHintManager(new EncryptHintManager.EncryptHintListner() {
            @Override
            public void onEncrypt(boolean hintAgain) {
                if (!hintAgain) {
                    mCurrNoteInfo.mEncyptHintState = Constants.ENCRYPT_HINT_DISABLE;
                }
                if (PlatformUtil.isSecurityOS() && !EncryptUtil.isDialcodeOpen(getContentResolver())) {
                    EncryptUtil.startDialSettingInterface(NewNoteActivity.this);
                } else {
                    encrypt();
                }
            }

            @Override
            public void onCancleEncrypt(boolean hintAgain) {
                if (!hintAgain) {
                    mCurrNoteInfo.mEncyptHintState = Constants.ENCRYPT_HINT_DISABLE;
                    //Gionee wanghaiyan 2017-9-14 modify for 205814 begin
                    saveNote();
                    //Gionee wanghaiyan 2017-9-14 modify for 205814 end
                }
            }
        });
    }

    private AttachmentSelector.OnSelectPicToAddListener mOnSelectPicToAddListener =
            new AttachmentSelector.OnSelectPicToAddListener() {
                @Override
                public void onAdd(CopyOnWriteArrayList<String> selectPicUris) {
                    final CopyOnWriteArrayList<String> lists = new CopyOnWriteArrayList<>(selectPicUris);
                    if (null != lists && lists.size() > 0) {
                        showProgressDialog(lists.size(), lists.size(),
                                NoteAppImpl.getContext().getString(R.string.add_pics));
                    }
                    mBackgroundHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            addImageFromAttach(lists);
                        }
                    });
                }
            };

    private void addImageFromAttach(CopyOnWriteArrayList<String> lists) {
        for (String picUri : lists) {
            if (mCancelBgTask) {
                break;
            }
            boolean hasEnoughFreeMemory = NoteUtils.checkEnoughFreeMemory();
            mAddPicNum++;
            if (hasEnoughFreeMemory && !mContentEditText.isSelectPositionReachMaxSize()) {
                addImage(Uri.parse(picUri), true, true);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mProgressDialog) {
                            mProgressDialog.setProgress(mAddPicNum);
                        }
                    }
                });
            } else {
                Runnable toast = new Runnable() {
                    @Override
                    public void run() {
                        dissmissProgressDialog();
                        mToastManager.showToast(getString(R.string.max_pic_input_mum_limit));
                    }
                };
                mMainHandler.post(toast);
                break;
            }
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                dissmissProgressDialog();
                mAddPicNum = 0;
                mCancelBgTask = false;
            }
        });
    }

    private void dissmissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void initEditMode(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return;
        }
        mIsEditMode = getIntent().getBooleanExtra(ENABLE_EDIT_MODE, false);
        if (mIsEditMode) {
            mContentEditText.requestFocus();
        } else {
            mContentEditText.setShowSoftInputOnFocus(false);
            mContentEditText.setCursorVisible(false);
            getFooterView().setVisibility(View.GONE);
        }
    }

    public void hideSoftInput() {
        if (mContentEditText.getShowSoftInputOnFocus()) {
            mContentEditText.setShowSoftInputOnFocus(false);
        }
    }

    public void showSoftInput() {
        if (!mContentEditText.getShowSoftInputOnFocus()) {
            mContentEditText.setShowSoftInputOnFocus(true);
        }
    }

    public void enterEditMode() {
        showSoftInput();

        if (mIsEditMode) {
            return;
        }
        mIsEditMode = true;
        getFooterView().setVisibility(View.VISIBLE);
        mContentEditText.setCursorVisible(true);
    }

    private void setFootActionEnable(boolean enable) {
        mSelectLabel.setEnabled(enable);
        mSelectBill.setEnabled(enable);
        mSelectReminder.setEnabled(enable);
        mSelectRecord.setEnabled(enable);
        mSelectGallery.setEnabled(enable);
        mSelectCamera.setEnabled(enable);
        //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 begin
        //mSelectOnlineImage.setEnabled(enable);
        //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 end
    }

    private void addImage(Uri imageUri, boolean photoFromOut, boolean fromAttach) {
        Context context = getApplicationContext();
        //Gionee wanghaiyan 2017-5-16 modify for 142327 begin
        if (!mContentEditText.checkContentKeepMore16CharsRemaing()) {
            sendMagAddImageFail(R.string.add_image_fail);
            return;
        }
        //Gionee wanghaiyan 2017-5-16 modify for 142327 end
        final Uri fileUri = NoteUtils.convertToFileUri(context, imageUri);
        Bitmap bitmap = NoteUtils.getAddBitmapFromUri(context, fileUri);
        if (bitmap == null) {
            //Gionee wanghaiyan 2017-3-28 modify for 96878 begin
            sendMagAddImageFail(R.string.add_image_fail);
            //Gionee wanghaiyan 2017-3-28 modify for 96878 end
            return;
        }



        int bitmapHeight = bitmap.getHeight();
        if (bitmapHeight > 2 * NoteUtils.sScreenHeight) {
            //Gionee wanghaiyan 2017-3-28 modify for 96878 begin
            //mToastManager.showToast(R.string.long_bitmap_add_failed);
            sendMagAddImageFail(R.string.long_bitmap_add_failed);
            //Gionee wanghaiyan 2017-3-28 modify for 96878 end
            //Gionee wanghaiyan 2017-9-18 modify for 202128 begin
            recycleBitmap(bitmap);
            //Gionee wanghaiyan 2017-9-18 modify for 202128 end
            return;
        }

        File thumbFile = NoteUtils.getSaveBitmapFile(context);
        final Uri thumbnailUri = Uri.fromFile(thumbFile);
        NoteUtils.saveBitmap(bitmap, thumbFile);
        if (photoFromOut) {
            sendMsgAddImage(thumbnailUri, thumbnailUri, bitmap, fromAttach);
        } else {
            sendMsgAddImage(thumbnailUri, fileUri, bitmap, fromAttach);
        }
    }

    //Gionee wanghaiyan 2017-3-28 modify for 96878 begin
    private void sendMagAddImageFail(final int failMsgId) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(context, R.string.add_image_fail, Toast.LENGTH_SHORT).show();
                mToastManager.showToast(failMsgId);
            }
        });

    }
    //Gionee wanghaiyan 2017-3-28 modify for 96878 end

    private void sendMsgAddImage(final Uri thumbnailUri, final Uri fileUri, final Bitmap bitmap,
                                 final boolean fromAttach) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!fromAttach || !mCancelBgTask) {
                    mContentEditText.insertPhoto(thumbnailUri, fileUri, bitmap, false);
                }
            }
        });
    }

    private void initWatcher() {
        mContentEditText.initWatcher(mShare, mEncryptAndDelete);
        mTitleEditText.initWatcher(mShare, mEncryptAndDelete);
    }

    private void startSave() {
        mMainHandler.postDelayed(mSaveRunnable, NOTE_SAVE_DURATION);
    }

    private void stopSave() {
        mMainHandler.removeCallbacks(mSaveRunnable);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        WidgetUtil.updateAllWidgets();
        mTakePhotoOutputUri = savedInstanceState.getParcelable(SAVE_TAKE_PHOTO_URI);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_INSTANCE_ID, mCurrNoteInfo.mId);
        outState.putParcelable(SAVE_TAKE_PHOTO_URI, mTakePhotoOutputUri);
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                //Gionee wanghaiyan 2017-05-08 modify for 159786 begin
                saveNote();
                //Gionee wanghaiyan 2017-05-08 modify for 159786 end
            }
        });
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //GIONEE wanghaiyan 2016-12-02 modify for 37596 begin
	    /*
        if (mAITipView != null) {
            mIsAiSwitchOpen = NoteShareDataManager.isAISwitchOpen(this);
            if (mIsAiSwitchOpen) {
                mAITipView.resume();
            }
        }
	   */
        //GIONEE wanghaiyan 2016-12-02 modify for 37596 end
        checkTimeChange();
        mActive = true;
        mIsOnResumed = true;
        startSave();
        mExecutor.resume();
        //Chenyee wanghaiyan add on 2018-1-27 for SW17W16A-2093 begin
        mContentEditText.setNoteTime(mNoteTime);
        //Chenyee wanghaiyan add on 2018-1-27 for SW17W16A-2093 end
    }

    private void checkTimeChange() {
        if (mCurDate == null) {
            return;
        }
        int[] newCurDate = NoteUtils.getToady();
        boolean isSomeDay = NoteUtils.isSomeDay(newCurDate, mCurDate);
        if (!isSomeDay) {
            mCurDate = newCurDate;
            updateActivityWindowBackground();
        }
    }

    @Override
    protected void onPause() {
        mActive = false;
        //GIONEE wanghaiyan 2016-12-02 modify for 37596 begin
	    /*
        if (mAITipView != null && mIsAiSwitchOpen) {
            mAITipView.pause();
        }
	   */
        //GIONEE wanghaiyan 2016-12-02 modify for 37596 end
        //Chenyee wanghaiyan 2018-9-28 modify for CSW1805A-155 begin
        hintInputMehtod();
        //Chenyee wanghaiyan 2018-9-28 modify for CSW1805A-155 end
        stopSave();
        mExecutor.pause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mCancelBgTask = true;
        }
        stopSoudPlayer();
        mAttachmentSelector.cancel();
        //Gionee wanghaiyan 2017-9-14 modify for 205814 begin
        saveNote();
        //Gionee wanghaiyan 2017-9-14 modify for 205814 end
        super.onStop();
    }

    private void stopSoudPlayer() {
        int length = mContentEditText.length();
        JsonableSpan[] jsonableSpans = mContentEditText.getText().getSpans(0, length, JsonableSpan.class);
        if (jsonableSpans != null) {
            for (JsonableSpan span : jsonableSpans) {
                if (span instanceof SoundImageSpan) {
                    ((SoundImageSpan) span).stop();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        mMainHandler.removeCallbacksAndMessages(null);
        if (!mRecording) {
            clearSpans();
            quitHandThread();
        }
        mLabelManager.removeLabelDataChangeListener(mLabelDataChangeListener);
        mExecutor.destroy();
        NoteAppImpl.getContext().unRegisterNoteDbInitCompleteNotify(this);
        if (null != mPreviewPictureMakeProxy) {
            mPreviewPictureMakeProxy.cancel();
        }
        mAttachmentSelector = null;
        //Gionee wanghaiyan 2017-9-18 modify for 202128 begin
        recyclebgBitmap();
        //Gionee wanghaiyan 2017-9-18 modify for 202128 end
        mToastManager.destroy();
        releaseWakeLock();
        super.onDestroy();
    }

    private void quitHandThread() {
        if (Build.VERSION.SDK_INT >= 18) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }
        mHandlerThread = null;
    }

    private void clearSpans() {
        int length = mContentEditText.length();
        JsonableSpan[] jsonableSpans = mContentEditText.getText().getSpans(0, length, JsonableSpan.class);

        if (jsonableSpans != null) {
            for (JsonableSpan span : jsonableSpans) {
                span.recycle();
            }
            Runtime.getRuntime().gc();
            if (jsonableSpans.length > 0) {
                mContentEditText.getText().clearSpans();
            }
        }
    }

    private void asynAddImage(final Uri uri, final boolean photoFromOut) {
        NoteAppImpl.getContext().getThreadPool().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                addImage(uri, photoFromOut, false);
                return null;
            }
        });
    }

    //Gionee wanghaiyan 20170307 add for 77568 begin
    private String getAttachmentFilePath(Context context, Uri uri)
    {
        if (uri == null) return null;
        if (uri.getScheme().equals("content") == true) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(uri, null, null, null, null);
            if (c == null) return null;
            c.moveToFirst();
            String str = c.getString(c.getColumnIndexOrThrow("_data"));
            c.close();
            return str;
        } else if (uri.getScheme().equals("file") == true) {
            String str = uri.getPath();
            return str;
        }
        return null;
    }
    //Gionee wanghaiyan 20170307 add for 77568 end

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_PICK_IMAGE: {
                mAttachmentSelector.dismissPicSelectDialog();
                final Uri uri = data.getData();
                if (null == uri) {
                    return;
                }

                boolean hasEnoughFreeMemory = NoteUtils.checkEnoughFreeMemory();
                if (hasEnoughFreeMemory && !mContentEditText.isSelectPositionReachMaxSize()) {
                    //Gionee wanghaiyan 20170307 add for 77568 begin
                    if(NoteUtils.gnKRFlag){
                        String strPath = getAttachmentFilePath(this, uri);
                        Log.d("kptc", "com.cydroid.note.app.NewNoteActivity->onActivityResult(): isLegalFile()");
                        int nFlag = pwinSign.isLegalFile(strPath);
                        Log.d("kptc", "retCode=" + nFlag);
                        if(nFlag != 1 && nFlag != 2) {
                            pwinSign.showIllegalFileMessage(this);
                            return;
                        }
                    }
                    //Gionee wanghaiyan 20170307 add for 77568 end
                    asynAddImage(uri, true);
                } else {
                    mToastManager.showToast(getString(R.string.max_pic_input_mum_limit));
                }
                break;
            }
            case REQUEST_TAKE_PHOTO: {
                if (mTakePhotoOutputUri != null) {
                    boolean hasEnoughFreeMemory = NoteUtils.checkEnoughFreeMemory();
                    if (hasEnoughFreeMemory && !mContentEditText.isSelectPositionReachMaxSize()) {
                        //Gionee wanghaiyan 20170307 add for 77568 begin
                        if(NoteUtils.gnKRFlag){
                            Log.d("kptc", "com.cydroid.note.app.NewNoteActivity->onActivityResult(): saveSelfSignFile()");
                            int errCode = pwinSign.saveSelfSignFile(mTakePhotoOutputUri.getPath());
                            Log.d("kptc", "errCode=" + errCode);
                            //Chenyee 2018-5-10 modify for CSW1703KR-68 begin
                            pwinSign.sendBroadcastToRedService(NoteAppImpl.getContext(), mTakePhotoOutputUri.getPath());
                            //Chenyee 2018-5-10 modify for CSW1703KR-68 end
                        }
                        //Gionee wanghaiyan 20170307 add for 77568 end
                        asynAddImage(mTakePhotoOutputUri, false);
                        mTakePhotoOutputUri = null;
                    } else {
                        mToastManager.showToast(getString(R.string.max_pic_input_mum_limit));
                    }
                }
                break;
            }
            case REQUEST_CUSTOM_LABEL: {
                if (!mContentEditText.isSelectPositionReachMaxSize()) {
                    mLabelSelector.updateLabelList(mCurrNoteInfo.mLabel);
                }
                break;
            }
            case EncryptUtil.REQUEST_DIAL_SETTING_SUCCESS: {
                encrypt();
                break;
            }
            //Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 begin
            case REQUEST_RECORDER: {
                getContentResolver().takePersistableUriPermission(data.getData(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Log.d(TAG, "SD PERMISSION--onActivityResult uri = " + data.getData());
            }
            //Chenyee wanghaiyan 2018-10-17 modify for CSW1805A-240 end
            default: {
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        onBack();
        if (isShouldShowEncryptHint()) {
            mEncryptHintManager.showEncryptHint(this);
        } else {
            super.onBackPressed();
        }
    }

    private void onBack() {
        hintInputMehtod();
        stopSave();
        exitNote();
    }

    public void hintInputMehtod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(mTitleEditText)) {
            imm.hideSoftInputFromWindow(mTitleEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else if (imm.isActive(mContentEditText)) {
            imm.hideSoftInputFromWindow(mContentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private boolean isShouldShowEncryptHint() {
        if (mCurrNoteInfo.mEncyptHintState == Constants.ENCRYPT_HINT_DISABLE) {
            return false;
        }
        //GIONEE wanghaiyan 2016-12-14 modify for 46059 for begin
        if(!FileUtils.gnEncryptionSpaceSupport){
            return false;
        }
        //GIONEE wanghaiyan 2016-12-14 modify for 46059 for end
        String text = mContentEditText.getText().toString();
        String checkText = "";
        if (!TextUtils.isEmpty(text) && text.length() >= 1000) {
            checkText = text.substring(0, 1000);
        } else {
            checkText = text;
        }
        return mEncryptHintManager.shouldShowEncryptHint(checkText)
                && NoteShareDataManager.isSecretSwitchOpen(this);
    }

    private boolean isNoteChanged() {
        UpdateHelper uh = new UpdateHelper();
        mPreNoteInfo.mId = uh.update(mPreNoteInfo.mId, mCurrNoteInfo.mId);
        mPreNoteInfo.mLabel = uh.update(mPreNoteInfo.mLabel, mCurrNoteInfo.mLabel);
        mPreNoteInfo.mDateReminderInMs = uh.update(mPreNoteInfo.mDateReminderInMs, mCurrNoteInfo.mDateReminderInMs);
        mPreNoteInfo.mEncyptHintState = uh.update(mPreNoteInfo.mEncyptHintState, mCurrNoteInfo.mEncyptHintState);
        boolean updated = uh.isUpdated();
        boolean titleChanged = mTitleEditText.getAndResetTextChanged();
        boolean contentChanged = mContentEditText.getAndResetTextChanged();

        boolean contentChangFromOut = isContentChangeFromOut();
        return updated || titleChanged || contentChanged || contentChangFromOut;
    }

    private boolean isContentChangeFromOut() {
        String action = getIntent().getAction();
        if (TextUtils.isEmpty(action)) {
            return false;
        }
        return CAMERA_ACTION.equals(action) || Intent.ACTION_SEND.equals(action);
    }

    private void deleteEmptyNote() {
        final int noteId = mCurrNoteInfo.mId;
        if (noteId != NoteItem.INVALID_ID) {
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    String selection = NoteContract.NoteContent._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(noteId)};
                    mResolver.delete(NoteUtils.getContentUri(mIsEncrypted), selection, selectionArgs);
                    WidgetUtil.updateAllWidgets();
                }
            });
        }
    }

    private boolean isShouldDelete() {
        boolean labelEmpty = mLabelSelector.isLabelInvalid(mCurrNoteInfo.mLabel);
        String title = mTitleEditText.getText().toString().trim();
        boolean titleEmpty = TextUtils.isEmpty(title);
        boolean contentEmpty = NoteUtils.isContentEmpty(mContentEditText.getText());
        boolean reminderEmpty = (mCurrNoteInfo.mDateReminderInMs == NoteItem.INVALID_REMINDER);
        return labelEmpty && titleEmpty && contentEmpty && reminderEmpty;
    }

    private void saveNote() {
        //gionee wanghaiyan add on 2016-12-22 for 39883 begin
        com.cydroid.note.common.Log.i("chen_long04","NewNoteActivity saveNote isNoteChanged "+isNoteChanged);
        //Gionee wanghaiyan 2017-9-14 modify for 205814 begin
        if (!previewBtnClicked) {
            isNoteChanged=isNoteChanged();
            if(!isNoteChanged){
                return;
            }
        }
        com.cydroid.note.common.Log.i("chen_long04","NewNoteActivity saveNote isNoteChanged "+isNoteChanged);
        final int noteId = mCurrNoteInfo.mId;
        final String title = mTitleEditText.getText().toString();
        final Editable content = mContentEditText.getText();
        boolean contentEmpty = NoteUtils.isContentEmpty(content);
        final String jsonContent = contentEmpty ? "" : DataConvert.editableConvertToJson(content);
        final long modifiedTime = mModifiedTime;
        final long dateReminderInMs = mCurrNoteInfo.mDateReminderInMs;
        final ArrayList<Integer> label = new ArrayList<>(mCurrNoteInfo.mLabel);
        final ContentResolver resolver = mResolver;
        final int encryptHintState = mCurrNoteInfo.mEncyptHintState;
        final int encrytRemindReadState = mCurrNoteInfo.mEncrytRemindReadState;
        if (noteId != NoteItem.INVALID_ID) {
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    NoteUtils.updateNoteData(title, jsonContent, resolver, noteId, modifiedTime, dateReminderInMs,
                            label, encryptHintState, encrytRemindReadState, mIsEncrypted);
                }
            });
            return;
        }
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                int id = NoteUtils.addNoteData(title, jsonContent, resolver, modifiedTime, dateReminderInMs, label,
                        encryptHintState, encrytRemindReadState, mIsEncrypted);
                mPreNoteInfo.mId = id;
                mCurrNoteInfo.mId = id;
            }
        });

    }
    //gionee wanghaiyan add on 2016-12-22 for 39883 end

    private void beforeThrowIntoTrash(NoteSelectionManager manager) {
        if (!isNoteChanged()) {
            return;
        }
        final int noteId = mCurrNoteInfo.mId;
        final String title = mTitleEditText.getText().toString();
        final Editable content = mContentEditText.getText();
        boolean contentEmpty = NoteUtils.isContentEmpty(content);
        final String jsonContent = contentEmpty ? "" : DataConvert.editableConvertToJson(content);
        final long modifiedTime = mModifiedTime;
        final long dateReminderInMs = mCurrNoteInfo.mDateReminderInMs;
        final ArrayList<Integer> label = new ArrayList<>(mCurrNoteInfo.mLabel);
        final ContentResolver resolver = mResolver;
        final int encryptHintState = mCurrNoteInfo.mEncyptHintState;
        final int encrytRemindReadState = mCurrNoteInfo.mEncrytRemindReadState;
        if (noteId != NoteItem.INVALID_ID) {
            NoteUtils.updateNoteData(title, jsonContent, resolver, noteId, modifiedTime, dateReminderInMs,
                    label, encryptHintState, encrytRemindReadState, mIsEncrypted);
            return;
        }

        int id = NoteUtils.addNoteData(title, jsonContent, resolver, modifiedTime, dateReminderInMs, label,
                encryptHintState, encrytRemindReadState, mIsEncrypted);
        mPreNoteInfo.mId = id;
        mCurrNoteInfo.mId = id;
        manager.deSelectAll();
        manager.toggle(LocalNoteItem.ITEM_PATH.getChild(mCurrNoteInfo.mId));
    }

    private void exitNote() {
        if (isShouldDelete()) {
            deleteEmptyNote();
            return;
        }
        if (mIsFromCamera) {
            setResult(Activity.RESULT_OK);
        }
        checkSetReminder();
        //Gionee wanghaiyan 2017-9-14 modify for 205814 begin
        saveNote();
        //Gionee wanghaiyan 2017-9-14 modify for 205814 end
    }

    private void checkSetReminder() {
        if (mCurrNoteInfo.mDateReminderInMs != NoteItem.INVALID_REMINDER
                && mCurrNoteInfo.mDateReminderInMs > System.currentTimeMillis()) {
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    ReminderManager.setReminder(NoteAppImpl.getContext(), mCurrNoteInfo.mId,
                            mCurrNoteInfo.mDateReminderInMs, false);
                }
            });

        }
    }

    private void insertSoundRecorder(String soundPath, int durationInSec) {
        mContentEditText.insertSound(soundPath, durationInSec, false);
    }

    private void throwIntoTrash() {
        stopSave();
        if (isShouldDelete()) {
            deleteEmptyNote();
            //GIONEE wanghaiyan 2016-12-21 modify for 51697 begin
            //return;
            //GIONEE wanghaiyan 2016-12-21 modify for 51697 end
        }

        final NoteSelectionManager manager = new NoteSelectionManager();
        manager.toggle(LocalNoteItem.ITEM_PATH.getChild(mCurrNoteInfo.mId));
        NoteActionExecutor executor = mExecutor;
        executor.startThrowIntoTrashAction(manager, new NoteActionExecutor.NoteActionListener() {
            @Override
            public void onActionPrepare() {
                beforeThrowIntoTrash(manager);
            }

            @Override
            public int onActionInvalidId() {
                return 1;
            }

            @Override
            public void onActionFinish(int success, int fail) {
                if (success > 0) {
                    WidgetUtil.updateAllWidgets();
                    if (Build.VERSION.SDK_INT >= 21) {
                        finishAndRemoveTask();
                    } else {
                        finish();
                    }
                }
            }
        });

        mTitleMoreDialog.dismiss();
    }

    public Looper getBackgroundHandlerLooper() {
        return mHandlerThread.getLooper();
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }

    private void selectLabel() {
        hideSoftInput();
        mLabelSelector.selectLabel(mCurrNoteInfo.mLabel);
    }

    private void selectBill() {
        if (!mContentEditText.isSelectPositionReachMaxSize()) {
            mContentEditText.toggleBillItem();
        } else {
            Toast.makeText(this, getString(R.string.max_content_input_mum_limit), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean mIsShowDateTimeDilaog;

    private void selectReminder() {
        hideSoftInput();
        if (mIsShowDateTimeDilaog) {
            return;
        }
        mDateTimeDialog = new DateTimeDialog(this, R.style.DialogTheme, mCurrNoteInfo.mDateReminderInMs, new AlarmSetListener());
        mDateTimeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mIsShowDateTimeDilaog = false;
            }
        });
        mIsShowDateTimeDilaog = true;
        mDateTimeDialog.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.new_note_activity_title_layout_back:
                hintInputMehtod();
                onBack();
                if (isShouldShowEncryptHint()) {
                    mEncryptHintManager.showEncryptHint(this);
                } else {
                    finish();
                }
                break;
            case R.id.new_note_activity_title_layout_share:
                //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
                isNoteChanged=isNoteChanged();
                previewBtnClicked=true;
                Log.d("chen_long04","NewNoteActivity onClick isNoteChanged "+isNoteChanged);
                PreviewPictureMakeProxy proxy = new PreviewPictureMakeProxy(this,isNoteChanged,mNoteId);
                //PreviewPictureMakeProxy proxy = new PreviewPictureMakeProxy(this);
                //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
                mPreviewPictureMakeProxy = proxy;
                EffectUtil effectUtil = new EffectUtil(System.currentTimeMillis());
                int effect = effectUtil.getEffect(mOldModifyTimeMillis);
                proxy.createPreviewPicture(mTitleEditText, mContentEditText, effect);
                break;
            case R.id.new_note_activity_title_layout_delete:
                selectTitleMore();
                break;
            case R.id.action_label:
                selectLabel();
                break;
            case R.id.action_bill:
                //gionee wanghaiyan add on 2016-08-03 for CR01739902 begin
                if(!mContentEditText.checkContentKeepMore16CharsRemaing()){
                    int charNums=mContentEditText.getNeededCharNums();
                    mToastManager.showToast(charNums>1?getString(R.string.alert_remaining_chars,charNums):getString       (R.string.alert_remaining_char,charNums));
                    return;
                }
                //gionee wanghaiyan add on 2016-08-03 for CR01739902 end

                selectBill();
                break;
            case R.id.action_reminder:
                selectReminder();
                break;
            case R.id.action_recorde:
                //wanghaiyan 2017-9-28 modify for 226343 begin
                mShouldSaveRecord = true;
                //wanghaiyan 2017-9-28 modify for 226343 end
                //gionee wanghaiyan add on 2016-08-03 for CR01739902 begin
                if(!mContentEditText.checkContentKeepMore16CharsRemaing()){
                    int charNums=mContentEditText.getNeededCharNums();
                    mToastManager.showToast(charNums>1?getString(R.string.alert_remaining_chars,charNums):getString       (R.string.alert_remaining_char,charNums));
                    return;
                }
                //gionee wanghaiyan add on 2016-08-03 for CR01739902 end
                //GIONEE wanghaiyan 2016-11-25 modify for 32782 begin
                addSafeRecordAudio();
                //GIONEE wanghaiyan 2016-11-25 modify for 32782 end
                break;
            case R.id.action_camera:
                //gionee wanghaiyan add on 2016-08-03 for CR01739902 begin
                if(!mContentEditText.checkContentKeepMore16CharsRemaing()){
                    int charNums=mContentEditText.getNeededCharNums();
                    mToastManager.showToast(charNums>1?getString(R.string.alert_remaining_chars,charNums):getString(R.string.alert_remaining_char,charNums));
                    return;
                }
                //gionee wanghaiyan add on 2016-08-03 for CR01739902 end
                //GIONEE wanghaiyan 2016-11-25 modify for 32782 begin
                addSafeTakePhotos();
                //GIONEE wanghaiyan 2016-11-25 modify for 32782 end
                break;
            case R.id.action_gallery:
                //gionee wanghaiyan add on 2016-08-03 for CR01739902 begin
                if(!mContentEditText.checkContentKeepMore16CharsRemaing()){
                    int charNums=mContentEditText.getNeededCharNums();
                    mToastManager.showToast(charNums>1?getString(R.string.alert_remaining_chars,charNums):getString(R.string.alert_remaining_char,charNums));
                    return;
                }
                //gionee wanghaiyan add on 2016-08-03 for CR01739902 end
                addSafeSelectImage();
                break;
            //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 begin
            //case R.id.action_online_image:
            //    gotoOnlineImage();
            //    break;
            //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 end
            case R.id.title_more_delete:
                throwIntoTrash();
                break;
            case R.id.title_more_encrypt_or_decrypt:
                if (PlatformUtil.isSecurityOS() && !EncryptUtil.isDialcodeOpen(getContentResolver())) {
                    EncryptUtil.startDialSettingInterface(NewNoteActivity.this);
                } else {
                    selectEncrypt();
                }
                break;
            default:
                break;
        }
    }

    private void selectSound() {
//        hideInputMethod();
        hideSoftInput();
        mAttachmentSelector.gotoRecordSound(this, new SoundRecorder.TakeSoundRecorderListener() {
            @Override
            public void onRecorderStart() {
                mRecording = true;
                acquireWakeLock();
            }

            @Override
            public void onRecorderComplete(String soundPath, int durationInSec) {
                //wanghaiyan 2017-9-28 modify for 226343 begin
                Log.d(TAG,"wanghaiyan_mShouldSaveRecord" + mShouldSaveRecord);
                if (!mShouldSaveRecord) {
                    mToastManager.showToast(R.string.record_fail);
                    return;
                }
                //wanghaiyan 2017-9-28 modify for 226343 end
                if (!mContentEditText.isSelectPositionReachMaxSize()) {
                    insertSoundRecorder(soundPath, durationInSec);
                    //Gionee wanghaiyan 2017-9-14 modify for 205814 begin
                    saveNote();
                    //Gionee wanghaiyan 2017-9-14 modify for 205814 end
                } else {
                    Toast.makeText(NewNoteActivity.this, R.string.max_content_input_mum_limit, Toast.LENGTH_SHORT).show();
                }
                if (isDestroyed()) {
                    clearSpans();
                    quitHandThread();
                }
                mRecording = false;
                releaseWakeLock();
            }
        }, false);
    }

/*    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (null != mContentEditText && imm.isActive(mContentEditText)) {
            imm.hideSoftInputFromWindow(mContentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else if (null != mTitleEditText && imm.isActive(mTitleEditText)) {
            imm.hideSoftInputFromWindow(mTitleEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }*/

    private void selectEncrypt() {
        encrypt();
        NoteShareDataManager.setIsEncryptUserGuideNormalClosed(this, false);
        mTitleMoreDialog.dismiss();
        setResult(Activity.RESULT_OK);
    }

    private void selectTitleMore() {
        View view = LayoutInflater.from(this).inflate(R.layout.new_note_title_more_dialog, null);
        view.findViewById(R.id.title_more_encrypt_or_decrypt).setOnClickListener(this);
        //GIONEE wanghaiyan 2016 -12-13 modify for 45337 begin
        if(!FileUtils.gnEncryptionSpaceSupport){
            view.findViewById(R.id.title_more_encrypt_or_decrypt).setVisibility(View.GONE);
        }
        //GIONEE wanghaiyan 2016 -12-13 modify for 45337 end
        view.findViewById(R.id.title_more_delete).setOnClickListener(this);
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle(R.string.title_more_dialog_title);
        mTitleMoreDialog = builder.create();
        mTitleMoreDialog.show();
    }


    @Override
    public void onEvent(int eventId) {
    }

    @Override
    public void onLabelEvent(int eventId, String label) {
    }

    @Override
    public void onNoteDbInitComplete() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mNoteInfoInitSuccess) {
                    String path = getIntent().getStringExtra(NOTE_ITEM_PATH);
                    int id = NoteUtils.getIdFromPath(path, mIsEncrypted);
                    initNoteInfoFromDB(id);
                }
            }
        });
    }

    private class AlarmSetListener implements DateTimeDialog.OnDateTimeSetListener {
        @Override
        public void onDateTimeSet(Calendar calendar) {
            mCurrNoteInfo.mDateReminderInMs = calendar.getTimeInMillis();
            mContentEditText.setReminderTime(mCurrNoteInfo.mDateReminderInMs);
            checkSetEmptyState();
        }

        @Override
        public void onDataTimeDelete() {
            mCurrNoteInfo.mDateReminderInMs = NoteItem.INVALID_REMINDER;
            ReminderManager.cancelAlarmAndNotification(getApplicationContext(), mCurrNoteInfo.mId, false);
            mContentEditText.setReminderTime(mCurrNoteInfo.mDateReminderInMs);
            //Gionee wanghaiyan 2017-9-14 modify for 205814 begin
            saveNote();
            //Gionee wanghaiyan 2017-9-14 modify for 205814 end
            checkSetEmptyState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mTitleEditText != null) {
            if (mTitleEditText.isFocused()) {
                mTitleEditText.setHint(R.string.title_focus_hint);
            } else {
                mTitleEditText.setHint(R.string.title_hint);
            }
        }
        if (mContentEditText != null) {
            mContentEditText.setHint(R.string.content_hint);
        }
        getWindow().getDecorView().requestLayout();
        super.onConfigurationChanged(newConfig);
    }

    private void showDataFlowHint(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.data_flow_dialog, null);
        ((TextView)view.findViewById(R.id.authority_alert_user_title)).setMovementMethod(ScrollingMovementMethod.getInstance());
        final CyeeCheckBox checkBox = (CyeeCheckBox) view.findViewById(R.id.authority_alert_user_checkBox);
        //GIONEE wanghaiyan 2016-12-08 modify for 40315 begin
        if(ChameleonColorManager.isNeedChangeColor()){
            checkBox.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
        }
        //GIONEE wanghaiyan 2016-12-08 modify for 40315 end
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(context);
        builder.setTitle(R.string.data_flow_alert_user_title);
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
                stopSave();
                exitNote();
                killSelf();
            }
        });
        CyeeAlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        //Gionee wanghaiyan 2017-3-23 modify for 90801 begin
        dialog.setCancelable(true);
        //Gionee wanghaiyan 2017-3-23 modify for 90801 end
        dialog.show();
    }

    private void killSelf() {
        if (Build.VERSION.SDK_INT < 21) {
            finish();
            return;
        }
        NoteUtils.finishAndRemoveAllTask(this);
    }

    private void ckeckShowDataFlowHint() {
        if (PlatformUtil.authorizeToPermisson()) {
            NoteShareDataManager.setShowDataFlowHint(this, true);
            return;
        }
        boolean fromOutContext = !getIntent().getBooleanExtra(FROM_INNER_CONTEXT, false);
        if (PlatformUtil.isGioneeDevice()
                && fromOutContext
                && !NoteShareDataManager.getHasShowDataFlowHint(getApplicationContext())) {
            showDataFlowHint(NewNoteActivity.this);
        }
    }

    private void encrypt() {
        NoteInfo info = new NoteInfo();
        info.mId = mCurrNoteInfo.mId;
        info.mTitle = mTitleEditText.getText().toString();
        Editable content = mContentEditText.getText();
        boolean contentEmpty = NoteUtils.isContentEmpty(content);
        info.mContent = contentEmpty ? "" : DataConvert.editableConvertToJson(content);
        info.mDateModifiedInMs = mCurrNoteInfo.mDateModifiedInMs;
        info.mDateReminderInMs = mCurrNoteInfo.mDateReminderInMs;
        info.mLabel = new ArrayList<>(mCurrNoteInfo.mLabel);
        info.mEncyptHintState = mCurrNoteInfo.mEncyptHintState;
        if (info.mDateReminderInMs != NoteItem.INVALID_REMINDER) {
            info.mEncrytRemindReadState = Constants.ENCRYPT_REMIND_READED;
        }

        mExecutor.startEncrypt(this, info, mNoteProgressListener);
    }

    private NoteActionProgressListener mNoteProgressListener = new NoteActionProgressListener() {
        @Override
        public void onStart(int count) {
            if (count > 0) {
                showProgressDialog(count, 1, getResources().getString(R.string.note_action_encrypt_string));
            }
        }

        @Override
        public void onOneComplete() {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog != null) {
                        mProgressDialog.setProgress(++mProgress, 0);
                    }
                }
            });
        }

        @Override
        public void onAllComplete(boolean isEncrpt) {
            mToastManager.showToast(EncryptUtil.getHint(isEncrpt, 1, 0));
            mProgress = 0;
            if (null != mProgressDialog) {
                mProgressDialog.dismiss();
            }
            mEncryptHintManager.dismissDialog();
            setResult(Activity.RESULT_OK);
            finish();
        }
    };

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return resources;
    }

    private void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }

    }
    //GIONEE wanghaiyan 2016-11-25 modify for 32782 begin
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_TAKE_PHOTOS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mAttachmentSelector.gotoTakePhotos();
                    return;
                } else {
                    showRemind();
                }
                break;
            case REQUEST_PERMISSION_RECORD_AUDIO:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectSound();
                    return;
                } else {
                    showRemind();
                }
                break;
            case REQUEST_PERMISSION_SELECT_IMAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mAttachmentSelector.gotoSelectImage();
                    return;
                } else {
                    showRemind();
                }
                break;
            default:
                break;
        }
    }

    private void showRemind() {
        Toast.makeText(this, R.string.authorization_failed, Toast.LENGTH_SHORT).show();
    }

    private void addSafeTakePhotos() {
        //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin

        if (!NoteUtils.checkNeededPermissionForRecord(this)) {
            return;
        }
        //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end
        mAttachmentSelector.gotoTakePhotos();
    }

    private void addSafeRecordAudio() {
        //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin

        if (!NoteUtils.checkNeededPermissionForRecord(this)) {
            return;
        }

        //Chenyee wanghaiyan 2018-10-24 modify for CSW1805A-1060 begin
        //Chenyee wanghaiyan 2018-10-25 modify for CSW1805A-1159 begin
        if(FileUtils.isSDCardInserted(this)) {
            mExternalStorageVolume = StorageManagerHelper.getExternalStorageVolume(this);
            Log.d(TAG, "mExternalStorageVolume" + mExternalStorageVolume + "rootPath" + rootPath);
            rootPath = StorageManagerHelper.getSDPath(this);
            boolean hasExternalPermission = ExternalPermissionsManager.hasExternalSDCardFilePermission(this, new File(rootPath));
            Log.d(TAG, "SD PERMISSOIN--mExternalStorageVolume = " + mExternalStorageVolume);
            Log.d(TAG, "hasExternalPermission = " + hasExternalPermission);
            if (!hasExternalPermission && rootPath != null) {
                Intent intent = mExternalStorageVolume.createAccessIntent(null);
                startActivityForResult(intent, REQUEST_RECORDER);
                return;
            }
            selectSound();
        }else{
            selectSound();
        }
        //Chenyee wanghaiyan 2018-10-24 modify for CSW1805A-1159 end
        //Chenyee wanghaiyan 2018-10-25 modify for CSW1805A-1060 end
    }

    private void addSafeSelectImage() {
        //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin

        if (!NoteUtils.checkNeededPermissionForRecord(this)) {
            return;
        }
        //Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end

        mAttachmentSelector.gotoSelectImage();
    }
    //GIONEE wanghaiyan 2016-11-25 modify for 32782 end
    //Chenyee wanghaiyan add on 2018-1-27 for SW17W16A-2093 begin
    private long mNoteTime;
    //Chenyee wanghaiyan add on 2018-1-27 for SW17W16A-2093 end
}
