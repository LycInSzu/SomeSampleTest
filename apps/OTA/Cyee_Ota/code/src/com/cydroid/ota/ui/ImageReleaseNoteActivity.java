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

import com.cydroid.ota.logic.ReleaseNoteManager;
import com.cydroid.ota.ui.widget.DirectionalViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.DisplayMetrics;
import com.cydroid.ota.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.cydroid.ota.R;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 
 * @author cuijiuyu
 *
 */
public class ImageReleaseNoteActivity extends FragmentActivity {
	private DirectionalViewPager mDirectionalViewPager;
	private boolean mIsLastPic;
    private Context mContext;
    private ProgressDialog mProgressDialog = null;
    private LinearLayout mImageLinearLayout;
    Object mLock = new Object();
    private boolean isLoading = false;
    private ReleaseNoteManager mReleaseNoteManager;
    public static String mReleaseNotesId;
    public static String RELEASE_NOTE_ID = "releaseNotesId";
    public static final int LOAD_SUCCESS = 0;
    public static final int LOAD_FAILED= 1;
    private ProgressBar mProgressBar = null;
    private TextView mTextView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_releasenote);
		// Set up the pager
		mDirectionalViewPager = (DirectionalViewPager) findViewById(R.id.pager);
		mImageLinearLayout =  (LinearLayout) findViewById(R.id.image_layout);
		 mProgressBar = (ProgressBar) findViewById(R.id.progress_loadimage);
		 mTextView = (TextView) findViewById(R.id.progress_textview);
		mReleaseNoteManager = ReleaseNoteManager.getInstance(ImageReleaseNoteActivity.this);
        new Thread(this.mRunnable).start();
		mDirectionalViewPager.setOrientation(DirectionalViewPager.VERTICAL);
        mReleaseNotesId = getIntent().getStringExtra(RELEASE_NOTE_ID);
        Log.d("mReleaseNotesId:",mReleaseNotesId + "");
    }
	
    /**
     * run a thread to get the game info and send a message when it is done.
     */
    Runnable mRunnable = new Runnable() {

        public void run() {
            try {
                synchronized (mLock) {
                    mReleaseNoteManager.init();
                    if (mReleaseNoteManager.mRelaseNoteList.size() != 0) {
                        for (int i = 0; i < mReleaseNoteManager.mRelaseNoteList.size(); i++) {
                        	if (!"".equals(mReleaseNoteManager.mRelaseNoteList.get(i).getmImageUrl())
                        			&& null != mReleaseNoteManager.mRelaseNoteList.get(i).getmImageUrl()) {                     		
                        		Bitmap bitmap = returnReleasseNotedBitMap(mReleaseNoteManager.mRelaseNoteList.get(i).getmImageUrl(),1);
                        		mReleaseNoteManager.mRelaseNoteList.get(i).setmImg(bitmap);
                        	}
                        }                        
                        Message msg = new Message();
                        msg.what = LOAD_SUCCESS;
                        mHandler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = LOAD_FAILED;
                        mHandler.sendMessage(msg);
                    }
                }
//                }
            } catch (Exception localException) {
                localException.printStackTrace();
                Message msg = new Message();
                msg.what = LOAD_FAILED;
                mHandler.sendMessage(msg);
            } finally {
                // / mIsLoading = false;
            }
        }
    };

    /**
     * Define a Handler, used to handle communication between the download thread and the UI
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                if (msg.what == LOAD_SUCCESS) {
                    isLoading = false;
                    mImageLinearLayout.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    mTextView.setVisibility(View.GONE);
            		mDirectionalViewPager.setAdapter(new ReleaseNoteFragmentAdapter(
            				getSupportFragmentManager()));
                }  else if (msg.what == LOAD_FAILED) {
                	//Toast.makeText(ImageReleaseNoteActivity.this, R.string.gn_su_string_questionnaire_error, Toast.LENGTH_SHORT).show();
                	 mProgressBar.setVisibility(View.GONE);
                	 mTextView.setText(R.string.gn_su_string_questionnaire_error);
                	//finish();
                }
                super.handleMessage(msg);
            }
        }
    };
    
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
    
    protected void onPause() {
        super.onPause();
//        if (mProgressDialog != null && mProgressDialog.isShowing()) {
//        	mProgressDialog.dismiss();
//        }  
        ImageReleaseNoteActivity.this.finish();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mReleaseNoteManager.mRelaseNoteList.size() != 0) {
            for (int i = 0; i < mReleaseNoteManager.mRelaseNoteList.size(); i++) {
                if (null != mReleaseNoteManager.mRelaseNoteList.get(i).getmImg()
                        && !mReleaseNoteManager.mRelaseNoteList.get(i).getmImg().isRecycled()) {
                    mReleaseNoteManager.mRelaseNoteList.get(i).getmImg().recycle();
                    mReleaseNoteManager.mRelaseNoteList.get(i).setmImg(null);
                    System.gc();
                }
            }
        }
        super.onDestroy();
    }
	
	
	class ReleaseNoteFragmentAdapter extends FragmentPagerAdapter {

		public ReleaseNoteFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return ReleaseNoteFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			return mReleaseNoteManager.mRelaseNoteList.size();
		}
	} 
}
