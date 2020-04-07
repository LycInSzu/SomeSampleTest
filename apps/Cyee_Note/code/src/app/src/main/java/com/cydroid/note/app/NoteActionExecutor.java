package com.cydroid.note.app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import com.cydroid.note.common.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
//Gionee wanghaiyan 2017-3-22 modify for 90829 begin
import android.text.method.ScrollingMovementMethod;
//Gionee wanghaiyan 2017-3-22 modify for 90829 end
import com.cydroid.note.R;
import com.cydroid.note.app.dialog.CyeeConfirmDialog;
import com.cydroid.note.app.dialog.CyeeDeterminateProgressDialog;
import com.cydroid.note.app.reminder.ReminderManager;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Future;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.data.DataManager;
import com.cydroid.note.data.LocalNoteItem;
import com.cydroid.note.data.NoteInfo;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.Path;
import com.cydroid.note.data.SecretNoteItem;
import com.cydroid.note.encrypt.DES;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.NoteActionProgressListener;
import com.cydroid.note.encrypt.NoteItemAttachInfo;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteShareDataManager;
import com.cydroid.note.trash.util.TrashUtils;
import com.cydroid.note.widget.WidgetUtil;

import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeCheckBox;
//GIONEE wanghaiyan 2016-12-08 modify for 43513 begin
import cyee.changecolors.ChameleonColorManager;
//GIONEE wanghaiyan 2016-12-08 modify for 43513 end

public class NoteActionExecutor {
    private static final String TAG = "NoteActionExecutor";
    private static final int MSG_TASK_COMPLETE = 1;
    private static final int MSG_TASK_UPDATE = 2;
    private static final int MSG_ENCRYPT_UPDATE = 3;
    private static final int EXECUTOR_ENCRYPT = 4;
    private static final int EXECUTOR_DECRYPT = 5;
    private static final int DELETE_TASK = 6;
    private static final int ENCRYPT_TASK = 7;
    private static final int DECRYPT_TASK = 8;
    private static final int RECOVERY_TASK = 9;
    private static final int TRASH_THROW_INTO_TASK = 10;
    private static final int TRASH_RECOVER_TASK = 11;
    private static final int TRASH_DELETE_TASK = 12;
    private static final int NO_TASK = 0;

    private CyeeConfirmDialog mConfirmDialog;
    private CyeeDeterminateProgressDialog mProgressDialog;
    private Handler mMainHandler;
    private Activity mActivity;
    private DataManager mDataManager;
    private Future mCurTask;
    private static volatile int sCurrentRunningTask = NO_TASK;

    public interface NoteActionListener {
        void onActionPrepare();

        int onActionInvalidId();

        void onActionFinish(int success, int fail);
    }

    public NoteActionExecutor(Activity activity) {
        mActivity = activity;
        init();
    }

    public NoteActionExecutor() {
        init();
    }

