package com.pri.factorytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pri.factorytest.Ddr.DdrSingleActivity;
//prize-added by tangan-add emmc test-begin
import com.pri.factorytest.emmc.ScanExceptionService;
import com.pri.factorytest.util.Utils;
//prize-added by tangan-add emmc test-end

public class PrizeOrderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            FactoryTestApplication app = (FactoryTestApplication) context.getApplicationContext();
            boolean mDdrReboot = app.getSharePref().getValue("ddr_test").equals("1");
            if (mDdrReboot) {
                Intent intentAtaInfo = new Intent(context, DdrSingleActivity.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentAtaInfo);
            }

            boolean bReboot = app.getSharePref().getValue("reboot_selected").equals("1");
            if (bReboot) {
                Intent intentAtaInfo = new Intent(context, AgingTestActivity.class);
                intentAtaInfo.putExtra("reboot", true);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentAtaInfo);
            }
			//prize-added by tangan-add emmc test-begin
			int emmcTestCount = -1;
			try{
				emmcTestCount = Integer.parseInt(ScanExceptionService.getInfoFromIndex(Utils.EMCC_TEST_START_SCAN_INDEX,
						Utils.EMCC_TEST_START_SCAN_INDEX_LENGTH));
			}catch(Exception e){
				e.printStackTrace();
			}
			if(emmcTestCount > 0 || ("1".equals(android.os.SystemProperties.get("ro.pri.pcba_clear_test","0")))){
				Intent scanExceptionIntent = new Intent();
				scanExceptionIntent.setClass(context, ScanExceptionService.class);
				context.startService(scanExceptionIntent);
			}
			//prize-added by tangan-add emmc test-end
        } else if ("com.pri.HARDWAREINFO".equals(intent.getAction())) {
            Intent intentFactory = new Intent();
            intentFactory.setClass(context, PrizeHwInfo.class);
            intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentFactory);
        } else {
            String number = intent.getExtras().getString("input");
            if (number.equals("*#8804#")) {
                Intent intentFactory = new Intent(context, PrizeFactoryTestActivity.class);
                intentFactory.putExtra("isAutoTest", true);
                intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentFactory);
            } else if (number.equals("*#8801#")) {
                Intent intentSnInfo = new Intent(context, PrizeSnInfo.class);
                intentSnInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentSnInfo);
            } else if (number.equals("*#8818#")) {
                Intent intentAtaInfo = new Intent(context, PrizeAtaInfo.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentAtaInfo);
            } else if (number.equals("*#8805#")) {
                Intent intentAtaInfo = new Intent(context, AgingTestActivity.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentAtaInfo);
            } else if(number.equals("*#8823#")){
                Intent intentAtaInfo = new Intent(context,ChargerProtectActivity.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentAtaInfo);
            } else if(number.equals("*#57399727#")){
                Intent intentAtaInfo = new Intent(context,PrizeHwAudioTest.class);
                intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentAtaInfo);
            }
        }
    }
}
