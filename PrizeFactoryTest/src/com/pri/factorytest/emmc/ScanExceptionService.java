package com.pri.factorytest.emmc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;

import com.pri.factorytest.R;
import com.pri.factorytest.Ddr.DdrSingleActivity;
import com.pri.factorytest.util.Utils;

public class ScanExceptionService extends Service {

	private final static String AEE_PATH = "/data/aee_exp";
	private final static String AEE_PATH1 = "/data/aee_exp_backup";
	private final static String AEE_PATH2 = "/sdcard/mtklog/aee_exp_backup";
	private final static String AEE_PATH3 = "/data/vendor/mtklog/aee_exp";
	private final static String CLEAR_COUNT_PATH = "/data/prize_backup/clear_count";
	public final static String START_SCAN_PATH = "/data/prize_backup/start_scan";
	private final static String KERNEL_SCAN_PATH = "sys/kernel/rtc_ddr_sign/rtc_ddr_value";
	public final static String EMCC_TEST_SUCCESS = "P";
	public final static String EMCC_TEST_FAIL = "F";
    public final static String DEFAUTLT_TIMES = "20";

	
	public final static int EMCC_TEST_TIMES = 5000;
	private   long trigger_time = 10 * 1000;
	WakeLock wakeLock = null;
	WakeLock wakeLock1 = null;
	AlarmManager alarmMgr = null;
	PendingIntent pendingIntent = null;
	
	Handler mHandler = new Handler();
	private int count = 0;
	private int isStartScan = 0;
	private int kernelValue = 0;
	private int scanTimes;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
        try {
            scanTimes = Integer.parseInt((android.os.SystemProperties.get("ro.pri.pcba_clear_test_time",DEFAUTLT_TIMES)));
		} catch (Exception e) {
		}
        
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(intent != null){
            int startScan =  intent.getIntExtra("startScan", 0);
			int kernel_value =  intent.getIntExtra("kernelValue", 0);
			if(startScan > 0){
				WriteInfo(Utils.EMCC_TEST_START_SCAN_INDEX, Utils.EMCC_TEST_START_SCAN_INDEX_LENGTH,
						String.valueOf(startScan));
				WriteInfo(Utils.EMCC_TEST_CLEAR_COUNT_INDEX, Utils.EMCC_TEST_CLEAR_COUNT_INDEX_LENGTH,
						String.valueOf("0"));
				writeClearCountFile(KERNEL_SCAN_PATH, String.valueOf(kernel_value));
				trigger_time = 1;
				try {
					Log.d("tangan", "kernel_value=" + Integer.parseInt(getClearCountFromFile(CLEAR_COUNT_PATH).trim()));
				} catch (Exception e) {
				}
			}
		}
		try {
			count = Integer.parseInt(getInfoFromIndex(Utils.EMCC_TEST_CLEAR_COUNT_INDEX,
					Utils.EMCC_TEST_CLEAR_COUNT_INDEX_LENGTH));
		} catch (Exception e) {
			Log.e("tangan", "e=" + e.toString());
		}
		try {
			isStartScan = Integer.parseInt(getInfoFromIndex(Utils.EMCC_TEST_START_SCAN_INDEX,
					Utils.EMCC_TEST_START_SCAN_INDEX_LENGTH));
			if(isStartScan > 0){
				scanTimes = isStartScan;
			}
		} catch (Exception e) {
			Log.e("tangan", "e=" + e.toString());
		}
		Log.d("tangan", "test ScanException time=" + count+"scanTimes="+scanTimes);
		try {
			kernelValue = Integer.parseInt(getClearCountFromFile(KERNEL_SCAN_PATH).trim());
		} catch (Exception e) {
		}
		
