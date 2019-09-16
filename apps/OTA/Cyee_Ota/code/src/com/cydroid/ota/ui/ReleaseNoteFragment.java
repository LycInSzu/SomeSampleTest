package com.cydroid.ota.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cydroid.ota.bean.RelaseNotePicEntity;
import com.cydroid.ota.logic.ReleaseNoteManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.cydroid.ota.R;

/**
 * 
 * @author cuijiuyu
 *
 */
public class ReleaseNoteFragment extends Fragment  {
	private static final String KEY_CONTENT = "TestFragment:Content";
	private int mContent;
	private Bitmap mBitmap;
	private boolean mIsLastPic;
    private Context mContext;
    public ScrollView view = null;
    private ProgressDialog mProgressDialog = null;
    Object mLock = new Object();
    private boolean isLoading = false;
    private ReleaseNoteManager mReleaseNoteManager;

	public static ReleaseNoteFragment newInstance(int content) {
		ReleaseNoteFragment fragment = new ReleaseNoteFragment();

		fragment.mContent = content;
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 mContext = inflater.getContext();
		 mReleaseNoteManager = ReleaseNoteManager.getInstance(mContext);
		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_CONTENT)) {
			mContent = savedInstanceState.getInt(KEY_CONTENT);
		}
		View root = inflater.inflate(R.layout.fragment_layout, container, false);
		ImageView iv = (ImageView) root.findViewById(R.id.iv);
		
        for (int i = 0; i < mReleaseNoteManager.mRelaseNoteList.size(); i++) {
            if (mContent == i) {          	
            	if (!"".equals(mReleaseNoteManager.mRelaseNoteList.get(i).getmImg())
            			&& null != mReleaseNoteManager.mRelaseNoteList.get(i).getmImg()) {
            		iv.setImageBitmap(mReleaseNoteManager.mRelaseNoteList.get(i).getmImg());
            	} 
//            	else {
//            		AsyncBitmapLoader asyncBitmapLoader = new AsyncBitmapLoader();
//            		asyncBitmapLoader.loadBitmap(iv, mReleaseNoteManager.mRelaseNoteList.get(i),
//            				new ImageCallBack() {
//            			@Override
//            			public void imageLoad(ImageView imageView, Bitmap bitmap) {
//            				imageView.setImageBitmap(bitmap);
//            			}
//            		});
//            	}
            }
        }
		return root;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_CONTENT, mContent);
	}
	
    public final static Bitmap returnReleasseNotedBitMap(String url, int size) {
        URL myFileUrl = null;
        Bitmap bitmap = null;

        try {
            myFileUrl = new URL(url);            
            HttpURLConnection conn;

            conn = ( HttpURLConnection ) myFileUrl.openConnection();

            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = size; // width，height设为原来的size分一
            bitmap = BitmapFactory.decodeStream(is, null, options);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        return CompressImage(bitmap);
        return bitmap;
    }

    public class AsyncBitmapLoader {

        private RelaseNotePicEntity mRelaseNotePicEntity = null;

        private void saveImageToLocal(String picLink, Bitmap bm) {
            if (null == bm) {
                return;
            }
            File bitmapFile = new File(getLocalImageFilePath(picLink));
            if (!bitmapFile.exists()) {
                try {
                    bitmapFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            bm.compress(Bitmap.CompressFormat.PNG, options, baos);
            while ((baos.toByteArray().length / 1024 > 50) && options > 10) {
                baos.reset();
                options -= 10;
                bm.compress(Bitmap.CompressFormat.PNG, options, baos);
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(bitmapFile);
                fos.write(baos.toByteArray());
                fos.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != fos) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        private Bitmap getImageFromLocal(String picLink) {
            Bitmap bitmap = null;
            String filePath = getLocalImageFilePath(picLink);
            if (isImageFileExit(filePath)) {
                try {
                    bitmap = BitmapFactory.decodeFile(filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return bitmap;
        }

        private Boolean isImageFileExit(String filePath) {
            File dir = new File(filePath);
            if (!dir.exists()) {
                return false;
            }

            return true;
        }        

        private String getLocalImageFilePath(String picLink) {

            String rootPath = Environment.getExternalStorageDirectory().getPath()  + "/";
            String imageCachePath = Environment.getExternalStorageDirectory().getPath() + "/" + "SettingUpdate"
                    + "/" + "imageCache" + "/";

            File dir = new File(rootPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File imageCacheDir = new File(imageCachePath);
            if (!imageCacheDir.exists()) {
                imageCacheDir.mkdirs();
            }

            String filePath = imageCachePath + picLink + "_" + "ReleaseNote" + ".png";
            return filePath;
        }

        public void loadBitmap(final ImageView imageView, RelaseNotePicEntity RelaseNotePicEntityInfo,
                final ImageCallBack imageCallBack) {

            mRelaseNotePicEntity = RelaseNotePicEntityInfo;

            final Handler handler = new Handler() {
                /* (non-Javadoc)   
                 * @see android.os.Handler#handleMessage(android.os.Message)   
                 */
                @Override
                public void handleMessage(Message msg) {
                    // TODO Auto-generated method stub
                    imageCallBack.imageLoad(imageView, (Bitmap) msg.obj);
                }
            };

            new Thread() {
                /* (non-Javadoc)   
                 * @see java.lang.Thread#run()   
                 */
                @Override
                public void run() { // mgr.getImag
                    synchronized (mLock) {
                        String picLink = "";
                        StringBuffer buffer = new StringBuffer();
                        buffer.append(picLink);
                        Pattern pattern = Pattern.compile("[^0-9]");
                        Matcher match = pattern.matcher(mRelaseNotePicEntity.getmImageUrl());
                        buffer.append(match.replaceAll("").trim());

                        picLink = buffer.toString();
                        Bitmap bm = getImageFromLocal(picLink);
                        //Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.biz_ad_new_version1_img1);
                        if (bm == null) {
                            String picUrl = mRelaseNotePicEntity.getmImageUrl();
                            bm = returnReleasseNotedBitMap(picUrl, 1);
                            if (null != bm) {
                                saveImageToLocal(picLink, bm);
                            }
                        }
                        mRelaseNotePicEntity.setmImg(bm);
                        Message msg = handler.obtainMessage(0, bm);                        
                        handler.sendMessage(msg);
                    }
                }
            }.start();
        }
    }
    
    public interface ImageCallBack {
        public void imageLoad(ImageView imageView, Bitmap bitmap);
    }

}
