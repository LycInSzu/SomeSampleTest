package com.cydroid.note.app.attachment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.FileUriExposedException;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cyee.app.CyeeAlertDialog;
import com.cydroid.note.common.Log;

import com.cydroid.note.common.Log;
import com.cydroid.note.R;
import com.cydroid.note.app.NewNoteActivity;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.view.AttachPicRecycleView;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.StorageUtils;
import com.cydroid.note.encrypt.EncryptDetailActivity;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

public class AttachmentSelector {
    private static final String TAG = "AttachmentSelector";
    public static final int IMAGW_MIN_SIZE = 1024 * 1024 * 10;

    private Activity mActivity;
    private SoundRecorder mSoundRecorder;
    private OnTakePhotoListener mTakePhotoListener;
    private OnSelectPicToAddListener mAddPicListener;
    private Dialog mPicSelectDialog;

    public interface OnTakePhotoListener {
        void onStartTakePhoto(Uri outputUri);
    }

    public interface OnSelectPicToAddListener {
        void onAdd(CopyOnWriteArrayList<String> selectPicUris);
    }

    public AttachmentSelector(Activity activity, OnTakePhotoListener takePhotoListener,
                              OnSelectPicToAddListener addPicListener) {
        mActivity = activity;
        mTakePhotoListener = takePhotoListener;
        mAddPicListener = addPicListener;
    }

    public void gotoRecordSound(Activity activity, SoundRecorder.TakeSoundRecorderListener listener,
                                boolean isEncrypt) {
        if (null == mSoundRecorder) {
            mSoundRecorder = new SoundRecorder(activity, listener);
        }
        if (null != listener) {
            listener.onRecorderStart();
        }
        mSoundRecorder.launchRecording(activity, isEncrypt);
    }

    public void cancel() {
        if (null != mSoundRecorder) {
            mSoundRecorder.cancel();
            mSoundRecorder = null;
        }
    }