		if(("1".equals(android.os.SystemProperties.get("ro.pri.pcba_clear_test","0")))&&(isStartScan >  0 || (kernelValue == 255))){
			Log.d("tangan", "test ScanException time=" + count);
			if (hasException()) {
				Log.d("tangan", "hasException");
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.FULL_WAKE_LOCK, "hasException");
				wakeLock.acquire();
				Builder builder = new AlertDialog.Builder(this,
						android.R.style.Theme_Material_Light_Dialog_Alert);
				builder.setTitle(getResources().getString(R.string.prize_emmc_test));
				builder.setMessage(String.format(getResources().getString(R.string.prize_emmc_failed),count));
				WriteInfo(Utils.EMCC_TEST_START_SCAN_INDEX, Utils.EMCC_TEST_START_SCAN_INDEX_LENGTH,
						String.valueOf("0"));
				WriteInfo(Utils.EMCC_TEST_CLEAR_COUNT_INDEX, Utils.EMCC_TEST_CLEAR_COUNT_INDEX_LENGTH,
						String.valueOf("0"));
				Utils.writeProInfo(EMCC_TEST_FAIL,Utils.EMCC_TEST_RESULT_SN_INDEX);
				builder.setCancelable(false);
				builder.setPositiveButton(getResources().getString(R.string.prize_emmc_confirm), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// to do
						if(wakeLock != null){
							wakeLock.release();
						}
						
					}
				});
				AlertDialog dialog = builder.create();
				dialog.getWindow().setType(
						(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR));
				dialog.show();
			} else {
				Log.d("tangan", "noException");
				if (count < scanTimes) {
					count++;
					alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					Intent clearIntent = new Intent(Intent.ACTION_FACTORY_RESET);
					Builder builder = new AlertDialog.Builder(this,
						android.R.style.Theme_Material_Light_Dialog_Alert);
				builder.setTitle(getResources().getString(R.string.prize_emmc_test));
				builder.setMessage(String.format(getResources().getString(R.string.prize_emmc_test_info),count,(scanTimes-count)));
				builder.setCancelable(false);
				builder.setNegativeButton(getResources().getString(R.string.prize_emmc_cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// to do
						
						if(alarmMgr!=null && pendingIntent!= null){
							WriteInfo(Utils.EMCC_TEST_START_SCAN_INDEX, Utils.EMCC_TEST_START_SCAN_INDEX_LENGTH,
								String.valueOf("0"));
						   WriteInfo(Utils.EMCC_TEST_CLEAR_COUNT_INDEX, Utils.EMCC_TEST_CLEAR_COUNT_INDEX_LENGTH,
								String.valueOf("0"));
						   writeClearCountFile(KERNEL_SCAN_PATH, String.valueOf("0"));
							alarmMgr.cancel(pendingIntent);
						}
					}
				});
				AlertDialog dialog = builder.create();
				dialog.getWindow().setType(
						(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR));
				dialog.show();
					clearIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    clearIntent.setPackage("android");
					clearIntent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
					clearIntent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, false);
					clearIntent.putExtra(Intent.EXTRA_WIPE_ESIMS, false);
					pendingIntent = PendingIntent.getBroadcast(this,
							0, clearIntent, 0);
					// int min = 3;//new Random().nextInt(5) + 10; //[10, 15)
					Log.d("tangan", "test ScanException begin clear");
					alarmMgr.setExact(AlarmManager.RTC_WAKEUP,
							System.currentTimeMillis() + trigger_time, pendingIntent);
					
					WriteInfo(Utils.EMCC_TEST_CLEAR_COUNT_INDEX, Utils.EMCC_TEST_CLEAR_COUNT_INDEX_LENGTH,
							String.valueOf(count));
				}else{
					PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.FULL_WAKE_LOCK, "hasException");
				wakeLock.acquire();
					Builder builder = new AlertDialog.Builder(this,
						android.R.style.Theme_Material_Light_Dialog_Alert);
				builder.setTitle(getResources().getString(R.string.prize_emmc_test));
				builder.setMessage(String.format(getResources().getString(R.string.prize_emmc_test_success),count));
				builder.setCancelable(false);
				WriteInfo(Utils.EMCC_TEST_START_SCAN_INDEX, Utils.EMCC_TEST_START_SCAN_INDEX_LENGTH,
						String.valueOf("0"));
				WriteInfo(Utils.EMCC_TEST_CLEAR_COUNT_INDEX, Utils.EMCC_TEST_CLEAR_COUNT_INDEX_LENGTH,
						String.valueOf("0"));
				Utils.writeProInfo(EMCC_TEST_SUCCESS,Utils.EMCC_TEST_RESULT_SN_INDEX);
				builder.setPositiveButton(getResources().getString(R.string.prize_emmc_confirm), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// to do
						
						if(wakeLock != null){
							wakeLock.release();
						}
					}
				});
				
				AlertDialog dialog = builder.create();
				dialog.getWindow().setType(
						(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR));
				dialog.show();
				}
			}
		}else{
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public void writeClearCountFile(String path, String imei) {
		try {
			FileOutputStream fout = new FileOutputStream(path);
			byte[] bytes = imei.getBytes();
			fout.write(bytes);
			fout.flush();
			fout.close();
		} catch (Exception e) {
			Log.d("tangan", "writeClearCountFile exception,e="+e.toString());
		}

	}
	public String getClearCountFromFile(String path) {
		String result = "";
		try {
			FileInputStream mFileInputStream = new FileInputStream(path);
			InputStreamReader mInputStreamReader = new InputStreamReader(
					mFileInputStream, "UTF-8");
			char[] input = new char[mFileInputStream.available()];
			mInputStreamReader.read(input);
			mInputStreamReader.close();
			mFileInputStream.close();
			result = new String(input);
		} catch (Exception e) {
		}
		return result;

	}

	private boolean hasException() {
		boolean hasExcepiton = false;
		File aee_dir = new File(AEE_PATH);
		File[] aeeList = aee_dir.listFiles();
		if (aeeList != null) {
			for (int i = 0; i < aeeList.length; i++) {
				Log.d("tangan", "file name=" + aeeList[i].getName());
				if (aeeList[i].getName().contains("KE")
						|| aeeList[i].getName().contains("Reboot")
						|| aeeList[i].getName().contains("NE")
						|| aeeList[i].getName().contains("HWT")) {
					hasExcepiton = true;
					return hasExcepiton;
				}
			}
		}

		File aee_dir1 = new File(AEE_PATH1);
		File[] aeeList1 = aee_dir1.listFiles();
		if (aeeList1 != null) {
			for (int i = 0; i < aeeList1.length; i++) {
				Log.d("tangan", "file name=" + aeeList1[i].getName());
				if (aeeList1[i].getName().contains("KE")
						|| aeeList1[i].getName().contains("Reboot")
						|| aeeList1[i].getName().contains("NE")
						|| aeeList1[i].getName().contains("HWT")

				) {
					hasExcepiton = true;
					return hasExcepiton;
				}
			}
		}

		File aee_dir2 = new File(AEE_PATH2);
		File[] aeeList2 = aee_dir2.listFiles();
		if (aeeList2 != null) {
			for (int i = 0; i < aeeList2.length; i++) {
				Log.d("tangan", "file name=" + aeeList2[i].getName());
				if (aeeList2[i].getName().contains("KE")
						|| aeeList2[i].getName().contains("Reboot")
						|| aeeList2[i].getName().contains("NE")
						|| aeeList2[i].getName().contains("HWT")) {
					hasExcepiton = true;
					return hasExcepiton;
				}
			}
		}
		
		File aee_dir3 = new File(AEE_PATH3);
		File[] aeeList3 = aee_dir3.listFiles();
		if (aeeList3 != null) {
			for (int i = 0; i < aeeList3.length; i++) {
				Log.d("tangan", "file name=" + aeeList3[i].getName());
				if (aeeList3[i].getName().contains("KE")
						|| aeeList3[i].getName().contains("Reboot")
						|| aeeList3[i].getName().contains("NE")
						|| aeeList3[i].getName().contains("HWT")) {
					hasExcepiton = true;
					return hasExcepiton;
				}
			}
		}

		return hasExcepiton;
	}
	
	public  static String getInfoFromIndex(int index,int length){
		String s = Utils.readProInfo(index, length).substring(index, index+length);
		String regStartSpace = "^[　 ]*";  
	    String regEndSpace = "[　 ]*$";  
	    s =  s.replaceAll(regStartSpace, "").replaceAll(regEndSpace, ""); 
		Log.d("tangan", "getInfoFromIndex s=" + s);
		return s;
		
	}
	
	public void WriteInfo(int index,int length,String value) {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<length;i++){
			if(i<value.length()){
				sb.append(value.charAt(i));
			}else{
				sb.append(" ");
			}
		}
		String s = String.valueOf(sb);
		Log.d("tangan", "writeProInfo s=" + s);
		Utils.writeProInfo(s, index);
		
	}
	
	
	
	

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
