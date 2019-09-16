package com.cydroid.ota.logic.sync;

import android.content.Context;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;

import com.cydroid.ota.Log;
import com.cydroid.ota.bean.IUpdateInfo;
import com.cydroid.ota.execption.*;
import com.cydroid.ota.logic.net.HttpHelper;
import com.cydroid.ota.logic.net.HttpUtils;
import com.cydroid.ota.logic.utils.ClltStatisticsUtil;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.Error;
import com.cydroid.ota.utils.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.params.CoreProtocolPNames;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyanfeng on 15-4-21.
 */
public class DownloadJob extends Job {
    private static final String TAG = "DownloadJob";
    private static final double NANOS_PER_SECOND = 1000000000.0;
    private Context mContext;
    private IUpdateInfo mUpdateInfo;
    private ISyncDownloadExecutor mSyncDownloadExecutor;
    private IStorage mStorage;
    private DownloadInfo mDownloadInfo;
    private boolean isFinished = false;

    public DownloadJob(Context context, ISyncCallback callback,
                       ISyncDownloadExecutor syncDownloadExecutor) {
        super(callback);
        mContext = context;
        mUpdateInfo = syncDownloadExecutor.getUpgradeInfo();
        mSyncDownloadExecutor = syncDownloadExecutor;
        mStorage = SettingUpdateDataInvoker.getInstance(mContext).settingStorage();
        mDownloadInfo = new DownloadInfo();
    }

    @Override
    public <T> T run() {

        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            sendMessage(MSG.MSG_DOWNLOAD_NETWORK_UNAVAILABLE);
            return null;
        }

        String fileName = mStorage.getString(Key.Setting.KEY_SETTING_DOWNLOAD_FILE_NAME, "");
        //Chenyee <CY_Bug> <xuyongji> <20180917> modify for SWW1617AO1-478 begin
		String filePath = "";
        if (!TextUtils.isEmpty(fileName)) {
            filePath = fileName.substring(0, fileName.lastIndexOf(File.separator));
        }

        if (TextUtils.isEmpty(fileName) ||
                !(FileUtils.isMountedPath(filePath, mContext) &&
                        FileUtils.isMemoryEnoughForDownload(fileName, mUpdateInfo.getFileSize()))) {
		//Chenyee <CY_Bug> <xuyongji> <20180917> modify for SWW1617AO1-478 end				
            String newFileName = FileUtils.getDownloadFileName(mUpdateInfo.getFileSize(), mContext);
            if (newFileName == null) {
                sendMessage(MSG.MSG_DOWNLOAD_SPACE_NOT_ENOUGH);
                return null;
            }
            if (!TextUtils.isEmpty(fileName) && !newFileName.equals(fileName)) {
                File file = new File(fileName);
                if (file.exists()) {
                    Log.d(TAG, "DownloadJob remove the old file");
                    FileUtils.copyFile(file, new File(newFileName));
                    file.delete();
                }
            }
            fileName = newFileName;
        }

        mStorage.putString(Key.Setting.KEY_SETTING_DOWNLOAD_FILE_NAME,
            fileName);

        Log.d(TAG, " OTA Download file's path >>>>>>>>" + fileName);

