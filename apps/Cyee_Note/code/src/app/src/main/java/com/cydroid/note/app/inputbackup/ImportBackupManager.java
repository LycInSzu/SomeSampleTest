package com.cydroid.note.app.inputbackup;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import com.cydroid.note.common.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.common.ThreadPool;

import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeCheckBox;

public class ImportBackupManager {

    private static final String TAG = "ImportBackupManager";
    private static final String PROGRESS_FORMAT = "%3d%%";
    private static final int PROGRESS_MAX = 100;
    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final int MSG_DISMISS_PROGRESS_DIALOG = 2;
    private Activity mActivity;
    private CyeeAlertDialog mImportDialog;
    private CyeeAlertDialog mProgressDialog;
    private ImportBackUp mImportBackUp;
    private Handler mMainHandler;
    private ProgressBar mProgressBar;
    private TextView mProgressMessage;


    public ImportBackupManager(Activity activity) {
        mActivity = activity;
        mMainHandler = new Handler(activity.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_DISMISS_PROGRESS_DIALOG:
                        dismissProgressDialog();
                        break;
                    case MSG_UPDATE_PROGRESS:
                        setProgress(mImportBackUp.getProgress());
                        if (mImportBackUp.isImportFail()) {
                            dismissProgressDialog();
                            showFailToast(mImportBackUp.getFailCode(), mImportBackUp.getMinSize());
                            return;
                        }
                        if (!mImportBackUp.isNeedInputBackup()) {
                            setProgress(PROGRESS_MAX);
                            sendEmptyMessageDelayed(MSG_DISMISS_PROGRESS_DIALOG, 100);
                            return;
                        }
                        sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 100);
                        break;
                    default:
                        break;
                }
            }
        };
    }


    private void showFailToast(int failCode, long minSize) {
        Log.d(TAG, "showFailToast failCode = " + failCode);
        int minSizeMB = (int) (minSize / (1000 * 1000));
        String message = mActivity.getResources().getString(R.string.import_backup_fail_message, minSizeMB);
        new ToastManager(mActivity).showToast(message);
    }

    public void startCheck() {
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        threadPool.submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                ImportBackUp importBackUp = NoteAppImpl.getContext().getImportBackUp();
                if (importBackUp.isNeedInputBackup()) {
                    mImportBackUp = importBackUp;
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mActivity.isFinishing() || mActivity.isDestroyed()) {
                                return;
                            }
                            if (mImportDialog == null) {
                                mImportDialog = createImportDialog();
                            }
                            mImportDialog.show();
                        }
                    });
                }
                return null;
            }
        });
    }

    private CyeeAlertDialog createImportDialog() {
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mActivity);
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.import_backup_config_dialog_content_ly, null);
        final CyeeCheckBox checkBox = (CyeeCheckBox) view.findViewById(R.id.import_dialog_checkBox_id);
        view.findViewById(R.id.cyee_confirm_dialog_id_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissImportDialog();
                if (checkBox.isChecked()) {
                    ImportBackUp.writeFinishImport();
                }
            }
        });
        view.findViewById(R.id.cyee_confirm_dialog_id_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissImportDialog();
                startInput();
            }
        });
        builder.setCancelable(true);
        builder.setView(view);
        return builder.create();
    }

    private CyeeAlertDialog createProgressDialog() {
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mActivity);
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.import_backup_progress_dialog_ly, null);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.import_backup_dialog_progress_bar_id);
        TextView progressMessage = (TextView) view.findViewById(R.id.import_backup_dialog_progress_message_id);
        progressBar.setMax(PROGRESS_MAX);
        mProgressBar = progressBar;
        mProgressMessage = progressMessage;
        builder.setView(view);
        CyeeAlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

    private void setProgress(int progress) {
        if (mProgressBar != null) {
            mProgressBar.setProgress(progress);
        }
        if (mProgressMessage != null) {
            mProgressMessage.setText(String.format(PROGRESS_FORMAT, progress));
        }
    }

    private void startInput() {
        if (mImportBackUp != null) {
            if (mProgressDialog == null) {
                mProgressDialog = createProgressDialog();
            }
            mProgressDialog.show();
            setProgress(mImportBackUp.getProgress());
            ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
            threadPool.submit(new ThreadPool.Job<Object>() {
                @Override
                public Object run(ThreadPool.JobContext jc) {
                    mImportBackUp.start();
                    return null;
                }
            });
            mMainHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        }
    }

    public void resume() {
    }

    public void pause() {
    }

    public void destroy() {
        mMainHandler.removeCallbacksAndMessages(null);
        dismissImportDialog();
        dismissProgressDialog();
    }

    private void dismissImportDialog() {
        if (mImportDialog != null && mImportDialog.isShowing()) {
            mImportDialog.dismiss();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}

