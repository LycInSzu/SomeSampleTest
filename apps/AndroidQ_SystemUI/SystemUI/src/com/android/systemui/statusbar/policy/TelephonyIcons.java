/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.policy;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.MobileSignalController.MobileIconGroup;

import java.util.HashMap;
import java.util.Map;

class TelephonyIcons {

    //add for EJQQQ-94 modify mobile signal icon by liyuchong 20191122 begin
    static final int TELEPHONY_NO_NETWORK = R.drawable.stat_sys_signal_null;
	static final int[][] TELEPHONY_SIGNAL_STRENGTH = {
        { R.drawable.stat_sys_signal_level_0,
          R.drawable.stat_sys_signal_level_1,
          R.drawable.stat_sys_signal_level_2,
          R.drawable.stat_sys_signal_level_3,
          R.drawable.stat_sys_signal_level_4 },
        { R.drawable.stat_sys_signal_level_0,
          R.drawable.stat_sys_signal_level_1,
          R.drawable.stat_sys_signal_level_2,
          R.drawable.stat_sys_signal_level_3,
          R.drawable.stat_sys_signal_level_4 }
    };

    //CarrierNetworkChange
    static final int[][] TELEPHONY_CARRIER_NETWORK_CHANGE = {
            { R.drawable.stat_sys_signal_carrier_network_change_animation,
              R.drawable.stat_sys_signal_carrier_network_change_animation,
              R.drawable.stat_sys_signal_carrier_network_change_animation,
              R.drawable.stat_sys_signal_carrier_network_change_animation,
              R.drawable.stat_sys_signal_carrier_network_change_animation },
            { R.drawable.stat_sys_signal_carrier_network_change_animation,
              R.drawable.stat_sys_signal_carrier_network_change_animation,
              R.drawable.stat_sys_signal_carrier_network_change_animation,
              R.drawable.stat_sys_signal_carrier_network_change_animation,
              R.drawable.stat_sys_signal_carrier_network_change_animation }
        };
    static final int ICON_CARRIER_NETWORK_CHANGE = R.drawable.stat_sys_signal_carrier_network_change_animation;
    //add for EJQQQ-94 modify mobile signal icon by liyuchong 20191122 end

    //***** Data connection icons
    static final int FLIGHT_MODE_ICON = R.drawable.stat_sys_airplane_mode;

//    modify for EJQQ-1115 by liyuchong 20191030 begin
//    /// M: show data icon above mobile signal
//    //  replace data icons with smaller icon {@
//    /*remove AOSP
//    static final int ICON_LTE = R.drawable.ic_lte_mobiledata;
//    static final int ICON_LTE_PLUS = R.drawable.ic_lte_plus_mobiledata;
//    static final int ICON_G = R.drawable.ic_g_mobiledata;
//    static final int ICON_E = R.drawable.ic_e_mobiledata;
//    static final int ICON_H = R.drawable.ic_h_mobiledata;
//    static final int ICON_H_PLUS = R.drawable.ic_h_plus_mobiledata;
//    static final int ICON_3G = R.drawable.ic_3g_mobiledata;
//    static final int ICON_4G = R.drawable.ic_4g_mobiledata;
//    static final int ICON_4G_PLUS = R.drawable.ic_4g_plus_mobiledata;
//    static final int ICON_5G_E = R.drawable.ic_5g_e_mobiledata;
//    static final int ICON_1X = R.drawable.ic_1x_mobiledata;
//    static final int ICON_5G = R.drawable.ic_5g_mobiledata;
//    static final int ICON_5G_PLUS = R.drawable.ic_5g_plus_mobiledata;*/
//
//    // replace with smaller icons
//    static final int ICON_LTE = R.drawable.stat_sys_data_fully_connected_lte;
//    static final int ICON_LTE_PLUS = R.drawable.stat_sys_data_fully_connected_lte_plus;
//    static final int ICON_G = R.drawable.stat_sys_data_fully_connected_g;
//    static final int ICON_E = R.drawable.stat_sys_data_fully_connected_e;
//    static final int ICON_H = R.drawable.stat_sys_data_fully_connected_h;
//    static final int ICON_H_PLUS = R.drawable.stat_sys_data_fully_connected_h_plus;
//    static final int ICON_3G = R.drawable.stat_sys_data_fully_connected_3g;
//    static final int ICON_4G = R.drawable.stat_sys_data_fully_connected_4g;
//    static final int ICON_4G_PLUS = R.drawable.stat_sys_data_fully_connected_4g_plus;
//    static final int ICON_5G_E = R.drawable.stat_sys_data_fully_connected_5g_e;
//    static final int ICON_1X = R.drawable.stat_sys_data_fully_connected_1x;
//    static final int ICON_5G = R.drawable.stat_sys_data_fully_connected_5g;
//    static final int ICON_5G_PLUS = R.drawable.stat_sys_data_fully_connected_5g_plus;
//    /// @}

