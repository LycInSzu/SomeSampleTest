package com.cydroid.note.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.cydroid.note.common.Log;

import com.gionee.framework.log.Logger;
import com.cydroid.note.R;
import com.cydroid.note.app.dialog.CyeeIndeterminateProgressDialog;
import com.cydroid.note.app.effect.DrawableManager;
import com.cydroid.note.app.view.NoteContentEditText;
import com.cydroid.note.app.view.NoteTitleEditText;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.StorageUtils;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.encrypt.EncryptDetailActivity;
import com.cydroid.note.photoview.PreviewActivity;

import java.io.File;

public class PreviewPictureMakeProxy {

    private static final boolean DEBUG = false;
    private static final String TAG = "PreviewPictureMakeProxy";
    private static final int MAX_SCREEN = 600;

    private static final int MSG_FAIL = 1;
    private static final int MSG_NO_SPACE = 2;
    private static final int MSG_DRAW_CONTENT = 3;
    private static final int MSG_SUCCESS = 4;
    private static final int EMPTY_TITLE_CONTENT_TOP = 6;
    private static final int MSG_CREATE_BMP_FAIL = 7;

    private Activity mActivity;
    private Handler mMainHandler;
    private CyeeIndeterminateProgressDialog mDialog;
    private EditText mTitleView;
    private EditText mContentView;
    private boolean mIsCancel;
    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
    private boolean mIsNoteChange;
    private int mNoteId;
    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end

    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883)
    public PreviewPictureMakeProxy(Activity activity,boolean isNoteChange,int noteId) {
        mActivity = activity;
        mMainHandler = new Handler(activity.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (mIsCancel) {
                    return;
                }
                switch (msg.what) {
                    case MSG_FAIL:
                        Logger.printLog(TAG, "MSG_FAIL");
                        showCursor(mTitleView, mContentView);
                        mDialog.dismiss();
                        Toast.makeText(mActivity, R.string.create_picture_fail,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_CREATE_BMP_FAIL:
                        Logger.printLog(TAG, "MSG_CREATE_BMP_FAIL");
                        showCursor(mTitleView, mContentView);
                        mDialog.dismiss();
                        Toast.makeText(mActivity, R.string.create_picture_create_bmp_fail,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_NO_SPACE:
                        Logger.printLog(TAG, "MSG_NO_SPACE");
                        showCursor(mTitleView, mContentView);
                        mDialog.dismiss();
                        Toast.makeText(mActivity, R.string.create_picture_fail_no_space,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_DRAW_CONTENT:
                        DrawContentProxy drawContentProxy = (DrawContentProxy) msg.obj;
                        drawContentProxy.draw();
                        saveShareBitmap(drawContentProxy.getTempShareFileDirectory(),
                                drawContentProxy.getDstBitmap());
                        break;
                    case MSG_SUCCESS:
                        Logger.printLog(TAG, "MSG_SUCCESS");
                        showCursor(mTitleView, mContentView);
                        mDialog.dismiss();
                        PreViewData preViewData = (PreViewData) msg.obj;
                        if (PreviewActivity.getsSharePreBitmap() != null) {
                            PreviewActivity.recycleSharePreBitmap();
                        }
                        PreviewActivity.setSharePreBitmap(preViewData.getPreBitmap());
                        //gionee chen_long02 modify on 2016-03-14 for CR01649481(39883) begin
			            Log.i("chen_long02","PreviewPictureMakerProxy mIsNoteChange "+mIsNoteChange);
                        if (!startPreviewActivity(preViewData.getFilePath(),mIsNoteChange)) {
                        //gionee chen_long02 modify on 2016-03-14 for CR01649481(39883) end
                            PreviewActivity.recycleSharePreBitmap();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        mDialog = new CyeeIndeterminateProgressDialog(activity);
        mDialog.setMessage(R.string.create_picture);
	    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
    	mIsNoteChange=isNoteChange;
    	mNoteId=noteId;
    	//gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
    }

    private boolean startPreviewActivity(String filePath) {
        Intent intent = new Intent(mActivity, PreviewActivity.class);
        intent.putExtra("img_path", filePath);
        if (mActivity instanceof EncryptDetailActivity) {
            intent.putExtra(Constants.IS_SECURITY_SPACE,
                    mActivity.getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false));
        }
        try {
            mActivity.startActivity(intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

   //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) begin
    private boolean startPreviewActivity(String filePath,boolean isNoteChange) {
        Intent intent = new Intent(mActivity, PreviewActivity.class);
        intent.putExtra("img_path", filePath);
        intent.putExtra("isNoteChange",isNoteChange);
        intent.putExtra("noteId",mNoteId);
	    if(mActivity instanceof EncryptDetailActivity) {
            intent.putExtra(Constants.IS_SECURITY_SPACE,
                    mActivity.getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false));
        }
        try {
            mActivity.startActivity(intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    //gionee chen_long02 add on 2016-03-14 for CR01649481(39883) end
    private void showCursor(EditText titleView, EditText contentView) {
        if (titleView != null) {
            titleView.setCursorVisible(true);
        }
        if (contentView != null) {
            contentView.setCursorVisible(true);
        }
    }

    private void hideCursor(EditText titleView, EditText contentView) {
        if (titleView.isCursorVisible()) {
            mTitleView = titleView;
            titleView.setCursorVisible(false);
        }
        if (contentView.isCursorVisible()) {
            mContentView = contentView;
            contentView.setCursorVisible(false);
        }
    }

    public void cancel() {
        mIsCancel = true;
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
            showCursor(mTitleView, mContentView);
        }
    }

    public void createPreviewPicture(final NoteTitleEditText titleEditText,
                                     final NoteContentEditText contentEditText, final int effect) {
        mDialog.show();
        hideCursor(titleEditText, contentEditText);
        ((NoteAppImpl) mActivity.getApplication()).getThreadPool().submit(new ThreadPool.Job<Void>() {
            @Override
            public Void run(ThreadPool.JobContext jc) {

                int tw = titleEditText.getWidth();
                int th = titleEditText.getHeight();
                int cw = contentEditText.getWidth();
                int ch = contentEditText.getHeight();

                boolean isTitleEmpty = isTitleEmpty(titleEditText);

                ViewGroup.MarginLayoutParams tlp = (ViewGroup.MarginLayoutParams)
                        titleEditText.getLayoutParams();
                int tMarginTop = tlp.topMargin;
                int tMarginBottom = tlp.bottomMargin;

                ViewGroup.MarginLayoutParams clp = (ViewGroup.MarginLayoutParams)
                        contentEditText.getLayoutParams();
                int cMarginTop = clp.topMargin;
                int cMarginBottom = clp.bottomMargin;

                if (cw == 0 | ch == 0 || tw == 0 | th == 0) {
                    mMainHandler.sendEmptyMessage(MSG_FAIL);
                    return null;
                }

                NoteUtils.assertTrue(tw == cw);
                int screenHeight = NoteUtils.sScreenHeight;
                int bitmapWidth = cw;
                int bitmapHeight = 0;
                if (isTitleEmpty) {
                    bitmapHeight = ch + cMarginBottom + (int) (EMPTY_TITLE_CONTENT_TOP * NoteUtils.sDensity);
                } else {
                    bitmapHeight = (th + tMarginTop + tMarginBottom) + (ch + cMarginTop + cMarginBottom);
                }

                float scale = 1.0f;
                int maxLimitHeight = screenHeight * MAX_SCREEN;
                if (bitmapHeight > maxLimitHeight) {
                    scale = ((float) maxLimitHeight) / bitmapHeight;
                }

                if (scale < 1.0f) {
                    bitmapWidth = (int) Math.ceil(bitmapWidth * scale);
                    bitmapHeight = (int) Math.ceil(bitmapHeight * scale);
                }

                File tempShareFileDirectory = StorageUtils.getAvailableFileDirectory(mActivity,
                        bitmapWidth * bitmapHeight * 4, Constants.NOTE_MEDIA_IMAGE_TEMP_SHARE_PATH);
                if (tempShareFileDirectory == null) {
                    mMainHandler.sendEmptyMessage(MSG_NO_SPACE);
                    return null;
                }

                Bitmap dstBmp = DrawableManager.getEffectBitmap(mActivity, effect, bitmapWidth, bitmapHeight);
                if (dstBmp == null) {
                    mMainHandler.sendEmptyMessage(MSG_CREATE_BMP_FAIL);
                    return null;
                }
                Canvas canvas = new Canvas(dstBmp);

                //draw content
                int contentTranslateY = 0;
                if (!isTitleEmpty) {
                    contentTranslateY = th + tMarginTop + tMarginBottom + cMarginTop;
                } else {
                    contentTranslateY = (int) (EMPTY_TITLE_CONTENT_TOP * NoteUtils.sDensity);
                }
                DrawContentProxy drawContentProxy = new DrawContentProxy(titleEditText, contentEditText,
                        canvas, tempShareFileDirectory, dstBmp, scale, tMarginTop,
                        contentTranslateY, !isTitleEmpty);
                mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_DRAW_CONTENT, drawContentProxy));
                return null;
            }
        });
    }

    private boolean isTitleEmpty(NoteTitleEditText titleEditText) {
        boolean empty = false;
        if (null == titleEditText) {
            empty = true;
        } else {
            Editable text = titleEditText.getText();
            if (null != text) {
                empty = TextUtils.isEmpty(text.toString());
            }
        }
        return empty;
    }

    private static class DrawContentProxy {
        private NoteTitleEditText mTitleView;
        private NoteContentEditText mContentView;
        private Canvas mCanvas;
        private File mTempShareFileDirectory;
        private Bitmap mDstBmp;
        private float mScale;
        private int mTitleTranslateY;
        private int mContentTranslateY;
        private boolean mShouldDrawTitleView;

        public DrawContentProxy(NoteTitleEditText titleView, NoteContentEditText contentView, Canvas canvas,
                                File tempShareFileDirectory, Bitmap dstBmp, float scale,
                                int titleTranslateY, int contentTranslateY, boolean shouldDrawTitleView) {
            mTitleView = titleView;
            mContentView = contentView;
            mCanvas = canvas;
            mTempShareFileDirectory = tempShareFileDirectory;
            mDstBmp = dstBmp;
            mScale = scale;
            mTitleTranslateY = titleTranslateY;
            mContentTranslateY = contentTranslateY;
            mShouldDrawTitleView = shouldDrawTitleView;
        }

        public void draw() {
            mCanvas.scale(mScale, mScale);
            if (mShouldDrawTitleView) {
                drawTitle();
            }
            drawContent();
        }

        private void drawTitle() {
            mCanvas.translate(0, mTitleTranslateY);
            mTitleView.draw(mCanvas);
            mCanvas.translate(0, -mTitleTranslateY);
        }

        private void drawContent() {
            mCanvas.translate(0, mContentTranslateY);
            mContentView.setAmiTagEnable(true);
            mContentView.draw(mCanvas);
            mContentView.setAmiTagEnable(false);
            mCanvas.translate(0, -mContentTranslateY);
        }

        public File getTempShareFileDirectory() {
            return mTempShareFileDirectory;
        }

        public Bitmap getDstBitmap() {
            return mDstBmp;
        }
    }

    private static class PreViewData {
        private Bitmap mBitmap;
        private String mFilePath;

        public PreViewData(Bitmap bitmap, String filePath) {
            mBitmap = bitmap;
            mFilePath = filePath;
        }

        public Bitmap getPreBitmap() {
            return mBitmap;
        }

        public String getFilePath() {
            return mFilePath;
        }
    }

    private void saveShareBitmap(final File tempShareFileDirectory, final Bitmap dstBmp) {

        ((NoteAppImpl) mActivity.getApplication()).getThreadPool().submit(new ThreadPool.Job<Void>() {

            @Override
            public Void run(ThreadPool.JobContext jc) {

                if (!tempShareFileDirectory.exists()) {
                    boolean success = tempShareFileDirectory.mkdirs();
                    if (!success) {
                        dstBmp.recycle();
                        mMainHandler.sendEmptyMessage(MSG_FAIL);
                        return null;
                    }
                } else {
                    clearOldTempFile(tempShareFileDirectory);
                }

                File saveFile = NoteUtils.getSaveImageFile(tempShareFileDirectory);
                saveFile = new File(saveFile.getPath() + ".png");
                if (DEBUG) {
                    Logger.printLog(TAG, "saveFile = " + saveFile);
                }
                if (!NoteUtils.saveBitmap(dstBmp, saveFile, Bitmap.CompressFormat.PNG, 100)) {
                    if (DEBUG) {
                        Logger.printLog(TAG, "saveFile fail");
                    }
                    dstBmp.recycle();
                    mMainHandler.sendEmptyMessage(MSG_FAIL);
                    return null;
                }
                if (DEBUG) {
                    Logger.printLog(TAG, "saveFile success");
                }
                PreViewData preViewData = new PreViewData(dstBmp, saveFile.getPath());
                mMainHandler.obtainMessage(MSG_SUCCESS, preViewData).sendToTarget();
                return null;
            }
        });
    }

    private void clearOldTempFile(File dirFile) {
        if (!dirFile.isDirectory()) {
            return;
        }
        File[] files = dirFile.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        long curTime = System.currentTimeMillis();
        long limit = 1000 * 60 * 10;
        for (File file : files) {
            long lastTime = file.lastModified();
            if ((curTime - lastTime) > limit) {
                boolean success = file.delete();
                if (!success) {
                    Logger.printLog(TAG, "clearOldTempFile del fail file = " + file);
                }
            }
        }
    }
}
