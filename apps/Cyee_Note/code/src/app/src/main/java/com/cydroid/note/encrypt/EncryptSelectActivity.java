package com.cydroid.note.encrypt;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.LoadingListener;
import com.cydroid.note.app.NoteActionExecutor;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.NoteItemDecoration;
import com.cydroid.note.app.NoteSelectionManager;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.app.view.NoteGridLayoutManager;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.data.DataManager;
import com.cydroid.note.data.LocalSource;
import com.cydroid.note.data.NoteSet;
import com.cydroid.note.data.Path;
import com.cydroid.note.widget.WidgetUtil;

import cyee.widget.CyeeTextView;

/**
 * Created by wuguangjie on 16-6-2.
 */
public class EncryptSelectActivity extends StandardActivity implements View.OnClickListener, LoadingListener,
        EncryptSelectRecyclerViewAdapter.OnSingleClickTouchListener, NoteSelectionManager.SelectionListener {
    private RecyclerView mRecyclerView;
    private EncryptSelectRecyclerViewAdapter mAdapter;
    private View mTipView;
    private CyeeTextView mTipTextView;
    private NoteActionExecutor mNoteDelExecutor;
    private CyeeTextView mConfirmTextView;
    private CyeeTextView mSelectCountView;
    private RelativeLayout mSelectContainer;
    private CyeeTextView mSelectAll;
    private NoteSelectionManager mNoteSelectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initView() {
        setNoteTitleView(R.layout.encrypt_select_title);
        ((TextView) findViewById(R.id.encrypt_select_cancel)).setTextColor(getTitleTextColor());
        setNoteContentView(R.layout.encrypt_select_content_layout);
        setNoteRootViewBackgroundColor();

        mConfirmTextView = (CyeeTextView) findViewById(R.id.encrypt_select_confirm);
        mConfirmTextView.setTextColor(getResources().getColor(R.color.encrypt_select_title_grayed_color));
        mConfirmTextView.setEnabled(false);
        mSelectCountView = (CyeeTextView) findViewById(R.id.encrypt_select_count);
        mSelectContainer = (RelativeLayout) findViewById(R.id.encrypt_select_count_container);
        mSelectAll = (CyeeTextView) findViewById(R.id.encrypt_select_all);
        mRecyclerView = (RecyclerView) findViewById(R.id.encrypt_select_note_recycler_view);
        mRecyclerView.addItemDecoration(new NoteItemDecoration(NoteAppImpl.getContext()));
        mRecyclerView.setLayoutManager(getLayoutManager());
        mRecyclerView.setAdapter(mAdapter);
        mTipView = findViewById(R.id.note_tip_view);
        mTipTextView = (CyeeTextView) findViewById(R.id.note_tip_text_view);
        int color = ColorThemeHelper.isDarkBgColor(this, false)?
                ContextCompat.getColor(this, R.color.note_tip_text_dart_bg_color):
                ContextCompat.getColor(this, R.color.note_tip_text_white_bg_color);
        mTipTextView.setTextColor(color);
        findViewById(R.id.encrypt_select_cancel).setOnClickListener(this);
        mSelectAll.setOnClickListener(this);
    }

    private void initData() {
        DataManager dataManager = ((NoteAppImpl) getApplication()).getDataManager();
        String setPath = LocalSource.LOCAL_SET_PATH;
        NoteSet noteSet = dataManager.getMediaSet(setPath);

        NoteSelectionManager noteSelectionManager = new NoteSelectionManager();
        noteSelectionManager.setSelectionListener(this);
        mNoteSelectionManager = noteSelectionManager;
        mNoteSelectionManager.setSourceMediaSet(noteSet);

        int displayMode = NoteUtils.getDisplayMode();
        mAdapter = new EncryptSelectRecyclerViewAdapter(this, noteSet, this, mNoteSelectionManager, displayMode);
        mAdapter.setOnTouchListener(this);
        int column = displayMode == Constants.NOTE_DISPLAY_LIST_MODE ? 1 : 2;
        mRecyclerView.setLayoutManager(new NoteGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        mNoteSelectionManager.enterSelectionMode();
        mNoteDelExecutor = new NoteActionExecutor(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.resume();
        mNoteDelExecutor.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.pause();
        mNoteDelExecutor.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
        mNoteDelExecutor.destroy();
        mNoteSelectionManager.leaveSelectionMode();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.encrypt_select_cancel:
                finish();
                break;
            case R.id.encrypt_select_confirm:
                selectEncrypt();
                break;
            case R.id.encrypt_select_all:
                selectOrDeleteAll();
                break;
            default:
                break;
        }

    }

    private void selectEncrypt() {
        onEncrypt();
    }

    private void selectOrDeleteAll() {
        if (mNoteSelectionManager.inSelectAllMode()) {
            mNoteSelectionManager.deSelectAll();
        } else {
            mNoteSelectionManager.selectAll();
        }
    }

    private void onEncrypt() {
        NoteActionExecutor noteDelExecutor = mNoteDelExecutor;
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
                new ToastManager(NoteAppImpl.getContext()).showToast(EncryptUtil.getHint(true, success, fail));
                WidgetUtil.updateAllWidgets();
                finish();
            }
        });
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        int column = getResources().getInteger(R.integer.home_note_item_column);
        return new StaggeredGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public void onLoadingStarted() {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mSelectContainer.setVisibility(View.GONE);
            mTipView.setVisibility(View.VISIBLE);
            mTipTextView.setText(R.string.note_tip_loading);
        } else {
            mTipView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mSelectContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadingFinished(boolean loadingFailed) {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            mSelectContainer.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            mTipView.setVisibility(View.VISIBLE);
            mTipTextView.setText(R.string.note_tip_loadFinish);
        } else {
            mTipView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mSelectContainer.setVisibility(View.VISIBLE);
        }
        if (mNoteSelectionManager != null && mNoteSelectionManager.inSelectAllMode()) {
            updateSelectCountViewText();
        }
    }

    @Override
    public void onSingleClickTouch(Path path) {
        mNoteSelectionManager.toggle(path);
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case NoteSelectionManager.ENTER_SELECTION_MODE: {
                mAdapter.notifyDataSetChanged();
                break;
            }
            case NoteSelectionManager.LEAVE_SELECTION_MODE: {
                mAdapter.notifyDataSetChanged();
                break;
            }
            case NoteSelectionManager.SELECT_ALL_MODE: {
                updateSelectCountViewText();
                updateSelectViewState();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case NoteSelectionManager.CANCEL_ALL_MODE: {
                updateSelectCountViewText();
                updateSelectViewState();
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
        updateSelectCountViewText();
        updateSelectViewState();
        mAdapter.notifyDataSetChanged();
    }

    private void updateSelectViewState() {
        if (mSelectAll != null && mNoteSelectionManager != null) {
            int strId = mNoteSelectionManager.inSelectAllMode() ? R.string.unselect_all : R.string.select_all;
            mSelectAll.setText(strId);
            if (mNoteSelectionManager.getSelectedCount() == 0) {
                mConfirmTextView.setEnabled(false);
                mConfirmTextView.setClickable(false);
                mConfirmTextView.setTextColor(getResources().getColor(R.color.encrypt_select_title_grayed_color));
            } else {
                mConfirmTextView.setEnabled(true);
                mConfirmTextView.setOnClickListener(this);
                mConfirmTextView.setTextColor(getTitleTextColor());
            }
        }
    }

    private void updateSelectCountViewText() {
        int count = mNoteSelectionManager.getSelectedCount();

        String format = getResources().getQuantityString(
                R.plurals.number_of_items_selected, count);
        if (null != mSelectCountView) {
            mSelectCountView.setText(String.format(format, count));
        }
    }
}