    static final int ICON_LTE = R.drawable.ic_lte_mobiledata;
    static final int ICON_LTE_PLUS = R.drawable.ic_lte_plus_mobiledata;
    static final int ICON_G = R.drawable.ic_g_mobiledata;
    static final int ICON_E = R.drawable.ic_e_mobiledata;
    static final int ICON_H = R.drawable.ic_h_mobiledata;
    static final int ICON_H_PLUS = R.drawable.ic_h_plus_mobiledata;
    static final int ICON_3G = R.drawable.ic_3g_mobiledata;
    static final int ICON_4G = R.drawable.ic_4g_mobiledata;
	//add TEJWQE-74 xudong.zhang 20200312(start)
    static final int ICON_LTE_BLU = R.drawable.stat_sys_data_fully_connected_lte_blu;
	//add TEJWQE-74 xudong.zhang 20200312(end)
    static final int ICON_4G_PLUS = R.drawable.ic_4g_plus_mobiledata;
    static final int ICON_5G_E = R.drawable.ic_5g_e_mobiledata;
    static final int ICON_1X = R.drawable.ic_1x_mobiledata;
    static final int ICON_5G = R.drawable.ic_5g_mobiledata;
    static final int ICON_5G_PLUS = R.drawable.ic_5g_plus_mobiledata;
//    modify for EJQQ-1115 by liyuchong 20191030 end
	//add TEJWQE-87 xudong.zhang 20200318(start)
    static final int ROAMING_ICON_E = R.drawable.stat_sys_data_fully_connected_roam_e;
    static final int ROAMING_ICON_4G = R.drawable.stat_sys_data_fully_connected_roam_4g;
    static final int ROAMING_ICON_LTE = R.drawable.stat_sys_data_fully_connected_roam_lte;
	//add TEJWQE-87 xudong.zhang 20200318(end)
    static final MobileIconGroup CARRIER_NETWORK_CHANGE = new MobileIconGroup(
            "CARRIER_NETWORK_CHANGE",
            TelephonyIcons.TELEPHONY_CARRIER_NETWORK_CHANGE,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.ICON_CARRIER_NETWORK_CHANGE,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.carrier_network_change_mode,
            0,
            false);

    static final MobileIconGroup THREE_G = new MobileIconGroup(
            "3G",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_3g,
            TelephonyIcons.ICON_3G,
            true);

    static final MobileIconGroup WFC = new MobileIconGroup(
            "WFC",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            0, 0, false);

    static final MobileIconGroup UNKNOWN = new MobileIconGroup(
            "Unknown",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            0, 0, false);

    static final MobileIconGroup E = new MobileIconGroup(
            "E",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_edge,
            TelephonyIcons.ICON_E,
            false);

    static final MobileIconGroup ONE_X = new MobileIconGroup(
            "1X",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_cdma,
            TelephonyIcons.ICON_1X,
            true);

    static final MobileIconGroup G = new MobileIconGroup(
            "G",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_gprs,
            TelephonyIcons.ICON_G,
            false);

    static final MobileIconGroup H = new MobileIconGroup(
            "H",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_3_5g,
            TelephonyIcons.ICON_H,
            false);

    static final MobileIconGroup H_PLUS = new MobileIconGroup(
            "H+",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_3_5g_plus,
            TelephonyIcons.ICON_H_PLUS,
            false);

    static final MobileIconGroup FOUR_G = new MobileIconGroup(
            "4G",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_4g,
            TelephonyIcons.ICON_4G,
            true);
    
	//add TEJWQE-74 xudong.zhang 20200312(start)
    static final MobileIconGroup LTE_BLU = new MobileIconGroup(
            "4G LTE",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_4g,
            TelephonyIcons.ICON_LTE_BLU,
            false
    );
	//add TEJWQE-74 xudong.zhang 20200312(end)