        PowerManager pm = (PowerManager) mContext
            .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG
                + Thread.currentThread().getId());
        wakeLock.acquire();
        try {

            Log.d(TAG, "state:" + mSyncDownloadExecutor.isContinue() + " isCanceled:"
                + isCanceled());
            if (mSyncDownloadExecutor.isReadyToDownload()) {
                sendClltDownloadStartInfo(true);
            }
            while (mSyncDownloadExecutor.isContinue() && !isCanceled()) {
                executeHttp(fileName);
            }
            sendMessage(MSG.MSG_DOWNLOAD_JOB_RUN_END);
        } catch (SettingUpdateVerifyFailedException e) {
            sendMessage(MSG.MSG_DOWNLOAD_VERIFY_FAILED);
            Log.e(TAG, e.getMessage());
        } catch (SettingUpdateFileNotExistException e) {
            sendMessage(MSG.MSG_DOWNLOAD_FILE_NOT_EXIST);
            Log.e(TAG, e.getMessage());
        } catch (SettingUpdateNetException e) {
            Log.e(TAG, "error code  :" + e.getHttpStatus());
            if (e.getHttpStatus() == 404) {
                sendMessage(MSG.MSG_DOWNLOAD_NETWORK_NO_LINK);
            } else {
                sendMessage(MSG.MSG_DOWNLOAD_NETWORK_INTERRUPT);
            }
            Log.e(TAG, e.getMessage());
        } catch (SettingUpdateStateErrorException e) {
            sendMessage(MSG.MSG_DOWNLOAD_STATE_ERROR);
            Log.e(TAG, e.getMessage());
        } catch (SettingUpdateStorageUnmountException e) {
            if (StorageUtils.getAllMountedStorageVolumesPath(mContext).size() == 0) {
                sendMessage(MSG.MSG_DOWNLOAD_STORAGE_UNMOUNT);
			//Chenyee <Cy_Bug> <xuyongji> <20180208> modify for CSW1705A-1497 begin 
            } else if(!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("Quota exceeded")) {
                sendMessage(MSG.MSG_DOWNLOAD_SPACE_NOT_ENOUGH);
			//Chenyee <Cy_Bug> <xuyongji> <20180208> modify for CSW1705A-1497 end
            } else {
                sendMessage(MSG.MSG_DOWNLOAD_FILE_ERROR);
            }
            Log.e(TAG, e.getMessage());
        } finally {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
        return null;
    }

    private void sendClltDownloadStartInfo(boolean isStart) {
        String packagename = mUpdateInfo.getDownloadUrl().substring(mUpdateInfo.getDownloadUrl().lastIndexOf("/"));
        String url = ClltStatisticsUtil.createRequestURl(true, isStart, false, packagename);
        Log.d(TAG, "url:" + url);
        List<NameValuePair> pairs = ClltStatisticsUtil.createVersionPairs(mContext, mSyncDownloadExecutor.isAutoUpgrade(), mSyncDownloadExecutor.isRootState(), true);
        try {
            HttpHelper.executeHttpGet(url, pairs, true, null, HttpUtils.getUAHeader(mContext));
        } catch (SettingUpdateNetException e) {
            e.printStackTrace();
        }
    }

    private void executeHttp(String fileName) throws SettingUpdateNetException,
        SettingUpdateFileNotExistException, SettingUpdateStateErrorException
        , SettingUpdateVerifyFailedException,
        SettingUpdateStorageUnmountException {
        long fileSize = mUpdateInfo.getFileSize();
        if (FileUtils.checkFinished(mUpdateInfo.getFileSize(), fileName)) {
            downloadProgress(fileSize, 0);
            if (!FileUtils.verifyFileByMD5(mUpdateInfo.getMd5(), fileName)) {
                FileUtils.deleteFileIfExist(fileName);
                throw new SettingUpdateVerifyFailedException("Verify failed!");
            }
            if (isFinished) {
                sendClltDownloadStartInfo(false);
                isFinished = false;
            }
            return;
        }
        long start = System.nanoTime();
        long totalRead = 0;
        File f = new File(fileName);
        long curLen = f.length();
        FileOutputStream fos = null;
        HttpEntity entity = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        try {
            entity = downloadUpdateZip(mUpdateInfo.getDownloadUrl(),
                curLen,
                fileSize);
            String s = entity.getContentType().getValue();
            Log.d(TAG, "s = " + s);
            if (s == null
                    || (s.equals("application/vnd.android.package-archive") || (
                    !s.contains("text/html") && !s.contains("text/xml")))) {
                byte[] buffer = new byte[1024 * 16];
                fos = new FileOutputStream(f, true);
                is = entity.getContent();
                bis = new BufferedInputStream(is);

                int readSize = 0;
                while ((readSize = bis.read(buffer, 0, 1024 * 16)) != -1) {
                    if (mSyncDownloadExecutor.isContinue() && !isCanceled()) {
                        if (!f.exists()) {
                            throw new SettingUpdateFileNotExistException(
                                "file deleted when downloading");
                        }
                        try {
                            fos.write(buffer, 0, readSize);
                        } catch (IOException e) {
                            throw new SettingUpdateStorageUnmountException(
                                e.getMessage());
                        }
                        curLen += readSize;
                        totalRead += readSize;
                        double speed = (NANOS_PER_SECOND / 1 * totalRead) / (
                            System.nanoTime() - start + 1);
                        downloadProgress(curLen, speed);
                    } else {
                        throw new SettingUpdateStateErrorException(
                            "state error or job is canceled");
                    }
                }
                fos.flush();
            }
        } catch (SettingUpdateNetException e) {
            if (e.getHttpStatus() != SettingUpdateNetException.ERROR_SOCKET_TIMEOUT) {
                throw e;
            }
        } catch (FileNotFoundException e) {
            throw new SettingUpdateFileNotExistException("file deleted when downloading");
        } catch (IOException e) {
            Log.d(TAG, "IOException e = " + e);
            if (!(e instanceof SocketTimeoutException)) {
                throw new SettingUpdateNetException(e.getMessage());
            }
        } finally {
            Log.d(TAG, "executeHttp finally begin!");
            Message fosCloseMessage = obtainMessage(MSG.MSG_ENTITY_FILEOUTPUTSTREAM_IO_CLOSE);
            fosCloseMessage.obj = fos;
            sendSyncMessage(fosCloseMessage);
            Message ioCloseMessage = obtainSyncMessage(MSG.MSG_HTTP_IO_CLOSE);
            ioCloseMessage.obj = is;
            sendSyncMessage(ioCloseMessage);
            Message bisCloseMessage = obtainMessage(MSG.MSG_HTTP_BUFFEREDINPUTSTREAM_IO_CLOSE);
            bisCloseMessage.obj = bis;
            sendSyncMessage(bisCloseMessage);
            Message entityCloseMessage = obtainMessage(
                MSG.MSG_ENTITY_STREAMING_CLOSE);
            entityCloseMessage.obj = entity;
            sendSyncMessage(entityCloseMessage);
            Log.d(TAG, "executeHttp finally end!");
        }
        isFinished = true;
    }

    private void downloadProgress(long fileSize, double speed) {
        mDownloadInfo.setDownloadLength(fileSize);
        mDownloadInfo.setDownloadSpeed(speed);
        sendMessage(MSG.MSG_DOWNLOAD_PROGRESS, mDownloadInfo);
    }


    private HttpEntity downloadUpdateZip(String url, long curlen,
                                         long fileSize) throws SettingUpdateNetException {
        Log.d(TAG,
            "downloadUpdateZip curlen = " + curlen + "  fileSize = " + fileSize);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CoreProtocolPNames.USER_AGENT, Util.getUaString(
            SystemPropertiesUtils.getImei(mContext)));
        Map<String, String> headers = new HashMap<String, String>();
        if (NetworkUtils.isWapConnection(mContext)) {
            Log.d(TAG, "wap download!");
            HttpHost proxy = new HttpHost(
                HttpUtils.CONNECTION_MOBILE_DEFAULT_HOST,
                HttpUtils.CONNECTION_MOBILE_DEFAULT_PORT);
            params.put(ConnRoutePNames.DEFAULT_PROXY, proxy);
            long rangeEnd = curlen + HttpUtils.MAX_REQ_LENGTH;
            if (rangeEnd > fileSize) {
                rangeEnd = fileSize;
            }
            headers.put("Range", "bytes=" + curlen + "-" + rangeEnd);
        } else {
            Log.d(TAG, "-------wifi download!");
            if (curlen != 0) {
                headers.put("Range", "bytes=" + curlen + "-");
            }
        }
        return HttpHelper.executeHttpGet(url, pairs, false, params, headers);
    }

    @Override
    void handleJobMessage(Message msg) {
        switch (msg.what) {
            case MSG.MSG_DOWNLOAD_PROGRESS:
                mJobCallback.onResult(true, msg.obj);
                break;
            case MSG.MSG_DOWNLOAD_NETWORK_UNAVAILABLE:
                mJobCallback.onError(Error.ERROR_CODE_NETWORK_ERROR);
                break;
            case MSG.MSG_DOWNLOAD_SPACE_NOT_ENOUGH:
                mJobCallback.onError(Error.ERROR_CODE_DOWNLOAD_NO_SPACE);
                break;
            case MSG.MSG_DOWNLOAD_FILE_NOT_EXIST:
                mJobCallback.onError(Error.ERROR_CODE_DOWNLOAD_FILE_NOT_EXIST);
                break;
            case MSG.MSG_DOWNLOAD_FILE_ERROR:
                mJobCallback.onError(Error.ERROR_CODE_DOWNLOAD_FILE_WRITE_ERROR);
                break;
            case MSG.MSG_DOWNLOAD_VERIFY_FAILED:
                Log.d(TAG, "*******MSG_DOWNLOAD_VERIFY_FAILED*******");
                mJobCallback.onError(Error.ERROR_CODE_DOWNLOAD_FILE_VERIFY_FAILED);
                break;
            case MSG.MSG_DOWNLOAD_STATE_ERROR:
            case MSG.MSG_DOWNLOAD_JOB_RUN_END:
                Log.d(TAG, "*******MSG_DOWNLOAD_JOB_RUN_END*******");
                mJobCallback.onResult();
                break;
            case MSG.MSG_DOWNLOAD_NETWORK_INTERRUPT:
                mJobCallback.onError(Error.ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT);
                break;
            case MSG.MSG_DOWNLOAD_NETWORK_NO_LINK:
                mJobCallback.onError(Error.ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT_NO_FILE);
                break;
            case MSG.MSG_DOWNLOAD_STORAGE_UNMOUNT:
                mJobCallback.onError(Error.ERROR_CODE_DOWNLOAD_STORAGE_INTERRUPT);
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleJobSyncMessage(Message message) {
        switch (message.what) {
            case MSG.MSG_HTTP_IO_CLOSE:
                InputStream is = (InputStream) message.obj;
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case MSG.MSG_ENTITY_STREAMING_CLOSE:
                HttpEntity entity = (HttpEntity) message.obj;
                consume(entity);
                break;
            case MSG.MSG_HTTP_BUFFEREDINPUTSTREAM_IO_CLOSE:
                BufferedInputStream bis = (BufferedInputStream) message.obj;
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case MSG.MSG_ENTITY_FILEOUTPUTSTREAM_IO_CLOSE:
                FileOutputStream fos = (FileOutputStream) message.obj;
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private static final class DownloadInfo implements IDownloadInfo {
        private long mDownloadLength;
        private double mDownloadSpeed;

        protected DownloadInfo() {

        }

        protected void setDownloadLength(long downloadLength) {
            mDownloadLength = downloadLength;
        }

        protected void setDownloadSpeed(double downloadSpeed) {
            mDownloadSpeed = downloadSpeed;
        }

        @Override
        public long getDownloadLength() {
            return mDownloadLength;
        }

        @Override
        public double getSpeed() {
            return mDownloadSpeed;
        }
    }

    private static final class MSG extends Job.MSG {
        private static final int BASE_DOWNLOAD_MSG = BASE * 2;

        /*static {
            Log.d(TAG, TAG + " BASE_DOWNLOAD_MSG = " + BASE_DOWNLOAD_MSG);
        }*/

        static final int MSG_DOWNLOAD_NETWORK_UNAVAILABLE = BASE_DOWNLOAD_MSG + 1;
        static final int MSG_DOWNLOAD_SPACE_NOT_ENOUGH = BASE_DOWNLOAD_MSG + 2;
        static final int MSG_DOWNLOAD_PROGRESS = BASE_DOWNLOAD_MSG + 3;
        static final int MSG_DOWNLOAD_FILE_NOT_EXIST = BASE_DOWNLOAD_MSG + 4;
        static final int MSG_HTTP_IO_CLOSE = BASE_DOWNLOAD_MSG + 5;
        static final int MSG_ENTITY_STREAMING_CLOSE = BASE_DOWNLOAD_MSG + 6;
        static final int MSG_DOWNLOAD_FILE_ERROR = BASE_DOWNLOAD_MSG + 7;
        static final int MSG_DOWNLOAD_VERIFY_FAILED = BASE_DOWNLOAD_MSG + 10;
        static final int MSG_DOWNLOAD_STATE_ERROR = BASE_DOWNLOAD_MSG + 11;
        static final int MSG_DOWNLOAD_JOB_RUN_END = BASE_DOWNLOAD_MSG + 12;
        static final int MSG_DOWNLOAD_NETWORK_INTERRUPT = BASE_DOWNLOAD_MSG + 13;
        static final int MSG_DOWNLOAD_STORAGE_UNMOUNT = BASE_DOWNLOAD_MSG + 14;
        static final int MSG_DOWNLOAD_NETWORK_NO_LINK = BASE_DOWNLOAD_MSG + 15;
        static final int MSG_HTTP_BUFFEREDINPUTSTREAM_IO_CLOSE = BASE_DOWNLOAD_MSG + 16;
        static final int MSG_ENTITY_FILEOUTPUTSTREAM_IO_CLOSE = BASE_DOWNLOAD_MSG + 17;
    }
}
