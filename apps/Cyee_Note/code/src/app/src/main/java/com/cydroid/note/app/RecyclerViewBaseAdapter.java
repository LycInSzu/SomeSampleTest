package com.cydroid.note.app;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import com.gionee.amiweather.library.WeatherData;
import com.cydroid.note.R;
import com.cydroid.note.app.attachment.LocalImageLoader;
import com.cydroid.note.app.effect.EffectUtil;
import com.cydroid.note.app.utils.WeatherIconHelp;
import com.cydroid.note.app.view.MarqueeTextView;
import com.cydroid.note.app.view.NoteCardBottomView;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.ThumbnailDecodeProcess;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteSet;
import com.cydroid.note.data.Path;
import com.cydroid.note.app.utils.PackageUtils;
import java.text.SimpleDateFormat;

import cyee.widget.CyeeCheckBox;
import cyee.widget.CyeeTextView;

/**
 * Created by spc on 16-7-11.
 */
public class RecyclerViewBaseAdapter extends RecyclerView.Adapter<NoteViewHolder> implements
        View.OnClickListener, View.OnLongClickListener {

    private static final int MESSAGE_CONTENT_CHANGE = 1;
	//Cyee wanghaiyan 2017-10-27 modify for SW17W16A-423 begin
    private static final int MESSAGE_COUNT_CHANGE =2;
	//Cyee wanghaiyan 2017-10-27 modify for SW17W16A-423 end	
    private static final String PACKAGE_NAME_WEATHER_INNER = "com.coolwind.weather";
    protected static final int TYPE_HEADER = 2;

    protected LayoutInflater mLayoutInflater;
    protected SlidingWindow mDataWindow;
    protected Handler mMainHandler;
    protected int mCount = 0;
    protected NoteSelectionManager mNoteSelectionManager;
    protected OnTouchListener mOnTouchListener;
    protected EffectUtil mEffectUtil;
    protected int[] mCurDate;
    protected LocalImageLoader mImageLoad;
    protected boolean mDisplayHeader=false;
    private String mUnkownTip;
    private RelateWeatherAndCalendar mWeatherAndCalendar;
    private WeatherData mWeatherInfo;
    private Drawable mRemindNotRead;
    private Drawable mRemind;
    private Activity mContext;
    private int mTimeColor;

    public interface OnTouchListener {
        void onSingleClickTouch(Path path);

        void onLongClickTouch(Path path);
    }

    public RecyclerViewBaseAdapter(Activity activity, NoteSet set,
                                   LoadingListener loadingListener,
                                   NoteSelectionManager noteSelectionManager,
                                   boolean displayHeader) {
        mContext = activity;
	    //Cheeyee wanghaiyan 2017-10-13 modify for 235937 begin
	    try {
	        Intent intent = getWeatherActivityIntent();
	       		if(intent != null) {
	            	mDisplayHeader = displayHeader;
		 }else{
	            	mDisplayHeader = false;
	    }
	    	} catch (Exception e) {
	        e.printStackTrace();
        }
	    //Cheeyee wanghaiyan 2017-10-13 modify for 235937 end
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
		            case MESSAGE_COUNT_CHANGE: {
		                 mCount =msg.arg1;
                        notifyDataSetChanged();
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        };
        mRemindNotRead = ContextCompat.getDrawable(NoteAppImpl.getContext(), R.drawable.note_item_remind_not_read);
        mRemind = ContextCompat.getDrawable(NoteAppImpl.getContext(), R.drawable.note_item_reminder);

        if (mDisplayHeader) {
            mWeatherAndCalendar = new RelateWeatherAndCalendar(mListener);
            mUnkownTip = activity.getResources().getString(R.string.unkown);
        }
    }

   //Cheeyee wanghaiyan 2017-10-13 modify for 235937 begin
   public Intent getWeatherActivityIntent() {
        Intent rtnIntent = PackageUtils.getAppLaunchIntent(mContext, PACKAGE_NAME_WEATHER_INNER);
        if (rtnIntent != null) {
        rtnIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        rtnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return rtnIntent;
    }
	//Cheeyee wanghaiyan 2017-10-13 modify for 235937 end
    private RelateWeatherAndCalendar.OnRelateDataListener mListener = new RelateWeatherAndCalendar.OnRelateDataListener() {
        @Override
        public void onRelateDataFinished(WeatherData situation) {
            final WeatherData info = situation;
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWeatherInfo = info;
		     if(mCount >0) {
                   	 notifyDataSetChanged();
		     }
                }
            });
        }
    };

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        NoteViewHolder holder = null;
        if (mDisplayHeader) {
            holder = createViewWithHeader(viewGroup, viewType);
        } else {
            holder = createHolder(viewGroup, viewType);
        }
        return holder;
    }

    private NoteViewHolder createViewWithHeader(ViewGroup viewGroup, int viewType) {
        NoteViewHolder holder = null;
        if (viewType == TYPE_HEADER) {
            holder = createHeader(viewGroup);
        } else {
            holder = createHolder(viewGroup, viewType);
        }
        return holder;
    }

    private NoteViewHolder createHeader(ViewGroup viewGroup) {
        View noteItem = mLayoutInflater.inflate(R.layout.show_weather_calendar_ly, viewGroup, false);
        NoteViewHolder holder = new NoteViewHolder(noteItem);
        noteItem.findViewById(R.id.weather_container).setOnClickListener(this);
        holder.mCity = (MarqueeTextView) noteItem.findViewById(R.id.weather_city);
        holder.mCity.setTextColor(ColorThemeHelper.getContentNormalTextColor(mContext, false));
        holder.mWeather = (ImageView) noteItem.findViewById(R.id.weather_icon);
        holder.mWeatherType = (CyeeTextView) noteItem.findViewById(R.id.weather_type);
        holder.mWeatherType.setTextColor(ColorThemeHelper.getContentNormalTextColor(mContext, false));
        holder.mTemperature = (CyeeTextView) noteItem.findViewById(R.id.weather_temperature);
        holder.mTemperature.setTextColor(ColorThemeHelper.getContentNormalTextColor(mContext, false));
        return holder;
    }

    private NoteViewHolder createHolder(ViewGroup viewGroup, int viewType) {
        View noteItem = mLayoutInflater.inflate(getLayoutId(viewType), viewGroup, false);
        NoteViewHolder holder = new NoteViewHolder(noteItem);
        View container = noteItem.findViewById(R.id.note_item_content_onclick_view);
        container.setTag(holder);
        container.setOnClickListener(this);
        container.setOnLongClickListener(this);
        holder.mTitle = (CyeeTextView) noteItem.findViewById(R.id.note_item_title);
        holder.mContent = (CyeeTextView) noteItem.findViewById(R.id.note_item_content);
        holder.mTime = (CyeeTextView) noteItem.findViewById(R.id.note_item_time);
        holder.mTime.setTextColor(mTimeColor);
        holder.mReminder = (ImageView) noteItem.findViewById(R.id.note_item_reminder);
		//GIONEE wanghaiyan 2016-12-20 modify for 50571 begin
        holder.mCheckBox = (CheckBox) noteItem.findViewById(R.id.note_item_checkbox);
		//GIONEE wanghaiyan 2016-12-20 modify for 50571 end
        holder.mImage = (viewType == NoteItem.MEDIA_TYPE_NONE ? null :
                (ImageView) noteItem.findViewById(R.id.note_item_image));
        holder.mNoteCardBottomView = (NoteCardBottomView) noteItem.findViewById(R.id.note_item_card_bottom_view);
        return holder;
    }

    public void setTimeTextColor(int color) {
        mTimeColor = color;
    }

    @Override
    public void onBindViewHolder(NoteViewHolder noteViewHolder, int position) {
        if (mDisplayHeader) {
            bindViewHolderWidthHeader(noteViewHolder, position);
        } else {
            bindNormalViewHolder(noteViewHolder, position);
        }
    }

    private void bindViewHolderWidthHeader(NoteViewHolder noteViewHolder, int position) {
        if (position == 0) {
            bindHeader(noteViewHolder);
        } else {
            bindNormalViewHolder(noteViewHolder, --position);
        }
    }

    private void bindHeader(NoteViewHolder noteViewHolder) {
        if (null != mWeatherInfo) {
            setViewText(noteViewHolder.mCity, mWeatherInfo.getCityName());
            setViewText(noteViewHolder.mWeatherType, mWeatherInfo.getForecastState());
            setViewText(noteViewHolder.mTemperature, mWeatherInfo.getForecastTemperatureWithUnit());
            int drawableId = WeatherIconHelp.getWeatherHelper(mWeatherInfo.getForecastStateInt(),
                    WeatherIconHelp.getTimeSection());
            Drawable drawable = ContextCompat.getDrawable(mContext.getApplicationContext(), drawableId);
            noteViewHolder.mWeather.setImageDrawable(drawable);
        } else {
            setViewText(noteViewHolder.mCity, mUnkownTip);
            setViewText(noteViewHolder.mWeatherType, mUnkownTip);
            setViewText(noteViewHolder.mTemperature, mUnkownTip);
            Drawable defaultDrawable = ContextCompat.getDrawable(mContext.getApplicationContext(), R.drawable.widget41_icon_nodata);
            noteViewHolder.mWeather.setImageDrawable(defaultDrawable);
        }
    }

    private void setViewText(TextView textView, String value) {
        if (TextUtils.isEmpty(value)) {
            value = mUnkownTip;
        }
        textView.setText(value);
    }

    private void bindNormalViewHolder(NoteViewHolder noteViewHolder, int position) {
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
        updateTitleState(noteViewHolder.mTitle, noteEntry.title);
        noteViewHolder.mContent.setText(noteEntry.content);
        //Chenyee wanghaiyan 2017-12-1 modify for SW17W16A-2093 begin
        //noteViewHolder.mTime.setText(noteEntry.time);
        String newDate = NoteUtils.formateTime(noteEntry.time,mContext);
        noteViewHolder.mTime.setText(newDate);
        //Chenyee wanghaiyan 2017-12-1 modify for SW17W16A-2093 end
        setCardBackground(noteViewHolder, noteEntry);
        updateReminderState(noteViewHolder.mReminder, noteEntry);
        if (mNoteSelectionManager != null) {
            updateCheckBoxState(mNoteSelectionManager, noteViewHolder.mCheckBox, path);
        }
    }

    private void setCardBackground(NoteViewHolder noteViewHolder, SlidingWindow.NoteEntry noteEntry) {
        noteViewHolder.mNoteCardBottomView.setCardBg(mEffectUtil.getEffect(noteEntry.timeMillis));
    }

    @Override
    public int getItemViewType(int position) {
        if (mDisplayHeader && mCount > 0) {
            if (position == 0) {
                return TYPE_HEADER;
            }
            return getViewType(--position);
        } else {
            return getViewType(position);
        }
    }

    private int getViewType(int position) {
        SlidingWindow.NoteEntry noteEntry = mDataWindow.get(position);
        if (noteEntry == null) {
            return NoteItem.MEDIA_TYPE_NONE;
        }
        return noteEntry.mediaType;
    }

    public void resume() {
        if (null != mWeatherAndCalendar) {
            mWeatherAndCalendar.queryWeatherInfo();
        }
        checkTimeChange();
        mDataWindow.resume();
    }

    public void pause() {
        mDataWindow.pause();
    }

    public boolean isEmpty() {
        return mCount == 0;
    }

    public void notifyVisibleRangeChanged(int visibleStart, int visibleEnd) {
        mDataWindow.setActiveWindow(visibleStart, visibleEnd);
    }

    public void destroy() {
        mDataWindow.destroy();
        if (null != mWeatherAndCalendar) {
            mWeatherAndCalendar.destroy();
        }
    }

    public boolean getIsDisplayHeader() {
        return mDisplayHeader;
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

    protected void updateReminderState(ImageView reminderView, SlidingWindow.NoteEntry noteEntry) {
        if (noteEntry.reminder == NoteItem.INVALID_REMINDER) {
            reminderView.setVisibility(View.GONE);
        } else {
            Drawable drawable = null;
            boolean displayNotReadRemind = noteEntry.reminder <= System.currentTimeMillis()
                    && noteEntry.encrytRemindReadState == Constants.ENCRYPT_REMIND_NOT_READ
                    && noteEntry.isEncrypt;
            if (displayNotReadRemind) {
                drawable = mRemindNotRead;
            } else {
                drawable = mRemind;
            }
            reminderView.setImageDrawable(drawable);
            reminderView.setVisibility(View.VISIBLE);
        }
    }

    protected void updateCheckBoxState(NoteSelectionManager noteSelectionManager, CheckBox checkBox, Path path) {
        if (noteSelectionManager.inSelectionMode()) {
            checkBox.setVisibility(View.VISIBLE);
            if (noteSelectionManager.isItemSelected(path)) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
        } else {
            checkBox.setVisibility(View.GONE);
        }
    }

    private void updateTitleState(TextView titleView, String title) {
        titleView.setText(title);
        if (TextUtils.isEmpty(title)) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setVisibility(View.VISIBLE);
        }
    }

    protected int getLayoutId(int viewType) {
        return -1;
    }

    @Override
    public int getItemCount() {
        if (mDisplayHeader) {
            return mCount == 0 ? 0 : mCount + 1;
        }
        return mCount;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.weather_container:
                mWeatherAndCalendar.goToWeather(mContext);
                break;
            case R.id.note_item_content_onclick_view:
                if (mOnTouchListener != null) {
                    NoteViewHolder holder = (NoteViewHolder) view.getTag();
                    Path path = holder.mPath;
                    mOnTouchListener.onSingleClickTouch(path);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnTouchListener != null) {
            NoteViewHolder holder = (NoteViewHolder) v.getTag();
            Path path = holder.mPath;
            mOnTouchListener.onLongClickTouch(path);
        }
        return true;
    }

    public void setOnTouchListener(OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    public class MyDataModelListener implements SlidingWindow.Listener {
        @Override
        public void onContentChanged() {
            mMainHandler.sendEmptyMessage(MESSAGE_CONTENT_CHANGE);
        }

        @Override
        public void onCountChanged(int count) {
		    //Cyee wanghaiyan 2017-10-27 modify for SW17W16A-423 begin
            //mCount = count;
            //notifyDataSetChanged();
            Message message = mMainHandler.obtainMessage(MESSAGE_COUNT_CHANGE);
            message.arg1 = count;
	        mMainHandler.sendMessage(message);
			//Cyee wanghaiyan 2017-10-27 modify for SW17W16A-423 end
        }
    }
}