    static final MobileIconGroup FOUR_G_PLUS = new MobileIconGroup(
            "4G+",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0,0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_4g_plus,
            TelephonyIcons.ICON_4G_PLUS,
            true);

    static final int ICON_4GLTE = R.drawable.stat_sys_data_fully_connected_4g_lte;
    static final MobileIconGroup FOUR_G_LTE = new MobileIconGroup(
            "4G-LTE",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0,0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_4g_plus,
            TelephonyIcons.ICON_4GLTE,
            true);

    static final MobileIconGroup LTE = new MobileIconGroup(
            "LTE",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_lte,
            TelephonyIcons.ICON_LTE,
            true);

    static final MobileIconGroup LTE_PLUS = new MobileIconGroup(
            "LTE+",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_lte_plus,
            TelephonyIcons.ICON_LTE_PLUS,
            true);

    static final MobileIconGroup LTE_CA_5G_E = new MobileIconGroup(
            "5Ge",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_5ge,
            TelephonyIcons.ICON_5G_E,
            true);

    static final MobileIconGroup NR_5G = new MobileIconGroup(
            "5G",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0,
            0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_5g,
            TelephonyIcons.ICON_5G,
            true);

    static final MobileIconGroup NR_5G_PLUS = new MobileIconGroup(
            "5G_PLUS",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0,
            0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_5g_plus,
            TelephonyIcons.ICON_5G_PLUS,
            true);

    static final MobileIconGroup DATA_DISABLED = new MobileIconGroup(
            "DataDisabled",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.cell_data_off_content_description,
            0,
            false);

    static final MobileIconGroup NOT_DEFAULT_DATA = new MobileIconGroup(
            "NotDefaultData",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.not_default_data_content_description,
            0,
            false);
	//add TEJWQE-87 xudong.zhang 20200318(start)
    static final MobileIconGroup BLU_ROAMING_E = new MobileIconGroup(
            "RE",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_edge,
            TelephonyIcons.ROAMING_ICON_E,
            false);

    static final MobileIconGroup BLU_ROAMING_FOUR_G = new MobileIconGroup(
            "R4G",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_4g,
            TelephonyIcons.ROAMING_ICON_4G,
            true);
			
    static final MobileIconGroup BLU_ROAMING_LTE = new MobileIconGroup(
            "RLTE",
            TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH,
            null,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH,
            0, 0,
            TelephonyIcons.TELEPHONY_NO_NETWORK,
            0,
            AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0],
            R.string.data_connection_4g,
            TelephonyIcons.ROAMING_ICON_LTE,
            false
    );
	//add TEJWQE-87 xudong.zhang 20200318(end)

    // When adding a new MobileIconGround, check if the dataContentDescription has to be filtered
    // in QSCarrier#hasValidTypeContentDescription

    /** Mapping icon name(lower case) to the icon object. */
    static final Map<String, MobileIconGroup> ICON_NAME_TO_ICON;
    static {
        ICON_NAME_TO_ICON = new HashMap<>();
        ICON_NAME_TO_ICON.put("carrier_network_change", CARRIER_NETWORK_CHANGE);
        ICON_NAME_TO_ICON.put("3g", THREE_G);
        ICON_NAME_TO_ICON.put("wfc", WFC);
        ICON_NAME_TO_ICON.put("unknown", UNKNOWN);
        ICON_NAME_TO_ICON.put("e", E);
        ICON_NAME_TO_ICON.put("1x", ONE_X);
        ICON_NAME_TO_ICON.put("g", G);
        ICON_NAME_TO_ICON.put("h", H);
        ICON_NAME_TO_ICON.put("h+", H_PLUS);
        ICON_NAME_TO_ICON.put("4g", FOUR_G);
        ICON_NAME_TO_ICON.put("4g+", FOUR_G_PLUS);
        ICON_NAME_TO_ICON.put("5ge", LTE_CA_5G_E);
        ICON_NAME_TO_ICON.put("lte", LTE);
        ICON_NAME_TO_ICON.put("lte+", LTE_PLUS);
        ICON_NAME_TO_ICON.put("5g", NR_5G);
        ICON_NAME_TO_ICON.put("5g_plus", NR_5G_PLUS);
        ICON_NAME_TO_ICON.put("datadisable", DATA_DISABLED);
        ICON_NAME_TO_ICON.put("notdefaultdata", NOT_DEFAULT_DATA);
    }
}

