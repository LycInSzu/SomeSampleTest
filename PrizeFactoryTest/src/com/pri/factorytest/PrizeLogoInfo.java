package com.pri.factorytest;

import com.pri.factorytest.R;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TextView;
import android.os.SystemProperties;
//import android.telephony.TelephonyManager;
//import com.mediatek.telephony.TelephonyManagerEx;
//import android.content.ContentResolver;
//import android.net.Uri;
//import android.content.ContentValues;
//import android.database.Cursor;
//import java.util.ArrayList;
//import android.widget.BaseAdapter;
//import android.widget.ListView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.IBinder;
import android.widget.Toast;

import com.pri.factorytest.NvRAMAgent;

import android.os.ServiceManager;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

//import javax.sound.sampled.AudioFormat.Encoding;

import android.widget.Toast;


import com.android.internal.util.HexDump;
import com.pri.factorytest.util.Utils;

import vendor.mediatek.hardware.nvram.V1_0.INvram;

public class PrizeLogoInfo extends PrizeBaseActivity {

    private static final String TAG = "PrizeLogoInfo";
    private static final int KEY_BOOTANIMATION1_VALUE = 48;//0;//榛樿鍚姩鍔ㄧ敾鍜屽０闊�
    private static final int KEY_BOOTANIMATION2_VALUE = 49;//1;//绗�2濂楀姩鐢诲拰澹伴煶
    private static final int KEY_BOOTANIMATION3_VALUE = 50;//2;//绗�3濂楀姩鐢诲拰澹伴煶
    private static final int KEY_BOOTANIMATION4_VALUE = 51;//3;//绗�4濂楀姩鐢诲拰澹伴煶
    private static final int KEY_BOOTANIMATION5_VALUE = 52;//4;//绗�5濂楀姩鐢诲拰澹伴煶
    private static final int KEY_BOOTANIMATION6_VALUE = 53;//5;//绗�6濂楀姩鐢诲拰澹伴煶
    private static final int KEY_BOOTANIMATION7_VALUE = 54;//6;//绗�7濂楀姩鐢诲拰澹伴煶
    private static final int KEY_BOOTANIMATION8_VALUE = 55;//7;//绗�8濂楀姩鐢诲拰澹伴煶
    private static final int KEY_BOOTANIMATION9_VALUE = 56;//8;//绗�9濂楀姩鐢诲拰澹伴煶
    private static final int KEY_BOOTANIMATION10_VALUE = 57;//9;//绗�10濂楀姩鐢诲拰澹伴煶
    private static final byte BOOT_LOG_1 = 0x30;//"0"
    private static final byte BOOT_LOG_2 = 0x31;//"1"
    private static final byte BOOT_LOG_3 = 0x32;//"2"
    private static final byte BOOT_LOG_4 = 0x33;//"3"
    private static final byte BOOT_LOG_5 = 0x34;//"4"
    private static final byte BOOT_LOG_6 = 0x35;//"5"
    private static final byte BOOT_LOG_7 = 0x36;//"6"
    private static final byte BOOT_LOG_8 = 0x37;//"7"
    private static final byte BOOT_LOG_9 = 0x38;//"8"
    private static final byte BOOT_LOG_10 = 0x39;//"9"
    private static final int AP_CFG_RDCL_LOGO_INFO_LID = 45;

    private static final int LOGO_INFO_INDEX = 350;//5;//105;
    private RadioGroup rgSelectStorage = null;
    private RadioButton mRadioButton = null;


    private Toast mToast;


