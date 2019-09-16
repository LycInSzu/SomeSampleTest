package com.cydroid.ota.logic.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.text.TextUtils;
import com.cydroid.ota.execption.SettingUpdateParserException;
import com.cydroid.ota.bean.SettingUpdateInfo;
import com.cydroid.ota.utils.Constants;
import com.cydroid.ota.utils.SystemPropertiesUtils;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.Util;
import org.json.JSONException;
import org.json.JSONObject;

public class OtaUpgradeInfoParser implements IParser<String, SettingUpdateInfo>{
    private static final String TAG = "OtaUpgradeInfoParser";

    @Override
    public SettingUpdateInfo parser(String data) throws
            SettingUpdateParserException{
        Log.d(TAG, "OtaUpgradeInfoParser parser data : " + data);
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        try {
            JSONObject upgradeInJsonObject = new JSONObject(data);
            String downloadUrl = upgradeInJsonObject.getString("url");
            long fileSize = Long.parseLong(upgradeInJsonObject.getString("fsize"));

            String releaseNote = upgradeInJsonObject.getString("desc");
            String md5 = upgradeInJsonObject.getString("md5");

           // int downloadedNum = upgradeInJsonObject.getInt("downcount");

            String internalVer = upgradeInJsonObject.getString("vc");
            //oversea rom is no change by cuijiuyu for CR01751527
            String version = upgradeInJsonObject.getString("vc");//upgradeInJsonObject.getString("vn");
            version = SystemPropertiesUtils.getVersionNum(version);
            version = version.replace("T", "V");
            String releaseNoteUrl = "";
            if (!upgradeInJsonObject.isNull("releaseNote")) {
                //releaseNoteUrl = upgradeInJsonObject.getString("releaseNote");
            }
            //add by cuijiuyu 
            String releaseNotesId = "";
            if (!upgradeInJsonObject.isNull("releaseNotesId")) {
                releaseNotesId = upgradeInJsonObject.getString("releaseNotesId");
            }          
            
            boolean extPkg = false;
            if (upgradeInJsonObject.has("extPkg")) {
                extPkg = upgradeInJsonObject.getBoolean("extPkg");
            }

            boolean isPreRelease = false;
            if (upgradeInJsonObject.has("prerelease")) {
                int result = upgradeInJsonObject.getInt("prerelease");
                if (result == 1) {
                    isPreRelease = true;
                }
            }
            boolean isbackupHint = false;
            if(upgradeInJsonObject.has("bw")){
                isbackupHint = upgradeInJsonObject.getBoolean("bw");
            }
            String versionReleaseDate = "";
            if (!upgradeInJsonObject.isNull("rdate")) {
                versionReleaseDate = Util.utcTimeToLocal(
                        upgradeInJsonObject.getLong("rdate"),
                        Constants.DATEFORMATE);
            }

            String versionSimpleRelaseNote = upgradeInJsonObject.optString(
                    "summary");

            SettingUpdateInfo otaUpgradeInfo = new SettingUpdateInfo();
            otaUpgradeInfo.setReleaseNote(releaseNote);
            //otaUpgradeInfo.setReleaseNoteUrl(releaseNoteUrl);
            otaUpgradeInfo.setReleaseNoteId(releaseNotesId);
           // otaUpgradeInfo.setDownloadedPeopleNum(downloadedNum);
            otaUpgradeInfo.setDownloadUrl(downloadUrl);
            otaUpgradeInfo.setFileSize(fileSize);
            otaUpgradeInfo.setInternalVer(internalVer);
            otaUpgradeInfo.setVersion(version);
            otaUpgradeInfo.setMd5(md5);
            otaUpgradeInfo.setExtPkg(extPkg);
            otaUpgradeInfo.setBackUp(isbackupHint);
            otaUpgradeInfo.setPreRelease(isPreRelease);
            otaUpgradeInfo.setVersionReleaseDate(versionReleaseDate);
            otaUpgradeInfo.setSimpleReleaseNote(versionSimpleRelaseNote);
            return otaUpgradeInfo;
        } catch (NumberFormatException e) {
            throw new SettingUpdateParserException(e.getMessage());
        } catch (JSONException e) {
           throw new SettingUpdateParserException(e.getMessage());
        }
    }
}