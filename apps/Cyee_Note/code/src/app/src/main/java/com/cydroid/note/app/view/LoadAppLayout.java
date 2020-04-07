package com.cydroid.note.app.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.util.AttributeSet;
import com.cydroid.note.common.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.note.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by xiaozhilong on 1/6/16.
 */
public class LoadAppLayout extends LinearLayout implements View.OnClickListener {
    private final static String TAG = "LoadAppLayout";
    private ArrayList<LifeApp> mLifeApps;
    private Activity mActivity;

    public LoadAppLayout(Context context) {
        super(context);
    }

    public LoadAppLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadAppLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == oldw) {
            return;
        }
        if (mLifeApps != null) {
            fullContent();
            updateImageState();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mActivity = null;
        mLifeApps = null;
    }

    public void initLoadData(String configName, Activity activity) {
        ArrayList<LifeApp> lifeApps = getLifeAppList(configName);
        if (lifeApps == null || lifeApps.size() == 0) {
            throw new RuntimeException();
        }
        mLifeApps = lifeApps;
        mActivity = activity;
        if (getWidth() != 0) {
            fullContent();
            updateImageState();
        }
    }

    private void fullContent() {
        ArrayList<LifeApp> lifeApps = mLifeApps;
        int number = lifeApps.size();
        int itemWidth = getWidth() / number;
        if (getChildCount() > 0) {
            removeAllViews();
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < number; i++) {
            View content = inflater.inflate(R.layout.app_info_load_item, null);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(itemWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            content.setLayoutParams(params);
            ImageView picture = (ImageView) content.findViewById(R.id.app_info_load_picture);
            ImageView arrow = (ImageView) content.findViewById(R.id.app_info_load_arrow);
            TextView name = (TextView) content.findViewById(R.id.app_info_load_name);
            LifeApp lifeApp = lifeApps.get(i);
            content.setTag(lifeApp.mPageName);
            picture.setImageResource(getDrawableId(lifeApp.mIconResName));
            picture.setTag(lifeApp.mUri);
            picture.setOnClickListener(this);
            arrow.setImageResource(getDrawableId("about_ami_load"));
            name.setText(getStringId(lifeApp.mTitleResName));
            addView(content);
        }
        postRequestLayout();
    }

    private void postRequestLayout() {
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    private int getDrawableId(String resName) {
        Resources rs = getContext().getResources();
        int id = rs.getIdentifier(resName, "drawable", getContext().getPackageName());
        if (id <= 0) {
            id = rs.getIdentifier(resName, "mipmap", getContext().getPackageName());
        }
        if (id > 0) {
            return id;
        } else {
            throw new RuntimeException();
        }
    }

    private int getStringId(String resName) {
        Resources rs = getContext().getResources();
        int id = rs.getIdentifier(resName, "string", getContext().getPackageName());
        if (id > 0) {
            return id;
        } else {
            throw new RuntimeException();
        }
    }

    public void updateImageState() {
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            String packageName = (String) child.getTag();
            boolean installed = isPackageInstall(getContext(), packageName);
            boolean clickable = installed ? false : true;
            int visible = installed ? View.GONE : View.VISIBLE;
            child.findViewById(R.id.app_info_load_picture).setClickable(clickable);
            child.findViewById(R.id.app_info_load_arrow).setVisibility(visible);
        }
    }

    private boolean isPackageInstall(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        return packageInfo != null;
    }

    @Override
    public void onClick(View v) {
        String uri = (String) v.getTag();
        if (uri != null) {
            startLoadApp(uri);
        }
    }

    private void startLoadApp(String url) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            mActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "ActivityNotFoundException", e);
        }
    }

    private static class LifeApp {
        String mUri;
        String mTitleResName;
        String mIconResName;
        String mPageName;
    }

    private ArrayList<LifeApp> getLifeAppList(String configName) {
        InputStream is = getConfigInputStream(configName);
        if (is == null) {
            return null;
        }
        ArrayList<LifeApp> lifeApps = new ArrayList<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(is, null);

            int eventType = xpp.getEventType();
            HashMap<String, String> map = new HashMap<>();
            do {
                if (eventType == XmlPullParser.START_TAG && "app".equals(xpp.getName())) {
                    int count = xpp.getAttributeCount();
                    map.clear();
                    for (int x = 0; x < count; x++) {
                        map.put(xpp.getAttributeName(x), xpp.getAttributeValue(x));
                    }
                    LifeApp lifeApp = new LifeApp();
                    lifeApp.mTitleResName = map.get("titleResName");
                    lifeApp.mIconResName = map.get("iconResName");
                    lifeApp.mPageName = map.get("pageName");
                    lifeApp.mUri = map.get("appUri");
                    lifeApps.add(lifeApp);
                }
                eventType = xpp.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            Log.w(TAG, "error", e);
        } catch (IOException e) {
            Log.w(TAG, "error", e);
        } catch (Exception e) {
            Log.w(TAG, "error", e);
        } finally {
            closeSilently(is);
        }
        return lifeApps;
    }

    private InputStream getConfigInputStream(String configName) {
        InputStream is;
        try {
            is = getContext().getAssets().open(configName);
        } catch (IOException e) {
            is = null;
        }
        return is;
    }

    private void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException t) {
            Log.w(TAG, "close fail ", t);
        }
    }
}
