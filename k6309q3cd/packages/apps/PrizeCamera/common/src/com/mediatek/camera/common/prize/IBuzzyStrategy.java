package com.mediatek.camera.common.prize;

public interface IBuzzyStrategy {
	void openCamera();
	void closeCamera();
	void startPreview();
	boolean isOcclusion();
	void saveMainBmp(byte[] data);
	int getCheckTime();
	public void attachSurfaceViewLayout();
	public void detachSurfaceViewLayout();
}
