package com.cydroid.note.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gionee.framework.log.Logger;
import com.cydroid.note.R;
import com.cydroid.note.app.view.NoteGridLayoutManager;
import com.cydroid.note.app.view.NoteSearchView;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.Log;
import com.cydroid.note.data.DataManager;
import com.cydroid.note.data.KeyNoteSet;
import com.cydroid.note.data.KeySource;
import com.cydroid.note.data.Path;
import com.cydroid.note.encrypt.EncryptDetailActivity;
import com.cydroid.note.encrypt.EncryptMainActivity;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeTextView;

public class NoteSearchFragment extends Fragment implements LoadingListener,
        AdapterView.OnItemClickListener, RecyclerViewBaseAdapter.OnTouchListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "NoteSearchFragment";
    private static final int MESSAGE_UPDATE_LABEL = 1;

    private RecyclerView mRecyclerView;
    private RecyclerViewBaseAdapter mRecyclerViewAdapter;
    private View mRootView;
    private ListView mLabelList;
    private SearchLabelAdapt mLabelAdapt;
    private View mSearchLabel;
    private KeyNoteSet mNoteSet;
    private View mTipView;
    private TextView mTipTextView;
    private boolean mSearchContentIsEmpty = true;
    private Handler mMainHandler;

    private LabelManager.LabelDataChangeListener mLabelDataChangeListener;
    private LabelManager mLabelManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (DEBUG) {
            Log.d(TAG, "onActivityCreated");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (DEBUG) {
            Log.d(TAG, "onAttach");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            Log.d(TAG, "onCreate");
        }
        initData();
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        if (activity instanceof NoteMainActivity) {
            ((NoteMainActivity) activity).beginNoteSearch(mQueryTextListener);
        } else {
            ((EncryptMainActivity) activity).beginNoteSearch(mQueryTextListener);
        }
    }

    private NoteSearchView.OnQueryTextListener mQueryTextListener = new NoteSearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            boolean isEmpty = TextUtils.isEmpty(newText);
            mSearchContentIsEmpty = isEmpty;
            if (isEmpty) {
                if ((mLabelAdapt != null && mLabelAdapt.getCount() > 0)) {
                    setSearchLabelVisibility(View.VISIBLE);
                }
            } else {
                setSearchLabelVisibility(View.GONE);
            }
            mNoteSet.setKey(newText);
            return false;
        }
    };

    private void setSearchLabelVisibility(int visibility) {
        if (mSearchLabel != null) {
            mSearchLabel.setVisibility(visibility);
        }
    }

    private void initData() {
        NoteAppImpl app = (NoteAppImpl) getActivity().getApplication();
        DataManager dataManager = app.getDataManager();
        Activity activity = getActivity();
        String setPathString = KeySource.KEY_SET_PATH;
        if (null != activity && activity instanceof EncryptMainActivity) {
            setPathString = KeySource.KEY_SECRET_SET_PATH;
        }
        KeyNoteSet noteSet = (KeyNoteSet) dataManager.getMediaSet(setPathString);
        mNoteSet = noteSet;

        mMainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_UPDATE_LABEL) {
                    mLabelAdapt.setLabels(mLabelManager.getLabelList());
                    mLabelAdapt.notifyDataSetChanged();
                }
            }
        };

        LabelManager labelManager = app.getLabelManager();
        mLabelDataChangeListener = new LabelManager.LabelDataChangeListener() {
            @Override
            public void onDataChange() {
                if (DEBUG) {
                    Log.d(TAG, "onDataChange");
                }
                mMainHandler.sendEmptyMessage(MESSAGE_UPDATE_LABEL);
            }
        };
        labelManager.addLabelDataChangeListener(mLabelDataChangeListener);
        mLabelManager = labelManager;
        mLabelAdapt = new SearchLabelAdapt(getActivity());
        mLabelAdapt.setLabels(labelManager.getLabelList());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(TAG, "onCreateView");
        }
        View rootView = mRootView;
        if (rootView == null) {

            boolean isSecuritySpace = getActivity().getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
            rootView = inflater.inflate(R.layout.note_search_fragment_layout, container, false);
            mRootView = rootView;

            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.search_recycler_view);
            mRecyclerView.addItemDecoration(new NoteItemDecoration(NoteAppImpl.getContext()));
            int displayMode = NoteUtils.getDisplayMode();
            if (displayMode == Constants.NOTE_DISPLAY_LIST_MODE) {
                mRecyclerViewAdapter = new ListRecyclerViewAdapter(getActivity(), mNoteSet, this, null, false);
            } else {
                mRecyclerViewAdapter = new GrideRecyclerViewAdapter(getActivity(), mNoteSet, this, null, false);
            }
            mRecyclerViewAdapter.setOnTouchListener(this);
            mRecyclerViewAdapter.setTimeTextColor(ContextCompat.getColor(NoteAppImpl.getContext(),
                    R.color.home_note_item_time_color));
            mRecyclerView.setLayoutManager(getLayoutManager(displayMode));
            mRecyclerView.setAdapter(mRecyclerViewAdapter);

            mTipView = rootView.findViewById(R.id.note_tip_view);
            mTipTextView = (CyeeTextView) rootView.findViewById(R.id.note_tip_text_view);
            int color = ColorThemeHelper.isDarkBgColor(getActivity(), isSecuritySpace) ?
                    ContextCompat.getColor(getActivity(), R.color.note_tip_text_dart_bg_color) :
                    ContextCompat.getColor(getActivity(), R.color.note_tip_text_white_bg_color);
            mTipTextView.setTextColor(color);
            mSearchLabel = rootView.findViewById(R.id.search_label);
			//Chenyee wanghaiyan 2018-6-13 modify for SWW1618OTA-484 begin
			/*
            if(ChameleonColorManager.isNeedChangeColor()) {
              mSearchLabel.setBackground(getResources().getDrawable(R.drawable.transparent_drawable));
            }
            */
			//Chenyee wanghaiyan 2018-6-13 modify for SWW1618OTA-484 end
            mLabelList = (ListView) rootView.findViewById(R.id.search_label_list);
            mLabelList.setAdapter(mLabelAdapt);
            mLabelList.setOnItemClickListener(this);
            if (mLabelAdapt.getCount() > 0) {
                setSearchLabelVisibility(View.VISIBLE);
            } else {
                setSearchLabelVisibility(View.GONE);
            }
        }
        return rootView;
    }

    protected RecyclerView.LayoutManager getLayoutManager(int displayMode) {
        int column = displayMode == Constants.NOTE_DISPLAY_LIST_MODE ? 1 : 2;
        return new NoteGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (DEBUG) {
            Log.d(TAG, "onDestroyView");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Log.d(TAG, "onDestroy");
        }
        mRecyclerViewAdapter.destroy();
        mLabelManager.removeLabelDataChangeListener(mLabelDataChangeListener);
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        if (activity instanceof NoteMainActivity) {
            ((NoteMainActivity) activity).endNoteSearch();
        } else if (activity instanceof EncryptMainActivity) {
            ((EncryptMainActivity) activity).endNoteSearch();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (DEBUG) {
            Log.d(TAG, "onDetach");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            Log.d(TAG, "onResume");
        }
        mRecyclerViewAdapter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            Log.d(TAG, "onPause");
        }
        mRecyclerViewAdapter.pause();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        try {
            int label = (Integer) view.getTag();
            Intent intent = new Intent(NoteAppImpl.getContext(), LabelNoteActivity.class);
            intent.putExtra(LabelNoteActivity.SELECT_LABEL_ID, label);
            Activity activity = getActivity();
            if (null == activity) {
                return;
            }
            if (activity instanceof NoteMainActivity) {
                intent.putExtra(LabelNoteActivity.SOURCE_PATH, LabelNoteActivity.LOCAL);
            } else if (activity instanceof EncryptMainActivity) {
                intent.putExtra(LabelNoteActivity.SOURCE_PATH, LabelNoteActivity.SECRET);
                intent.putExtra(Constants.IS_SECURITY_SPACE,
                        activity.getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false));
                intent.putExtra(Constants.NOTE_IS_CRYPTED, true);
            }
            intent.putExtra(LabelNoteActivity.SELECT_LABEL_NAME, ((TextView) view).getText()); //NOSONAR
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.d(TAG, "start LabelNoteActivity fail : " + e.toString());
        }
    }

    @Override
    public void onLoadingStarted() {
        if (mRecyclerViewAdapter.isEmpty() && !mSearchContentIsEmpty) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mTipView.setVisibility(View.VISIBLE);
            mTipTextView.setText(R.string.note_tip_search);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTipView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoadingFinished(boolean loadingFailed) {
        if (mRecyclerViewAdapter.isEmpty() && !mSearchContentIsEmpty) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mTipView.setVisibility(View.VISIBLE);
            mTipTextView.setText(R.string.note_tip_searchFinish);

        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTipView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSingleClickTouch(Path path) {
        try {
            Intent intent = new Intent();
            Activity activity = getActivity();
            if (null == activity) {
                return;
            }
            if (activity instanceof EncryptMainActivity) {
                intent.setClass(getActivity(), EncryptDetailActivity.class);
                intent.putExtra(Constants.NOTE_IS_CRYPTED, true);
            } else {
                intent.setClass(getActivity(), NewNoteActivity.class);
            }
            intent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, path.toString());
            intent.putExtra(NewNoteActivity.FROM_INNER_CONTEXT, true);
            getActivity().startActivity(intent);
        } catch (Exception e) {
            Log.d(TAG, "start NewNoteActivity fail : " + e.toString());
        }
    }

    @Override
    public void onLongClickTouch(Path path) {

    }
}
