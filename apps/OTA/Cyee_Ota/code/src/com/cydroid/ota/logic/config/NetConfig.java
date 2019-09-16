package com.cydroid.ota.logic.config;
import android.os.SystemProperties;

/**
 * Created by borney on 14-9-11.
 */
public final class NetConfig {
   // public static final String TEST_HOST = "http://test1.gionee.com";
    //public static final String NORMAL_HOST = "http://update.gionee.com";
    //chenyee yewq 2017-10-26 modify for 247205 begin
    //public static final String TEST_HOST = "http://18.8.8.249:81";
	//Chenyee <CY_bug> <xuyongji> <20180528> modify for CSW1703A-2951 begin
    public static final String TEST_HOST = "http://otatest.chenyee.com:90";
	//Chenyee <CY_bug> <xuyongji> <20180528> modify for CSW1703A-2951 end
    //chenyee yewq 2017-10-26 modify for 247205 end
	//Chenyee <CY_Bug> <xuyongji> <20171110> modify for SW17W16A-329 begin
    public static  String NORMAL_HOST = "http://" + SystemProperties.get("ro.cy.ota.server");
    /*static {
        if(SystemProperties.get("ro.gn.oversea.product").equals("yes")) {
            NORMAL_HOST = "http://" + SystemProperties.get("ro.cy.ota.server");
        }
    }*/
	//Chenyee <CY_Bug> <xuyongji> <20171110> modify for SW17W16A-329 end
    public static final String GIONEE_HTTP_CHECK = "/ota/check.do?";
    public static final String GIONEE_HTTP_QUERY = "/ota/query.do?";
    public static final String QUESTIONNAIRE_TEST_HOST = "http://t-telepath.gionee.com";
    public static final String QUESTIONNAIRE_NOMAL_HOST = "http://telepath.gionee.com";
    public static final String GIONEE_QUESTIONNAIRE_CHECK = "/tlp-api/ota/survey.do?";
    public static final String APPS_CHECK = "/synth/open/checkUpgradeList.do?";
    public static final String APPS_GET_LIST = "/synth/open/appList.do?";
    public static final String GIONEE_RECOMMEND_GET_LIST = "/synth/open/appsWithGrp.do?";

    public static final int GIONEE_CONNECT_TIMEOUT = 6 * 1000;
    public static final int GIONEE_SOCKET_TIMEOUT = 10 * 1000;
    public static final int SOCKET_BUFFER_SIZE = 2048;

    public static final int CONNECTION_TYPE_WAP = 100;
    public static final int CONNECTION_TYPE_NET = 201;

    public static enum ConnectionType {
        CONNECTION_TYPE_IDLE, CONNECTION_TYPE_WIFI, CONNECTION_TYPE_3G, CONNECTION_TYPE_2G, CONNECTION_TYPE_4G
    }

    public static final String CONNECTION_MOBILE_DEFAULT_HOST = "10.0.0.172";
    public static final int CONNECTION_MOBILE_DEFAULT_PORT = 80;
}
