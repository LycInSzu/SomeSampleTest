package com.pri.factorytest;

import com.pri.factorytest.util.Utils;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.util.Log;
import android.os.SystemProperties;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pri.factorytest.FactoryTestApplication.PRIZE_CUSTOMER;

public class PrizeFactoryTestListActivity extends PrizeBaseActivity implements
        OnItemSelectedListener, OnItemClickListener {
    private LayoutInflater mInflater;
    private FunnyLookingAdapter mFunnyLookingAdapter;
    private int mItem = 0;

    private volatile String[] mItemsValue = new String[70];
    private FactoryTestApplication mApp;
    private static final boolean isReadNv = Stream.of("pcba-sea").anyMatch(x -> x.equals(PRIZE_CUSTOMER));
    private List<Integer> mNvIndexList = null;
    private String mNvInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.factory_test_list);
        mInflater = LayoutInflater.from(this);
        mApp = (FactoryTestApplication) getApplication();
        Utils.paddingLayout(findViewById(R.id.grid), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] testItems = mApp.getItems();
        if ("".equals(testItems[0])) {
            Log.e("liup-listtest", "---testItems elements is all empty---------");
        }
        if (TextUtils.isEmpty(mItemsValue[0])) {
            mItemsValue = mApp.getItemsValue();
            if (TextUtils.isEmpty(mItemsValue[0])) {
                Log.e("liup-listtest", "---ittemsValue elements is all empty---restart Homepage-------");
                finish2HomePage();
                return;
            }
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        GridView grid = (GridView) findViewById(R.id.grid);

        if (isReadNv) {
            String[] array = mItemsValue;
            String[] totalItems = mApp.getPrizeFactoryTotalItems();
            if (totalItems == null) {
                Log.e("liup-listtest", "---totalItems is empty-------");
                finish2HomePage();
                return;
            }
            List<Integer> itemValueList = Arrays.stream(array).map(x -> Arrays.asList(totalItems)
                    .indexOf(x)).collect(Collectors.toList());
            mNvIndexList = itemValueList.stream().filter(x -> x >= 0).map(x
                    -> Integer.parseInt(mApp.getPrizeFactoryTotalNvIndexs(x))).collect(Collectors.toList());
            Integer maxNvIndex = mNvIndexList.stream().max(Comparator.comparing(Function.identity())).get();
            mNvInfo = Utils.readProInfo(Utils.PRIZE_FACTORY_FACTORY_INFO_OFFSET + maxNvIndex, 1);
        }

        mFunnyLookingAdapter = new FunnyLookingAdapter(this,
                android.R.layout.simple_list_item_1, testItems);
        grid.setAdapter(mFunnyLookingAdapter);
        grid.setOnItemSelectedListener(this);
        grid.setOnItemClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mItemsValue != null && !TextUtils.isEmpty(mItemsValue[0])) {
            outState.putStringArray("item_test_list", mItemsValue);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Optional.ofNullable(savedInstanceState).ifPresent(x -> mItemsValue = x.getStringArray("item_test_list"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e("liup", "list resultCode = " + resultCode);
        Log.e("liup", "toStartAutoTest = " + Utils.toStartAutoTest);
        Log.e("liup", "itempos = " + Utils.mItemPosition);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            mApp.setResultCode(mItem, 1);
            mApp.setReportResult(mItem, getResources().getString(R.string.result_normal));
        } else if (resultCode == RESULT_CANCELED) {
            mApp.setResultCode(mItem, 2);
            mApp.setReportResult(mItem, getResources().getString(R.string.result_error));
        } else if (resultCode == 3) {
            String appname = mItemsValue[mItem];
            Utils.startTestItemActivity(this, appname);
            return;
        }

        if (mFunnyLookingAdapter != null) {
            mFunnyLookingAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        //String appname = mTestItems[position];
        if (position >= mItemsValue.length) {
            Log.e("liup", "---position:" + position + "mItemsValue.length:" + mItemsValue.length);
            finish2HomePage();
            return;
        }
        mItem = position;
        Utils.toStartAutoTest = false;
        Utils.startTestItemActivity(this, mItemsValue[position]);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private void finish2HomePage() {
        Intent intent = new Intent();
        intent.setClass(this, PrizeFactoryTestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("isAutoTest", true);
        startActivity(intent);
        finish();
    }

    private class FunnyLookingAdapter extends ArrayAdapter<String> {
        private String[] theItems;
        private final int windowWidth;
        private final int windowHeight;

        FunnyLookingAdapter(Context context, int resource, String[] items) {
            super(context, resource, items);
            theItems = items;
            WindowManager wm = getWindowManager();
            windowWidth = wm.getDefaultDisplay().getWidth();
            windowHeight = wm.getDefaultDisplay().getHeight();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item, null);
                viewHolder = new ViewHolder();
                viewHolder.label = (TextView) convertView.findViewById(R.id.item_red);
                convertView.setLayoutParams(new GridView.LayoutParams(
                        (windowWidth - 10) / 3, windowHeight / 11));

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.label.setText(theItems[position]);
            String nvValue = null;
            if (isReadNv) {
                int nvIndex = Utils.PRIZE_FACTORY_FACTORY_INFO_OFFSET + mNvIndexList.get(position);
                nvValue = String.valueOf(mNvInfo.charAt(nvIndex)).trim();
            }
            int resultCode = mApp.getResultCode(position);
            if (resultCode == 1 || "P".equals(nvValue)) {
                viewHolder.label.setBackgroundResource(R.drawable.green_button);
            } else if (resultCode == 2 || "F".equals(nvValue)) {
                viewHolder.label.setBackgroundResource(R.drawable.red_button);
            } else if (resultCode == 0 || "".equals(nvValue)) {
                viewHolder.label.setBackgroundResource(R.drawable.gray_button);
            }
            return convertView;
        }
    }

    private class ViewHolder {
        TextView label;
    }
}
