package com.pri.factorytest;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pri.factorytest.util.Utils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pri.factorytest.FactoryTestApplication.PRIZE_CUSTOMER;

public class FactoryTestReport extends PrizeBaseActivity {
    private TextView mTestReport;
    private ListView testReportListView;
    private TestReportAdapter testReportAdapter;
    private FactoryTestApplication mApp;
    private volatile String[] mTestItems = new String[70];
    private String mLastOperation = "";
    private static final boolean isReadNv = Stream.of("pcba-sea").anyMatch(x -> x.equals(PRIZE_CUSTOMER));
    private List<Integer> mNvIndexList = null;
    private Button mClearReport = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLastOperation = getIntent().getStringExtra("last_operation");
        mApp = (FactoryTestApplication) getApplication();
        mTestItems = mApp.getItems();
        setContentView(R.layout.testreport);
        initView();
    }

    private void initView() {
        mTestReport = findViewById(R.id.testreport_show);
        testReportListView = findViewById(R.id.testreport_lv);
        Utils.paddingLayout(findViewById(R.id.testreport_show), 0, ACTIVITY_TOP_PADDING, 0, 0);
        mClearReport = findViewById(R.id.clear_report);
        mClearReport.setVisibility(isReadNv ? View.VISIBLE : View.GONE);
        mTestReport.setText(getTestReport());
        mClearReport.setOnClickListener((view) -> {
            String spaceChars94 = String.format("%94s", "");
            Utils.writeProInfo(spaceChars94, Utils.PRIZE_FACTORY_FACTORY_INFO_OFFSET);

            testReportDisplay();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        testReportDisplay();
    }

    private String getTestReport() {
        String temp = "";
        SimpleDateFormat formatter = new SimpleDateFormat(getResources().getString(R.string.date_format));
        Date curDate = new Date(System.currentTimeMillis());
        temp = formatter.format(curDate);
        return temp;
    }

    private void testReportDisplay() {
        String nvInfo = null;
        if (isReadNv) {
            String[] array = mApp.getItemsValue();
            List<Integer> itemValueList = Arrays.stream(array).map(x -> Arrays.asList(mApp.getPrizeFactoryTotalItems())
                    .indexOf(x)).collect(Collectors.toList());
            mNvIndexList = itemValueList.stream().map(x -> Integer.parseInt(mApp.getPrizeFactoryTotalNvIndexs(x)))
                    .collect(Collectors.toList());
            Integer maxNvIndex = mNvIndexList.stream().max(Comparator.comparing(Function.identity())).get();
            nvInfo = Utils.readProInfo(Utils.PRIZE_FACTORY_FACTORY_INFO_OFFSET + maxNvIndex, 1);
        }
        ArrayList<HashMap<String, String>> testReportList = getListViewData(nvInfo);
        testReportAdapter = new TestReportAdapter(getBaseContext(), testReportList);
        testReportListView.setAdapter(testReportAdapter);
    }

    private ArrayList<HashMap<String, String>> getListViewData(String nvInfo) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        String fail = getString(R.string.result_error);
        String pass = getString(R.string.pass);
        String noTest = getString(R.string.no_test);
        for (int index = 0; index < mTestItems.length; index++) {
            int tmp = index;
            String iResult = Optional.ofNullable(nvInfo).map(x -> {
                int nvIndex = Utils.PRIZE_FACTORY_FACTORY_INFO_OFFSET + mNvIndexList.get(tmp);
                if (nvInfo.length() <= nvIndex) {
                    return noTest;
                }
                String str = String.valueOf(x.charAt(nvIndex)).trim();
                return "".equals(str) ? noTest : "P".equals(str) ? pass : fail;
            }).orElse(mApp.getReportResult(tmp));
            boolean hasResult = null != iResult;
            if (!TextUtils.isEmpty(mLastOperation) && !hasResult) {
                continue;
            }

            HashMap<String, String> testReportInfo = new HashMap<String, String>();
            testReportInfo.put("key", mTestItems[index] + ":");
            testReportInfo.put("value", hasResult ? iResult : noTest);

            list.add(testReportInfo);
        }
        return list;
    }

    public class TestReportAdapter extends BaseAdapter {
        Context context;
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        String fail = getString(R.string.result_error);
        String pass = getString(R.string.pass);

        public TestReportAdapter(Context context, ArrayList<HashMap<String, String>> snList) {
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
            HashMap<String, String> item = list.get(position);
            holder.textViewItem01.setText(item.get("key"));
            holder.textViewItem02.setText(item.get("value"));
            if (pass.equals(item.get("value"))) {
                holder.textViewItem01.setTextColor(Color.WHITE);
                holder.textViewItem02.setTextColor(Color.GREEN);
            } else if (fail.equals(item.get("value"))) {
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
