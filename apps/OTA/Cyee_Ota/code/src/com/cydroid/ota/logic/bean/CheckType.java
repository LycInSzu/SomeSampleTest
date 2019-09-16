package com.cydroid.ota.logic.bean;

/**
 * Created by kangjj on 15-7-29.
 */
public enum CheckType {
    CHECK_TYPE_DEFAULT(-2), CHECK_TYPE_AUTO(-1), CHECK_TYPE_PUSH(1);

    int value;

    CheckType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public void setTypeValue(int value) {
        this.value = value;
    }
}