    private String pcba;
    private String mobile;
    private static final String PRODUCT_INFO_FILENAME = "/vendor/nvdata/APCFG/APRDEB/PRODUCT_INFO";
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prize_logo_info_main);

        if (!fileIsExists("bootanimation02")) {
            RadioButton animation2 = (RadioButton) findViewById(R.id.rb_anmi_sound_2);
            animation2.setVisibility(View.GONE);
        }
        if (!fileIsExists("bootanimation03")) {
            RadioButton animation3 = (RadioButton) findViewById(R.id.rb_anmi_sound_3);
            animation3.setVisibility(View.GONE);
        }
        if (!fileIsExists("bootanimation04")) {
            RadioButton animation4 = (RadioButton) findViewById(R.id.rb_anmi_sound_4);
            animation4.setVisibility(View.GONE);
        }
        if (!fileIsExists("bootanimation05")) {
            RadioButton animation5 = (RadioButton) findViewById(R.id.rb_anmi_sound_5);
            animation5.setVisibility(View.GONE);
        }
        if (!fileIsExists("bootanimation06")) {
            RadioButton animation6 = (RadioButton) findViewById(R.id.rb_anmi_sound_6);
            animation6.setVisibility(View.GONE);
        }
        if (!fileIsExists("bootanimation07")) {
            RadioButton animation7 = (RadioButton) findViewById(R.id.rb_anmi_sound_7);
            animation7.setVisibility(View.GONE);
        }
        if (!fileIsExists("bootanimation08")) {
            RadioButton animation8 = (RadioButton) findViewById(R.id.rb_anmi_sound_8);
            animation8.setVisibility(View.GONE);
        }

        if (!fileIsExists("bootanimation09")) {
            RadioButton animation9 = (RadioButton) findViewById(R.id.rb_anmi_sound_9);
            animation9.setVisibility(View.GONE);
        }
        if (!fileIsExists("bootanimation10")) {
            RadioButton animation10 = (RadioButton) findViewById(R.id.rb_anmi_sound_10);
            animation10.setVisibility(View.GONE);
        }
        //writeProInfo("2",int index)
        Utils.paddingLayout(findViewById(R.id.rg_group), 0, ACTIVITY_TOP_PADDING, 0, 0);

        String logo_index = readProInfo(LOGO_INFO_INDEX);
        Log.d(TAG, "---------onCreate()=" + logo_index);
        initComponent(Integer.parseInt(logo_index));

        mToast = Toast.makeText(this, null, Toast.LENGTH_SHORT);

    }

    public boolean fileIsExists(String nameStr) {
        try {
            File f = new File("/system/media/" + nameStr + ".zip");
            if (!f.exists()) {
                Log.d(TAG, "------------NO--" + nameStr + "exists----------");
                return false;
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.d(TAG, "------------NO-Exception-" + nameStr + "exists----------");
            return false;
        }

        Log.d(TAG, "--------------" + nameStr + "exists----------");
        return true;
    }


    private String readProInfo(int proininfoindex) {
        String st = "";
        try {
            int i = 0;
            INvram agent = INvram.getService();
            //byte[] macAddr = new byte[MAC_ADDRESS_DIGITS];
            if (agent == null) {
                mToast.setText("No support read Proinfo due to NVRAM");
                mToast.show();
                Log.e(TAG, "NvRAMAgent is null");
                return "0";
            }


            String buff = "";
            try {
                buff = agent.readFileByName(PRODUCT_INFO_FILENAME, proininfoindex);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "readProInfo()-----------buff=" + buff);
            Log.d(TAG, "readProInfo()-----------buff.length()=" + buff.length());
            byte[] buffArr = HexDump.hexStringToByteArray(buff.substring(0, buff.length() - 1));

            Log.d(TAG, "readProInfo()-----------buffArr.length=" + buffArr.length);

            for (int j = LOGO_INFO_INDEX - 5; j < buffArr.length; j++) {
                Log.d(TAG, "--buffArr[" + j + "]=" + buffArr[j]);
            }

            //Log.d(TAG,"readProInfo()-----------buffArr[proininfoindex-2]="+buffArr[proininfoindex-2]);
            //Log.d(TAG,"readProInfo()-----------buffArr[proininfoindex-1]="+buffArr[proininfoindex-1]);
            //Log.d(TAG,"readProInfo()-----------String.valueOf(buffArr[proininfoindex-1])="+String.valueOf(buffArr[proininfoindex-1]));

            char c = (char) buffArr[proininfoindex - 1];
            Log.d(TAG, "readProInfo()-----------c=" + c);
            //byte mbyte.
            //byte[] byteData=new byte[5]{0x01,0x02,0x03,0x04,0x05};
            //char[] cChar=Encoding.ASCII.GetChars(byteData);

            //String sn=new String(buff);

            //if(c>=48&&c<=57)
            st = String.valueOf(buffArr[proininfoindex - 1]);

            //else{//c default 0x0,so return "0"
            //	st="0";
            //	Log.d(TAG,"readProInfo()--------------c default value 0x0 ");
            //}

        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
        Log.d(TAG, "readProInfo()---------- st=" + st);
        return st;

		/*IBinder binder = ServiceManager.getService("NvRAMAgent");
        NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte[] buff = null;
		try {
			buff = agent.readFileByName(PRODUCT_INFO_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		char c=(char)buff[index];
		//String sn=new String(buff);
		String st="";
		if(c>=48&&c<=57)
			st=String.valueOf((char)buff[index]);
		else{//c default 0x0,so return "0"
			st="0";
			Log.d(TAG,"--------------c default value 0x0------------ ");
		}
		return st;*/
    }


    private void writeProInfo(String logoindex, int proinfoindex) {
        Log.d(TAG, "yahoo_writeProInfo()-----logoindex=" + logoindex + "  proinfoindex=" + proinfoindex);

        if (null == logoindex || logoindex.length() < 1) {
            Toast.makeText(mContext, "logo index empty", 0).show();
            return;
        }
        try {

            INvram agent = INvram.getService();
            if (agent == null) {
                mToast.setText("No support proinfo address writing due to NVRAM");
                mToast.show();
                Log.e(TAG, "NvRAMAgent is null");
                return;
            }

            String buff = "";
            try {
                buff = agent.readFileByName(PRODUCT_INFO_FILENAME, LOGO_INFO_INDEX);
            } catch (Exception e) {
                e.printStackTrace();
            }


            // Remove \0 in the end
            Log.d(TAG, "writeProInfo()----RAW buff=" + buff);
            Log.d(TAG, "writeProInfo()----buff.length()=" + buff.length());

            if (buff.length() < 2 * LOGO_INFO_INDEX) {
                mToast.setText("The foramt of NVRAM is not correct");
                mToast.show();
                return;
            }

            byte[] buffArr = HexDump.hexStringToByteArray(buff.substring(0, buff.length() - 1));
            Log.d(TAG, "writeProInfo()----buffArr.length=" + buffArr.length);
            Log.d(TAG, "writeProInfo()----LOGO_INFO_INDEX=" + LOGO_INFO_INDEX);
            for (int j = LOGO_INFO_INDEX - 5; j < buffArr.length; j++)
                Log.d(TAG, "writeProInfo()--buffArr[" + j + "]=" + buffArr[j]);

            ArrayList<Byte> dataArray = new ArrayList<Byte>(LOGO_INFO_INDEX);

            byte[] by = logoindex.toString().getBytes();

            for (int i = 0; i < LOGO_INFO_INDEX; i++) {
                if (buffArr[i] == 0x00) {
                    buffArr[i] = " ".toString().getBytes()[0];
                }
                if (i == (LOGO_INFO_INDEX - 1)) {
                    Log.d(TAG, "writeProInfo()---add----by[0]=" + by[0]);
                    dataArray.add(i, by[0]);
                } else {
                    dataArray.add(i, new Byte(buffArr[i]));
                }

            }


            int flag = 0;
            try {

                flag = agent.writeFileByNamevec(PRODUCT_INFO_FILENAME, LOGO_INFO_INDEX, dataArray);
                Log.d(TAG, "writeProInfo()-----flag=" + flag);
            } catch (Exception e) {
                e.printStackTrace();
                mToast.setText(e.getMessage() + ":" + e.getCause());
                mToast.show();
                return;
            }
            mToast.setText("Update successfully.\r\nPlease reboot this device");
            mToast.show();
        } catch (Exception e) {
            mToast.setText(e.getMessage() + ":" + e.getCause());
            mToast.show();
            e.printStackTrace();
        }
		
		
		
		
		
		/*
		try {
            int flag = 0;
			byte[] buff=null;
			IBinder binder = ServiceManager.getService("NvRAMAgent");
			NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
			
			try {
				buff = agent.readFileByName(PRODUCT_INFO_FILENAME);
			} catch (Exception e) {
				e.printStackTrace();
			}
			byte[] by = logoinfo.toString().getBytes();
			
			for(int i=0;i<LOGO_INFO_index+1;i++)
			{
				if(buff[i]==0x00){
					buff[i] = " ".toString().getBytes()[0];
				}				
			}	   
			
			buff[index] = by[0];
            try {
                flag = agent.writeFileByName(PRODUCT_INFO_FILENAME, buff);
		    Log.d(TAG,"-------writeProInfo()----------flag="+flag+"   logoinfo="+logoinfo);	
            } catch (Exception e) {
                e.printStackTrace();
            }
			
		} catch (Exception e) {            
            e.printStackTrace();
        }*/
    }


    private void writeprotect_f(byte logo_choice) {
        byte BufToWrite[] = new byte[1];

        BufToWrite[0] = logo_choice;
        File logo_choiceFile = new File("/protect_f/", "logo_choice.file");

        try {
            FileOutputStream fos = new FileOutputStream(logo_choiceFile, false);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    fos);
            bufferedOutputStream.write(BufToWrite, 0, BufToWrite.length);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (Exception e) {
            Log.e(TAG,
                    "Exception Occured: Trying to write logo_choice.file "
                            + e.toString());
        }
    }

    private void initComponent(int index) {
        Log.d(TAG, "initComponent()----------index=" + index);
        rgSelectStorage = (RadioGroup) findViewById(R.id.rg_group);
        switch (/*SystemProperties.getInt("persist.sys.bootanimation",0)*/index) {
            case KEY_BOOTANIMATION1_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_1);
                mRadioButton.setChecked(true);
                break;
            case KEY_BOOTANIMATION2_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_2);
                mRadioButton.setChecked(true);
                break;
            case KEY_BOOTANIMATION3_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_3);
                mRadioButton.setChecked(true);
                break;
            case KEY_BOOTANIMATION4_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_4);
                mRadioButton.setChecked(true);
                break;
            case KEY_BOOTANIMATION5_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_5);
                mRadioButton.setChecked(true);
                break;
            case KEY_BOOTANIMATION6_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_6);
                mRadioButton.setChecked(true);
                break;
            case KEY_BOOTANIMATION7_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_7);
                mRadioButton.setChecked(true);
                break;
            case KEY_BOOTANIMATION8_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_8);
                mRadioButton.setChecked(true);
                break;
            case KEY_BOOTANIMATION9_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_9);
                mRadioButton.setChecked(true);
                break;
            case KEY_BOOTANIMATION10_VALUE:
                mRadioButton = (RadioButton) findViewById(R.id.rb_anmi_sound_10);
                mRadioButton.setChecked(true);
                break;
            default:
                break;
        }
        rgSelectStorage
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.rb_anmi_sound_1:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION1_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION1_VALUE - 48));
                                writeProInfo("0", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_1);
                                Toast.makeText(getApplicationContext(), R.string.reset_setting_success, Toast.LENGTH_LONG).show();
                                break;
                            case R.id.rb_anmi_sound_2:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION1_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION2_VALUE - 48));
                                writeProInfo("1", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_2);
                                Toast.makeText(getApplicationContext(), R.string.reset_setting_success, Toast.LENGTH_LONG).show();
                                break;
                            case R.id.rb_anmi_sound_3:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION3_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION3_VALUE - 48));
                                writeProInfo("2", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_3);
                                break;
                            case R.id.rb_anmi_sound_4:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION4_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION4_VALUE - 48));
                                writeProInfo("3", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_4);
                                break;
                            case R.id.rb_anmi_sound_5:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION5_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION5_VALUE - 48));
                                writeProInfo("4", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_5);
                                break;
                            case R.id.rb_anmi_sound_6:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION6_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION6_VALUE - 48));
                                writeProInfo("5", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_6);
                                break;
                            case R.id.rb_anmi_sound_7:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION7_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION7_VALUE - 48));
                                writeProInfo("6", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_7);
                                break;
                            case R.id.rb_anmi_sound_8:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION8_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION8_VALUE - 48));
                                writeProInfo("7", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_8);
                                break;
                            case R.id.rb_anmi_sound_9:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION9_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION9_VALUE - 48));
                                writeProInfo("8", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_9);
                                break;
                            case R.id.rb_anmi_sound_10:
                                Log.d(TAG, "use animation " + KEY_BOOTANIMATION10_VALUE);
                                SystemProperties.set("persist.sys.bootanimation", Integer.toString(KEY_BOOTANIMATION10_VALUE - 48));
                                writeProInfo("9", LOGO_INFO_INDEX);
                                //writeprotect_f(BOOT_LOG_10);
                                break;
                        }
                    }
                });
    }


}
