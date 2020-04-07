package com.cydroid.note.app;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.cydroid.note.app.view.MarqueeTextView;
import com.cydroid.note.app.view.NoteCardBottomView;
import com.cydroid.note.data.Path;

import cyee.widget.CyeeCheckBox;
import cyee.widget.CyeeTextView;
//GIONEE wanghaiyan 2016-12-20 modify for 50571 begin
import android.widget.CheckBox;
//GIONEE wanghaiyan 2016-12-20 modify for 50571 end

public class NoteViewHolder extends RecyclerView.ViewHolder {
    public Path mPath;
    public ImageView mImage = null;
    public CyeeTextView mTitle;
    public CyeeTextView mContent;
    public CyeeTextView mTime;
    public ImageView mReminder;
    //GIONEE wanghaiyan 2016-12-20 modify for 50571 begin
    public CheckBox mCheckBox;
    //GIONEE wanghaiyan 2016-12-20 modify for 50571 end
    public NoteCardBottomView mNoteCardBottomView;
    public MarqueeTextView mCity;
    public CyeeTextView mTemperature;
    public CyeeTextView mWeatherType;
    public ImageView mWeather;

    public NoteViewHolder(View itemView) {
        super(itemView);
    }

}
