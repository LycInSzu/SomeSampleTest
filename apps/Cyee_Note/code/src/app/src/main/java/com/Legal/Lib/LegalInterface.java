package com.Legal.Lib;

public class LegalInterface {
	static {
		System.loadLibrary("LegalInterface");
	}
	
	public static native int isLegalFile(String strfname);
	public static native int checkLegalFile(String strfname);
	public static native int saveLegalFile(String strfname);
	public static native int getLegalInfoSize(String strfname);
	public static native int isSended(String strfname);
	public static native int setMMSInfo(String strfname);
	public static native int isMagic(String strfname);
	public static native int isTempApkFile(String srcFileName, String keyFileName);
	public static native int generateTempApk(String srcFileName, String dstFileName, String keyFileName);
}