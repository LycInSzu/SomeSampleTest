package com.cydroid.note.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
//gionee wanghaiyan 2016-10-11 added for 41382 start
import android.text.format.Time;
//gionee wanghaiyan 2016-10-11 added for 41382 end
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.data.NoteItem;

import java.lang.reflect.Field;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import cyee.changecolors.ChameleonColorManager;

import cyee.widget.CyeeDatePicker;
import cyee.widget.CyeeTimePicker;
import com.cydroid.note.common.Log;


public class DateTimeDialog extends Dialog implements View.OnClickListener, CyeeTimePicker.OnTimeChangedListener {
	private static final String TAG = "DateTimeDialog";
    private Context mContext;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private CyeeDatePicker mDatePicker;
    private CyeeTimePicker mTimePicker;
    private Drawable mSelectedDrawable;
    private Drawable mDefaultDrawable;
    private Calendar mCalendar;
    private DateFormatSymbols mDfSymbols;
    private Date mDate;

    private int mYear;
    private int mMonthOfYear;
    private int mDayOfMonth;
    private int mHourOfDay;
    private int mMinute;
    private int mSelectedTextColor;
    private int mDefaultTextColor;
    private boolean mIs24HourView;

    private final OnDateTimeSetListener mDateTimeCallback;

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    public interface OnDateTimeSetListener {
        /*
         * view The view associated with this listener. year The year that was set monthOfYear The month that was set (0-11) for
         * compatibility dayOfMonth The day of the month that was set. hourOfDay The hour that was set. minute The minute that was
         * set.
         */
        void onDateTimeSet(Calendar calendar);

        void onDataTimeDelete();
    }

    public DateTimeDialog(Context context, int theme, long time, OnDateTimeSetListener dateTimeCallBack) {
        super(context, theme);
        setCanceledOnTouchOutside(true);
        mContext = context;
        mDfSymbols = new DateFormatSymbols();
        mDateTimeCallback = dateTimeCallBack;
        Calendar calendar = Calendar.getInstance();
        if (time != NoteItem.INVALID_REMINDER) {
            calendar.setTimeInMillis(time);
        }
        mYear = calendar.get(Calendar.YEAR);
        mMonthOfYear = calendar.get(Calendar.MONTH);
        mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        mIs24HourView = DateFormat.is24HourFormat(context);
        mCalendar = calendar;

        mSelectedTextColor = ContextCompat.getColor(context, R.color.system_stress_color);
        mDefaultTextColor = ContextCompat.getColor(context, R.color.datetime_title_text_normal_color);
        mSelectedDrawable = ContextCompat.getDrawable(context, R.drawable.datetime_select_bg);
        mDefaultDrawable = ContextCompat.getDrawable(context, R.drawable.datetime_light_bg);

        View view = LayoutInflater.from(mContext).inflate(R.layout.date_time_picker_dialog, null);

        initTextView(view, time);
        if (ChameleonColorManager.isNeedChangeColor()) {
            view.setBackgroundColor(ChameleonColorManager.getPopupBackgroudColor_B2());
            mTimeTextView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
        }
        setContentView(view);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
    }

