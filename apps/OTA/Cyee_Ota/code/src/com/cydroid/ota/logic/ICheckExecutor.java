package com.cydroid.ota.logic;

import com.cydroid.ota.logic.bean.CheckType;

/**
 * Created by borney on 4/15/15.
 */
public interface ICheckExecutor extends ICancelable {

    void check(CheckType checkType);

}
