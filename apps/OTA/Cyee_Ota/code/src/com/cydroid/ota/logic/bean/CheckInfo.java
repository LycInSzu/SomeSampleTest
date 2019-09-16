package com.cydroid.ota.logic.bean;

import com.cydroid.ota.logic.config.NetConfig;

/**
 * Created by borney on 4/15/15.
 */
public class CheckInfo {
    private boolean isRoot;
    private boolean isSupportPatch;
    private NetConfig.ConnectionType connectionType;
    private CheckType checkType;
    private int pushId;
    private boolean isWapNetwork;
    private String imei;
    private String data;

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public boolean isSupportPatch() {
        return isSupportPatch;
    }

    public void setSupportPatch(boolean isSupportPatch) {
        this.isSupportPatch = isSupportPatch;
    }

    public NetConfig.ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(NetConfig.ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public int getPushId() {
        return pushId;
    }

    public void setPushId(int pushId) {
        this.pushId = pushId;
    }

    public boolean isWapNetwork() {
        return isWapNetwork;
    }

    public void setWapNetwork(boolean isWapNetwork) {
        this.isWapNetwork = isWapNetwork;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setCheckType(CheckType checkType) {
        this.checkType = checkType;
    }

    public CheckType getCheckType() {
        return checkType;
    }

}
