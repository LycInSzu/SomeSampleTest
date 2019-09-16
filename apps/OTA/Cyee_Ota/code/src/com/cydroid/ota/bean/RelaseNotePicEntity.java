package com.cydroid.ota.bean;

import android.graphics.Bitmap;
import android.widget.ImageView;
/**
 * 
 * @author cuijiuyu
 *
 */
public class RelaseNotePicEntity {
    private int mImageId; 
    private String mImageUrl;
    private String mImageName; 
    private Bitmap mImg;
    public RelaseNotePicEntity(int id,String url,String name,Bitmap img) {
    	mImageId = id;
    	mImageUrl = url;
    	mImageName = name;
    	mImg = img;
    }
    
	public int getmImageId() {
		return mImageId;
	}
	public void setmImageId(int mImageId) {
		this.mImageId = mImageId;
	}
	public String getmImageUrl() {
		return mImageUrl;
	}
	public void setmImageUrl(String mImageUrl) {
		this.mImageUrl = mImageUrl;
	}
	public String getmImageName() {
		return mImageName;
	}
	public void setmImageName(String mImageName) {
		this.mImageName = mImageName;
	}
	public Bitmap getmImg() {
		return mImg;
	}
	public void setmImg(Bitmap mImg) {
		this.mImg = mImg;
	}
    
    
}
