package com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans;

import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class CountDownBean  {

    public CountDownBean(String name,boolean finishend, long durition) {

        this.name=name;
        this.isFinished=finishend;
        this.durition=durition;
    }


    String name;

    public void setName(String name) {
        this.name = name;
    }

    public void setDurition(long durition) {
        this.durition = durition;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    long durition;

    public String getName() {
        return name;
    }

    public long getDurition() {
        return durition;
    }

    public boolean isFinished() {
        return isFinished;
    }

    boolean isFinished=false;//0:未结束， 1:结束；



    @NonNull
    @Override
    public String toString() {
        return "name is :"+name+" ;  durition is :"+durition+"  isFinished :"+isFinished;
    }
}
