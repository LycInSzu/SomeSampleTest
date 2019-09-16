package com.cydroid.ota.logic;

/**
 * Created by liuyanfeng on 15-6-11.
 */
public interface IAutoUpgradeSystem {
    void autoUpgradeSystem();

    void stopAutoUpgradeSystem();

    void registerBatteryReceiver();

    boolean isAutoUpgrade();

    IAutoUpgradeDispatcher getAutoUpgradeDispatcher();
}
