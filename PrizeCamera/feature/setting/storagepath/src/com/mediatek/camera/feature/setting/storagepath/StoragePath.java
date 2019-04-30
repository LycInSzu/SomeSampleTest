package com.mediatek.camera.feature.setting.storagepath;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.prize.FeatureSwitcher;
import com.mediatek.camera.prize.PrizeDataRevert;

import java.util.ArrayList;
import java.util.List;

public class StoragePath extends SettingBase  implements StoragePathSettingView.OnValueChangeListener,PrizeDataRevert {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(StoragePath.class.getSimpleName());
    private static final String KEY_STORAGEPATH = "key_storagepath";

    private StoragePathSettingView mSettingView;
    private ISettingChangeRequester mSettingChangeRequester;
    private List<String> mSupportValues = new ArrayList<String>();
    private boolean mIsSupported = false;
    private static final String PHONE = "phone";
    private static final String SD = "sd";
    @Override
    public void unInit() {

    }

    @Override
    public void init(IApp app, ICameraContext cameraContext, ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        initSettingValue();
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO;
    }

    @Override
    public String getKey() {
        return KEY_STORAGEPATH;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    @Override
    public void addViewEntry() {
        if (!mIsSupported) {
            return;
        }
        if (mSettingView == null) {
            mSettingView = new StoragePathSettingView(getKey(), mActivity);
            mSettingView.setOnValueChangeListener(this);
        }
        mAppUi.addSettingView(mSettingView);
    }

    /**
     * Initialize values when platform supported values is ready.
     *
     * @param platformSupportedValues The platform supported values.
     * @param defaultValue The platform default values
     */
/*    public void onValueInitialized(List<String> platformSupportedValues,
                                   String defaultValue) {
        if (platformSupportedValues != null && platformSupportedValues.size() > 0) {
            setSupportedPlatformValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setEntryValues(platformSupportedValues);
            String value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
            setValue(value);
            mIsSupported = true;
        }
    }*/

    @Override
    public void onValueChanged(String value) {
        LogHelper.i(TAG, "[onValueChanged], value:" + value);
        if (!getValue().equals(value)) {
            setValue(value);
            /*prize-modify-feature Camera front and rear settings remain the same-xiaoping-20190420-start*/
            mDataStore.setValue(getKey(), value, getBackStoreScope(), false);
            mDataStore.setValue(getKey(), value, getFrontStoreScope(), false);
            /*prize-modify-feature Camera front and rear settings remain the same-xiaoping-20190420-end*/
        }
    }


    @Override
    public void refreshViewEntry() {
        if (mSettingView != null) {
            mSettingView.setEntryValues(getEntryValues());
            if (mApp.isExtranelStorageMount() /*&& mApp.isHasPermissionForSD() modify for bug 71247 xiaoping*/) {
                /*prize-modify-bugid:72856-Change camera default storage to sd card-xiaoping-20190322-start*/
                String value = "phone";
                if (FeatureSwitcher.getCurrentProjectValue() == 3 || SD.equals(FeatureSwitcher.getStoragePathDefaultValue())) {
                    value = mDataStore.getValue(getKey(),
                            SD, getStoreScope());
                    /**
                     * Use a flag to determine if the camera is the first time to mount the sd card, and if so, change the storage default to sd
                     */
                    if ("false".equals(mDataStore.getValue("key_sdmount","true",getStoreScope()))) {
                        value = SD;
                        mDataStore.setValue("key_sdmount","true",getStoreScope(),false);
                    }
                    mDataStore.setValue(KEY_STORAGEPATH,value,getStoreScope(),false);
                } else {
                    value = getValue();
                }
                mSettingView.setValue(value);
                mSettingView.setEnabled(getEntryValues().size() > 1);
            } else {
                mSettingView.setValue(PHONE);
                mDataStore.setValue(KEY_STORAGEPATH,PHONE,getStoreScope(),false);
                if (FeatureSwitcher.getCurrentProjectValue() == 3 || SD.equals(FeatureSwitcher.getStoragePathDefaultValue())) {
                    mDataStore.setValue("key_sdmount","false",getStoreScope(),false);
                }
                /*prize-modify-bugid:72856-Change camera default storage to sd card-xiaoping-20190322-end*/
                mSettingView.setEnabled(false);
            }

        }
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    private void initSettingValue() {
        mSupportValues.add(PHONE);
        mSupportValues.add(SD);
        setSupportedPlatformValues(mSupportValues);
        setSupportedEntryValues(mSupportValues);
        setEntryValues(mSupportValues);
        String valueInStore = mDataStore.getValue(getKey(),
                PHONE, getStoreScope());
        setValue(valueInStore);
        mIsSupported = true;
    }

    @Override
    public void clearCache() {
        mDataStore.clearCache();
        onValueChanged(PHONE);
    }
}
