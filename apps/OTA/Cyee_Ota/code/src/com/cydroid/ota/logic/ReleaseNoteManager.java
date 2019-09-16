package com.cydroid.ota.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;
import com.cydroid.ota.bean.RelaseNotePicEntity;
import com.cydroid.ota.logic.net.HttpUtils;
import com.cydroid.ota.ui.ImageReleaseNoteActivity;

import android.content.Context;
import com.cydroid.ota.Log;

/**
 * 
 * @author cuijiuyu
 *
 */
public class ReleaseNoteManager {

    private static ReleaseNoteManager sInstance = null;
    private Context mContext;
    Object mLock = new Object();
    public List<RelaseNotePicEntity> mRelaseNoteList = new ArrayList<RelaseNotePicEntity>();

    public static ReleaseNoteManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ReleaseNoteManager(context);
        }

        return sInstance;
    }

    private ReleaseNoteManager(Context context) {
        mContext = context;
    }

    /**
     * Parse the image release note page json data.
     * 
     * @param json
     *            the json data getting from network
     */
    public void parseImageRelaseNoteJson(String json) {
    	mRelaseNoteList.clear();
        try {
            JSONArray jsonObjs = new JSONObject(json).getJSONArray("data");
            for (int i = 0; i < jsonObjs.length(); i++) {
                JSONObject imageinfo = jsonObjs.getJSONObject(i);
                RelaseNotePicEntity relasenote = new RelaseNotePicEntity(0, "", "", null);
                relasenote.setmImageId(Integer.parseInt(imageinfo.getString("id")));
                relasenote.setmImageName(imageinfo.getString("imageName"));
                relasenote.setmImageUrl(imageinfo.getString("imageUrl"));
                mRelaseNoteList.add(relasenote);              
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }       
    }

    /**
     * init the image release note data.
     */
    public synchronized void init() {
//        String url ="http://18.8.8.39:8080/GnApi_OTA/rnImage.do?releaseNotesId=1";
        //需要判断测试模式判断
        String host = HttpUtils.getServerHost(mContext);
        String url = host  + "/ota/rnImage.do?releaseNotesId=" + ImageReleaseNoteActivity.mReleaseNotesId;
        Log.d("getServerIP:",url);
        String json = getImageRelaseNoteInfo(url);
        if ("".equals(json) || null == json) {
        } else {
            parseImageRelaseNoteJson(json);
        }
    }
    
    /**
     * 
     * @return server IP.
     */
//	public String getServerIP() {			
//		if (CommonHelper.getOtaChangeIpMode(mContext)) {
//			Config.RELEASENOTESERVERIP = CommonHelper.initHttpCommunicatorHost(mContext);			
//		}
//		return Config.RELEASENOTESERVERIP;
//	}
    
    /**
     * get the json data str from url
     * 
     * @param url
     *            the url addr
     * @return string. the json data.
     */
    public String getImageRelaseNoteInfo(String url) {
        StringBuilder builder = new StringBuilder();
        InputStream is = null;
        try {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpGet httpGet = new HttpGet(url);
            int res = 0;
            HttpResponse httpResponse = httpClient.execute(httpGet);

            res = httpResponse.getStatusLine().getStatusCode();
            String contentType = httpResponse.getEntity().getContentType().getValue();
            String spStr[] = contentType.split("/");
            if ("image".equals(spStr[0])) {
                return "fail";
            }
            if (res == 200) {
                is = httpResponse.getEntity().getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                for (String s = bufferedReader.readLine(); s != null; s = bufferedReader.readLine()) {
                    builder.append(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
    
}
