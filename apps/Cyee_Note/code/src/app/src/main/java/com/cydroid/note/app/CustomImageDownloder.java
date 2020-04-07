package com.cydroid.note.app;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by spc on 16-4-9.
 */
public class CustomImageDownloder extends BaseImageDownloader {

    public CustomImageDownloder(Context context) {
        super(context);
    }

    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        return new FileInputStream(imageUri);
    }
}