    private void init() {
        NoteAppImpl app = NoteAppImpl.getContext();
        mDataManager = app.getDataManager();
        mMainHandler = new Handler(app.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_TASK_COMPLETE:
                        mCurTask = null;
                        sCurrentRunningTask = NO_TASK; //NOSONAR
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                        int success = msg.arg1;
                        int fail = msg.arg2;
                        Object listener = msg.obj;
                        if (listener != null) {
                            ((NoteActionListener) listener).onActionFinish(success, fail);
                        }
                        break;
                    case MSG_TASK_UPDATE:
                        if (mProgressDialog != null) {
                            mProgressDialog.setProgress(msg.arg1);
                        }
                        break;
                    case MSG_ENCRYPT_UPDATE:
                        if (mProgressDialog != null) {
                            mProgressDialog.setProgress(msg.arg1, msg.arg2);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void resume() {
    }

    public void pause() {
        if (sCurrentRunningTask != DELETE_TASK && mCurTask != null) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            mCurTask.cancel();
        }
    }

    public void destroy() {
        mMainHandler.removeCallbacksAndMessages(null);
    }

    public void startDeleteAction(final long id, final NoteActionListener listener, final boolean isEncrypted) {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(NoteAppImpl.getContext()).showToast(getToastString(sCurrentRunningTask));
            return;
        }

        int titleId = R.string.note_action_del_string;
        int msgId = isEncrypted ? R.string.encrypt_note_action_del_message : R.string.note_action_del_message;
        showConfirmDialog(new CyeeConfirmDialog.OnClickListener() {
            @Override
            public void onClick(int which) {
                if (which == CyeeConfirmDialog.BUTTON_POSITIVE) {
                    sCurrentRunningTask = DELETE_TASK; //NOSONAR
                    executorDel(id, listener, isEncrypted);
                }
            }
        }, titleId, msgId);
    }

    public void startEncryptAction(final NoteSelectionManager selectionManager, final NoteActionListener listener) {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(NoteAppImpl.getContext()).showToast(getToastString(sCurrentRunningTask));
            return;
        }
        sCurrentRunningTask = ENCRYPT_TASK; //NOSONAR
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        mCurTask = threadPool.submit(new ThreadPool.Job() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                final List<NoteItem> items = getNoteItems(selectionManager, listener);
                final int count = EncryptUtil.getAttachCount(items);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog(count + selectionManager.getSelectedCount(), selectionManager.getSelectedCount(),
                                NoteAppImpl.getContext().getResources().getString(R.string.note_action_encrypt_string));
                    }
                });
                EncryptRunnable encryptRunnable = new EncryptRunnable(items, listener, jc);
                encryptRunnable.run();
                return null;
            }
        });
    }

    public void startDecryptAction(final NoteSelectionManager selectionManager, final NoteActionListener listener) {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(NoteAppImpl.getContext()).showToast(getToastString(sCurrentRunningTask));
            return;
        }
        sCurrentRunningTask = DECRYPT_TASK; //NOSONAR
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        mCurTask = threadPool.submit(new ThreadPool.Job() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                final List<NoteItem> items = getNoteItems(selectionManager, listener);
                final int count = EncryptUtil.getAttachCount(items);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog(count + selectionManager.getSelectedCount(), selectionManager.getSelectedCount(),
                                NoteAppImpl.getContext().getResources().getString(R.string.note_action_decrypt_string));
                    }
                });
                DecryptRunnable decryptRunnable = new DecryptRunnable(items, listener, jc);
                decryptRunnable.run();
                return null;
            }
        });

    }

    public void startDeleteAction(final NoteSelectionManager selectionManager, final NoteActionListener listener,
                                  final boolean isEncrypted) {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(NoteAppImpl.getContext()).showToast(getToastString(sCurrentRunningTask));
            return;
        }

        int titleId = R.string.note_action_del_string;
        int msgId = isEncrypted ? R.string.encrypt_note_action_del_message : R.string.note_action_del_message;
        showConfirmDialog(new CyeeConfirmDialog.OnClickListener() {
            @Override
            public void onClick(int which) {
                if (which == CyeeConfirmDialog.BUTTON_POSITIVE) {
                    sCurrentRunningTask = DELETE_TASK; //NOSONAR
                    executorDel(selectionManager, listener, isEncrypted);
                }
            }
        }, titleId, msgId);
    }

    public void startThrowIntoTrashAction(final NoteSelectionManager selectionManager, final NoteActionListener listener) {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(NoteAppImpl.getContext()).showToast(getToastString(sCurrentRunningTask));
            return;
        }

        int titleId = R.string.note_action_del_string;
        int msgId = R.string.trash_first_delete_alert;
        boolean showAlert = NoteShareDataManager.getShowTrashAlert(mActivity);
        if (showAlert) {
            showTipDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sCurrentRunningTask = TRASH_THROW_INTO_TASK; //NOSONAR
                    executeThrowIntoTrash(selectionManager, listener);
                }
            }, titleId, msgId);
            return;
        }

        sCurrentRunningTask = TRASH_THROW_INTO_TASK; //NOSONAR
        executeThrowIntoTrash(selectionManager, listener);
    }

    public void startTrashRecoverAction(final NoteSelectionManager selectionManager, final NoteActionListener listener) {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(NoteAppImpl.getContext()).showToast(getToastString(sCurrentRunningTask));
            return;
        }

        sCurrentRunningTask = TRASH_RECOVER_TASK; //NOSONAR
        executeTrashRecover(selectionManager, listener);
    }

    public void startTrashDeleteAction(final NoteSelectionManager selectionManager, final NoteActionListener listener) {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(NoteAppImpl.getContext()).showToast(getToastString(sCurrentRunningTask));
            return;
        }

        int titleId = R.string.note_action_del_string;
        int msgId = R.string.trash_delete_message;
        showConfirmDialog(new CyeeConfirmDialog.OnClickListener() {
            @Override
            public void onClick(int which) {
                if (which == CyeeConfirmDialog.BUTTON_POSITIVE) {
                    sCurrentRunningTask = TRASH_DELETE_TASK; //NOSONAR
                    executeTrashDelete(selectionManager, listener);
                }
            }
        }, titleId, msgId);
    }

    private void executorDel(final long id, final NoteActionListener listener, final boolean isEncrypted) {
        if (NoteItem.INVALID_ID == id) {
            return;
        }
        sCurrentRunningTask = DELETE_TASK; //NOSONAR
        showProgressDialog(1,
                NoteAppImpl.getContext().getResources().getString(R.string.note_action_del_string));
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        mCurTask = threadPool.submit(new ThreadPool.Job() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                Path path = null;
                if (isEncrypted) {
                    path = SecretNoteItem.SECRET_ITEM_PATH.getChild(id);
                } else {
                    path = LocalNoteItem.ITEM_PATH.getChild(id);
                }
                try {
                    mDataManager.delete(path);
                    mMainHandler.sendMessageDelayed(
                            mMainHandler.obtainMessage(MSG_TASK_COMPLETE, 1, 0, listener), 10);
                } catch (Exception e) {
                }
                return null;
            }
        });
    }

    private void executorDel(final NoteSelectionManager selectionManager, final NoteActionListener listener,
                             final boolean isEncrypted) {
        showProgressDialog(selectionManager.getSelectedCount(),
                NoteAppImpl.getContext().getResources().getString(R.string.note_action_del_string));
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        mCurTask = threadPool.submit(new ThreadPool.Job() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                ArrayList<Path> paths = selectionManager.getSelected();
                if (paths == null || paths.size() == 0) {
                    mMainHandler.sendMessage(
                            mMainHandler.obtainMessage(MSG_TASK_COMPLETE, 0, 0, listener));
                    return null;
                }
                List<NoteItem> items = getNoteItems(selectionManager, listener);
                DelRunnable delRunnable = new DelRunnable(items, listener, jc, isEncrypted);
                delRunnable.run();
                return null;
            }
        });
    }

    private void executeThrowIntoTrash(final NoteSelectionManager selectionManager, final NoteActionListener listener) {
        showProgressDialog(selectionManager.getSelectedCount(),
                NoteAppImpl.getContext().getResources().getString(R.string.note_action_del_string));
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        mCurTask = threadPool.submit(new ThreadPool.Job() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                ArrayList<Path> paths = selectionManager.getSelected();
                if (paths == null || paths.size() == 0) {
                    mMainHandler.sendMessage(
                            mMainHandler.obtainMessage(MSG_TASK_COMPLETE, 0, 0, listener));
                    return null;
                }
                if (null != listener) {
                    listener.onActionPrepare();
                }
                List<NoteItem> items = getNoteItems(selectionManager, listener);
                ThrowIntoTrashRunnable runnable = new ThrowIntoTrashRunnable(items, listener, jc);
                runnable.run();
                return null;
            }
        });
    }

    private void executeTrashRecover(final NoteSelectionManager selectionManager, final NoteActionListener listener) {
        showProgressDialog(selectionManager.getSelectedCount(),
                NoteAppImpl.getContext().getResources().getString(R.string.trash_recover));
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        mCurTask = threadPool.submit(new ThreadPool.Job() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                ArrayList<Path> paths = selectionManager.getSelected();
                if (paths == null || paths.size() == 0) {
                    mMainHandler.sendMessage(
                            mMainHandler.obtainMessage(MSG_TASK_COMPLETE, 0, 0, listener));
                    return null;
                }
                List<NoteItem> items = getNoteItems(selectionManager, listener);
                TrashRecoverRunnable runnable = new TrashRecoverRunnable(items, listener, jc);
                runnable.run();
                return null;
            }
        });
    }

    private void executeTrashDelete(final NoteSelectionManager selectionManager, final NoteActionListener listener) {
        showProgressDialog(selectionManager.getSelectedCount(),
                NoteAppImpl.getContext().getResources().getString(R.string.trash_recover));
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        mCurTask = threadPool.submit(new ThreadPool.Job() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                ArrayList<Path> paths = selectionManager.getSelected();
                if (paths == null || paths.size() == 0) {
                    mMainHandler.sendMessage(
                            mMainHandler.obtainMessage(MSG_TASK_COMPLETE, 0, 0, listener));
                    return null;
                }
                List<NoteItem> items = getNoteItems(selectionManager, listener);
                TrashDeleteRunnable runnable = new TrashDeleteRunnable(items, listener, jc);
                runnable.run();
                return null;
            }
        });
    }

    private List<NoteItem> getNoteItems(NoteSelectionManager selectionManager, NoteActionListener listener) {
        ArrayList<Path> paths = selectionManager.getSelected();
        if (paths == null || paths.size() == 0) {
            mMainHandler.sendMessage(
                    mMainHandler.obtainMessage(MSG_TASK_COMPLETE, 0, 0, listener));
            return null;
        }
        List<NoteItem> items = new ArrayList<>();
        DataManager dataManager = ((NoteAppImpl) mActivity.getApplication()).getDataManager();
        for (Path path : paths) {
            NoteItem item = (NoteItem) dataManager.getMediaObject(path);
            items.add(item);
        }
        return items;
    }

    private void showTipDialog(final DialogInterface.OnClickListener listener, int titleId, int msgId) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.encrypt_hint_dialog, null);
        final CyeeCheckBox checkBox = (CyeeCheckBox) view.findViewById(R.id.encrypt_hint_dialog_checkBox);
		//GIONEE wanghaiyan 2016-12-08 modify for 43513 begin
	    if(ChameleonColorManager.isNeedChangeColor()){
           checkBox.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
	    }else{
           checkBox.setTextColor(ColorThemeHelper.getContentColorSecondaryOnBackgroud_C2(mActivity));
	    }
        //GIONEE wanghaiyan 2016-12-08 modify for 43513 end
        TextView message = (TextView) view.findViewById(R.id.encrypt_hint_dialog_content);
	    //Gionee wanghaiyan 2017-3-22 modify for 90829 begin
	    message.setMovementMethod(ScrollingMovementMethod.getInstance());
	    //Gionee wanghaiyan 2017-3-22 modify for 90829 end
        message.setVisibility(View.VISIBLE);
        message.setText(msgId);
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mActivity);
        builder.setView(view);
        builder.setTitle(titleId);
        builder.setPositiveButton(R.string.alert_user_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    NoteShareDataManager.setShowTrashAlert(mActivity, false);
                }
                if (listener != null) {
                    listener.onClick(dialog, which);
                }
            }
        });
        CyeeAlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
	    //Gionee wanghaiyan 2017-3-23 modify for 90801 begin
        dialog.setCancelable(true);
	    //Gionee wanghaiyan 2017-3-23 modify for 90801 end
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
        dialog.show();
        //Gionee wanghaiyan 2017-3-24 modify for 90654 end
    }

    private void showConfirmDialog(CyeeConfirmDialog.OnClickListener listener, int titleId, int msgId) {
        CyeeConfirmDialog confirmDialog = mConfirmDialog;
        if (confirmDialog == null) {
            confirmDialog = new CyeeConfirmDialog(mActivity);
            confirmDialog.setMessage(msgId);
            confirmDialog.setTitle(titleId);
            mConfirmDialog = confirmDialog;
        }
        confirmDialog.setOnClickListener(listener);
        confirmDialog.show();
    }

    private void showProgressDialog(int maxProgress, String message) {
        CyeeDeterminateProgressDialog progressDialog = mProgressDialog;
        if (progressDialog == null) {
            progressDialog = new CyeeDeterminateProgressDialog(mActivity);
			//Gionee wanghaiyan 2017-3-28 modify for 97043 begin
            //progressDialog.setMessage(message);
			//Gionee wanghaiyan 2017-3-28 modify for 97043 end
            progressDialog.setOnCancelListener(new CyeeDeterminateProgressDialog.OnCancelListener() {
                @Override
                public void onCancel() {
                    if (mCurTask != null) {
                        mCurTask.cancel();
                    }
                }
            });
            mProgressDialog = progressDialog;
        }
		//Gionee wanghaiyan 2017-3-28 modify for 97043 begin
        progressDialog.setMessage(message);
		//Gionee wanghaiyan 2017-3-28 modify for 97043 end
        progressDialog.setMax(maxProgress);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    private void showProgressDialog(int maxProgress, int textMaxProgress, String message) {
        CyeeDeterminateProgressDialog progressDialog = mProgressDialog;
        if (progressDialog == null) {
            progressDialog = new CyeeDeterminateProgressDialog(mActivity);
			//Gionee wanghaiyan 2017-3-28 modify for 97043 begin
            //progressDialog.setMessage(message);
			//Gionee wanghaiyan 2017-3-28 modify for 97043 end
            progressDialog.setOnCancelListener(new CyeeDeterminateProgressDialog.OnCancelListener() {
                @Override
                public void onCancel() {
                    if (mCurTask != null) {
                        mCurTask.cancel();
                    }
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                }
            });
            mProgressDialog = progressDialog;
        }
		//Gionee wanghaiyan 2017-3-28 modify for 97043 begin
        progressDialog.setMessage(message);
		//Gionee wanghaiyan 2017-3-28 modify for 97043 end
        progressDialog.setMax(maxProgress, textMaxProgress);
        progressDialog.setProgress(0, 0);
        progressDialog.show();
    }

    private abstract class ExectorRunnable implements Runnable {
        private List<NoteItem> mNoteItems;
        protected NoteActionListener mListener;
        private ThreadPool.JobContext mJc;
        private int mExectorProgressNum;
        private int mExectorProgressTextNum;
        private NoteActionProgressListener mNoteProgressListener = new NoteActionProgressListener() {
            @Override
            public void onStart(int count) {

            }

            @Override
            public void onOneComplete() {
                mMainHandler.sendMessage(
                        mMainHandler.obtainMessage(MSG_ENCRYPT_UPDATE, ++mExectorProgressNum, mExectorProgressTextNum));
            }

            @Override
            public void onAllComplete(boolean isEncrpte) {
            }
        };

        public ExectorRunnable(List<NoteItem> items, NoteActionListener listener, ThreadPool.JobContext jc) {
            mNoteItems = items;
            mListener = listener;
            mJc = jc;
        }

        protected abstract int excuteAction(NoteItem item, NoteActionProgressListener listener);

        @Override
        public void run() {
            int success = 0;
            int fail = 0;
            int index = 0;
            List<NoteItem> paths = mNoteItems;
            for (NoteItem item : paths) {
                int rowsDeleted = excuteAction(item, mNoteProgressListener);
                if (rowsDeleted != EXECUTOR_ENCRYPT && rowsDeleted != EXECUTOR_DECRYPT) {
                    if (rowsDeleted > 0) {
                        success++;
                    } else {
                        fail++;
                    }
                    mMainHandler.sendMessage(
                            mMainHandler.obtainMessage(MSG_TASK_UPDATE, ++index, 0));
                } else {
                    success++;
                    mMainHandler.sendMessage(
                            mMainHandler.obtainMessage(MSG_ENCRYPT_UPDATE, mExectorProgressNum++, mExectorProgressTextNum));
                }
                if (mJc.isCancelled()) {
                    fail = paths.size() - success;
                    break;
                }
                mExectorProgressTextNum++;
            }
            mExectorProgressNum = 0;
            mMainHandler.sendMessageDelayed(
                    mMainHandler.obtainMessage(MSG_TASK_COMPLETE, success, fail, mListener), 10);
        }
    }

    private class DecryptRunnable extends ExectorRunnable {

        public DecryptRunnable(List<NoteItem> items, NoteActionListener listener, ThreadPool.JobContext jc) {
            super(items, listener, jc);
        }

        @Override
        protected int excuteAction(NoteItem item, NoteActionProgressListener listener) {
            return decrypt(item, listener);
        }
    }

    private class DelRunnable extends ExectorRunnable {
        private boolean isEncrypted;

        public DelRunnable(List<NoteItem> items, NoteActionListener listener, ThreadPool.JobContext jc,
                           boolean isEncrypted) {
            super(items, listener, jc);
            this.isEncrypted = isEncrypted;
        }

        @Override
        protected int excuteAction(NoteItem item, NoteActionProgressListener listener) {
            return onDel(item.getId(), mListener, isEncrypted ? SecretNoteItem.SECRET_ITEM_PATH
                    : LocalNoteItem.ITEM_PATH);
        }
    }

    private class ThrowIntoTrashRunnable extends ExectorRunnable {

        public ThrowIntoTrashRunnable(List<NoteItem> items, NoteActionListener listener, ThreadPool.JobContext jc) {
            super(items, listener, jc);
        }

        @Override
        protected int excuteAction(NoteItem item, NoteActionProgressListener listener) {
            return onThrowIntoTrash(item, mListener);
        }
    }

    private class TrashRecoverRunnable extends ExectorRunnable {

        public TrashRecoverRunnable(List<NoteItem> items, NoteActionListener listener, ThreadPool.JobContext jc) {
            super(items, listener, jc);
        }

        @Override
        protected int excuteAction(NoteItem item, NoteActionProgressListener listener) {
            return onTrashRecover(item, mListener);
        }
    }

    private class TrashDeleteRunnable extends ExectorRunnable {

        public TrashDeleteRunnable(List<NoteItem> items, NoteActionListener listener, ThreadPool.JobContext jc) {
            super(items, listener, jc);
        }

        @Override
        protected int excuteAction(NoteItem item, NoteActionProgressListener listener) {
            return onTrashDelete(item, mListener);
        }
    }

    private class EncryptRunnable extends ExectorRunnable {
        public EncryptRunnable(List<NoteItem> items, NoteActionListener listener, ThreadPool.JobContext jc) {
            super(items, listener, jc);
        }

        @Override
        protected int excuteAction(NoteItem item, NoteActionProgressListener listener) {
            return encrypt(item, listener);
        }

    }

    private int onDel(long id, NoteActionListener listener, Path parent) {
        if (id == NoteItem.INVALID_ID) {
            return listener.onActionInvalidId();
        }
        try {
            Path path = parent.getChild(id);
            mDataManager.delete(path);
            return 1;
        } catch (Exception e) {

        }
        return -1;
    }

    private int onThrowIntoTrash(NoteItem item, NoteActionListener listener) {
        if (null == item || item.getId() == NoteItem.INVALID_ID) {
            return listener.onActionInvalidId();
        }

        try {
            TrashUtils.throwIntoTrash(mActivity, item);
            return 1;
        } catch (Exception e) {
            Log.d(TAG, "onThrowIntoTrash fail : " + e);
        }
        return -1;
    }

    private int onTrashRecover(NoteItem item, NoteActionListener listener) {
        if (null == item || item.getId() == NoteItem.INVALID_ID) {
            return listener.onActionInvalidId();
        }

        try {
            TrashUtils.recoverFromTrash(mActivity, item);

            return 1;
        } catch (Exception e) {
            Log.d(TAG, "onTrashRecover fail : " + e);
        }
        return -1;
    }

    private int onTrashDelete(NoteItem item, NoteActionListener listener) {
        if (null == item || item.getId() == NoteItem.INVALID_ID) {
            return listener.onActionInvalidId();
        }

        try {
            item.delete();
            ReminderManager.cancelTrashCleanReminder(mActivity, item.getId());

            return 1;
        } catch (Exception e) {
            Log.d(TAG, "onTrashRecover fail : " + e);
        }
        return -1;
    }

    private int encrypt(NoteItem item, NoteActionProgressListener listener) {
        if (null == item) {
            return -1;
        }
        try {
            item.encrypt(listener);
            return EXECUTOR_ENCRYPT;
        } catch (Exception e) {
        }
        return -1;
    }

    private int decrypt(NoteItem item, NoteActionProgressListener listener) {
        if (null == item) {
            return -1;
        }
        try {
            item.decrypt(listener);
            return EXECUTOR_DECRYPT;
        } catch (Exception e) {
        }
        return -1;
    }

    public void startEncrypt(final Activity activity, final NoteInfo info, final NoteActionProgressListener noteProgressListener) {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(activity).showToast(getToastString(sCurrentRunningTask));
            return;
        }
        sCurrentRunningTask = ENCRYPT_TASK; //NOSONAR
        final List<NoteItemAttachInfo> attachInfos = EncryptUtil.getAttachs(info.mContent);
        if (null != noteProgressListener) {
            noteProgressListener.onStart(attachInfos.size());
        }
        if (info.mId != NoteItem.INVALID_ID) {
            ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
            mCurTask = threadPool.submit(new ThreadPool.Job<Object>() {
                @Override
                public Object run(ThreadPool.JobContext jc) {
                    EncryptUtil.checkPhotoSpanHasSize(info.mContent);
                    DES des = new DES();
                    int secretId = NoteUtils.addNoteData(des.authcode(info.mTitle, DES.OPERATION_ENCODE, DES.DES_KEY),
                            des.authcode(info.mContent, DES.OPERATION_ENCODE, DES.DES_KEY),
                            activity.getContentResolver(), info.mDateModifiedInMs, info.mDateReminderInMs,
                            info.mLabel, info.mEncyptHintState, info.mEncrytRemindReadState, true);
                    if (info.mDateReminderInMs != NoteItem.INVALID_REMINDER
                            && info.mDateReminderInMs > System.currentTimeMillis()) {
                        ReminderManager.cancelAlarmAndNotification(NoteAppImpl.getContext(),
                                info.mId, false);
                        ReminderManager.setReminder(NoteAppImpl.getContext(), secretId, info.mDateReminderInMs, true);
                    }
                    if (info.mId != NoteItem.INVALID_ID) {
                        activity.getContentResolver().delete(NoteContract.NoteContent.CONTENT_URI, "_id=?",
                                new String[]{String.valueOf(info.mId)});
                    }
                    if (PlatformUtil.isSecurityOS()) {
                        EncryptUtil.encryptAttachFileForSecurityOS(attachInfos, noteProgressListener);
                    } else {
                        EncryptUtil.encryptAttachFile(attachInfos, noteProgressListener);
                    }
                    if (!PlatformUtil.isSecurityOS() && NoteShareDataManager.isShowEncryptUserGuide(NoteAppImpl.getContext()) == NoteMainActivity.ENCRYPT_USER_GUIDE_DEFAULT) {
                        NoteShareDataManager.setShowEncryptUserGuide(NoteAppImpl.getContext(), NoteMainActivity.ENCRYPT_USER_GUIDE_SHOW);
                    }
                    WidgetUtil.updateAllWidgets();
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            sCurrentRunningTask = NO_TASK; //NOSONAR
                            noteProgressListener.onAllComplete(true);
                        }
                    });
                    return null;
                }
            });
        }
    }

    public void startDecrypt(final Activity activity, final NoteInfo info, final NoteActionProgressListener noteProgressListener) {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(activity).showToast(getToastString(sCurrentRunningTask));
            return;
        }
        sCurrentRunningTask = DECRYPT_TASK; //NOSONAR
        final List<NoteItemAttachInfo> attachInfos = EncryptUtil.getAttachs(info.mContent);
        if (null != noteProgressListener) {
            noteProgressListener.onStart(attachInfos.size());
        }
        if (info.mId != NoteItem.INVALID_ID) {
            ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
            mCurTask = threadPool.submit(new ThreadPool.Job<Object>() {
                @Override
                public Object run(ThreadPool.JobContext jc) {
                    int id = NoteUtils.addNoteData(info.mTitle, info.mContent, activity.getContentResolver(),
                            info.mDateModifiedInMs, info.mDateReminderInMs, info.mLabel, info.mEncyptHintState,
                            info.mEncrytRemindReadState, false);
                    if (info.mDateReminderInMs != NoteItem.INVALID_REMINDER
                            && info.mDateReminderInMs > System.currentTimeMillis()) {
                        ReminderManager.cancelAlarmAndNotification(NoteAppImpl.getContext(),
                                info.mId, true);
                        ReminderManager.setReminder(NoteAppImpl.getContext(), id, info.mDateModifiedInMs, false);
                    }
                    if (info.mId != NoteItem.INVALID_ID) {
                        activity.getContentResolver().delete(NoteContract.NoteContent.SECRET_CONTENT_URI, "_id=?",
                                new String[]{String.valueOf(info.mId)});
                    }
                    if (PlatformUtil.isSecurityOS()) {
                        EncryptUtil.decrpyAttachFileForSecurityOS(attachInfos, noteProgressListener);
                    } else {
                        EncryptUtil.decryptAttachFile(attachInfos, noteProgressListener);
                    }
                    WidgetUtil.updateAllWidgets();
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            sCurrentRunningTask = NO_TASK; //NOSONAR
                            noteProgressListener.onAllComplete(false);
                        }
                    });
                    return null;
                }
            });
        }
    }

    public void startRecoveryEncryptUnCompleteNote() {
        if (sCurrentRunningTask != NO_TASK) {
            new ToastManager(NoteAppImpl.getContext()).showToast(getToastString(sCurrentRunningTask));
            return;
        }
        sCurrentRunningTask = RECOVERY_TASK; //NOSONAR
        final Context context = NoteAppImpl.getContext();
        ThreadPool threadPool = NoteAppImpl.getContext().getThreadPool();
        mCurTask = threadPool.submit(new ThreadPool.Job() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                EncryptUtil.recoveryEncryptUnCompleteNote(context);
                sCurrentRunningTask = NO_TASK; //NOSONAR
                return null;
            }
        });
    }

    private String getToastString(int currentTaskType) {
        int resId = 0;
        switch (currentTaskType) {
            case DECRYPT_TASK:
                resId = R.string.decrypt_hint;
                break;
            case ENCRYPT_TASK:
                resId = R.string.encrypt_hint;
                break;
            case DELETE_TASK:
                resId = R.string.delete_hint;
                break;
            case RECOVERY_TASK:
                resId = R.string.recovery_hint;
                break;
            case TRASH_THROW_INTO_TASK:
                resId = R.string.delete_hint;
                break;
            case TRASH_RECOVER_TASK:
                resId = R.string.trash_recover_hint;
                break;
            case TRASH_DELETE_TASK:
                resId = R.string.delete_hint;
                break;
            default:
                break;
        }
        return resId == 0 ? "" : NoteAppImpl.getContext().getString(resId);
    }
}
