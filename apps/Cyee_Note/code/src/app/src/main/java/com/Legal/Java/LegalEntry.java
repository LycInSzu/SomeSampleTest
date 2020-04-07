package com.Legal.Java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.cydroid.note.common.Log;

import com.Legal.Lib.LegalInterface;

public class LegalEntry {
	public static final String		TAG		= "kptc.LegalEntry";
	public static final boolean 	DBG		= true;
	public static final int Legal_FAIL = 0;
	public static final int Legal_NAT = 1;
	public static final int Legal_SELF = 2;
	public static final int Legal_UNKNOWN = 4;

	public static int isVcfFile(String filename){
		if(DBG)
			Log.e(TAG, "fileName = " + filename);
		if(!filename.toLowerCase().endsWith(".vcf"))
			return -1;
		String start = null, endLine = null, end = null;
		try {
			BufferedReader buf = new BufferedReader(new FileReader(filename));
			start = buf.readLine();
			do {
				end = endLine;
				endLine = buf.readLine();
				if (endLine == null)
					break;
			}while (true);
			buf.close();
		}
		catch (IOException e) {
		}		if (start == null || end == null)
			return -2;
		if (start.startsWith("BEGIN:VCARD") && end.endsWith("END:VCARD"))
			return 1;

		return -1;
	}
	
	public static int isVcsFile(String filename){
		if(DBG)
			Log.e(TAG, "fileName = " + filename);
		if(!filename.toLowerCase().endsWith(".vcs"))
			return -1;
		String start = null, endLine = null, end = null;
		try {
			BufferedReader buf = new BufferedReader(new FileReader(filename));
			start = buf.readLine();
			do {
				end = endLine;
				endLine = buf.readLine();
				if (endLine == null)
					break;
			}while (true);
			buf.close();
		}
		catch (IOException e) {
		}		if (start == null || end == null)
			return -2;
		if (start.startsWith("BEGIN:VCALENDAR") && end.endsWith("END:VCALENDAR"))
			return 1;

		return -1;
	}
	
	public static boolean isMagicCorrect(String filename) {
		int returnCode;		
		// Verify if the file is natLegaled
		returnCode = LegalInterface.isMagic(filename);
		if (DBG)
			Log.e(TAG, "isMagicCorrect() returnCode = " + returnCode);
		return (returnCode != 0);
	}
	
	public static int isLegalFile(String filename) {
		if(DBG)
			Log.e(TAG, "fileName = " + filename);
		int returnCode;
		if(filename.length() - filename.lastIndexOf('.') > 6)
		{
			if(DBG)
				Log.d(TAG, "Legal is unknown");
			return Legal_UNKNOWN;
		}
		else {
			if(DBG)			
				Log.d(TAG, "File is not unknown : " + filename.contains("."));
		}
		//Verify if the file is VCF
		returnCode = isVcfFile(filename);
		if (returnCode == 1)
		{
			if(DBG)
				Log.d(TAG, "Legal is VcfFile");
			return Legal_NAT;
		}
		else {
			if(DBG)
				Log.d(TAG, "File is not Vcf : " + returnCode);
		}
		//Verify if the file is VCS
		returnCode = isVcsFile(filename);
		if (returnCode == 1)
		{
			if(DBG)
				Log.d(TAG, "Legal is VcsFile");
			return Legal_NAT;
		}
		else {
			if(DBG)
				Log.d(TAG, "File is not Vcs : " + returnCode);
		}
		// Verify if the file is natLegaled
		returnCode = LegalInterface.isLegalFile(filename);
		if(DBG)
			Log.e(TAG, "Legal File returns " + returnCode);
		if (returnCode == 1)
			return Legal_NAT;
		else if(returnCode == 2)
			return Legal_SELF;
		
		return Legal_FAIL;
	}
	
	public static int isVideoLegalFile(String filename) {
		int returnCode;

		if(filename.length() - filename.lastIndexOf('.') > 6)
		{
			if(DBG)
				Log.d(TAG, "Legal is unknown");
			return Legal_UNKNOWN;
		}
		else {
			if(DBG)
				Log.d(TAG, "File is not unknown : " + filename.contains("."));
		}
		// Verify if the file is natLegaled
		returnCode = LegalInterface.isLegalFile(filename);
		if(DBG)
			Log.e(TAG, "Legal File returns " + returnCode);
		if (returnCode == 1)
			return Legal_NAT;
		else if(returnCode == 2) {
			returnCode = LegalInterface.checkLegalFile(filename);
			if(DBG)
				Log.e(TAG, "CheckLegal File returns " + returnCode);
			if(returnCode == 1)
				return Legal_SELF;
		}		
		return Legal_FAIL;
	}
	
	public static int isApkLegalFile(String filename) {
		int returnCode;
		// Verify if the file is natLegaled
		returnCode = LegalInterface.isLegalFile(filename);
		if(DBG)
			Log.e(TAG, "Legal File returns " + returnCode);
		if (returnCode == 1)
			return Legal_NAT;
		return Legal_FAIL;		
	}
	
	public static int saveLegalFile(String filename){
		int returnCode;
		returnCode = LegalInterface.saveLegalFile(filename);
		if(DBG)
			Log.e(TAG, "SaveLegalFile returns " + returnCode);
		return returnCode;
	}
	
	public static int checkMMSFile(String filename){
		int returnCode;
		returnCode = LegalInterface.isSended(filename);
		if(DBG)
			Log.e(TAG, "CheckMMSFile returns " + returnCode);
		return returnCode;
	}
	
	public static int setMMSFile(String filename){
		int returnCode;
		returnCode = LegalInterface.setMMSInfo(filename);
		if(DBG)
			Log.e(TAG, "SetMMSFile returns " + returnCode);
		return returnCode;
	}
	
	public static int isTempApkFile(String srcFileName, String keyFileName) {
		int returnCode;
		returnCode = LegalInterface.isTempApkFile(srcFileName, keyFileName);
		if(DBG)
			Log.e(TAG, "isTempApkFile returns " + returnCode);
		return returnCode;
	}
	
	public static int generateTempApk(String srcFileName, String dstFileName, String keyFileName) {
		int returnCode;
		returnCode = LegalInterface.generateTempApk(srcFileName, dstFileName, keyFileName);
		if(DBG)
			Log.e(TAG, "generateTempApk returns " + returnCode);
		return returnCode;
	}
	
	public static int getLegalInfoSize(String strfname) {
		int infoLen;
		infoLen = LegalInterface.getLegalInfoSize(strfname);
		if(DBG)
			Log.e(TAG, "getLegalInfoSize returns " + infoLen);
		return infoLen;		
	}
	
	public static int isMMSFile(String filename, int[] outErrCode) {
		outErrCode[0] = 658;
		return -1;
	}
}
