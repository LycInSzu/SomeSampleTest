package com.cydroid.note.ai;

import android.content.Context;
import com.cydroid.note.common.Log;

import com.cydroid.note.ai.algorithm.DFA;
import com.cydroid.note.common.NoteUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by gaojt on 16-1-12.
 */
public class IntelligentAssistant {
    private final static String TAG = "IntelligentAssistant";
    private final static String KEY_WORD_DIR = "keyWords";
    private final static int Buffered_SIZE = 128;
    private DFA mDFA;

    public IntelligentAssistant(Context context) {
        initDictionary(context);
    }

    private void initDictionary(Context context) {
        DFA dfa = new DFA();
        try {
            String[] workFiles = context.getAssets().list(KEY_WORD_DIR);
            for (int i = 0, length = workFiles.length; i < length; i++) {
                readKeyWords(context, dfa, KEY_WORD_DIR + "/" + workFiles[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDFA = dfa;
    }

    private void readKeyWords(Context context, DFA dfa, String assetPath) {
        BufferedReader bufferedReader = null;
        try {
            InputStream is = context.getAssets().open(assetPath);
            bufferedReader = new BufferedReader(new InputStreamReader(is), Buffered_SIZE);//NOSONAR
            String keyWord;
            while ((keyWord = bufferedReader.readLine()) != null) {
                dfa.addKeyword(keyWord.trim());
            }
        } catch (IOException e) {
            Log.w(TAG, "ERROR", e);
        } finally {
            NoteUtils.closeSilently(bufferedReader);
        }
    }

    public ArrayList<String> getKeyWords(String content) {
        DFA dfa = mDFA;
        try {
            return dfa.searchKeyword(content);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "error", e);
            return null;
        }
    }
}
