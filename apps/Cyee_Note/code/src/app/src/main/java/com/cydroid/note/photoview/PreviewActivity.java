package com.cydroid.note.photoview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.content.FileProvider;
//Chenyee wanghaiyan 2018-1-23 modify for CSW1702A-2611 begin
import android.media.MediaScannerConnection;
//Chenyee wanghaiyan 2018-1-23 modify for CSW1702A-2611 end

import com.cydroid.note.common.Log;
import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.app.view.SharePreView;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.DecodeUtils;
import com.cydroid.note.common.FileUtils;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.StorageUtils;
import com.cydroid.note.common.ThreadPool;
//Gionee wanghaiyan 20170307 add for 77568 begin
import com.Legal.Java.pwinSign;
//Gionee wanghaiyan 20170307 add for 77568 end

//gionee chen_long02 add on 2016-03-14 for CR01649481 begin
import android.widget.Toast;
import com.cydroid.note.provider.NoteContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.content.ContentValues;
//gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
import com.cydroid.note.common.Log;

import java.io.File;

public class PreviewActivity extends StandardActivity implements View.OnClickListener {
    private static final String TAG = "PreviewActivity";
    private static final boolean DEBUG = false;
    private Uri mUri;
    private String mImgPath;
    private static Bitmap sSharePreBitmap;
    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
    private boolean mIsNoteChange;
    private int mNoteId;
    private  ImageView mSaveImageView;
    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
    private Uri[] mNfcPushUris = new Uri[1];
    private NfcAdapter.CreateBeamUrisCallback mCreateBeamUrisCallback = new NfcAdapter.CreateBeamUrisCallback() {
        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            return mNfcPushUris;
        }
    };


    public static void setSharePreBitmap(Bitmap sharePreBitmap) {
        try{
            NoteUtils.assertTrue(sSharePreBitmap == null);
        }catch (AssertionError error){
            Log.d(TAG,"setSharePreBitmap : "+error.toString());
        }
        sSharePreBitmap = sharePreBitmap;
    }

    public static Bitmap getsSharePreBitmap() {
        return sSharePreBitmap;
    }

    public static void recycleSharePreBitmap() {
        if (sSharePreBitmap != null) {
            sSharePreBitmap.recycle();
            sSharePreBitmap = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView();
        initListener();
        Intent intent = getIntent();
        mImgPath = intent.getStringExtra("img_path");
        //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
        mIsNoteChange = (boolean)intent.getExtra("isNoteChange");
        mNoteId = (int)intent.getExtra("noteId");
        Log.i(TAG, "PreviewActivity onCreate mIsNoteChange " + mIsNoteChange + " mNoteId " + mNoteId);
        //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
        if (DEBUG) {
            Log.d(TAG, "preview imgPath = " + mImgPath);
        }
        //Chenyee wanghaiyan 2018-1-23 modify for CSW1702A-2611 begin
        //Cyee wanghaiyan 2017-9-22 modify for 217993 begin
        //mUri = Uri.parse(Uri.decode(FileProvider.getUriForFile(NoteAppImpl.getContext(),"com.cydroid.note.fileprovider",new File   (mImgPath)).toString()));
        //Cyee wanghaiyan 2017-9-22 modify for 217993 end
        String[] paths = new String[]{mImgPath};
        MediaScannerConnection.scanFile(NoteAppImpl.getContext(), paths, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                mUri = uri;
                Log.d(TAG,"mUri" + mUri + "mImgPath+" + mImgPath);

            }
        });
        //Chenyee wanghaiyan 2018-1-23 modify for CSW1702A-2611 end
        mNfcPushUris[0] = mUri;
