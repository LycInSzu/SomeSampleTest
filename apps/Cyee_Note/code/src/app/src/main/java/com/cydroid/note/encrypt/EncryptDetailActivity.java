package com.cydroid.note.encrypt;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextUtils;
import com.cydroid.note.common.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.note.R;
import com.cydroid.note.ai.AIActivity;
import com.cydroid.note.ai.AITipView;
import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.EditMovementMethod;
import com.cydroid.note.app.IYouJuCallback;
import com.cydroid.note.app.LabelManager;
import com.cydroid.note.app.LabelSelector;
import com.cydroid.note.app.NoteActionExecutor;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.PreviewPictureMakeProxy;
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
import com.cydroid.note.data.NoteInfo;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteShareDataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;

import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeTextView;

/**
 * Created by spc on 16-4-22.
 */
public class EncryptDetailActivity extends StandardActivity implements IYouJuCallback, View.OnClickListener {
    private static final String TAG = "NewNoteActivity";
    public static final int REQUEST_PICK_IMAGE = 1;
    public static final int REQUEST_TAKE_PHOTO = 2;
    public static final int REQUEST_CUSTOM_LABEL = 3;

    public static final String NOTE_ITEM_PATH = "path";
    private static final String SAVE_INSTANCE_ID = "id";

    private static final long NOTE_SAVE_DURATION = 10000L;
    private ContentResolver mResolver;
    private LabelManager mLabelManager;
    private boolean mActive = false;
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
    private ImageView mDncryptAndDelete;
    private Uri mTakePhotoOutputUri;
    private AttachmentSelector mAttachmentSelector;
    private LabelSelector mLabelSelector;
    private PreviewPictureMakeProxy mPreviewPictureMakeProxy;
    private NoteInfo mPreNoteInfo = new NoteInfo();
    private NoteInfo mCurrNoteInfo = new NoteInfo();
    private final long mModifiedTime = System.currentTimeMillis();
    private LabelManager.LabelDataChangeListener mLabelDataChangeListener;

    private boolean mIsEditMode;
    private NoteActionExecutor mNoteDelExecutor;
    private long mOldModifyTimeMillis;
    private int[] mCurDate;
    //GIONEE wanghaiyan 2016-12-02 modify for 40545 begin
    //private AITipView mAITipView;
    //GIONEE wanghaiyan 2016-12-02 modify for 40545 end
    private boolean mIsAiSwitchOpen;
    public boolean mIsEncrypted;
    private CyeeAlertDialog mTitleMoreDialog;
    private volatile boolean mCancelBgTask;
    private boolean mRecording;
    protected Handler mBackgroundHandler;
    protected HandlerThread mHandlerThread;

