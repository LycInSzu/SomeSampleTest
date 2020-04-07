/**
 * ****************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package com.cydroid.note.photoview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

import com.gionee.framework.log.Logger;
import com.cydroid.note.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Lock/Unlock button is added to the ActionBar.
 * Use it to temporarily disable ViewPager navigation in order to correctly interact with ImageView by gestures.
 * Lock/Unlock state of ViewPager is saved and restored on configuration changes.
 * <p/>
 * Julia Zudikova
 */

public class PhotoViewActivity extends Activity {

    private static final String ISLOCKED_ARG = "isLocked";
    private static final boolean DEBUG = false;
    private static final String TAG = "PhotoViewActivity";

    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoview_activity_layout);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);

        Intent intent = getIntent();
        String[] uriStrs = intent.getStringArrayExtra("imageUris");
        int currentImage = intent.getIntExtra("currentImage", 0);

        mViewPager.setAdapter(new SamplePagerAdapter(this, uriStrs));
        mViewPager.setCurrentItem(currentImage);

        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }

    }

    private class SamplePagerAdapter extends PagerAdapter {
        private String[] mUriStrs;
        private LayoutInflater mInflater;
        private DisplayImageOptions mOptions;

        public SamplePagerAdapter(Context context, String[] uris) {
            mInflater = LayoutInflater.from(context);
            mUriStrs = uris;
            mOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_launcher)
                    .showImageOnFail(R.drawable.photo_view_loading_fail)
                    .resetViewBeforeLoading(true)
                    .cacheOnDisk(false)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .considerExifParams(true)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .build();
        }

        @Override
        public int getCount() {
            return mUriStrs.length;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View imageLayout = mInflater.inflate(R.layout.photeview_pager_image, container, false);
            assert imageLayout != null;
            PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.image);
            final ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.loading);

            String path = null;
            if(mUriStrs[position].startsWith("file://")){
                path = Uri.parse(mUriStrs[position]).getPath();
            }else {
                path = mUriStrs[position];
            }
            ImageLoader.getInstance().displayImage(path, photoView, mOptions,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            String message = null;
                            switch (failReason.getType()) {
                                case IO_ERROR:
                                    message = "Input/Output error";
                                    break;
                                case DECODING_ERROR:
                                    message = "Image can't be decoded";
                                    break;
                                case NETWORK_DENIED:
                                    message = "Downloads are denied";
                                    break;
                                case OUT_OF_MEMORY:
                                    message = "Out Of Memory error";
                                    break;
                                case UNKNOWN:
                                    message = "Unknown error";
                                    break;
                            }
                            if (DEBUG) {
                                Logger.printLog(TAG, "onLoadingFailed msg = " + message);
                            }
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float v, float v1) {
                    goBack();
                }
            });

            // Now just add PhotoView to ViewPager and return it
            container.addView(imageLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            return imageLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private void goBack() {
        finish();
    }

    private void toggleViewPagerScrolling() {
        if (isViewPagerActive()) {
            ((HackyViewPager) mViewPager).toggleLock();
        }
    }


    private boolean isViewPagerActive() {
        return (mViewPager != null && mViewPager instanceof HackyViewPager);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (isViewPagerActive()) {
            outState.putBoolean(ISLOCKED_ARG, ((HackyViewPager) mViewPager).isLocked());
        }
        super.onSaveInstanceState(outState);
    }

}
