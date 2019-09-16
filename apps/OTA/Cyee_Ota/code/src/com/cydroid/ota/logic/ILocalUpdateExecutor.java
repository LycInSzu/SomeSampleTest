package com.cydroid.ota.logic;

/**
 * Created by borney on 4/21/15.
 */
public interface ILocalUpdateExecutor extends ICancelable {
    void scanAvailable();

    void install(String fileName);
}
