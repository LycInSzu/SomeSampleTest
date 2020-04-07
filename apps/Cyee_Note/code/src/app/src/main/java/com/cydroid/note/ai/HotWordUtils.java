package com.cydroid.note.ai;

import android.content.Context;
import com.cydroid.note.common.Log;

import com.cydroid.note.common.NoteUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaojt on 16-1-14.
 */
class HotWordUtils {

    private final static String TAG = "HotWordUtils";
    private final static String HTTP_URL = "http://ts.mobile.sogou.com/query?pid=sogou-mobp-97ffcbd95363387c&num=10&length=15&select=1,2,5,6,10,11,13,20&leadip=10.139.10.23";
    private final static String TITLE = "kwd";
    private final static String SEARCH_WORD = "url";
    private final static int CACHE_SIZE = 1024;
    private final static long MIN_SPACE = 1000 * 60 * 45L;
    private final static String KEY_WORDS_DIR_NAME = "key_words";

    static class HotWord {
        private String mTitle;
        private String mSearchWord;

        public HotWord(String title, String searchWord) {
            mTitle = title;
            mSearchWord = searchWord;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getSearchWord() {
            return mSearchWord;
        }
    }

    public synchronized List<HotWord> getHotWorks(Context context, boolean isOpenNetwork) {
        File dataFilesDir = context.getFilesDir();
        File keyWordsDir = new File(dataFilesDir, "/" + KEY_WORDS_DIR_NAME);
        File[] keyWords = keyWordsDir.listFiles();

        if (!isOpenNetwork) {
            if (keyWords == null || keyWords.length == 0) {
                return null;
            }
            try {
                byte[] data = readKeyWordData(keyWords[0]);
                return getHotWorks(data);
            } catch (Exception e) {
                Log.w(TAG, e);
            }
            return null;
        }
        return getHotWorks(keyWordsDir, keyWords);
    }


    private List<HotWord> getHotWorks(File keyWordsDir, File[] keyWords) {
        List<HotWord> data1 = getHotWords(keyWords);
        if (data1 != null) return data1;

        try {
            byte[] data = readKeyWordData(HTTP_URL);
            File saveFile = null;
            if (!keyWordsDir.exists()) {
                boolean success = keyWordsDir.mkdirs();
                if (success) {
                    saveFile = new File(keyWordsDir, Long.toString(System.currentTimeMillis()));
                }
            } else {
                saveFile = new File(keyWordsDir, Long.toString(System.currentTimeMillis()));
            }
            if (saveFile != null) {
                writeFile(saveFile, data);
            }
            return getHotWorks(data);
        } catch (Exception e) {
            Log.w(TAG, "error", e);
            return null;
        }
    }

    private List<HotWord> getHotWords(File[] keyWords) {
        if (keyWords == null) {
            return null;
        }

        if (keyWords.length > 1) {
            for (File file : keyWords) {
                file.delete();//NOSONAR
            }
            return null;
        }

        File keyWordFile = keyWords[0];
        String name = keyWordFile.getName();
        long oldTime = Long.parseLong(name);
        if (Math.abs(oldTime - System.currentTimeMillis()) >= MIN_SPACE) {
            keyWordFile.delete();
            return null;

        }

        try {
            byte[] data = readKeyWordData(keyWordFile);
            return getHotWorks(data);
        } catch (Exception e) {
            Log.w(TAG, "error", e);
        }
        return null;
    }

    private void writeFile(File file, byte[] data) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            NoteUtils.closeSilently(fos);
        }
    }

    private List<HotWord> getHotWorks(byte[] data) throws Exception {
        JSONTokener jsonParser = new JSONTokener(new String(data));//NOSONAR
        JSONArray array = (JSONArray) jsonParser.nextValue();
        int length = array.length();
        ArrayList<HotWord> workList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            JSONObject item = array.getJSONObject(i);
            String title = item.getString(TITLE);
            String searchWord = item.getString(SEARCH_WORD);
            workList.add(new HotWord(title, searchWord));
        }
        return workList;
    }

    private byte[] readKeyWordData(File file) {
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            return readKeyWordData(inStream);
        } catch (FileNotFoundException e) {
            Log.w(TAG, e);
        } finally {
            NoteUtils.closeSilently(inStream);
        }
        return null;
    }

    private byte[] readKeyWordData(String urlPath) {
        InputStream inStream = null;
        try {
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            inStream = conn.getInputStream();
            return readKeyWordData(inStream);
        } catch (MalformedURLException e) {
            Log.w(TAG, e);
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            NoteUtils.closeSilently(inStream);
        }
        return null;
    }

    private byte[] readKeyWordData(InputStream inStream) {
        try {
            byte[] data = new byte[CACHE_SIZE];
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int len;
            while ((len = inStream.read(data)) != -1) {
                outStream.write(data, 0, len);
            }
            return outStream.toByteArray();
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            NoteUtils.closeSilently(inStream);
        }
        return null;
    }
}