    public void gotoSelectImage() {
        if (isPicSelectDialogShowing()) {
            return;
        }
        Handler bgHandle = null;
        if (mActivity instanceof NewNoteActivity) {
            Looper looper = ((NewNoteActivity) mActivity).getBackgroundHandlerLooper();
            bgHandle = new Handler(looper);
        } else {
            Looper looper = ((EncryptDetailActivity) mActivity).getBackgroundHandlerLooper();
            bgHandle = new Handler(looper);
        }
        final Handler mainHandler = getMainHandler(mActivity);
        bgHandle.post(new Runnable() {
            @Override
            public void run() {
                final CopyOnWriteArrayList<PicInfo> picUris = getPicUris();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (null == picUris || picUris.size() == 0) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NoteAppImpl.getContext(), R.string.no_pictures,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        createSelectPicDialog(picUris);
                    }
                };
                mainHandler.post(runnable);
            }
        });
    }

    private Handler getMainHandler(Activity activity) {
        if (mActivity instanceof NewNoteActivity) {
            return ((NewNoteActivity) mActivity).getMainHandler();
        } else {
            return ((EncryptDetailActivity) mActivity).getMainHandler();
        }
    }

    private void createSelectPicDialog(CopyOnWriteArrayList<PicInfo> picUris) {
        LinearLayout content = (LinearLayout) LayoutInflater.from(mActivity.getApplicationContext()).
                inflate(R.layout.attach_picture_selector_layout, null, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        final TextView addPics = (TextView) content.findViewById(R.id.pic_select_add);
        final AttachPicRecycleView adapterView = (AttachPicRecycleView) content.findViewById(R.id.pic_select_list);
        adapterView.setLayoutManager(layoutManager);
        final PicSelectorAdapter adapter = new PicSelectorAdapter(mActivity, picUris, new PicSelectorAdapter.SelectionPicturesListener() {
            @Override
            public void onSelectionChanged(CopyOnWriteArrayList<String> selectedPicUris) {
                if (null != selectedPicUris && selectedPicUris.size() > 0) {
                    addPics.setTextColor(ContextCompat.getColor(mActivity, R.color.attachment_selector_dialog_grid_item_text_color));
                } else {
                    addPics.setTextColor(ContextCompat.getColor(mActivity, (R.color.attachment_selector_dialog_grid_item_graded_text_color)));
                }
            }
        });
        adapterView.setAdapter(adapter);

        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mActivity);
        builder.setView(content);
        mPicSelectDialog = builder.create();
        mPicSelectDialog.setCanceledOnTouchOutside(true);
        handleAddPic(adapter, mPicSelectDialog, content);
        handleCancelPic(content, mPicSelectDialog);
        mPicSelectDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                adapter.clearSelectedPicUris();
                mPicSelectDialog = null;
            }
        });
        Window window = mPicSelectDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
        //Chenyee wanghaiyan 2017-11-27 modify for SW17W16A-1877 begin
        if(!mActivity.isFinishing()) {
            mPicSelectDialog.show();
        }
        //Chenyee wanghaiyan 2017-11-27 modify for SW17W16A-1877 end
    }

    private CopyOnWriteArrayList<PicInfo> getPicUris() {
        Cursor cursor = null;
        try {
            cursor = mActivity.getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                            MediaStore.Images.Media.DATE_ADDED + " DESC");
            if (null == cursor || cursor.getCount() == 0) {
                return null;
            }
            CopyOnWriteArrayList<PicInfo> picUris = new CopyOnWriteArrayList();
            cursor.moveToFirst();
            int width = -1;
            int height = -1;
            final String column = "_data";
            do {
                width = cursor.getInt(cursor.getColumnIndexOrThrow("width"));
                height = cursor.getInt(cursor.getColumnIndexOrThrow("height"));
                if(height < 1 || width < 1){
                    continue;
                }
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(column));
                PicInfo info = new PicInfo();
                info.uri = Uri.fromFile(new File(filePath)).toString();
               // if (LogSwitch.DEBUG) {
                //    Log.i(LogSwitch.TAG + TAG, "info.uri = " + info.uri);
               // }
                picUris.add(info);
            } while (cursor.moveToNext());
            return picUris;
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void dismissPicSelectDialog() {
        if (mPicSelectDialog != null && mPicSelectDialog.isShowing()) {
            mPicSelectDialog.dismiss();
        }
        mPicSelectDialog = null;
    }

    private boolean isPicSelectDialogShowing() {
        return mPicSelectDialog != null && mPicSelectDialog.isShowing();
    }

    private void handleAddPic(final PicSelectorAdapter adapter, final Dialog dialog, View content) {
        TextView addPics = (TextView) content.findViewById(R.id.pic_select_add);
        addPics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CopyOnWriteArrayList selectPic = adapter.getSelectedPicUris();
                Log.d(TAG,"wanghaiyan_selectPic.size()" + selectPic.size());
                if (selectPic.size() > 0) {
                    mAddPicListener.onAdd(selectPic);
		            //Gionee wanghaiyan 2017-8-16 modify for 188845 begin
		            dialog.dismiss();
                    //Gionee wanghaiyan 2017-8-16 modify for 188845 end
                }  
            }
        });
    }

    private void handleCancelPic(View content, final Dialog dialog) {
        TextView canle = (TextView) content.findViewById(R.id.pic_select_cancel);
        canle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void gotoTakePhotos() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File fileDirectory = StorageUtils.getAvailableFileDirectory(mActivity, IMAGW_MIN_SIZE,
                    Constants.NOTE_MEDIA_PHOTO_PATH);
            if (fileDirectory == null) {
                fileDirectory = Constants.NOTE_MEDIA_PHOTO_PATH;
            }
            if (!fileDirectory.exists()) {
                boolean success = fileDirectory.mkdirs();
                if (!success) {
                    return;
                }
            }
            File file = NoteUtils.getSaveImageFile(fileDirectory);
            Uri uri;
            if (Build.VERSION.SDK_INT >= 24) {
		    //GN_Oversea_Bug><wanghaiyan><20161230> modify for #57307 begin
		    uri = FileProvider.getUriForFile(mActivity,"com.cydroid.note.fileprovider",file);
            //GN_Oversea_Bug><wanghaiyan><20161230> modify for #57307 end
            }else {
                uri = Uri.fromFile(file);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            mActivity.startActivityForResult(intent, NewNoteActivity.REQUEST_TAKE_PHOTO);
            if (mTakePhotoListener != null) {
                mTakePhotoListener.onStartTakePhoto(uri);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mActivity, R.string.attachment_enter_camera_fail, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "gotoTakePhotos fail : " + e.toString());
        } catch (FileUriExposedException e) {
            Toast.makeText(mActivity, R.string.attachment_enter_camera_fail, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "gotoTakePhotos fail : " + e.toString());
        }
    }
}
