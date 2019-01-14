package com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans;

import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Utils.MyTimeUtil;

public class CountDownBean implements Parcelable {

    public CountDownBean(String name, boolean finishend, String entTime) {

        this.name = name;
        this.isFinished = finishend;
        this.entTime = entTime;
    }


    String name;

    protected CountDownBean(Parcel in) {
        name = in.readString();
//        durition = in.readLong();
        isFinished = in.readByte() != 0;
        entTime = in.readString();
    }

    public static final Creator<CountDownBean> CREATOR = new Creator<CountDownBean>() {
        @Override
        public CountDownBean createFromParcel(Parcel in) {
            return new CountDownBean(in);
        }

        @Override
        public CountDownBean[] newArray(int size) {
            return new CountDownBean[size];
        }
    };

    public void setName(String name) {
        this.name = name;
    }

//    public void setDurition(long durition) {
//        this.durition = durition;
//    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }


    public String getName() {
        return name;
    }

    public long getDurition() {
        return MyTimeUtil.convertToMilliSeconds(entTime) > 0 ? MyTimeUtil.convertToMilliSeconds(entTime) - System.currentTimeMillis() : 0;
    }

    public boolean isFinished() {
        return isFinished;
    }

    boolean isFinished = false;//0:未结束， 1:结束；

    public String getEntTime() {
        return entTime;
    }

    String entTime;

    public void setEntTime(String entTime) {
        this.entTime = entTime;
    }

    @NonNull
    @Override
    public String toString() {
        return "name is :" + name + " ;  entTime is :" + entTime + " ;  durition is :" + getDurition() + "  isFinished :" + isFinished;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
//        dest.writeLong(durition);
        dest.writeByte((byte) (isFinished ? 1 : 0));
        dest.writeString(entTime);
    }
}
