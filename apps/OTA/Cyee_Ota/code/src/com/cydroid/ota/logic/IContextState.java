package com.cydroid.ota.logic;

/**
 * Created by borney on 4/14/15.
 */
public interface IContextState {
    State state();

    boolean isRoot();

    boolean isBackState();

    int error();
}
