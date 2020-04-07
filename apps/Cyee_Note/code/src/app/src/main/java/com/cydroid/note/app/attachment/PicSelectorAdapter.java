package com.cydroid.note.app.attachment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.cydroid.note.R;
import com.cydroid.note.app.NewNoteActivity;
//import com.cydroid.note.app.utils.LogSwitch;
import com.cydroid.note.app.view.AttachPicRecycleView;
import com.cydroid.note.app.view.PicSelectorItemView;
import com.cydroid.note.common.ThumbnailDecodeProcess;
//Chenyee wanghaiyan 2018-6-13 modify for CSW1703CX-1072 begin
import com.cydroid.note.common.NoteUtils;
//Chenyee wanghaiyan 2018-6-13 modify for CSW1703CX-1072 end

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.cydroid.note.app.NoteAppImpl;

public class PicSelectorAdapter extends RecyclerView.Adapter<PicSelectorAdapter.AttachPicViewHolder>
        implements View.OnClickListener {

    private static final String TAG = "PicSelAdapter";
    private Activity mContext;
    private LocalImageLoader mLocalImageLoader;
    private CopyOnWriteArrayList<PicInfo> mAttachPicInfos;//NOSONAR
    private LayoutInflater mLayoutInflater;
    private int mVisibleStart;
    private int mVisibleEnd;
    private int mPreLoadPicWith;
    private int mPreloadPicHeight;
    private CopyOnWriteArrayList<String> mSelectedPicUris = new CopyOnWriteArrayList();
    private Drawable mPicSelectedDrawable;
    private Drawable mPicUnSelecteDrawable;
    private static final int GO_TO_GALLERY = -1;
    private SelectionPicturesListener mListener;
    private Handler mHandler;

    public interface SelectionPicturesListener {
        void onSelectionChanged(CopyOnWriteArrayList<String> selectedPicUris);
    }

    public PicSelectorAdapter(Activity context, CopyOnWriteArrayList<PicInfo> attachInfos, SelectionPicturesListener listener) {
        mContext = context;
        Resources resources = context.getResources();
        mListener = listener;
        mAttachPicInfos = attachInfos;//NOSONAR
        mLocalImageLoader = new LocalImageLoader(mContext);
        mLocalImageLoader.setLoadErrorListener(mPicLoadErrorListener);
        mLayoutInflater = LayoutInflater.from(mContext);
        mPreLoadPicWith = resources.getDimensionPixelOffset
                (R.dimen.attach_selector_pic_default_widht);
        mPreloadPicHeight = resources.getDimensionPixelOffset
                (R.dimen.attach_selector_pic_height);
	    //Gionee wanghaiyan 2017-5-25 modify for 123857 begin
        //mPicSelectedDrawable = ResourcesCompat.getDrawable(resources, R.drawable.pic_selected, null);
        //mPicUnSelecteDrawable = ResourcesCompat.getDrawable(resources, R.drawable.pic_unselected, null);
        mPicSelectedDrawable = resources.getDrawable(R.drawable.pic_selected);
	    mPicUnSelecteDrawable = resources.getDrawable(R.drawable.pic_unselected);
	    //Gionee wanghaiyan 2017-5-25 modify for 123857 end
        mHandler = new Handler(context.getMainLooper());
    }

    @Override
    public PicSelectorAdapter.AttachPicViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType) {
        View item = mLayoutInflater.inflate(R.layout.pic_selector_item, parent, false);
        PicSelectorItemView container = (PicSelectorItemView) item.findViewById
                (R.id.pic_selector_item_view);
        AttachPicViewHolder holder = new AttachPicViewHolder(item);
        container.setTag(holder);
        container.setOnClickListener(this);
        holder.pic = container.getImageView();
        holder.selectBox = container.getCheckBox();
        holder.selectBox.setOnClickListener(this);
        holder.selectBox.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(AttachPicViewHolder holder, int position) {
        if (position == 0) {
        	//Gionee wanghaiyan 2017-5-25 modify for 123857 begin
            //Drawable drawable = ResourcesCompat.getDrawable
            //(NoteAppImpl.getContext().getResources(), R.drawable.go_gallery, null);
            //Chenyee wanghaiyan 2018-6-13 modify for CSW1703CX-1072 begin
            Drawable drawable = null;
            if(NoteUtils.gnXLJFlag){
                drawable =NoteAppImpl.getContext().getResources().getDrawable(R.drawable.go_gallery_zh);
            }else{
                drawable =NoteAppImpl.getContext().getResources().getDrawable(R.drawable.go_gallery);
            }
            //Chenyee wanghaiyan 2018-6-13 modify for CSW1703CX-1072 end
	        //Gionee wanghaiyan 2017-5-25 modify for 123857 end
            holder.pic.setImageDrawable(drawable);
            ViewGroup.LayoutParams params = holder.pic.getLayoutParams();
            params.width = drawable.getIntrinsicWidth();
            holder.pic.setLayoutParams(params);
            holder.selectBox.setVisibility(View.GONE);
        } else {
            holder.selectBox.setVisibility(View.VISIBLE);
            PicInfo info = mAttachPicInfos.get(position - 1);
            if (info == null) {
                return;
            }
            String picUri = info.uri;
            if (!TextUtils.isEmpty(picUri)) {
                mLocalImageLoader.loadImage(picUri, holder.pic,
                        ThumbnailDecodeProcess.ThumbnailDecodeMode.HEIGHT_FIXED_WIDTH_SCALE);
            }
            if (info.isSelected) {
                holder.selectBox.setImageDrawable(mPicSelectedDrawable);
            } else {
                holder.selectBox.setImageDrawable(mPicUnSelecteDrawable);
            }
        }
        holder.postion = position - 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mAttachPicInfos.size() + 1;
    }

    @Override
    public void onClick(View v) {
        AttachPicViewHolder selectHolder = (AttachPicViewHolder) (v.getTag());
        int position = selectHolder.postion;
        if (position == GO_TO_GALLERY) {
            gotoPickPics();
        } else {
            PicInfo selectInfo = mAttachPicInfos.get(position);
            selectInfo.isSelected = selectInfo.isSelected ? false : true;
            if (selectInfo.isSelected && !mSelectedPicUris.contains(selectInfo.uri)) {
                //if (LogSwitch.DEBUG) {
                //    Log.i(LogSwitch.TAG + TAG, "sel uri = " + selectInfo.uri);
               // }
                mSelectedPicUris.add(selectInfo.uri);
            } else {
                mSelectedPicUris.remove(selectInfo.uri);
            }
            if (mListener != null) {
                mListener.onSelectionChanged(mSelectedPicUris);
            }
            notifyDataSetChanged();
        }
    }

    private void gotoPickPics() {
        //Chenyee wanghaiyan 2018-8-1 modify for CSW1705P-68 begin
        Intent intent = new Intent("com.cydroid.gallery.intent.action.GET_CONTENT");
        //Chenyee wanghaiyan 2018-8-1 modify for CSW1705P-68 end
        intent.setType("image/*");
        if (!isInstalledAPK(intent)) {
            Intent newIntent = new Intent(Intent.ACTION_GET_CONTENT);
            newIntent.setType("image/*");
            intent = Intent.createChooser(newIntent, null);
        }
        try {
            mContext.startActivityForResult(intent, NewNoteActivity.REQUEST_PICK_IMAGE);
        } catch (ActivityNotFoundException e) {
        }
    }

    public boolean isInstalledAPK(Intent intent) {
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (null != resolveInfo && resolveInfo.size() > 0) {
            return true;
        }
        return false;
    }

    public CopyOnWriteArrayList getSelectedPicUris() {
        return mSelectedPicUris;
    }

    public void clearSelectedPicUris() {
        mSelectedPicUris.clear();
        mLocalImageLoader.setLoadErrorListener(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    public void preLoadPic(int preLoadDirection) {
        if (preLoadDirection == AttachPicRecycleView.SCROLL_LEFT) {
            preLoadPre();
        } else if (preLoadDirection == AttachPicRecycleView.SCROLL_RIGHT) {
            preLoadNext();
        }
    }

    private void preLoadPre() {
        if (mVisibleStart <= 0) {
            return;
        }
        int preLoadStart = mVisibleStart;
        for (int i = 0; i < 5; i++) {
            preLoadStart--;
            if (preLoadStart <= 0) {
                break;
            }
            mLocalImageLoader.preLoadImage(mAttachPicInfos.get(preLoadStart).uri, mPreLoadPicWith,
                    mPreloadPicHeight);
        }
    }

    private void preLoadNext() {
        if (mVisibleEnd >= mAttachPicInfos.size() - 1) {
            return;
        }
        int preLoadStart = mVisibleEnd;
        for (int i = 0; i < 5; i++) {
            preLoadStart++;
            if (preLoadStart >= mAttachPicInfos.size() - 1) {
                break;
            }
            mLocalImageLoader.preLoadImage(mAttachPicInfos.get(preLoadStart).uri, mPreLoadPicWith,
                    mPreloadPicHeight);
        }
    }

    public void notifyVisibleRangeChanged(int visibleStart, int visibleEnd) {
        mVisibleStart = visibleStart;
        mVisibleEnd = visibleEnd;
    }
    private LocalImageLoader.PicLoadErrorListener mPicLoadErrorListener = new LocalImageLoader.PicLoadErrorListener() {
        @Override
        public void onLoadError(final String picUrl) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAttachPicInfos.remove(picUrl);
                    notifyDataSetChanged();
                }
            });
        }
    };

    static class AttachPicViewHolder extends RecyclerView.ViewHolder {
        ImageView pic;
        ImageView selectBox;
        int postion;

        public AttachPicViewHolder(View itemView) {
            super(itemView);
        }
    }
}