    private void initTextView(View view, long time) {
        mDateTextView = (TextView) view.findViewById(R.id.reminder_date_text);
        mDate = mCalendar.getTime();
        mDateTextView.setText(formatDate());
        mDateTextView.setBackground(mSelectedDrawable);
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDatePicker.setVisibility(View.VISIBLE);
                mTimePicker.setVisibility(View.GONE);
                mDateTextView.setBackground(mSelectedDrawable);
                mTimeTextView.setBackground(mDefaultDrawable);
                mDateTextView.setTextColor(mSelectedTextColor);
				//GIONEE wanghaiyan 2016-12-07 modify for 41541 begin
				if(ChameleonColorManager.isNeedChangeColor()){
                   mTimeTextView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
				}else{
				   mTimeTextView.setTextColor(mDefaultTextColor);
				}
				//GIONEE wanghaiyan 2016-12-07 modify for 41541 end
            }
        });

        mTimeTextView = (TextView) view.findViewById(R.id.reminder_time_text);
        mTimeTextView.setText(formatTime());
        mTimeTextView.setBackground(mDefaultDrawable);
        mTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePicker.setVisibility(View.GONE);
                mTimePicker.setVisibility(View.VISIBLE);
                mDateTextView.setBackground(mDefaultDrawable);
                mTimeTextView.setBackground(mSelectedDrawable);
				//GIONEE wanghaiyan 2016-12-07 modify for 41541 begin
				if(ChameleonColorManager.isNeedChangeColor()){
                   mDateTextView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
                } else {
				   mDateTextView.setTextColor(mDefaultTextColor);
				}
				//GIONEE wanghaiyan 2016-12-07 modify for 41541 end
                mTimeTextView.setTextColor(mSelectedTextColor);
            }
        });

        mDatePicker = (CyeeDatePicker) view.findViewById(R.id.reminder_datePicker);
        mDatePicker.init(mYear, mMonthOfYear, mDayOfMonth, new DateChangedListener());
        mCalendar.clear();
        //gionee wanghaiyan 2016-10-11 added for 41382 start
        mDatePicker.setMinDate(System.currentTimeMillis());
      	mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
      	Time maxTime=new Time();
      	maxTime.set(59,59,23,31,11,2037);//2037/12/31
      	long maxDate=maxTime.toMillis(false);
      	maxDate=maxDate+999;//in millsec
      	mDatePicker.setMaxDate(maxDate);
      	//gionee wanghaiyan 2016-10-11 added for 41382 end
        mCalendar.set(mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute);

        mTimePicker = (CyeeTimePicker) view.findViewById(R.id.reminder_timePicker);
        mTimePicker.setIs24HourView(mIs24HourView);
        mTimePicker.setCurrentHour(mHourOfDay);
        mTimePicker.setCurrentMinute(mMinute);
        mTimePicker.setOnTimeChangedListener(this);

        if (time != NoteItem.INVALID_REMINDER) {
            TextView deleteButton = (TextView) view.findViewById(R.id.reminder_delete);
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(this);
        }
        TextView cancelButton = (TextView) view.findViewById(R.id.reminder_cancel);
        TextView sureButton = (TextView) view.findViewById(R.id.reminder_sure);
        if (ChameleonColorManager.isNeedChangeColor()) {
            cancelButton.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
        }
        cancelButton.setOnClickListener(this);
        sureButton.setOnClickListener(this);

    }

    private void updateDate() {
        mCalendar.set(mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute);
        mDate = mCalendar.getTime();
    }

    private class DateChangedListener implements CyeeDatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(CyeeDatePicker view, int year, int monthOfYear, int dayOfMonth) {
            //unShowDialog();
            mYear = year;
            mMonthOfYear = monthOfYear;
            mDayOfMonth = dayOfMonth;
            updateDate();
            mDateTextView.setText(formatDate());
        }
    }

    @Override
    public void onTimeChanged(CyeeTimePicker view, int hourOfDay, int minute) {
        if (isSuitableTime(hourOfDay, minute)) {
            mHourOfDay = hourOfDay;
            mMinute = minute;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 1 * 60 * 1000);
            mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
            mTimePicker.setCurrentHour(mHourOfDay);
            mTimePicker.setCurrentMinute(mMinute);
        }
        updateDate();
        mTimeTextView.setText(formatTime());
        //unShowDialog();
    }

    private String formatDate() {
        return DateFormat.getDateFormat(mContext).format(mDate);
    }

    private String formatTime() {
        return DateFormat.getTimeFormat(mContext).format(mDate);
    }

    private boolean isSuitableTime(int h, int m) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(c.getTimeInMillis() + 1 * 60 * 1000);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        if (year == mYear && month == mMonthOfYear && day == mDayOfMonth) {
            if (h < hour) {
                return false;
            } else if (h == hour && m < minute) {
                return false;
            }
            return true;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reminder_delete: {
                unShowDialog();
                if (mDateTimeCallback != null) {
                    mDateTimeCallback.onDataTimeDelete();
                }
                break;
            }
            case R.id.reminder_cancel: {
                unShowDialog();
                break;
            }
            case R.id.reminder_sure: {
                setDatePickByInput();
                setTimePickByInput();
                updateDate();
                if (mCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    try {
                        Field field = getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(this, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    new ToastManager(mContext).showToast(R.string.alert_time_early);
                } else {
                    if (mDateTimeCallback != null) {
                        mDateTimeCallback.onDateTimeSet(mCalendar);
                    }
                    if (mDatePicker.getVisibility() == View.VISIBLE) {
                        mDatePicker.clearFocus();
                    } else if (mTimePicker.getVisibility() == View.VISIBLE) {
                        mTimePicker.clearFocus();
                    }
                    unShowDialog();
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    public void updateDate(Calendar c) {
        mDatePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        mTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minutOfHour);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());

        state.putInt(HOUR, mTimePicker.getCurrentHour());
        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mDatePicker.init(year, month, day, new DateChangedListener());

        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
    }

    private void unShowDialog() {
        dismiss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                unShowDialog();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void hideInputMethod() {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (this.getWindow() != null && this.getWindow().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getWindow().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void setDatePickByInput() {
        Integer dayValue = StringConvertInteger(getPropertyValueByReflect(mDatePicker, "mDaySpinnerInput"));
        if (dayValue != null) {
            mDayOfMonth = dayValue;
        }

        Integer monthValue = getMonthByInput(getPropertyValueByReflect(mDatePicker, "mMonthSpinnerInput"));
        if (monthValue != null) {
            mMonthOfYear = monthValue;
        }
        Integer yearValue = StringConvertInteger(getPropertyValueByReflect(mDatePicker, "mYearSpinnerInput"));
        if (yearValue != null) {
            mYear = yearValue;
        }

    }

    private void setTimePickByInput() {
        Integer minuteValue = StringConvertInteger(getPropertyValueByReflect(mTimePicker, "mMinuteSpinnerInput"));
        if (minuteValue != null) {
            mMinute = minuteValue;
        }

        Integer hourValue = StringConvertInteger(getPropertyValueByReflect(mTimePicker, "mHourSpinnerInput"));
        if (hourValue != null) {
            //Gionee wanghaiyan 2017-5-23 modify for 147201 begin 
            if (!mIs24HourView && hourValue ==12) {
                boolean pmFlag = isPM(getPropertyValueByReflect(mTimePicker, "mAmPmSpinnerInput"));
                if (pmFlag) {
                    mHourOfDay = hourValue;
                    return;
                }else{
                	mHourOfDay = (hourValue -12);
                	return;
                }
            }else if (!mIs24HourView){
                boolean pmFlag = isPM(getPropertyValueByReflect(mTimePicker, "mAmPmSpinnerInput"));
                if (pmFlag) {
                    mHourOfDay = (hourValue + 12);
                    return;
                }else{
                	mHourOfDay = hourValue;
                	return;
                }
            //Gionee wanghaiyan 2017-5-23 modify for 147201 end 	
            }
            mHourOfDay = hourValue;
        }

    }

    private Integer StringConvertInteger(String value) {
        Integer intValue = null;
        if (!TextUtils.isEmpty(value)) {
            if (TextUtils.isDigitsOnly(value)) {
                intValue = Integer.parseInt(value);
            }
        }
        return intValue;
    }

    private Integer getMonthByInput(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        Integer intvalue = null;
        String[] shortMonth = mDfSymbols.getShortMonths();
        int index = 0;

        for (String mon : shortMonth) {
            index++;
            if (mon.equalsIgnoreCase(value)) {
                intvalue = index - 1;
                return intvalue;
            }

        }

        index = 0;
        for (String mon : shortMonth) {
            index++;
            String digitMonth = mon.substring(0, mon.length() - 1);
            if (value.equals(digitMonth)) {
                intvalue = index - 1;
                break;
            }
        }
        return intvalue;
    }

    private boolean isPM(String propertyValue) {
        if (!TextUtils.isEmpty(propertyValue)) {
            String[] AMPM = mDfSymbols.getAmPmStrings();
            if (AMPM[1].equals(propertyValue)) {
                return true;
            }
        }
        return false;
    }

    private String getPropertyValueByReflect(Object obj, String property) {
        String propertyValue = null;
        if (!TextUtils.isEmpty(property)) {
            try {
                Field field = obj.getClass().getDeclaredField(property);
                field.setAccessible(true);
                try {
                    EditText editText = (EditText) field.get(obj);
                    if (!TextUtils.isEmpty(editText.getText())) {
                        propertyValue = editText.getText().toString();

                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return propertyValue;
    }

}
