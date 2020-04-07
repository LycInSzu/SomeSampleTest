package com.Legal.Java;

import android.content.Context;
import com.cydroid.note.common.Log;
import android.widget.Toast;

import com.Legal.Java.LegalEntry;
import com.Legal.Lib.LegalInterface;

//siwei <GN_Oversea_Req> <bianrong> <2017-4-8> modify for 77987 begin
public class pwinSign {
	
	public static final boolean 	DBG		= true;
	public static final String		TAG		= "kptc.pwinSign";

	public static int isLegalFile(String strFileName) {

		int retCode = 0;
		if (DBG) Log.d(TAG, "isLegalFile(): strFileName=" + strFileName);
		
		retCode = LegalEntry.isLegalFile(strFileName);
		Log.d(TAG, "isLegalFile(): retCode=" + retCode);
		
	    return retCode;
	}

	public static int isNatSignFile(String strFileName) {
		
		int retCode = 0;
		if (DBG) Log.d(TAG, "isNatSignFile(): strFileName=" + strFileName);
		
		retCode = LegalEntry.isApkLegalFile(strFileName);
		Log.d(TAG, "isNatSignFile(): retCode=" + retCode);
		
	    return retCode;
	}

	public static int isSelfSignFile(String strFileName) {
		
		int retCode = 0;
		if (DBG) Log.d(TAG, "isSelfSignFile(): strFileName=" + strFileName);
		
		retCode = LegalEntry.isLegalFile(strFileName);
		Log.d(TAG, "isSelfSignFile(): retCode=" + retCode);
		
	    return retCode;
	}
	
	public static int isVideoSignFile(String strFileName) {
		
		int retCode = 0;
		if (DBG) Log.d(TAG, "isVideoSignFile(): strFileName=" + strFileName);
		
		retCode = LegalEntry.isVideoLegalFile(strFileName);
		Log.d(TAG, "isVideoSignFile(): retCode=" + retCode);
		
	    return retCode;
	}

	public static int isSendedByMMS(String strFileName) {

		int retCode = 0;
		if (DBG) Log.d(TAG, "isSendedByMMS(): strFileName=" + strFileName);
		
		retCode = LegalEntry.checkMMSFile(strFileName);
		Log.d(TAG, "isSendedByMMS(): retCode=" + retCode);
		
	    return retCode;
	}
	
	public static int setMMSInfoToFile(String strFileName) {
		
		int retCode = 0;
		if (DBG) Log.d(TAG, "setMMSInfoToFile(): strFileName=" + strFileName);
		
		retCode = LegalEntry.setMMSFile(strFileName);
		Log.d(TAG, "setMMSInfoToFile(): retCode=" + retCode);
		
		return retCode;
	}
	
	public static int getNatSignInfoLen(String strFileName) {
		
		int retInfoLen = 0;
		if (DBG) Log.d(TAG, "getNatSignInfoLen(): strFileName=" + strFileName);
		
		retInfoLen = LegalEntry.getLegalInfoSize(strFileName);
		Log.d(TAG, "getNatSignInfoLen(): retInfoLen=" + retInfoLen);
		
		return retInfoLen;
	}
	
	public static int saveSelfSignFile(String strFileName) {
		
		int retCode = 0;
		if (DBG) Log.d(TAG, "saveSelfSignFile(): strFileName=" + strFileName);
		
		retCode = LegalEntry.saveLegalFile(strFileName);
		Log.d(TAG, "saveSelfSignFile(): retCode=" + retCode);

		return retCode;
	}
	
	public static void showIllegalFileMessage(Context context) {
		Toast.makeText(context, "이 화일은 비서명화일입니다.", Toast.LENGTH_SHORT).show();	
		return;
	}
	
	public static int isTempApkFile(String strFileName, String keyFileName) {
		
		int retCode = 0;
		if (DBG) Log.d(TAG, "isTempApkFile(): strFileName=" + strFileName);
		
		retCode = LegalEntry.isTempApkFile(strFileName, keyFileName);
		Log.d(TAG, "isTempApkFile(): retCode=" + retCode);

	    return retCode;
	}
		
	public static int generateTempApk(String srcFileName, String dstFileName, String keyFileName) {
		
		int retCode = 0;
		if(DBG) Log.d(TAG, "generateTempApk(): srcFileName=" + srcFileName);

		retCode = LegalEntry.generateTempApk(srcFileName, dstFileName, keyFileName);
		Log.d(TAG, "generateTempApk(): retCode=" + retCode);
		
		return retCode;
	}
	
	public static int isMMSFile(String filename, int[] errCode) {

		int retCode = 0;
		
		if(DBG) Log.d(TAG, "isMMSFile(): filename=" + filename);

		retCode = LegalEntry.isMMSFile(filename, errCode);
		if(DBG) Log.d(TAG, "isMMSFile(): retCode=" + retCode + "， errCode=" + errCode[0]);
		
		return retCode;
	}

	public static boolean isSkipCheckFile(String strFileName) {

		return false;
	}

	public static int isMtpFile(String strFileName) {

		int retCode = 0;
		if (DBG) Log.d(TAG, "isMtpFile(): strFileName=" + strFileName);
		
		retCode = LegalEntry.isLegalFile(strFileName);
		Log.d(TAG, "isMtpFile(): retCode=" + retCode);

	    return retCode;
	}

	public static int sendBrowsePathBroadcast(Context context, String strFileName) {
		if(DBG) Log.d(TAG, "sendBrowsePathBroadcast(): filename=" + strFileName);
		return 1;
	}

	public static int sendBroadcastToRedService(Context context, String strFileName) {
		if(DBG) Log.d(TAG, "sendBroadcastToRedService(): filename=" + strFileName);
		return 1;
	}
}
//siwei <GN_Oversea_Req> <bianrong> <2017-4-8> modify for 77987 end