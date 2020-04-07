package com.cydroid.note.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.view.NoteGrideView;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.ThreadPool;

import java.util.ArrayList;
import java.util.List;

import cyee.changecolors.ChameleonColorManager;

/**
 * Created by gaojt on 16-1-12.
 */
public class AIActivity extends StandardActivity implements StandardActivity.StandardAListener {
    public static final String KEY_AMI_Recommend = "key_ami_recommend";

    private static final String URI_PREFIX = "http://wap.sogou.com/web/sl?keyword=";
    private static final String URI_SUFFIX = "&bid=sogou-mobp-b8c8c63d4b8856c7";
    private static final int COLUMN_COUNT = 2;
    private View mAmiRecommendPanel;
    private View mFavorRecommendPanel;
    private NoteGrideView mAmiRecommendGridLy;
    private NoteGrideView mFavorRecommendGridLy;
    private AISearchView mSearchView;
    private boolean mIsOpenNetwork;
    private BroadcastReceiver mNetworkReceiver;
    private boolean mIsDestroy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.ai_activity_title);
        setStandardAListener(this);
        setNoteContentView(R.layout.ai_activity_content_ly);
        setNoteRootViewBackgroundColor();
        initNetworkEnv();
        initView();
        initData();
    }

    private void initNetworkEnv() {
        ConnectivityManager cm = (ConnectivityManager) NoteAppImpl.getContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            mIsOpenNetwork = true;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        BroadcastReceiver networkReceiver = new NetworkReceiver();
        mNetworkReceiver = networkReceiver;
        registerReceiver(networkReceiver, filter);
    }

    private class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager cm = (ConnectivityManager) context.
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    mIsOpenNetwork = true;
                } else {
                    mIsOpenNetwork = false;
                }
            }
        }
    }

    private void initView() {
        mAmiRecommendPanel = findViewById(R.id.ami_recommend_panel);
        if(ChameleonColorManager.isNeedChangeColor()) {
            mAmiRecommendPanel.setBackground(getDrawable(R.drawable.transparent_drawable));
        }
        mFavorRecommendPanel = findViewById(R.id.favor_recommend_panel);
        if(ChameleonColorManager.isNeedChangeColor()) {
            mFavorRecommendPanel.setBackground(getDrawable(R.drawable.transparent_drawable));
        }
        mAmiRecommendGridLy = (NoteGrideView) findViewById(R.id.ami_recommend_grid_view);
        mFavorRecommendGridLy = (NoteGrideView) findViewById(R.id.favor_recommend_grid_view);
        mSearchView = (AISearchView) findViewById(R.id.recommend_search_panel);
        mSearchView.setOnQueryTextListener(new AISearchView.OnQueryTextListener() {
            @Override
            public void onQueryText(String newText) {
                if (!mIsOpenNetwork) {
                    showNotNetworkTip();
                    return;
                }
                startWebPageActivity(newText, newText);
            }
        });
    }

    private AdapterView.OnItemClickListener createItemOnClickListener() {
        AdapterView.OnItemClickListener itemOnClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mIsOpenNetwork) {
                    showNotNetworkTip();
                    return;
                }
                Object searchObj = parent.getItemAtPosition(position);
                if (searchObj instanceof HotWordUtils.HotWord) {
                    HotWordUtils.HotWord hotWord = (HotWordUtils.HotWord) searchObj;
                    startHotWordActivity(hotWord.getTitle(), hotWord.getSearchWord());
                    return;
                }
                if (searchObj instanceof String) {
                    startWebPageActivity((String) searchObj, (String) searchObj);
                }
            }
        };
        return itemOnClickListener;
    }

    private void initData() {
        final AdapterView.OnItemClickListener itemOnClickListener = createItemOnClickListener();
        final LayoutInflater inflater = LayoutInflater.from(this);
        ArrayList<String> keyWords = getIntent().getExtras().getStringArrayList(KEY_AMI_Recommend);

        if (keyWords != null && keyWords.size() > 0) {
            mAmiRecommendPanel.setVisibility(View.VISIBLE);
            fullAmiRecommendPanel(itemOnClickListener, inflater, keyWords);
        }

        final boolean isOpenNetwork = mIsOpenNetwork;
        NoteAppImpl.getContext().getThreadPool().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                HotWordUtils hotWordUtils = new HotWordUtils();
                final List<HotWordUtils.HotWord> hotWords = hotWordUtils.getHotWorks(AIActivity.this, isOpenNetwork);
                if (hotWords == null) {
                    return null;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsDestroy) {
                            return;
                        }
                        mFavorRecommendPanel.setVisibility(View.VISIBLE);
                        fullFavorRecommendPanel(hotWords, inflater, itemOnClickListener);
                    }
                });
                return null;
            }
        });
    }

    private void fullFavorRecommendPanel(List<HotWordUtils.HotWord> hotWords,
                                         LayoutInflater inflater,
                                         AdapterView.OnItemClickListener itemOnClickListener) {
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        AIHotWordAdapter adapter = new AIHotWordAdapter(hotWords, inflater, isSecuritySpace);
        mFavorRecommendGridLy.setAdapter(adapter);
        mFavorRecommendGridLy.setOnItemClickListener(itemOnClickListener);
    }

    private void fullAmiRecommendPanel(AdapterView.OnItemClickListener itemOnClickListener,
                                       LayoutInflater inflater, ArrayList<String> keyWords) {
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        AIStrAdapter adapter = new AIStrAdapter(keyWords, inflater, isSecuritySpace);
        mAmiRecommendGridLy.setAdapter(adapter);
        mAmiRecommendGridLy.setOnItemClickListener(itemOnClickListener);
    }

    private void showNotNetworkTip() {
        Toast.makeText(this, R.string.ai_activity_not_network_tip, Toast.LENGTH_SHORT).show();
    }

    private void startWebPageActivity(String title, String searchWord) {
        Intent intent = new Intent();
        intent.setClass(this, WebPageActivity.class);
        intent.putExtra(WebPageActivity.DATA_TITLE, title);
        intent.putExtra(WebPageActivity.DATA_URL, URI_PREFIX + searchWord + URI_SUFFIX);
        intent.putExtra(Constants.IS_SECURITY_SPACE,
                getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false));
        startActivity(intent);

    }

    private void startHotWordActivity(String title, String searchUrl) {
        Intent intent = new Intent();
        intent.setClass(this, WebPageActivity.class);
        intent.putExtra(WebPageActivity.DATA_TITLE, title);
        intent.putExtra(WebPageActivity.DATA_URL, searchUrl);
        intent.putExtra(Constants.IS_SECURITY_SPACE,
                getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false));
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkReceiver);
        mIsDestroy = true;
    }

    @Override
    public void onClickHomeBack() {
        mSearchView.hintInputMehtod();
        finish();
    }

    @Override
    public void onClickRightView() {

    }
}
