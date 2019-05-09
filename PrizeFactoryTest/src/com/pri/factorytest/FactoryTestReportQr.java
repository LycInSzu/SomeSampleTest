package com.pri.factorytest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pri.factorytest.Version.Version;
import com.pri.factorytest.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class FactoryTestReportQr extends PrizeBaseActivity {
    private final static String TAG = "FactoryTestReportQr";
    private TextView mTestReportResult;
    private TextView mTestReportResultItem;
    private TextView mSnNumber;
    private ImageView mImageViewQr;
    private Button factorySetButton = null;
    private Button softInfoButton = null;
    private String snNumber = null;

    private WindowManager.LayoutParams lp;
    private long mTestTime;
    private FactoryTestApplication mApp;
    private volatile String[] mTestItems = new String[70];

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        LinearLayout VersionLayout = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.testreport_qr, null);
        setContentView(VersionLayout);
        mApp = (FactoryTestApplication) getApplication();
        mTestItems = mApp.getItems();
        lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);
        snNumber = Utils.readProInfo(0, 20);
        mTestTime = getIntent().getLongExtra("testTimeReportQr", 0L);
        Utils.paddingLayout(findViewById(R.id.prize_title), 0, ACTIVITY_TOP_PADDING, 0, 0);

        mTestReportResult = (TextView) findViewById(R.id.testreport_result);
        mTestReportResult.setText(getTestReportResult());

        mTestReportResultItem = (TextView) findViewById(R.id.testreport_result_item);
        mTestReportResultItem.setText(getTestReportResultItem());

        mSnNumber = (TextView) findViewById(R.id.sn_number);
        mSnNumber.setText("SN" + ":" + snNumber);

        makeQRCode(getTestReportQr());
        initViews();
    }

    private void makeQRCode(String content) {
        mImageViewQr = (ImageView) findViewById(R.id.prize_image_view);
        try {
            Bitmap qrcodeBitmap = EncodingHandler.createQRCode(content, 400);
            mImageViewQr.setImageBitmap(qrcodeBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        factorySetButton = (Button) findViewById(R.id.factoryset);
        softInfoButton = (Button) findViewById(R.id.softinfo);
        factorySetButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(
                        FactoryTestReportQr.this);
                dialog.setCancelable(false)
                        .setTitle(R.string.factoryset)
                        .setMessage(R.string.factoryset_confirm)
                        .setPositiveButton

                                (R.string.confirm,
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(
                                                    DialogInterface dialoginterface,
                                                    int i) {
                                                Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
                                                intent.setPackage("android");
                                                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                                                intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
                                                intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, /*mEraseSdCard*/false);
                                                intent.putExtra(Intent.EXTRA_WIPE_ESIMS, /*mEraseEsims*/true);
                                                sendBroadcast(intent);
                                            }
                                        })
                        .setNegativeButton

                                (R.string.cancel,
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(
                                                    DialogInterface dialoginterface,
                                                    int i) {

                                            }
                                        }).show();

            }
        });

        softInfoButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent().setClass(
                        FactoryTestReportQr.this, Version.class);
                intent.putExtra("softinfo", true);
                startActivity(intent);
            }
        });
    }

    private String getTestReportQr() {
        String temp = null;
        StringBuilder info = new StringBuilder();
        temp = mTestTime + ",";
        info.append(temp);
        temp = snNumber + ",";
        info.append(temp);
        char[] resultHex = new char[4];
        List<String> resultList = new ArrayList<>();
        String noTest = getString(R.string.no_test);
        for (int itemPos = 0; itemPos < mTestItems.length; itemPos++) {
            String iResult = mApp.getReportResult(itemPos);
            if (!noTest.equals(iResult)) {
                resultList.add(iResult);
            }
        }
        for (int i = 0; i < resultList.size(); i++) {
            if (i < 18) {
                int index = i % 4;
                resultHex[index] = get(mustChangeResultTohex(mApp.getReportResult(i)), index);
                if (index == 3) {
                    temp = String.valueOf(hexToChar((char) (resultHex[0] | resultHex[1] | resultHex[2] | resultHex[3])));
                    info.append(temp);
                }
                if (i == 17) {
                    temp = String.valueOf(hexToChar((char) (resultHex[0] | resultHex[1])));
                    info.append(temp);
                }
            } else {
                String testItem = resultList.get(i);
                String testItemResult = mApp.getReportResult(i);
                temp = saveValueArray(testItem, testItemResult);
            }
        }
        temp = "," + temp;
        info.append(temp);
        return info.toString();
    }

    private char get(char resultHexSinger, int index) {
        return (char) (resultHexSinger << index);
    }

    private char hexToChar(char c) {
        char r = '0';
        switch (c) {
            case 0x00:
                r = '0';
                break;
            case 0x01:
                r = '1';
                break;
            case 0x02:
                r = '2';
                break;
            case 0x03:
                r = '3';
                break;
            case 0x04:
                r = '4';
                break;
            case 0x05:
                r = '5';
                break;
            case 0x06:
                r = '6';
                break;
            case 0x07:
                r = '7';
                break;
            case 0x08:
                r = '8';
                break;
            case 0x09:
                r = '9';
                break;
            case 0x0a:
                r = 'A';
                break;
            case 0x0b:
                r = 'B';
                break;
            case 0x0c:
                r = 'C';
                break;
            case 0x0d:
                r = 'D';
                break;
            case 0x0e:
                r = 'E';
                break;
            case 0x0f:
                r = 'F';
                break;
            default:
                r = '0';
                break;
        }
        return r;
    }


    private char unMustchangeResultTohex(String TestResult) {
        char result = 0x00;
        if (null != TestResult) {
            if (TestResult.equals("pass")) {
                result = 0x02;
            } else if (TestResult.equals("fail")) {
                result = 0x01;
            }
        }
        return result;
    }

    private char mustChangeResultTohex(String TestResult) {
        char result = 0x00;
        if (null != TestResult) {
            if (TestResult.equals("pass")) {
                result = 0x01;
            } else if (TestResult.equals("fail")) {
                result = 0x00;
            }
        }
        return result;
    }

    char[][] resultHex = {{0x00, 0x00}, {0x00, 0x00}, {0x00, 0x00}, {0x00, 0x00}, {0x00, 0x00}, {0x00, 0x00}};
    char[] resultValues = new char[6];

    private String saveValueArray(String testItem, String testItemResult) {
        if (null == testItem) {
            return null;
        } else if (testItem.equals(getResources().getString(R.string.fingerprint))) {
            resultHex[0][0] = (char) unMustchangeResultTohex(testItemResult);
        } else if (testItem.equals(getResources().getString(R.string.light_sensor))) {
            resultHex[0][1] = (char) (unMustchangeResultTohex(testItemResult) << 2);
        } else if (testItem.equals(getResources().getString(R.string.rang_sensor))) {
            resultHex[1][0] = (char) unMustchangeResultTohex(testItemResult);
        } else if (testItem.equals(getResources().getString(R.string.magnetic_sensor))) {
            resultHex[1][1] = (char) (unMustchangeResultTohex(testItemResult) << 2);
        } else if (testItem.equals(getResources().getString(R.string.gysensor_name))) {
            resultHex[2][0] = (char) unMustchangeResultTohex(testItemResult);
        } else if (testItem.equals(getResources().getString(R.string.infrared))) {
            resultHex[2][1] = (char) (unMustchangeResultTohex(testItemResult) << 2);
        } else if (testItem.equals(getResources().getString(R.string.flash_lamp_front))) {
            resultHex[3][0] = (char) unMustchangeResultTohex(testItemResult);
        } else if (testItem.equals(getResources().getString(R.string.hall_sensor))) {
            resultHex[3][1] = (char) (unMustchangeResultTohex(testItemResult) << 2);
        } else if (testItem.equals(getResources().getString(R.string.prize_led))) {
            resultHex[4][0] = (char) unMustchangeResultTohex(testItemResult);
        } else if (testItem.equals(getResources().getString(R.string.prize_ycd))) {
            resultHex[4][1] = (char) (unMustchangeResultTohex(testItemResult) << 2);
        } else if (testItem.equals(getResources().getString(R.string.otg))) {
            resultHex[5][0] = (char) unMustchangeResultTohex(testItemResult);
        } else if (testItem.equals(getResources().getString(R.string.prize_nfc))) {
            resultHex[5][1] = (char) (unMustchangeResultTohex(testItemResult) << 2);
        }
        for (int i = 0; i < 6; i++) {
            resultValues[i] = hexToChar((char) (resultHex[i][0] | resultHex[i][1]));
        }

        return new String(resultValues);
    }

    private String getTestReportResult() {
        android.util.Log.d(TAG, "getTestReportResult()---PrizeFactoryTestListActivity.items.length=" + mTestItems.length);
        List<String> resultList = new ArrayList<>();
        for (int itemPos = 0; itemPos < mTestItems.length; itemPos++) {
            resultList.add(mApp.getReportResult(itemPos));
        }
        String fail = getString(R.string.result_error);
        String pass = getString(R.string.pass);
        boolean hasFail = resultList.stream().anyMatch(x -> fail.equals(x));

        mTestReportResult.setTextColor(hasFail ? Color.RED : Color.GREEN);

        return hasFail ? fail : pass;
    }

    private String getTestReportResultItem() {
        StringBuilder info = new StringBuilder();
        for (int i = 0; i < mTestItems.length; i++) {
            String iResult = mApp.getReportResult(i);
            boolean hasResult = null != iResult;
            if (hasResult) {
                if (getString(R.string.result_error).equals(iResult)) {
                    mTestReportResultItem.setVisibility(View.VISIBLE);
                    mTestReportResultItem.setTextColor(Color.RED);
                    info.append((i + 1) + ",");
                }
            }
        }
        return info.toString();
    }
}
