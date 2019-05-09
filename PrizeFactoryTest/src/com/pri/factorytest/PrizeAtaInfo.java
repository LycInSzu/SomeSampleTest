package com.pri.factorytest;

import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.os.SystemProperties;

import java.util.ArrayList;

import android.widget.BaseAdapter;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;

import java.util.HashMap;

import android.content.Context;

import static com.pri.factorytest.FactoryTestApplication.GSM_SERIAL;

public class PrizeAtaInfo extends PrizeBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ata_info);
        ataInfoDisplay();
        Utils.paddingLayout(findViewById(R.id.ata_lv), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    private int get(char c, int index) {
        return (c & (0x1 << index)) >> index;
    }

    private char charToHex(char c) {
        char r = 0x00;
        switch (c) {
            case '0':
                r = 0x00;
                break;
            case '1':
                r = 0x01;
                break;
            case '2':
                r = 0x02;
                break;
            case '3':
                r = 0x03;
                break;
            case '4':
                r = 0x04;
                break;
            case '5':
                r = 0x05;
                break;
            case '6':
                r = 0x06;
                break;
            case '7':
                r = 0x07;
                break;
            case '8':
                r = 0x08;
                break;
            case '9':
                r = 0x09;
                break;
            case 'A':
                r = 0x0a;
                break;
            case 'B':
                r = 0x0b;
                break;
            case 'C':
                r = 0x0c;
                break;
            case 'D':
                r = 0x0d;
                break;
            case 'E':
                r = 0x0e;
                break;
            case 'F':
                r = 0x0f;
                break;
            default:
                r = 0x00;
                break;
        }
        return r;
    }

    private void ataTestInfo(String temp) {
        ataList.add(getAtaItem("GPS: ", 33, 0));
        ataList.add(getAtaItem("FM: ", 33, 1));
        ataList.add(getAtaItem("BT: ", 33, 2));
        ataList.add(getAtaItem("WIFI: ", 33, 3));
        ataList.add(getAtaItem("ALS/PS: ", 34, 0));
        ataList.add(getAtaItem("M-Sensor: ", 34, 1));
        ataList.add(getAtaItem("G-Sensor: ", 34, 2));
        ataList.add(getAtaItem("Signal RSSI: ", 34, 3));
        ataList.add(getAtaItem("MainCamera: ", 35, 0));
        ataList.add(getAtaItem("Touch Panel: ", 35, 1));
        ataList.add(getAtaItem("LCM: ", 35, 2));
        ataList.add(getAtaItem("GYROSCOPE: ", 35, 3));
        ataList.add(getAtaItem("Key Pad: ", 36, 0));
        ataList.add(getAtaItem("SIM: ", 36, 1));
        ataList.add(getAtaItem("T Card: ", 36, 2));
        ataList.add(getAtaItem("SubCamera: ", 36, 3));
        ataList.add(getAtaItem("Vibrator: ", 37, 0));
        ataList.add(getAtaItem("Headset: ", 37, 1));
        ataList.add(getAtaItem("Speaker: ", 37, 2));
        ataList.add(getAtaItem("Receiver: ", 37, 3));
        ataList.add(getAtaItem("Idle Current: ", 38, 0));
        ataList.add(getAtaItem("OTG: ", 38, 1));
        ataList.add(getAtaItem("LED: ", 38, 2));
        ataList.add(getAtaItem("Charger: ", 38, 3));
        ataList.add(getAtaItem("Fingerprint: ", 39, 2));
        ataList.add(getAtaItem("Off Current: ", 39, 3));
    }

    private HashMap<String, String> getAtaItem(String itemName, int serialIndex, int compIndex) {
        HashMap<String, String> ataItemInfo = new HashMap<String, String>();
        ataItemInfo.put("key", itemName);
        if (null != GSM_SERIAL && GSM_SERIAL.length() > serialIndex) {
            if (get(charToHex(GSM_SERIAL.charAt(serialIndex)), compIndex) == 1) {
                ataItemInfo.put("value", getString(R.string.is_test));
            } else {
                ataItemInfo.put("value", getString(R.string.no_test));
            }
        } else {
            ataItemInfo.put("value", getString(R.string.no_test));
        }
        return ataItemInfo;
    }

    private ArrayList<HashMap<String, String>> ataList = new ArrayList<HashMap<String, String>>();
    ListView ataListView;
    AtaInfoAdapter ataAdapter;

    private void ataInfoDisplay() {
        ataListView = (ListView) findViewById(R.id.ata_lv);
        getListViewData(GSM_SERIAL);
        ataAdapter = new AtaInfoAdapter(getBaseContext(), ataList);
        ataListView.setAdapter(ataAdapter);
    }

    private void getListViewData(String temp) {
        ataTestInfo(temp);
    }

    public class AtaInfoAdapter extends BaseAdapter {
        Context context;
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        public AtaInfoAdapter(Context context, ArrayList<HashMap<String, String>> snList) {
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
            if (list.get(position).get("value").toString().equals(getString(R.string.is_test))) {
                holder.textViewItem01.setTextColor(Color.WHITE);
                holder.textViewItem02.setTextColor(Color.GREEN);
            } else {
                holder.textViewItem01.setTextColor(Color.WHITE);
                holder.textViewItem02.setTextColor(Color.RED);
            }
            return convertView;
        }
    }

    public class ViewHolder {
        TextView textViewItem01;
        TextView textViewItem02;
    }
}