    private Runnable mSaveRunnable = new Runnable() {
        @Override
        public void run() {
            saveNote(false);
            if (mActive) {
                mMainHandler.postDelayed(mSaveRunnable, NOTE_SAVE_DURATION);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView();
        initCurDate();
        initView();
        initData(savedInstanceState);
        initListener();
        initWatcher();
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
        mDncryptAndDelete.setOnClickListener(this);

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

        findViewById(R.id.new_note_activity_title_layout_share).setVisibility(View.GONE);
        mDncryptAndDelete = (ImageView) findViewById(R.id.new_note_activity_title_layout_delete);

        tintImageViewDrawableList(R.id.action_label,
                R.drawable.action_label_icon, R.color.action_bar_image_color);

        tintImageViewDrawableList(R.id.action_bill,
                R.drawable.action_bill_icon, R.color.action_bar_image_color);

        tintImageViewDrawableList(R.id.action_reminder,
                R.drawable.action_alert_icon, R.color.action_bar_image_color);

        tintImageViewDrawableList(R.id.action_recorde,
                R.drawable.attachment_sound_recorder, R.color.action_bar_image_color);

        tintImageViewDrawableList(R.id.action_gallery,
                R.drawable.attachment_select_image, R.color.action_bar_image_color);

        tintImageViewDrawableList(R.id.action_camera,
                R.drawable.attachment_take_photos, R.color.action_bar_image_color);
        //GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 begin
        //tintImageViewDrawableList(R.id.action_online_image,
        //      R.drawable.attachment_online_image, R.color.action_bar_image_color);
		//GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 end
	    //GIONEE wanghaiyan 2016-12-02 modify for 40545 begin
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
	    //GIONEE wanghaiyan 2016-12-02 modify for 40545 end
    }

    private void startAIActivity(ArrayList<String> keywords) {
        try {
            Intent intent = new Intent();
            intent.setClass(this, AIActivity.class);
            intent.putExtra(Constants.IS_SECURITY_SPACE,
                    getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false));
            intent.putExtra(AIActivity.KEY_AMI_Recommend, keywords);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void tintImageViewDrawableList(int imageViewId, int iconId, int colorsId) {
        Drawable icon = ContextCompat.getDrawable(this, iconId);
        Drawable tintIcon = DrawableCompat.wrap(icon);
        DrawableCompat.setTintList(tintIcon, ContextCompat.getColorStateList(this, colorsId));
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

    private void updateActivityWindowBackground() {
        EffectUtil effectUtil = new EffectUtil(System.currentTimeMillis());
        int effect = effectUtil.getEffect(mOldModifyTimeMillis);
        Bitmap bgBitmap = DrawableManager.getEffectBitmap(EncryptDetailActivity.this, effect,
                NoteUtils.sScreenWidth, NoteUtils.sScreenHeight);
        Drawable bgDrawable = null;
        if (bgBitmap != null) {
            bgDrawable = new BitmapDrawable(getResources(), bgBitmap);
        }
        getWindow().setBackgroundDrawable(bgDrawable);
    }

    private void initNoteInfo(Bundle savedInstanceState) {
        String path = getIntent().getStringExtra(NOTE_ITEM_PATH);
        int id = NoteItem.INVALID_ID;
        if (savedInstanceState != null) {
            id = savedInstanceState.getInt(SAVE_INSTANCE_ID, NoteItem.INVALID_ID);
        }
        if (path != null) {
            NoteItem item = (NoteItem) NoteAppImpl.getContext().getDataManager().getMediaObject(path);
            if (item == null) {
                return;
            }
            mPreNoteInfo.mId = item.getId();
            mCurrNoteInfo.mId = mPreNoteInfo.mId;
            mPreNoteInfo.mDateReminderInMs = item.getDateTimeReminder();
            mCurrNoteInfo.mDateReminderInMs = mPreNoteInfo.mDateReminderInMs;
            mCurrNoteInfo.mEncyptHintState = item.getEncyptHintState();
            mPreNoteInfo.mEncrytRemindReadState = item.getEncrytRemindReadState();
            mCurrNoteInfo.mEncrytRemindReadState = mPreNoteInfo.mEncrytRemindReadState;
            mCurrNoteInfo.mDateModifiedInMs = item.getDateTimeModified();
            if (mCurrNoteInfo.mDateReminderInMs != NoteItem.INVALID_REMINDER
                    && item.getDateTimeReminder() <= System.currentTimeMillis()
                    && mCurrNoteInfo.mEncrytRemindReadState == Constants.ENCRYPT_REMIND_NOT_READ) {
                mCurrNoteInfo.mEncrytRemindReadState = Constants.ENCRYPT_REMIND_READED;
            }
            ArrayList<Integer> labels = item.getLabel();
            mPreNoteInfo.mLabel.addAll(labels);
            mCurrNoteInfo.mLabel.addAll(labels);
            String title = item.getTitle();
            String jsonContent = item.getContent();

            if (!TextUtils.isEmpty(jsonContent)) {
                DataConvert.applySpanToEditableFromJson(this, jsonContent, mContentEditText, true);
            }

            mTitleEditText.getText().append(title);
            mContentEditText.setNoteTime(item.getDateTimeModified());
            mOldModifyTimeMillis = item.getDateTimeModified();
            mContentEditText.setReminderTime(mCurrNoteInfo.mDateReminderInMs);
            if (mCurrNoteInfo.mDateReminderInMs != NoteItem.INVALID_REMINDER) {
                ReminderManager.cancelAlarmAndNotification(this, mCurrNoteInfo.mId, true);
            }
            showLabel();
            checkSetEmptyState();
        } else if (id != NoteItem.INVALID_ID) {
            mPreNoteInfo.mId = id;
            mCurrNoteInfo.mId = id;
        } else {
            mContentEditText.setNoteTime(mModifiedTime);
        }
    }

    private void initData(Bundle savedInstanceState) {
        mHandlerThread = new HandlerThread("");
        mHandlerThread.start();
        mBackgroundHandler = new Handler(mHandlerThread.getLooper());
        mIsEncrypted = true;
        mAttachmentSelector = new AttachmentSelector(this, new AttachmentSelector.OnTakePhotoListener() {
            @Override
            public void onStartTakePhoto(Uri outputUri) {
                mTakePhotoOutputUri = outputUri;
            }
        }, mOnSelectPicToAddListener);
        mResolver = getContentResolver();
        mLabelSelector = new LabelSelector(this, new LabelSelector.OnLabelChangedListener() {
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
        updateActivityWindowBackground();
        mNoteDelExecutor = new NoteActionExecutor(this);
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
                        new ToastManager(EncryptDetailActivity.this).showToast(R.string.max_pic_input_mum_limit);
                    }
                };
                mMainHandler.post(toast);
                break;
            }
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mAddPicNum = 0;
                dissmissProgressDialog();
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
		//Gionee wanghaiyan2017-3-28 modify for 96878 begin
        final Uri fileUri = NoteUtils.convertToFileUri(context, imageUri);
		//Gionee wanghaiyan2017-3-28 modify for 96878 end
        Bitmap bitmap = NoteUtils.getAddBitmapFromUri(context, fileUri);
        if (bitmap == null) {
	        //Gionee wanghaiyan2017-3-28 modify for 96878 begin
            sendMagAddImageFail(R.string.add_image_fail);
	        //Gionee wanghaiyan2017-3-28 modify for 96878 end
            return;
        }

        int bitmapHeight = bitmap.getHeight();
        if (bitmapHeight > 2 * NoteUtils.sScreenHeight) {
	        //Gionee wanghaiyan2017-3-28 modify for 96878 begin
            //new ToastManager(this).showToast(R.string.long_bitmap_add_failed);
            sendMagAddImageFail(R.string.long_bitmap_add_failed);
 	        //Gionee wanghaiyan2017-3-28 modify for 96878 end
            return;
        }

        File thumbFile = NoteUtils.getSaveBitmapFile(context);
        final Uri thumbnailUri = Uri.fromFile(thumbFile);
        NoteUtils.saveBitmap(bitmap, thumbFile);
        encryptImage(thumbFile.getPath());

        if (photoFromOut) {
            sendMsgAddImage(thumbnailUri, thumbnailUri, bitmap, fromAttach);
        } else {
            encryptImage(fileUri.getPath());
            sendMsgAddImage(thumbnailUri, fileUri, bitmap, fromAttach);
        }
    }

    private void encryptImage(String filePath) {
        if (PlatformUtil.isSecurityOS()) {
            EncryptUtil.encryptFileForSecurityOS(filePath);
        } else {
            EncryptUtil.encryptImageFile(filePath);
        }
    }
    //Gionee wanghaiyan2017-3-28 modify for 96878 begin
    private void sendMagAddImageFail(final int failMsgId) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(context, R.string.add_image_fail, Toast.LENGTH_SHORT).show();
                new ToastManager(EncryptDetailActivity.this).showToast(failMsgId);
            }
        });

    }
	//Gionee wanghaiyan2017-3-28 modify for 96878 end

    private void sendMsgAddImage(final Uri thumbnailUri, final Uri fileUri, final Bitmap bitmap,
                                 final boolean fromAttach) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!fromAttach || !mCancelBgTask) {
                    mContentEditText.insertPhoto(thumbnailUri, fileUri, bitmap, true);
                }
            }
        });
    }

    private void initWatcher() {
        mContentEditText.initWatcher(null, mDncryptAndDelete);
        mTitleEditText.initWatcher(null, mDncryptAndDelete);
    }

    private void startSave() {
        mMainHandler.postDelayed(mSaveRunnable, NOTE_SAVE_DURATION);
    }

    private void stopSave() {
        mMainHandler.removeCallbacks(mSaveRunnable);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_INSTANCE_ID, mCurrNoteInfo.mId);
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                saveNote(false);
            }
        });
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tintTitleMoreDrawableList();
	    //GIONEE wanghaiyan 2016-12-02 modify for 40545 begin
	    /*
        if (mAITipView != null) {
            mIsAiSwitchOpen = NoteShareDataManager.isAISwitchOpen(this);
            if (mIsAiSwitchOpen) {
                mAITipView.setVisibility(View.VISIBLE);
                mAITipView.resume();
            } else {
                mAITipView.setVisibility(View.INVISIBLE);
            }
        }
	   */
	    //GIONEE wanghaiyan 2016-12-02 modify for 40545 end
        checkTimeChange();
        mActive = true;
        startSave();
        mNoteDelExecutor.resume();
    }

    private void tintTitleMoreDrawableList() {
        if (PlatformUtil.isSecurityOS()) {
            tintImageViewDrawableList(R.id.new_note_activity_title_layout_delete,
                    R.drawable.title_more, R.color.security_os_title_more_image_color);
        } else if (PlatformUtil.isBusinessStyle()) {
            tintImageViewDrawableList(R.id.new_note_activity_title_layout_delete,
                    R.drawable.title_more, R.color.new_note_title_share_color_business_style);
        } else {
            tintImageViewDrawableList(R.id.new_note_activity_title_layout_delete,
                    R.drawable.title_more, R.color.new_note_title_share_color);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mCancelBgTask = true;
        }
        mAttachmentSelector.cancel();
        stopSoudPlayer();
        saveNote(false);
    }

    private void initCurDate() {
        mCurDate = NoteUtils.getToady();
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
        hintInputMethod();
	    //GIONEE wanghaiyan 2016-12-02 modify for 40545 begin
	    /*
        if (mAITipView != null && mIsAiSwitchOpen) {
            mAITipView.pause();
        }
        */
        //GIONEE wanghaiyan 2016-12-02 modify for 40545 end
        stopSave();
        mNoteDelExecutor.pause();
        super.onPause();
    }

    private void hintInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(mContentEditText)) {
            imm.hideSoftInputFromWindow(mContentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else if (imm.isActive(mTitleEditText)) {
            imm.hideSoftInputFromWindow(mTitleEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onDestroy() {
        mMainHandler.removeCallbacksAndMessages(null);
        if (!mRecording) {
            clearSpans();
            quit();
        }
        mLabelManager.removeLabelDataChangeListener(mLabelDataChangeListener);
        mNoteDelExecutor.destroy();
        if (null != mPreviewPictureMakeProxy) {
            mPreviewPictureMakeProxy.cancel();
        }
        super.onDestroy();
    }

    private void quit() {
        if (Build.VERSION.SDK_INT >= 18) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }
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

    private void clearSpans() {
        int length = mContentEditText.length();
        JsonableSpan[] jsonableSpans = mContentEditText.getText().getSpans(0, length, JsonableSpan.class);

        if (jsonableSpans != null) {
            for (JsonableSpan span : jsonableSpans) {
                span.recycle();
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        mAttachmentSelector.dismissPicSelectDialog();
        switch (requestCode) {
            case REQUEST_PICK_IMAGE: {
                final Uri uri = data.getData();
                if (null == uri) {
                    return;
                }

                boolean hasEnoughFreeMemory = NoteUtils.checkEnoughFreeMemory();
                if (hasEnoughFreeMemory && !mContentEditText.isSelectPositionReachMaxSize()) {
                    asynAddImage(uri, true);
                } else {
                    Toast.makeText(this, getString(R.string.max_pic_input_mum_limit), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_TAKE_PHOTO: {
                if (mTakePhotoOutputUri != null) {
                    boolean hasEnoughFreeMemory = NoteUtils.checkEnoughFreeMemory();
                    if (hasEnoughFreeMemory && !mContentEditText.isSelectPositionReachMaxSize()) {
                        asynAddImage(mTakePhotoOutputUri, false);
                        mTakePhotoOutputUri = null;
                    } else {
                        Toast.makeText(this, getString(R.string.max_pic_input_mum_limit), Toast.LENGTH_SHORT).show();
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
            default: {
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        onBack();
        super.onBackPressed();
    }

    private void onBack() {
        stopSave();
        exitNote();
    }

    private boolean isNoteChanged() {
        UpdateHelper uh = new UpdateHelper();
        mPreNoteInfo.mId = uh.update(mPreNoteInfo.mId, mCurrNoteInfo.mId);
        mPreNoteInfo.mLabel = uh.update(mPreNoteInfo.mLabel, mCurrNoteInfo.mLabel);
        mPreNoteInfo.mDateReminderInMs = uh.update(mPreNoteInfo.mDateReminderInMs, mCurrNoteInfo.mDateReminderInMs);
        boolean updated = uh.isUpdated();
        boolean titleChanged = mTitleEditText.getAndResetTextChanged();
        boolean contentChanged = mContentEditText.getAndResetTextChanged();
        return updated || titleChanged || contentChanged;
    }

    private boolean isNoteRemindReadStateChange() {
        UpdateHelper uh = new UpdateHelper();
        mPreNoteInfo.mEncrytRemindReadState = uh.update(mPreNoteInfo.mEncrytRemindReadState,
                mCurrNoteInfo.mEncrytRemindReadState);
        return uh.isUpdated();
    }

    private void deleteEmptyNote() {
        final int noteId = mCurrNoteInfo.mId;
        if (noteId != NoteItem.INVALID_ID) {
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    String selection = NoteContract.NoteContent._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(noteId)};
                    mResolver.delete(NoteContract.NoteContent.SECRET_CONTENT_URI, selection, selectionArgs);
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

    private void saveNote(boolean forceSave) {
        boolean isRemindStateChage = isNoteRemindReadStateChange();
        boolean isNoteContentChange = isNoteChanged();
        if (forceSave || isNoteContentChange || isRemindStateChage) {
            final int noteId = mCurrNoteInfo.mId;
            final String title = mTitleEditText.getText().toString();
            final Editable content = mContentEditText.getText();
            boolean contentEmpty = NoteUtils.isContentEmpty(content);
            final String jsonContent = contentEmpty ? "" : DataConvert.editableConvertToJson(content);
            final long modifiedTime = (!isNoteContentChange && isRemindStateChage) ? mCurrNoteInfo.mDateModifiedInMs :
                    mModifiedTime;
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
    }

    private void exitNote() {
        if (isShouldDelete()) {
            deleteEmptyNote();
            return;
        }
        checkSetReminder();
        saveNote(false);
    }

    private void checkSetReminder() {
        if (mCurrNoteInfo.mDateReminderInMs != NoteItem.INVALID_REMINDER
                && mCurrNoteInfo.mDateReminderInMs > System.currentTimeMillis()) {
            mCurrNoteInfo.mEncrytRemindReadState = Constants.ENCRYPT_REMIND_NOT_READ;
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    ReminderManager.setReminder(NoteAppImpl.getContext(), mCurrNoteInfo.mId,
                            mCurrNoteInfo.mDateReminderInMs, true);
                }
            });

        }
    }

    private void insertSoundRecorder(String soundPath, int durationInSec) {
        mContentEditText.insertSound(soundPath, durationInSec, true);
    }

    private void deleteNote() {
        if (mCurrNoteInfo.mId != NoteItem.INVALID_ID) {
            mResolver.delete(NoteContract.NoteContent.SECRET_CONTENT_URI, "_id=?",
                    new String[]{String.valueOf(mCurrNoteInfo.mId)});
        }

        Editable editable = mContentEditText.getText();
        String json = DataConvert.editableConvertToJson(editable);
        NoteUtils.deleteOriginMediaFile(json, true);
    }

    private void selectDelete() {
        NoteActionExecutor noteDelExecutor = mNoteDelExecutor;
        noteDelExecutor.startDeleteAction(mCurrNoteInfo.mId, new NoteActionExecutor.NoteActionListener() {
            @Override
            public void onActionPrepare() {
                stopSave();
            }

            @Override
            public int onActionInvalidId() {
                deleteNote();
                return 1;
            }

            @Override
            public void onActionFinish(int success, int fail) {
                if (success > 0) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        finishAndRemoveTask();
                    } else {
                        mTitleMoreDialog.dismiss();
                        finish();
                    }
                }
            }
        }, mIsEncrypted);
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
        mContentEditText.toggleBillItem();
    }

    private boolean mIsShowDateTimeDilaog;

    private void selectReminder() {
        hideSoftInput();
        if (mIsShowDateTimeDilaog) {
            return;
        }
        Dialog dialog = new DateTimeDialog(this, R.style.DialogTheme, mCurrNoteInfo.mDateReminderInMs, new AlarmSetListener());
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mIsShowDateTimeDilaog = false;
            }
        });
        mIsShowDateTimeDilaog = true;
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.new_note_activity_title_layout_back:
                onBack();
                finish();
                break;
            case R.id.new_note_activity_title_layout_delete:
                selectTitleMore();
                break;
            case R.id.action_label:
                selectLabel();
                break;
            case R.id.action_bill:
                selectBill();
                break;
            case R.id.action_reminder:
                selectReminder();
                break;
            case R.id.action_recorde: {
                gotoRecordSound();
                break;
            }
            case R.id.action_camera:
                mAttachmentSelector.gotoTakePhotos();
                break;
            case R.id.action_gallery:
                mAttachmentSelector.gotoSelectImage();
                break;
			//GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 begin
            //case R.id.action_online_image:
            //     gotoOnlineImage();
            //     break;
			//GIONEE:wanghaiyan 2016-11-16 modify for CR01773334 end
            case R.id.title_more_delete:
                selectDelete();
                break;
            case R.id.title_more_encrypt_or_decrypt:
                selectDecrypt();
                break;
            default:
                break;
        }
    }

    private void gotoRecordSound() {
        mAttachmentSelector.gotoRecordSound(EncryptDetailActivity.this, new SoundRecorder.TakeSoundRecorderListener() {
            @Override
            public void onRecorderStart() {
                mRecording = true;
            }

            @Override
            public void onRecorderComplete(String soundPath, int durationInSec) {
                if (!mContentEditText.isSelectPositionReachMaxSize()) {
                    insertSoundRecorder(soundPath, durationInSec);
                    saveNote(true);
                } else {
                    Toast.makeText(EncryptDetailActivity.this, R.string.max_content_input_mum_limit, Toast.LENGTH_SHORT).show();
                }
                if (isDestroyed()) {
                    clearSpans();
                    quit();
                }
                mRecording = false;
            }
        }, true);
    }

    private void selectDecrypt() {
        decrypt();
        mTitleMoreDialog.dismiss();
    }

    private void selectTitleMore() {
        View view = LayoutInflater.from(this).inflate(R.layout.new_note_title_more_dialog, null);
        CyeeTextView cyeeTextView = (CyeeTextView) view.findViewById(R.id.title_more_encrypt_or_decrypt);
        cyeeTextView.setText(R.string.note_action_decrypt_string);
        cyeeTextView.setOnClickListener(this);
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
            saveNote(false);
            checkSetEmptyState();
        }
    }

    private void checkSetEmptyState() {
        boolean enable = !isShouldDelete();
        mDncryptAndDelete.setEnabled(enable);
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
        super.onConfigurationChanged(newConfig);
    }

    private void decrypt() {
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
        info.mEncrytRemindReadState = mCurrNoteInfo.mEncrytRemindReadState;
        mNoteDelExecutor.startDecrypt(this, info, mNoteProgressListener);
    }

    private NoteActionProgressListener mNoteProgressListener = new NoteActionProgressListener() {
        @Override
        public void onStart(int count) {
            if (count > 0) {
                showProgressDialog(count, 1, NoteAppImpl.getContext().getResources()
                        .getString(R.string.note_action_decrypt_string));
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
            new ToastManager(EncryptDetailActivity.this).showToast(EncryptUtil.getHint(isEncrpt, 1, 0));
            mProgress = 0;
            if (null != mProgressDialog) {
                mProgressDialog.dismiss();
            }
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
}
