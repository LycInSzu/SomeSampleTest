package com.cydroid.ota.ui.widget;

import com.cydroid.ota.logic.IContextState;

/**
 * Created by kangjj on 15-6-8.
 */
public interface IStateView {
    void changeState(IContextState contextState);

    void onDestory();
}
