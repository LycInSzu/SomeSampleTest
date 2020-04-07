package com.cydroid.note.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.note.common.Log;
import com.cydroid.note.R;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.app.view.GestureDetectionContainer;
import com.cydroid.note.app.view.NoteGridLayoutManager;
import com.cydroid.note.app.view.SlideScaleListener;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.Log;
import com.cydroid.note.data.DataManager;
import com.cydroid.note.data.LocalSource;
import com.cydroid.note.data.NoteSet;
import com.cydroid.note.data.Path;
import com.cydroid.note.encrypt.EncryptDetailActivity;
import com.cydroid.note.encrypt.EncryptMainActivity;
import com.cydroid.note.encrypt.EncryptUtil;
import com.cydroid.note.encrypt.PasswordActivity;
import com.cydroid.note.provider.NoteShareDataManager;
import com.cydroid.note.widget.WidgetUtil;
//GIONEE wanghaiyan 2016 -12-22 modify for 52253 begin
import com.cydroid.note.common.FileUtils;
//GIONEE wanghaiyan 2016 -12-22 modify for 52253 end

public class NoteMainFragment extends Fragment implements LoadingListener,
        RecyclerViewBaseAdapter.OnTouchListener, NoteSelectionManager.SelectionListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "NoteMainFragment";

    private GestureDetectionContainer mRootView;
    private RecyclerViewBaseAdapter mAdapter;
    private ListRecyclerViewAdapter mListAdapter;
    private GrideRecyclerViewAdapter mGrideAdapter;
    private NoteSelectionManager mNoteSelectionManager;
    private View mTipView;
    private TextView mTipTextView;
    private ImageView mTipImageView;
    private RecyclerView mRecyclerView;

    private NoteActionExecutor mExecutor;
    private NoteSet mNoteSet;
    private int mCurrentDisplayMode = 0;
    private boolean mDisplayModeSwitching = false;

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
    }

    private void initData() {
        DataManager dataManager = ((NoteAppImpl) getActivity().getApplication()).getDataManager();
        Activity activity = getActivity();
        String setPath = "";
        if (activity instanceof NoteMainActivity) {
            setPath = LocalSource.LOCAL_SET_PATH;
        } else {
            setPath = LocalSource.LOCAL_SECRET_SET_PATH;
        }
        mNoteSet = dataManager.getMediaSet(setPath);
        NoteSelectionManager noteSelectionManager = new NoteSelectionManager();
        noteSelectionManager.setSelectionListener(this);
        mNoteSelectionManager = noteSelectionManager;
        mNoteSelectionManager.setSourceMediaSet(mNoteSet);
        mExecutor = new NoteActionExecutor(getActivity());
    }

    public void setDisplayMode(int displayMode, boolean displayHeader) {
        mCurrentDisplayMode = displayMode;
        NoteShareDataManager.setNoteDisplayMode(NoteAppImpl.getContext(), mCurrentDisplayMode);
        mRecyclerView.setLayoutManager(getLayoutManager(displayMode == Constants.NOTE_DISPLAY_LIST_MODE ? 1 : 2));
        mAdapter = getAdapter(displayMode, displayHeader);
        mRecyclerView.setAdapter(getAdapter(displayMode, displayHeader));
    }

    private RecyclerViewBaseAdapter getAdapter(int displayMode, boolean displayHeader) {
        if (displayMode == Constants.NOTE_DISPLAY_LIST_MODE) {
            if (null == mListAdapter) {
                mListAdapter = new ListRecyclerViewAdapter(getActivity(), mNoteSet, this, mNoteSelectionManager, displayHeader);
                mListAdapter.setOnTouchListener(this);
                mListAdapter.setTimeTextColor(ContextCompat.getColor(NoteAppImpl.getContext(), R.color.home_note_item_time_color));
            }
            return mListAdapter;
        } else {
            if (null == mGrideAdapter) {
                mGrideAdapter = new GrideRecyclerViewAdapter(getActivity(), mNoteSet, this, mNoteSelectionManager, displayHeader);
                mGrideAdapter.setOnTouchListener(this);
                mGrideAdapter.setTimeTextColor(ContextCompat.getColor(NoteAppImpl.getContext(), R.color.home_note_item_time_color));
            }
            return mGrideAdapter;
        }
    }

    public void loadDataForChangeMode() {
        mDisplayModeSwitching = true;
        mAdapter.resume();
    }

    public int getDisplayMode() {
        return mCurrentDisplayMode;
    }

    private RecyclerView.LayoutManager getLayoutManager(int column) {
        return new NoteGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public void onLoadingStarted() {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mTipView.setVisibility(View.VISIBLE);
            //GIONEE wanghaiyan 2016-12-17 modify for 48534 begin
            /*
            if (mDisplayModeSwitching) {
                //mTipTextView.setText(R.string.display_change);
            } else {
               // mTipTextView.setText(R.string.note_tip_loading);
            }
            */
            //GIONEE wanghaiyan 2016-12-17 modify for 48534 end
            if (PlatformUtil.isSecurityOS() && getActivity() instanceof EncryptMainActivity) {
                mTipImageView.setVisibility(View.INVISIBLE);
            }
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTipView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoadingFinished(boolean loadingFailed) {
        mDisplayModeSwitching = false;
        int count = mAdapter.getItemCount();
        if (count == 0) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mTipView.setVisibility(View.VISIBLE);
            if (getActivity() instanceof EncryptMainActivity) {
                if (PlatformUtil.isSecurityOS()) {
                    mTipTextView.setText(R.string.encrypt_main_note_security_os_tip_load_finish);
                    mTipImageView.setVisibility(View.INVISIBLE);
                    updateSecurityOSFooterViewsState(false);
                } else {
                    mTipTextView.setText(R.string.encrypt_main_note_tip_load_finish);
                }
            } else {
                mTipTextView.setText(R.string.note_tip_loadFinish);
            }
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTipView.setVisibility(View.GONE);
            if (PlatformUtil.isSecurityOS()) {
                updateSecurityOSFooterViewsState(true);
                if (mNoteSelectionManager != null && mNoteSelectionManager.inSelectAllMode()) {
                    updateSelectionModeTitle();
                }
            }
        }
    }

    private void updateSecurityOSFooterViewsState(boolean enable) {
        Activity activity = getActivity();
        if (activity instanceof EncryptMainActivity) {
            ((EncryptMainActivity) activity).updateSecurityOSFooterViewsState(enable);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(TAG, "onCreateView");
        }
        View rootView = mRootView;
        if (rootView == null) {
            if (DEBUG) {
                Log.d(TAG, "init view");
            }
            rootView = inflater.inflate(R.layout.note_main_fragment_layout, container, false);
            mRootView = (GestureDetectionContainer) rootView;
            Activity activity = getActivity();
            if (activity instanceof EncryptMainActivity) {
                mRootView.setDisableGestureDetect(true);
            } else {
                if (PlatformUtil.isSecurityOS()
                        && !EncryptUtil.isDialcodeOpen(NoteAppImpl.getContext().getContentResolver())) {
                    mRootView.setDisableGestureDetect(true);
                }
            }
            mRootView.getSlideScaleListener().setSlideEndResponse(mSlideEndResponse);
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.home_recycler_view);
            recyclerView.addItemDecoration(new NoteItemDecoration(NoteAppImpl.getContext()));
            mRecyclerView = recyclerView;
            setDisplayMode(NoteUtils.getDisplayMode(), activity instanceof NoteMainActivity ? true : false);

            mTipView = rootView.findViewById(R.id.note_tip_view);
            mTipTextView = (TextView) rootView.findViewById(R.id.note_tip_text_view);
            boolean isSecuritySpace = activity.getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
            int color = ColorThemeHelper.isDarkBgColor(activity, isSecuritySpace)?
                    ContextCompat.getColor(activity, R.color.note_tip_text_dart_bg_color):
                    ContextCompat.getColor(activity, R.color.note_tip_text_white_bg_color);
            mTipTextView.setTextColor(color);
            mTipImageView = (ImageView) rootView.findViewById(R.id.note_tip_image_view);
        }
        return rootView;
    }

    SlideScaleListener.SlideEndResponse mSlideEndResponse = new SlideScaleListener.SlideEndResponse() {
        @Override
        public void onSlideEndResponse() {
            Activity activity = getActivity();
	        //GIONEE wanghaiyan 2016 -12-22 modify for 52253 begin
            if (null == activity || !FileUtils.gnEncryptionSpaceSupport) {
                return;
            }
	        //GIONEE wanghaiyan 2016 -12-22 modify for 52253 end
            if (PlatformUtil.isSecurityOS()) {
                if (EncryptUtil.isGestureOpen(activity.getContentResolver())) {
                    Intent targetIntent = new Intent(activity, EncryptMainActivity.class);
                    targetIntent.putExtra(Constants.IS_SECURITY_SPACE, true);
                    targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pIntent = PendingIntent.getActivity(activity, 100, targetIntent, 0);
                    Intent intent = new Intent(EncryptUtil.ACTION_ENTER_ENCRYPT_VERIFICATION);
                    intent.putExtra(EncryptUtil.EXTRA_PENDINGINTENT, pIntent);
                    activity.startActivityForResult(intent, 101);
                }
            } else {
                try {
                    Intent intent = new Intent(activity, PasswordActivity.class);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
            }
        }
    };

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
        mAdapter.destroy();
        mExecutor.destroy();
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
        mAdapter.resume();
        mExecutor.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            Log.d(TAG, "onPause");
        }
        mAdapter.pause();
        mExecutor.pause();
    }

    @Override
    public void onSingleClickTouch(Path path) {
        if (mNoteSelectionManager.inSelectionMode()) {
            mNoteSelectionManager.toggle(path);
            return;
        }
        try {
            Activity activity = getActivity();
            if (null == activity) {
                return;
            }
            Intent intent = new Intent();
            if (activity instanceof EncryptMainActivity) {
                intent.putExtra(EncryptDetailActivity.NOTE_ITEM_PATH, path.toString());
                intent.setClass(getActivity(), EncryptDetailActivity.class);
                intent.putExtra(Constants.NOTE_IS_CRYPTED, true);
                boolean isSecuritySpace = activity.getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false)
                        || ("action.note.private.space".equals(activity.getIntent().getAction()));
                intent.putExtra(Constants.IS_SECURITY_SPACE,isSecuritySpace);
/*                intent.putExtra(Constants.IS_SECURITY_SPACE,
                        activity.getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false));*/
                activity.startActivity(intent);
            } else {
                intent.putExtra(NewNoteActivity.FROM_INNER_CONTEXT, true);
                intent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, path.toString());
                intent.setClass(getActivity(), NewNoteActivity.class);
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            Log.d(TAG, "error:" + e);
        }
    }

    @Override
    public void onLongClickTouch(Path path) {
        mNoteSelectionManager.toggle(path);
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case NoteSelectionManager.ENTER_SELECTION_MODE: {
                startSelectionMode();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case NoteSelectionManager.LEAVE_SELECTION_MODE: {
                finishSelectionMode();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case NoteSelectionManager.SELECT_ALL_MODE: {
                updateSelectionModeTitle();
                updateSelectionViewsState();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case NoteSelectionManager.CANCEL_ALL_MODE: {
                updateSelectionModeTitle();
                updateSelectionViewsState();
                mAdapter.notifyDataSetChanged();
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onSelectionChange(Path path, boolean selected) {
        updateSelectionModeTitle();
        updateSelectionViewsState();
        mAdapter.notifyDataSetChanged();
    }

    private void updateSelectionModeTitle() {
        int count = mNoteSelectionManager.getSelectedCount();
        if (DEBUG) {
            Log.d(TAG, "count = " + count);
        }
		//GIONEE wanghaiyan 2016-4-26 modify for 58968 begin
        Activity activity = getActivity();
        if (activity == null) return;
        String format = activity.getResources().getQuantityString(
                R.plurals.number_of_items_selected, count);
		//GIONEE wanghaiyan 2016-4-26 modify for 58968 end
        setSelectionModeTitle(String.format(format, count));
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.note_main_activity_action_mode_title_layout_back:
                    if (DEBUG) {
                        Log.d(TAG, "note_main_activity_action_mode_title_layout_back");
                    }
                    mNoteSelectionManager.leaveSelectionMode();
                    break;
                case R.id.note_main_activity_action_mode_title_layout_select:
                    if (DEBUG) {
                        Log.d(TAG, "note_main_activity_action_mode_title_layout_select");
                    }
                    if (mNoteSelectionManager.inSelectAllMode()) {
                        mNoteSelectionManager.deSelectAll();
                    } else {
                        mNoteSelectionManager.selectAll();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void startSelectionMode() {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        if (activity instanceof NoteMainActivity) {
            ((NoteMainActivity) activity).startSelectionMode(mNoteSelectionManager, mOnClickListener);
        } else if (activity instanceof EncryptMainActivity) {
            ((EncryptMainActivity) activity).startSelectionMode(mNoteSelectionManager, mOnClickListener);
        }
    }

    public void onDel() {
        NoteActionExecutor noteDelExecutor = mExecutor;
        Activity activity = getActivity();
        boolean isEncrypted = false;
        if (null != activity && activity instanceof EncryptMainActivity) {
            isEncrypted = true;
        }
        noteDelExecutor.startDeleteAction(mNoteSelectionManager, new NoteActionExecutor.NoteActionListener() {

            @Override
            public void onActionPrepare() {
            }

            @Override
            public int onActionInvalidId() {
                return 0;
            }

            @Override
            public void onActionFinish(int success, int fail) {
                if (DEBUG) {
                    Log.d(TAG, "success = " + success + ",fail = " + fail);
                }
                if (success > 0) {
                    WidgetUtil.updateAllWidgets();
                }
                mNoteSelectionManager.leaveSelectionMode();
            }
        }, isEncrypted);
    }

    public void onThrowIntoTrash() {
        NoteActionExecutor executor = mExecutor;
        executor.startThrowIntoTrashAction(mNoteSelectionManager, new NoteActionExecutor.NoteActionListener() {

            @Override
            public void onActionPrepare() {

            }

            @Override
            public int onActionInvalidId() {
                return 0;
            }

            @Override
            public void onActionFinish(int success, int fail) {
                if (success > 0) {
                    WidgetUtil.updateAllWidgets();
                }
                mNoteSelectionManager.leaveSelectionMode();
            }
        });
    }

    public void setGestureDetectEnable() {
        if (mRootView.getDisableGestureDetect()) {
            mRootView.setDisableGestureDetect(false);
        }
    }

    public void onEncrpt() {
        NoteActionExecutor noteDelExecutor = mExecutor;
        noteDelExecutor.startEncryptAction(mNoteSelectionManager, new NoteActionExecutor.NoteActionListener() {
            @Override
            public void onActionPrepare() {
            }

            @Override
            public int onActionInvalidId() {
                return 0;
            }

            @Override
            public void onActionFinish(int success, int fail) {
                WidgetUtil.updateAllWidgets();
                mNoteSelectionManager.leaveSelectionMode();
                Activity activity = getActivity();
                if (null == activity) {
                    return;
                }

                if (!PlatformUtil.isSecurityOS() && NoteShareDataManager.isShowEncryptUserGuide(NoteAppImpl.getContext()) == NoteMainActivity.ENCRYPT_USER_GUIDE_DEFAULT) {
                    NoteShareDataManager.setShowEncryptUserGuide(NoteAppImpl.getContext(), NoteMainActivity.ENCRYPT_USER_GUIDE_SHOW);
                }
                new ToastManager(NoteAppImpl.getContext()).showToast(EncryptUtil.getHint(true, success, fail));
                if (!PlatformUtil.isSecurityOS() && activity instanceof NoteMainActivity) {
                    ((NoteMainActivity) activity).checkShowEncryptUserGuide();
                }
            }
        });
    }

    public void onDecrypt() {
        NoteActionExecutor noteDelExecutor = mExecutor;
        noteDelExecutor.startDecryptAction(mNoteSelectionManager, new NoteActionExecutor.NoteActionListener() {
            @Override
            public void onActionPrepare() {
            }

            @Override
            public int onActionInvalidId() {
                return 0;
            }

            @Override
            public void onActionFinish(int success, int fail) {
                WidgetUtil.updateAllWidgets();
                mNoteSelectionManager.leaveSelectionMode();
                new ToastManager(NoteAppImpl.getContext()).showToast(EncryptUtil.getHint(false, success, fail));
            }
        });
    }

    private void finishSelectionMode() {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        if (activity instanceof NoteMainActivity) {
            ((NoteMainActivity) activity).finishSelectionMode();
        } else if (activity instanceof EncryptMainActivity) {
            ((EncryptMainActivity) activity).finishSelectionMode();
        }
    }

    private void setSelectionModeTitle(String title) {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        if (activity instanceof NoteMainActivity) {
            ((NoteMainActivity) activity).setSelectionModeTitle(title);
        } else if (activity instanceof EncryptMainActivity) {
            ((EncryptMainActivity) activity).setSelectionModeTitle(title);
        }

    }

    private void updateSelectionViewsState() {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        if (activity instanceof NoteMainActivity) {
            ((NoteMainActivity) getActivity()).updateSelectionViewsState();
        } else if (activity instanceof EncryptMainActivity) {
            ((EncryptMainActivity) getActivity()).updateSelectionViewsState();
        }
    }

}
