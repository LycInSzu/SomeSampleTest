package com.prize.ui;

import java.util.ArrayList;
import java.util.List;

import com.android.fmradio.FmStation;
import com.android.fmradio.FmUtils;
import com.android.fmradio.R;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {

	protected static final String TAG = "ChannelAdapter";
	
	private int mFavoriteOffset = 1;
	private int mChannelOffset = 2;
	
	private boolean mIsEditMode;
	private List<StationBean> mDeList;
	public interface OnChannelLitener {
		void onPlay(StationBean stationBean);
		void onShowRenameDialog(StationBean stationBean);
		void onCancelFavorite(StationBean stationBean);
		void onSelectChange(int number, int size);
	}

	private OnChannelLitener mOnChannelLitener;;

	public void setOnChannelLitener(OnChannelLitener onChannelLitener) {
		this.mOnChannelLitener = onChannelLitener;
	}

	private LayoutInflater mInflater;
	private StationList mStationList;
	
	private Cursor mCursor;
	private Context mContext;

	public ChannelAdapter(Context context, StationList stationList) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mStationList = stationList;
		mDeList = new ArrayList<StationBean>();
	}
	
	public ChannelAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mStationList = new StationList();
		mDeList = new ArrayList<StationBean>();
	}
	
	public ChannelAdapter(Context context, Cursor cursor) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mStationList = new StationList();
		mDeList = new ArrayList<StationBean>();
		swapCursor(cursor);
	}
	
	private class CursorContentObserver extends ContentObserver {

		public CursorContentObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (mCursor.isClosed()) {
				return;
			}
			mCursor.requery();
		}

	}

	private void refreshData() {
		if (mCursor == null || mCursor.isClosed()) {
			return;
		}
		mCursor.requery();
		updateData();
	}

	private void updateData() {
		if (mCursor == null) {
			mStationList.clearAll();
		} else {
			mStationList = StationUtil.getStationList(mContext, mCursor);
		}
		notifyDataSetChanged();
	}
	
	public void swapCursor(Cursor cursor) {
		mCursor = cursor;
		if (mCursor != null) {
			mCursor.registerContentObserver(new CursorContentObserver());
		}
		updateData();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View arg0) {
			super(arg0);
		}

		TextView mStationFreqView;
		TextView mStationNameView;
		CheckBox mStationSelect;
		ImageView mStationFavoriteIm;
		TextView mTitleTv;
		View mTitleView;
		View mContentView;
	}

	@Override
	public int getItemCount() {
		int count = mStationList.getSize() + 2;
		LogTools.i(TAG, "getItemCount count=" + count);
		return count;
	}
	
	public void editMode() {
		mIsEditMode = true;
		mDeList.clear();
	}
	
	public void setEditMode(boolean editMode) {
		mIsEditMode = editMode;
	}
	
	/**
	 * 
	 * Cancel the clear pattern
	 */
	public void clearEditMode() {
		mIsEditMode = false;
		mDeList.clear();
	}
	
	public boolean isEditMode() {
		return mIsEditMode;
	}
	
	public boolean isAllSelect() {
		return mDeList.size() == mStationList.getSize();
	}
	
	/**
	 * 
	 * Access to remove the id of the queue
	 */
	public List<StationBean> getDelStationBeans() {
		return mDeList;
	}
	
	public void renameStation() {
		if (mOnChannelLitener != null) { // rename
			mOnChannelLitener.onShowRenameDialog(mDeList.get(0));
		}
	}
	
	/**
	 * 
	 * Id selection, add all the data
	 */
	public void switchSelectAll() {
		if (mDeList.size() == mStationList.getSize()) {
			unSelectAll();
		} else {
			selectAll();
		}
		if (mOnChannelLitener != null) { 
			mOnChannelLitener.onSelectChange(mDeList.size(), mStationList.getSize());
		}
	}
	
	private void selectAll() {
		mDeList.clear();
		mDeList.addAll(mStationList.getStationBeanList());
		notifyDataSetChanged();
	}

	private void unSelectAll() {
		mDeList.clear();
		notifyDataSetChanged();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View view = mInflater.inflate(R.layout.simpleadapter_prize, viewGroup,
				false);
		ViewHolder viewHolder = new ViewHolder(view);
		viewHolder.mTitleView = view
				.findViewById(R.id.view_title);
		viewHolder.mContentView = view
				.findViewById(R.id.view_content);
		viewHolder.mStationFreqView = (TextView) view
				.findViewById(R.id.lv_station_freq);
		viewHolder.mStationNameView = (TextView) view
				.findViewById(R.id.lv_station_name);
		viewHolder.mStationFavoriteIm = (ImageView) view
				.findViewById(R.id.btn_cancle_favorite);
		viewHolder.mStationSelect = (CheckBox) view
				.findViewById(R.id.fm_station_choose);
		viewHolder.mTitleTv = (TextView) view.findViewById(R.id.tv);
		return viewHolder;
	}
	
	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
		LogTools.i(TAG, "onBindViewHolder i=" + i);
		if (i == mStationList.getFavoriteSize() + 1) {
			viewHolder.mContentView.setVisibility(View.GONE);
			viewHolder.mTitleView.setVisibility(View.VISIBLE);
			viewHolder.mTitleTv.setText(R.string.channel_title);
			viewHolder.itemView.setEnabled(false);
		} else if (i == 0) {
			viewHolder.mContentView.setVisibility(View.GONE);
			viewHolder.mTitleView.setVisibility(View.VISIBLE);
			viewHolder.mTitleTv.setText(R.string.favorite_title);
			viewHolder.itemView.setEnabled(false);
		} else {
			viewHolder.mTitleView.setVisibility(View.GONE);
			viewHolder.mContentView.setVisibility(View.VISIBLE);
			final StationBean stationBean;
			if (i <= mStationList.getFavoriteSize()) {
				stationBean = mStationList.getStationBean(i - mFavoriteOffset);
			} else {
				stationBean = mStationList.getStationBean(i - mChannelOffset);
			}
			LogTools.i(TAG, "onBindViewHolder stationBean=" + stationBean);
			viewHolder.mStationFreqView.setText(FmUtils
					.formatStation(stationBean.getStationFreq()));
			viewHolder.mStationNameView.setText(stationBean.getStationName());
			if (mIsEditMode) { // Clear pattern
				viewHolder.mStationFavoriteIm.setVisibility(View.GONE);
				viewHolder.mStationSelect.setVisibility(View.VISIBLE);
			} else{
				if (stationBean.isFavorite()) {
					viewHolder.mStationFavoriteIm.setVisibility(View.VISIBLE);
				} else {
					viewHolder.mStationFavoriteIm.setVisibility(View.GONE);
				}
				viewHolder.mStationSelect.setVisibility(View.GONE);
			}
			
			if (mDeList.contains(stationBean)) { // To be selected to remove
				viewHolder.mStationSelect.setChecked(true);
			} else {
				viewHolder.mStationSelect.setChecked(false);
			}
			
			viewHolder.mStationFavoriteIm.setOnClickListener(new OnClickListener() { 
				@Override
				public void onClick(View v) {
					if (mOnChannelLitener != null) { // rename
						mOnChannelLitener.onCancelFavorite(stationBean);
					}
				}
			});
			
			viewHolder.itemView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					if (mIsEditMode) { // Clear pattern, select the entire line in accordance with operating check box
						boolean old = viewHolder.mStationSelect.isChecked();
						viewHolder.mStationSelect.setChecked(!old);
						changeDeleteData(!old, stationBean);
					} else { // Jump to the corresponding channel
						if (mOnChannelLitener != null) { // rename
							mOnChannelLitener.onPlay(stationBean);
						}
					}
				}
			});
		}
	}
	
	private void changeDeleteData(boolean isDelete, final StationBean stationBean) {
		if (isDelete) { // Added to remove the queue
			if (!mDeList.contains(stationBean)) {
				mDeList.add(stationBean);
			}
		} else { // Remove from the clear queue
			if (mDeList.contains(stationBean)) {
				mDeList.remove(stationBean);
			}
		}
		if (mOnChannelLitener != null) { // rename
			mOnChannelLitener.onSelectChange(mDeList.size(), mStationList.getSize());
		}
	}
}