//        final SharePreView sharePreView = ((SharePreView) findViewById(R.id.share_preview));
//        ViewTreeObserver viewTreeObserver = sharePreView.getViewTreeObserver();
//        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                Bitmap bitmap = getsSharePreBitmap();
//                if (bitmap != null) {
//                    sharePreView.setBitmap(getsSharePreBitmap());
//                } else {
//                    sharePreView.setVisibility(View.VISIBLE);
//                    asynLoadSharePreviewBitmap();
//                }
//            }
//        });
        Bitmap bitmap = getsSharePreBitmap();
        if (bitmap != null) {
            ((SharePreView) findViewById(R.id.share_preview)).setBitmap(bitmap);
        } else {
            findViewById(R.id.preview_load_bar).setVisibility(View.VISIBLE);
            asynLoadSharePreviewBitmap();
        }
    }

    private void asynLoadSharePreviewBitmap() {
        NoteAppImpl.getContext().getThreadPool().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                final Bitmap preViewBitmap = DecodeUtils.decodeBitmap(NoteAppImpl.getContext(), mUri);
                PreviewActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!validActivityEnv()) {
                            return;
                        }
                        if (preViewBitmap == null) {
                            finish();
                            return;
                        }
                        findViewById(R.id.preview_load_bar).setVisibility(View.GONE);
                        PreviewActivity.setSharePreBitmap(preViewBitmap);
                        ((SharePreView) findViewById(R.id.share_preview)).setBitmap(preViewBitmap);
                    }
                });
                return null;
            }
        });
    }

    private boolean validActivityEnv() {
        return !(isFinishing() || isDestroyed());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNfcBeamPush(null, mCreateBeamUrisCallback, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setupNfcBeamPush(null, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.preview_activity_title_layout_back: {
                onBack();
                break;
            }
            case R.id.preview_activity_title_layout_share: {
                shareNoteImage();
                break;
            }
            case R.id.preview_activity_title_layout_save: {
                //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
                mSaveImageView.setEnabled(false);
                boolean mNoteIsSavedAsImage=checkNoteIsSavedAsImage();
                Log.i(TAG, "mNoteIsSavedAsImage mIsNoteChange "+mNoteIsSavedAsImage+" "+mIsNoteChange);
                if(mIsNoteChange){
                    saveImage();
                    return;
                }else if(!mIsNoteChange&&!mNoteIsSavedAsImage){
                    saveImage();
                    return;
                }else{
                    Toast.makeText(this, getResources().getString(R.string.note_content_notChange_hint), Toast.LENGTH_SHORT).show();
                }
                //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
            }
            default: {
                break;
            }
        }

    }

    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
    private void tintImageViewDrawable(int imageViewId, int iconId, int colorsId) {
        Drawable icon = ContextCompat.getDrawable(this, iconId);
        Drawable tintIcon = DrawableCompat.wrap(icon);
        DrawableCompat.setTintList(tintIcon, ContextCompat.getColorStateList(this, colorsId));
        ((ImageView) findViewById(imageViewId)).setImageDrawable(tintIcon);
    }

    private boolean checkNoteIsSavedAsImage(){
        final String selection = "_id=?";
        String noteSavedImagePath=NoteUtils.getNoteSavedImagePathColumn(this, NoteContract.NoteContent.CONTENT_URI, selection,
                new String[]{mNoteId+""});
        Log.d(TAG,"noteSavedImagePath" + noteSavedImagePath);
        if(noteSavedImagePath==null||noteSavedImagePath.equals("")){
            return false;
        }else{
            File imageFile=new File(noteSavedImagePath);
            Log.d(TAG,"imageFile" + imageFile);
            if(!imageFile.exists()){
                return false;
            };
        }
        return true;
    }
    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end

    private void onBack() {
        finish();
    }

    private void shareNoteImage() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, mUri);
        startActivity(Intent.createChooser(intent,
                getResources().getString(R.string.note_action_share_string)));
    }

    private void saveImage() {
        File originFile = new File(mImgPath);
        if (!originFile.exists()) {
            Toast.makeText(this, R.string.file_save_fail_origin_no_exist, Toast.LENGTH_SHORT).show();
            return;
        }
        //GIONEE wanghaiyan 2016-12-06 modify for 39890 and 40287 begin
        //File defaultFile = new File(Environment.getExternalStorageDirectory(), "/cyee/AmiNote");
        //Chenyee wanghaiyan 2019-10-26 modify for CSW1805A-765 begin
        //String saveImagePath = FileUtils.getSaveImagePath(this);
        //File defaultFile = new File(saveImagePath, "/Notes");
        File defaultFile = new File(Environment.getExternalStorageDirectory(), "/Notes");
        //Chenyee wanghaiyan 2019-10-26 modify for CSW1805A-765 end
        //GIONEE wanghaiyan 2016-12-06 modify for 39890 and 40287 end
        long size = originFile.length();
        File targetFile = StorageUtils.getAvailableFileDirectory(this, size, defaultFile);
        if (targetFile != null) {
            boolean allowSave;
            if (!targetFile.exists()) {
                allowSave = targetFile.mkdirs();
                if (!allowSave) {
                    Log.d(TAG, "create AmiNote dir fail");
                }
            } else {
                allowSave = true;
            }
            if (allowSave) {
                String finallyPath = NoteUtils.getSaveImageFile(targetFile).getPath() + ".png";
                boolean success = FileUtils.copyFile(mImgPath, finallyPath);
                if (success) {
                    //Gionee wanghaiyan 20170307 add for 77568 begin
                    if (NoteUtils.gnKRFlag) {
                        Log.d("kptc", "com.cydroid.note.photoview.PreviewActivity->saveImage(): saveSelfSignFile()");
                        int retCode = pwinSign.saveSelfSignFile(finallyPath);
                        Log.d("kptc", "retCode=" + retCode);
                        //Chenyee 2018-5-10 modify for CSW1703KR-68 begin
                        pwinSign.sendBroadcastToRedService(NoteAppImpl.getContext(), finallyPath);
                        //Chenyee 2018-5-10 modify for CSW1703KR-68 end
                    }
                    //Gionee wanghaiyan 20170307 add for 77568 end
                    notifyMediaScanFile(new File(finallyPath));
                    showCopyState(finallyPath);
                    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
                    //更新数据库
                    NoteUtils.updateNoteData(getContentResolver(),mNoteId,finallyPath);
                    Log.i(TAG, "finallyPath "+finallyPath);
                    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
                    return;
                }
            }
        }
        Toast.makeText(this, R.string.file_save_fail_toast_text, Toast.LENGTH_SHORT).show();
    }

    private void notifyMediaScanFile(File file) {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(uri);
        sendBroadcast(intent);
    }

    private void showCopyState(String fPath) {
        fPath = NoteUtils.customName(this, fPath);
        //GIONEE wanghaiyan 2016-12-6 modify for 39890 begin
        fPath = customName(fPath);
        //GIONEE wanghaiyan 2016-12-6 modify for 39890 end
        String msg = getResources().getString(R.string.file_save_toast_text) + fPath;
        new ToastManager(this).showToast(msg);
    }
    //GIONEE wanghaiyan 2016-12-6 modify for 39890 begin
    private String customName(String fPath){
        if(!fPath.toLowerCase().contains("internal shared storage")){
            return fPath;
        }
        String[] fPathRegs=fPath.split("/");
        fPathRegs[0]=getResources().getString(R.string.note_storage_inner);
        StringBuilder strBuilder=new StringBuilder();
        for(int i=0;i<fPathRegs.length;i++){
            strBuilder.append(fPathRegs[i]+"/");
        }
        fPath=strBuilder.substring(0,strBuilder.length()-1);
        return fPath;
    }
    //GIONEE wanghaiyan 2016-12-6 modify for 39890 end

    private void initContentView() {
        //Gionee bianrong 201606025 modify for CR01723129 begin
        if (NoteUtils.gnKRFlag){
            setNoteTitleView(R.layout.shared_image_preview_title_kr);
        }else{
            setNoteTitleView(R.layout.shared_image_preview_title);
        }
        //Gionee bianrong 201606025 modify for CR01723129 end
        setTitleIconColor();
        setNoteContentView(R.layout.preview_layout);
        setNoteRootViewBackgroundColor();
    }

    private void setTitleIconColor() {
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        tintImageViewDrawable(R.id.preview_activity_title_layout_back,
                R.drawable.note_title_back_icon, ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));
        tintImageViewDrawable(R.id.preview_activity_title_layout_share,
                R.drawable.action_share_icon, ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));
        tintImageViewDrawable(R.id.preview_activity_title_layout_save,
                R.drawable.action_save_icon, ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));
        ((TextView) findViewById(R.id.share_text)).setTextColor(getTitleTextColor());
    }

    private void initListener() {
        findViewById(R.id.preview_activity_title_layout_back).setOnClickListener(this);
        findViewById(R.id.preview_activity_title_layout_share).setOnClickListener(this);
        //gionee chen_long02 modify on 2016-03-14 for CR01649481(39883) begin
        mSaveImageView=(ImageView)findViewById(R.id.preview_activity_title_layout_save);
        tintImageViewDrawable(R.id.preview_activity_title_layout_save,
                R.drawable.action_save_icon, R.color.new_note_title_delete_color);
        mSaveImageView.setOnClickListener(this);
        //gionee chen_long02 modify on 2016-03-14 for CR01649481(39883) end
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupNfcBeamPush(Uri[] uris, NfcAdapter.CreateBeamUrisCallback callback, Activity activity) {
        if (Build.VERSION.SDK_INT < 16) return;

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter != null) {
            adapter.setBeamPushUris(uris, activity);
            adapter.setBeamPushUrisCallback(callback, activity);
        }
    }
}
