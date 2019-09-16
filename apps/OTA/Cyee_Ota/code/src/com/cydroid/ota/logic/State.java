package com.cydroid.ota.logic;

/**
 * Created by borney on 4/13/15.
 */

public enum  State {
    INITIAL(1),
    CHECKING(2),
    READY_TO_DOWNLOAD(3),
    DOWNLOADING(4),
    DOWNLOAD_INTERRUPT(5),
    DOWNLOAD_PAUSE(6),
    DOWNLOAD_PAUSEING(7),
    DOWNLOAD_COMPLETE(8),
    DOWNLOAD_VERIFY(9),
    INSTALLING(10),
    ERROR(11);


    private int mValue;

    private State(int value) {
        this.mValue = value;
    }

    public int value() {
        return mValue;
    }

    public static State getState(int value) {
        switch (value) {
        case 1:
            return INITIAL;
        case 2:
            return CHECKING;
        case 3:
            return READY_TO_DOWNLOAD;
        case 4:
            return DOWNLOADING;
        case 5:
            return DOWNLOAD_INTERRUPT;
        case 6:
            return DOWNLOAD_PAUSE;
        case 7:
            return DOWNLOAD_PAUSEING;
        case 8:
            return DOWNLOAD_COMPLETE;
        case 9:
            return DOWNLOAD_VERIFY;
        case 10:
            return INSTALLING;
        default:
            break;
        }
        return ERROR;
    }
}
