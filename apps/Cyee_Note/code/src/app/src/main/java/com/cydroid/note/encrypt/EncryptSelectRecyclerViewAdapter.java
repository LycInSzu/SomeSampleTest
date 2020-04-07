package com.cydroid.note.encrypt;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.LoadingListener;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.NoteSelectionManager;
import com.cydroid.note.app.NoteViewHolder;
import com.cydroid.note.app.SlidingWindow;
import com.cydroid.note.app.attachment.LocalImageLoader;
import com.cydroid.note.app.effect.EffectUtil;
import com.cydroid.note.app.view.NoteCardBottomView;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.ThumbnailDecodeProcess;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteSet;
import com.cydroid.note.data.Path;

import cyee.widget.CyeeCheckBox;
import cyee.widget.CyeeTextView;

/**
 * Created by wuguangjie on 16-6-2.
 */
public class EncryptSelectRecyclerViewAdapter extends RecyclerView.Adapter<NoteViewHolder> implements View.OnClickListener {
    private static final int MESSAGE_CONTENT_CHANGE = 1;

    private LayoutInflater mLayoutInflater;
    private SlidingWindow mDataWindow;
    private Handler mMainHandler;
    private int mCount = 0;
    private int[] mCurDate;
    private EffectUtil mEffectUtil;
    private LocalImageLoader mImageLoad;
    private OnSingleClickTouchListener mOnTouchListener;
    private NoteSelectionManager mNoteSelectionManager;
    private int mDisplayMode;

    public EncryptSelectRecyclerViewAdapter(Activity activity, NoteSet set,
                                            LoadingListener loadingListener,
                                            NoteSelectionManager noteSelectionManager,
                                            int displayMode) {
        mDisplayMode = displayMode;
        mImageLoad = new LocalImageLoader(NoteAppImpl.getContext());
        mImageLoad.setLoadingImage(R.drawable.note_card_default_image);

        mEffectUtil = new EffectUtil(System.currentTimeMillis());
        mNoteSelectionManager = noteSelectionManager;
        mLayoutInflater = LayoutInflater.from(activity);
        mDataWindow = new SlidingWindow(activity, set, loadingListener);
        mDataWindow.setListener(new MyDataModelListener());
        mMainHandler = new Handler(activity.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_CONTENT_CHANGE: {
                        notifyDataSetChanged();
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        };
    }

    public interface OnSingleClickTouchListener {
        void onSingleClickTouch(Path path);
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutId = getLayoutId(viewType);
        View noteItem = mLayoutInflater.inflate(layoutId, viewGroup, false);
        NoteViewHolder holder = new NoteViewHolder(noteItem);
        View container = noteItem.findViewById(R.id.note_item_content_onclick_view);
        container.setTag(holder);
        container.setOnClickListener(this);
        holder.mTitle = (CyeeTextView) noteItem.findViewById(R.id.note_item_title);
        holder.mContent = (CyeeTextView) noteItem.findViewById(R.id.note_item_content);
        holder.mTime = (CyeeTextView) noteItem.findViewById(R.id.note_item_time);
        holder.mReminder = (ImageView) noteItem.findViewById(R.id.note_item_reminder);
        //GIONEE wanghaiyan 2016-12-20 modify for 50571 begin
        holder.mCheckBox = (CheckBox) noteItem.findViewById(R.id.note_item_checkbox);
        //GIONEE wanghaiyan 2016-12-20 modify for 50571 end
        holder.mCheckBox.setVisibility(View.VISIBLE);
        holder.mImage = (viewType == NoteItem.MEDIA_TYPE_NONE ? null :
                (ImageView) noteItem.findViewById(R.id.note_item_image));
        holder.mNoteCardBottomView = (NoteCardBottomView) noteItem.findViewById(R.id.note_item_card_bottom_view);
        return holder;
    }

    @Override
    public void onBindViewHolder(NoteViewHolder noteViewHolder, int position) {
        SlidingWindow.NoteEntry noteEntry = mDataWindow.get(position);
        if (noteEntry == null || noteEntry.item == null) {
            return;
        }
        ImageView imageView = noteViewHolder.mImage;
        if (imageView != null) {
            mImageLoad.loadImage(noteEntry, imageView,
                    ThumbnailDecodeProcess.ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT);
        }
        Path path = noteEntry.path;
        noteViewHolder.mPath = path;
        noteViewHolder.mContent.setText(noteEntry.content);
        noteViewHolder.mTime.setText(noteEntry.time);
        noteViewHolder.mNoteCardBottomView.setCardBg(mEffectUtil.getEffect(noteEntry.timeMillis));
        updateReminderState(noteViewHolder.mReminder, noteEntry.reminder);
        if (null != mNoteSelectionManager) {
            updateCheckBoxState(mNoteSelectionManager, noteViewHolder.mCheckBox, path);
        }
    }

    private int getLayoutId(int viewType) {
        if (mDisplayMode == Constants.NOTE_DISPLAY_GRID_MODE) {
            return (viewType == NoteItem.MEDIA_TYPE_NONE ? R.layout.note_item_no_image
                    : R.layout.note_item_have_image);
        } else {
            return (viewType == NoteItem.MEDIA_TYPE_NONE ? R.layout.list_note_item_have_no_image
                    : R.layout.list_note_item_hava_image);
        }
    }

    private void updateCheckBoxState(NoteSelectionManager noteSelectionManager, CheckBox checkBox, Path path) {
        if (noteSelectionManager.inSelectionMode()) {
            checkBox.setVisibility(View.VISIBLE);
            if (noteSelectionManager.isItemSelected(path)) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
        } else {
            checkBox.setVisibility(View.INVISIBLE);
        }
    }

    private void updateReminderState(ImageView reminderView, long reminderTime) {
        if (reminderTime == NoteItem.INVALID_REMINDER) {
            reminderView.setVisibility(View.INVISIBLE);
        } else {
            reminderView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        SlidingWindow.NoteEntry noteEntry = mDataWindow.get(position);
        if (noteEntry == null) {
            return NoteItem.MEDIA_TYPE_NONE;
        }
        return noteEntry.mediaType;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    public void resume() {
        checkTimeChange();
        mDataWindow.resume();
    }

    public void pause() {
        mDataWindow.pause();
    }

    public void destroy() {
        mDataWindow.destroy();
    }

    private void checkTimeChange() {
        if (mCurDate == null) {
            mCurDate = NoteUtils.getToady();
            return;
        }
        int[] newCurDate = NoteUtils.getToady();
        boolean isSomeDay = NoteUtils.isSomeDay(newCurDate, mCurDate);
        if (!isSomeDay) {
            mCurDate = newCurDate;
            mEffectUtil = new EffectUtil(System.currentTimeMillis());
            notifyDataSetChanged();
        }
    }

    public void notifyVisibleRangeChanged(int visibleStart, int visibleEnd) {
        mDataWindow.setActiveWindow(visibleStart, visibleEnd);
    }

    @Override
    public void onClick(View view) {
        if (mOnTouchListener != null) {
            NoteViewHolder holder = (NoteViewHolder) view.getTag();
            Path path = holder.mPath;
            mOnTouchListener.onSingleClickTouch(path);
        }
    }

    public void setOnTouchListener(OnSingleClickTouchListener listener) {
        mOnTouchListener = listener;
    }

    public class MyDataModelListener implements SlidingWindow.Listener {
        @Override
        public void onContentChanged() {
            mMainHandler.sendEmptyMessage(MESSAGE_CONTENT_CHANGE);
        }

        @Override
        public void onCountChanged(int count) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}
