package com.cydroid.note.trash.app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.GrideRecyclerViewAdapter;
import com.cydroid.note.app.ListRecyclerViewAdapter;
import com.cydroid.note.app.LoadingListener;
import com.cydroid.note.app.NoteActionExecutor;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.NoteItemDecoration;
import com.cydroid.note.app.NoteSelectionManager;
import com.cydroid.note.app.RecyclerViewBaseAdapter;
import com.cydroid.note.app.view.NoteGridLayoutManager;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.data.DataManager;
import com.cydroid.note.data.NoteSet;
import com.cydroid.note.data.Path;
import com.cydroid.note.trash.data.TrashSource;
import com.cydroid.note.widget.WidgetUtil;


/**
 * Created by xiaozhilong on 7/1/16.
 */
public class TrashMainActivity extends StandardActivity implements NoteSelectionManager.SelectionListener,
        LoadingListener, RecyclerViewBaseAdapter.OnTouchListener, StandardActivity.StandardAListener {
    private static final String TAG = "TrashMainActivity";

    private NoteActionExecutor mExecutor;
    private NoteSelectionManager mSelectionManager;
    private RecyclerViewBaseAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private View mTipContainerView;
    private TextView mTipTextView;
    private TextView mSelectionTitleView;
    private TextView mSelectionAllView;
    private TextView mRecoverView;
    private TextView mDeleteView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initListener();
        updateFootViewState();
    }

    private void initView() {
        setTitle(R.string.trash);
        setNoteContentView(R.layout.label_note_activity_content_layout);
        setNoteFooterView(R.layout.trash_main_activity_action_mode_footer_layout);

        mTipContainerView = findViewById(R.id.note_tip_view);
        mTipTextView = (TextView) findViewById(R.id.note_tip_text_view);
        int color = ColorThemeHelper.isDarkBgColor(this, false)?
                ContextCompat.getColor(this, R.color.note_tip_text_dart_bg_color):
                ContextCompat.getColor(this, R.color.note_tip_text_white_bg_color);
        mTipTextView.setTextColor(color);
        mRecyclerView = (RecyclerView) findViewById(R.id.label_recycler_view);
        mRecyclerView.addItemDecoration(new NoteItemDecoration(NoteAppImpl.getContext()));
        mRecoverView = (TextView) findViewById(R.id.trash_recover_action);
        mDeleteView = (TextView) findViewById(R.id.trash_delete_action);

        setNoteRootViewBackgroundColor();
    }

    private void initData() {
        DataManager dataManager = NoteAppImpl.getContext().getDataManager();
        NoteSet trashSet = dataManager.getMediaSet(TrashSource.TRASH_SET_PATH);
        NoteSelectionManager noteSelectionManager = new NoteSelectionManager();
        noteSelectionManager.setSelectionListener(this);
        mSelectionManager = noteSelectionManager;
        mSelectionManager.setSourceMediaSet(trashSet);
        int displayMode = NoteUtils.getDisplayMode();
        if (displayMode == Constants.NOTE_DISPLAY_LIST_MODE) {
            mAdapter = new ListRecyclerViewAdapter(this, trashSet, this, noteSelectionManager, false);
        } else {
            mAdapter = new GrideRecyclerViewAdapter(this, trashSet, this, noteSelectionManager, false);
        }
        mAdapter.setOnTouchListener(this);
        mAdapter.setTimeTextColor(getResources().getColor(R.color.home_note_item_trash_time_color));
        int column = displayMode == Constants.NOTE_DISPLAY_LIST_MODE ? 1 : 2;
        mRecyclerView.setLayoutManager(new NoteGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        mExecutor = new NoteActionExecutor(this);
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        Drawable recoverIcon = getDrawable(this, R.drawable.trash_recover, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));
        Drawable deleteIcon = getDrawable(this, R.drawable.note_main_del_icon, ColorThemeHelper.getFooterBarIconColor(this, isSecuritySpace));
        mRecoverView.setCompoundDrawables(null, recoverIcon, null, null);
        mDeleteView.setCompoundDrawables(null, deleteIcon, null, null);
    }

    private void initListener() {
        mRecoverView.setOnClickListener(this);
        mDeleteView.setOnClickListener(this);
        setStandardAListener(this);
    }

    private void updateFootViewState() {
        if (mSelectionManager.inSelectionMode()) {
            mFooterViewGroup.setVisibility(View.VISIBLE);
        } else {
            mFooterViewGroup.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.resume();
        mExecutor.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.pause();
        mExecutor.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
        mExecutor.destroy();
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
                mAdapter.notifyDataSetChanged();
                break;
            }
            case NoteSelectionManager.CANCEL_ALL_MODE: {
                updateSelectionModeTitle();
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
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadingStarted() {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mTipContainerView.setVisibility(View.VISIBLE);
            mTipTextView.setText(R.string.note_tip_loading);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTipContainerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoadingFinished(boolean loadingFailed) {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mTipContainerView.setVisibility(View.VISIBLE);
            mTipTextView.setText(R.string.note_tip_loadFinish);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTipContainerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSingleClickTouch(Path path) {
        if (mSelectionManager.inSelectionMode()) {
            mSelectionManager.toggle(path);
        }
    }

    @Override
    public void onLongClickTouch(Path path) {
        mSelectionManager.toggle(path);
    }

    @Override
    public void onBackPressed() {
        if (mSelectionManager.inSelectionMode()) {
            mSelectionManager.leaveSelectionMode();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onClickHomeBack() {
        finish();
    }

    @Override
    public void onClickRightView() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.note_main_activity_action_mode_title_layout_back:
                mSelectionManager.leaveSelectionMode();
                break;
            case R.id.note_main_activity_action_mode_title_layout_select:
                if (mSelectionManager.inSelectAllMode()) {
                    mSelectionManager.deSelectAll();
                } else {
                    mSelectionManager.selectAll();
                }
                break;
            case R.id.trash_recover_action: {
                onTrashRecover();
                break;
            }
            case R.id.trash_delete_action: {
                onTrashDelete();
                break;
            }
            default:
                break;
        }
    }

    private void onTrashRecover() {
        NoteActionExecutor executor = mExecutor;
        executor.startTrashRecoverAction(mSelectionManager, new NoteActionExecutor.NoteActionListener() {

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
                mSelectionManager.leaveSelectionMode();
            }
        });
    }

    public void onTrashDelete() {
        NoteActionExecutor executor = mExecutor;
        executor.startTrashDeleteAction(mSelectionManager, new NoteActionExecutor.NoteActionListener() {

            @Override
            public void onActionPrepare() {

            }

            @Override
            public int onActionInvalidId() {
                return 0;
            }

            @Override
            public void onActionFinish(int success, int fail) {
                mSelectionManager.leaveSelectionMode();
            }
        });
    }

    public void startSelectionMode() {
        updateFootViewState();
        setNoteTitleView(R.layout.note_main_activity_action_mode_title_layout);
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        tintImageViewDrawable(R.id.note_main_activity_action_mode_title_layout_back,
                R.drawable.note_title_back_icon, ColorThemeHelper.getActionBarIconColor(this, isSecuritySpace));
        ((TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_text))
                .setTextColor(getTitleTextColor());
        ((TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_select))
                .setTextColor(getTitleTextColor());

        findViewById(R.id.note_main_activity_action_mode_title_layout_back).setOnClickListener(this);
        mSelectionAllView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_select);
        mSelectionAllView.setOnClickListener(this);
        mSelectionTitleView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_text);
    }

    private void finishSelectionMode() {
        updateFootViewState();
        setTitle(R.string.trash);
    }

    private void updateSelectionModeTitle() {
        int count = mSelectionManager.getSelectedCount();
        String format = getResources().getQuantityString(R.plurals.number_of_items_selected, count);
        mSelectionTitleView.setText(String.format(format, count));

        int strId = mSelectionManager.inSelectAllMode() ? R.string.unselect_all : R.string.select_all;
        mSelectionAllView.setText(strId);

        boolean enable = (count == 0 ? false : true);
        mRecoverView.setEnabled(enable);
        mDeleteView.setEnabled(enable);
    }

}
