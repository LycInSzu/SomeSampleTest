package com.pri.factorytest;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import static com.pri.factorytest.FactoryTestApplication.NETWORK_TYPE;
import static com.pri.factorytest.FactoryTestApplication.PRIZE_CUSTOMER;

public class PrizeSnInfo extends PrizeBaseActivity {
    private static final String TAG = "PrizeSnInfo";
    private TextView mVersion;
    private TelephonyManager mTelMgr;
    private ArrayList<HashMap<String, String>> pcbaList = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> phoneList = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> autotestList = new ArrayList<HashMap<String, String>>();
    ListView pcbaListView;
    ListView phoneListView;
    ListView autotestListView;
    SnInfoAdapter pcbaAdapter;
    SnInfoAdapter phoneAdapter;
    SnInfoAdapter autotestAdapter;
    private Context mContext;
    private String NVData = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sn_info);
        mVersion = (TextView) findViewById(R.id.sn_show);
        Utils.paddingLayout(findViewById(R.id.sn_show), 0, ACTIVITY_TOP_PADDING, 0, 0);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String apNvData = Utils.readProInfo(0, 63);
        NVData = apNvData;
        Log.i(TAG, "---SN--factoryNvString:" + apNvData);
        String versionInfo = getVersionInfo(apNvData);
        mVersion.setText(versionInfo);
        teeInfoDisplay();
        snInfoDisplay(apNvData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.clearNvBuffCache();
    }

    private void teeInfoDisplay() {
        if (Stream.of("koobee", "odm","BLU","customer").noneMatch(x -> PRIZE_CUSTOMER.equals(x))) {
            return ;
        }
        View viewFlag = findViewById(R.id.tee_flag);
        TextView teeText = findViewById(R.id.tee_info);
        String teeInfoStr = PrizeHwInfo.getTeeInfo(this);
        Optional.ofNullable(teeInfoStr).ifPresent(x -> {
            viewFlag.setVisibility(View.VISIBLE);
            teeText.setVisibility(View.VISIBLE);
            teeText.setText(x);
        });
    }

    private String getVersionInfo(String apNvData) {
        String temp = null;
        StringBuilder info = new StringBuilder();
        mTelMgr = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));

        if (NETWORK_TYPE.contains("CB")) {
            String meid = SystemProperties.get("gsm.mtk.meid");
            info.append("[MEID] : ");
            if (TextUtils.isEmpty(meid)) {
                //meid = TelephonyManagerEx.getDefault().getMeid(0);
            }
            if (TextUtils.isEmpty(meid)) {
                temp = getString(R.string.imei_invalid);
            } else {
                temp = meid.toUpperCase();
            }
            info.append(temp);
        }

        String imei1 = SystemProperties.get("gsm.mtk.imei1");
        String imei2 = SystemProperties.get("gsm.mtk.imei2");

        info.append("\n[IMEI1] : ");
        temp = imei1;//mTelMgr.getDeviceId(0);
        info.append(temp);

        info.append("\n[IMEI2] : ");
        temp = imei2;//mTelMgr.getDeviceId(1);
        info.append(temp);

        info.append("\n[SN] : ");
        temp = apNvData;
        info.append(temp);

        return info.toString();
    }

    private void snInfoDisplay(String apNvData) {
        getListViewData(apNvData);
        pcbaListView = (ListView) findViewById(R.id.pcba_lv);
        phoneListView = (ListView) findViewById(R.id.phone_lv);
        autotestListView = (ListView) findViewById(R.id.autotest_lv);
        pcbaAdapter = new SnInfoAdapter(mContext, pcbaList);
        phoneAdapter = new SnInfoAdapter(mContext, phoneList);
        autotestAdapter = new SnInfoAdapter(mContext, autotestList);
        pcbaListView.setAdapter(pcbaAdapter);
        phoneListView.setAdapter(phoneAdapter);
        autotestListView.setAdapter(autotestAdapter);
    }

    private void getListViewData(String apNvData) {
        if (pcbaList != null && !pcbaList.isEmpty()) {
            pcbaList.clear();
        }
        if (phoneList != null && !phoneList.isEmpty()) {
            phoneList.clear();
        }
        if (autotestList != null && !autotestList.isEmpty()) {
            autotestList.clear();
        }
        if (PRIZE_CUSTOMER == null) {
            koobeeTestInfo(apNvData);
        } else {
            if (PRIZE_CUSTOMER.contains("pcba")) {
                pcbaTestInfo(apNvData);
            } else if (Stream.of("koobee", "odm","BLU","customer").anyMatch(x -> PRIZE_CUSTOMER.equals(x))) {
                koobeeTestInfo(apNvData);
            } else {
                koobeeTestInfo(apNvData);
            }
        }
    }

    private void koobeeTestInfo(String apNvData) {
        phoneList.add(getResultInfo("整机CIT: ", 45, ""));
        if (NETWORK_TYPE.contains("GB")) {
            pcbaList.add(getResultInfo("GSM-BT: ", 62, ""));
            pcbaList.add(getResultInfo("GSM-FT: ", 61, ""));

            phoneList.add(getResultInfo("GSM耦合: ", 48, ""));
        }

        if (NETWORK_TYPE.contains("WB")) {
            pcbaList.add(getResultInfo("WCDMA-BT: ", 60, ""));
            pcbaList.add(getResultInfo("WCDMA-FT: ", 59, ""));
            phoneList.add(getResultInfo("WCDMA耦合: ", 44, ""));
        }

        if (NETWORK_TYPE.contains("TB")) {
            pcbaList.add(getResultInfo("TDSCDMA-BT: ", 58, ""));
            pcbaList.add(getResultInfo("TDSCDMA-FT: ", 57, ""));
            //phoneList.add(getResultInfo("TDSCDMA耦合: ", 43, ""));
        }

        if (NETWORK_TYPE.contains("CB")) {
            pcbaList.add(getResultInfo("CDMA-BT: ", 56, ""));
            pcbaList.add(getResultInfo("CDMA-FT: ", 55, ""));

            phoneList.add(getResultInfo("CDMA耦合: ", 46, ""));
        }

        if (NETWORK_TYPE.contains("LtB")) {
            pcbaList.add(getResultInfo("LTETDD-BT: ", 54, ""));
            pcbaList.add(getResultInfo("LTETDD-FT: ", 53, ""));
        }
        if (NETWORK_TYPE.contains("LfB")) {
            pcbaList.add(getResultInfo("LTEFDD-BT: ", 52, ""));
            pcbaList.add(getResultInfo("LTEFDD-FT: ", 51, ""));
        }
        if (NETWORK_TYPE.contains("LtB") || NETWORK_TYPE.contains("LfB")) {
            phoneList.add(getResultInfo("LTE耦合: ", 47, ""));
        }

        String NV49 = getNVData(49);
        String NV50 = getNVData(50);
        pcbaList.add(getResultInfo("PCBA功能：", Integer.MAX_VALUE, NV49, NV50));

        phoneList.add(getResultInfo("WBG耦合: ", 41, ""));

        autotestList.add(getResultInfo("DDR测试: ", 36, ""));
        autotestList.add(getResultInfo("人工测试: ", 37, ""));
        autotestList.add(getResultInfo("MMI自动化测试: ", 38, ""));
        autotestList.add(getResultInfo("camera自动化测试: ", 39, ""));
        autotestList.add(getResultInfo("音频自动化测试: ", 40, ""));

        if (Stream.of("gw9518").anyMatch(x -> SystemProperties
                .get("ro.pri.fingerprint").contains(x.trim()))) {
            String huidingCali = Utils.readProInfo(Utils.PRIZE_HUIDING_FINGERPRINT_CALI, 1);
            huidingCali = String.valueOf(huidingCali.charAt(huidingCali.length() - 1));
            autotestList.add(getResultInfo("指纹校准: ", Utils.PRIZE_HUIDING_FINGERPRINT_CALI, huidingCali));
        }
        if (Utils.isSupportDoubuleCameraStand(this)) {
            String cameraStandar = Utils.readProInfo(Utils.PRIZE_DOUBLE_CAMERA_STANDAR, 1);
            cameraStandar = String.valueOf(cameraStandar.charAt(cameraStandar.length() - 1));
            autotestList.add(getResultInfo("双摄标定: ", Utils.PRIZE_DOUBLE_CAMERA_STANDAR, cameraStandar));
        }
    }

    private void pcbaTestInfo(String apNvData) {
        phoneList.add(getResultInfo("整机CIT: ", 45, ""));
        if (NETWORK_TYPE.contains("GB")) {
            pcbaList.add(getResultInfo("GSM-BT: ", 62, ""));
            phoneList.add(getResultInfo("GSM耦合: ", 48, ""));
        }

        if (NETWORK_TYPE.contains("WB")) {
            pcbaList.add(getResultInfo("WCDMA-BT: ", 60, ""));
            phoneList.add(getResultInfo("WCDMA耦合: ", 44, ""));
        }

        if (NETWORK_TYPE.contains("TB")) {
            pcbaList.add(getResultInfo("TDSCDMA-BT: ", 58, ""));
            //phoneList.add(getResultInfo("TDSCDMA耦合: ", 43, ""));
        }

        if (NETWORK_TYPE.contains("CB")) {
            pcbaList.add(getResultInfo("CDMA-BT: ", 56, ""));
            phoneList.add(getResultInfo("CDMA耦合: ", 46, ""));
        }

        if (NETWORK_TYPE.contains("LtB")) {
            pcbaList.add(getResultInfo("LTETDD-BT: ", 54, ""));
        }
        if (NETWORK_TYPE.contains("LfB")) {
            pcbaList.add(getResultInfo("LTEFDD-BT: ", 52, ""));
        }
        if (NETWORK_TYPE.contains("LtB") || NETWORK_TYPE.contains("LfB")) {
            phoneList.add(getResultInfo("LTE耦合: ", 47, ""));
        }

        String NV49 = getNVData(49);
        String NV50 = getNVData(50);
        pcbaList.add(getResultInfo("PCBA功能：", Integer.MAX_VALUE, NV49, NV50));

        phoneList.add(getResultInfo("WBG耦合: ", 41, ""));

        autotestList.add(getResultInfo("DDR测试: ", 36, ""));
        /*autotestList.add(getResultInfo("人工测试: ", 37, manual));
        autotestList.add(getResultInfo("MMI自动化测试: ", 38, autommi));
        autotestList.add(getResultInfo("camera自动化测试: ", 39, autocamera));
        autotestList.add(getResultInfo("音频自动化测试: ", 40, autoaudio));*/
    }

    /**
     * @param itemName
     * @param nvIndex
     * @param nvResult    must not null, instead of "".
     * @return
     */
    private HashMap<String, String> getResultInfo(String itemName, int nvIndex, String... nvResult) {
        HashMap<String, String> itemInfo = new HashMap<String, String>();
        if (itemName == null || "".equals(itemName)) {
            throw new UnsupportedOperationException("Unknown item name,must not empty or null");
        }
        itemInfo.put("key", itemName);
        String result = null;
        if (NVData.length() > nvIndex) {
            result = String.valueOf(NVData.charAt(nvIndex));
            result = result.toUpperCase();
        }
        if ("P".equals(result) || Arrays.stream(nvResult).anyMatch(x->"P".equals(x))) {
            itemInfo.put("value", getString(R.string.pass));
        } else if ("F".equals(result) || Arrays.stream(nvResult).anyMatch(x->"F".equals(x))) {
            itemInfo.put("value", getString(R.string.fail));
        } else {
            itemInfo.put("value", getString(R.string.no_test));
        }

        return itemInfo;
    }
    
    private String getNVData(int nvIndex) {
        String result = "";
        if (NVData.length() > nvIndex) {
            result = String.valueOf(NVData.charAt(nvIndex));
            result = result.toUpperCase();
        }
        return result;
    }

    public class SnInfoAdapter extends BaseAdapter {
        Context context;
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        public SnInfoAdapter(Context context, ArrayList<HashMap<String, String>> snList) {
            this.context = context;
            list = snList;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int id) {
            return id;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.sn_item, parent, false);
                holder = new ViewHolder();
                holder.textViewItem01 = (TextView) convertView.findViewById(
                        R.id.key);
                holder.textViewItem02 = (TextView) convertView.findViewById(
                        R.id.value);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textViewItem01.setText(list.get(position).get("key").toString());
            holder.textViewItem02.setText(list.get(position).get("value").toString());
            if (list.get(position).get("value").toString().equals(getString(R.string.pass))) {
                holder.textViewItem01.setTextColor(Color.WHITE);
                holder.textViewItem02.setTextColor(Color.GREEN);
            } else if (list.get(position).get("value").toString().equals(getString(R.string.fail))) {
                holder.textViewItem01.setTextColor(Color.WHITE);
                holder.textViewItem02.setTextColor(Color.RED);
            } else {
                holder.textViewItem01.setTextColor(Color.WHITE);
                holder.textViewItem02.setTextColor(Color.GRAY);
            }
            return convertView;
        }
    }

    public class ViewHolder {
        TextView textViewItem01;
        TextView textViewItem02;
    }
}
