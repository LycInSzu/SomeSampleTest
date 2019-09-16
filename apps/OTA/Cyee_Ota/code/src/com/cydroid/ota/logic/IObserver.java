package com.cydroid.ota.logic;

/**
 * Created by borney on 4/14/15.
 */
public interface IObserver {
    void onStateChange(IContextState state);

    void onError(IContextState state, int error);
}
