/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.bluetooth.gatt;
/** M: Enable debugging log for sqc test mode @{ */
import android.os.SystemProperties;
/** @} */

/**
 * GattService configuration.
 */
/*package*/ class GattServiceConfig {
    /** M: Enable debugging log for sqc test mode @{ */
    public static final boolean DBG = SystemProperties
                                      .get("persist.vendor.bluetooth.hostloglevel", "")
                                      .equals("sqc");
    public static final boolean VDBG = DBG;
    /** M: Force to disable Le Scan by adb command @{ */
    public static boolean isLeScanDisableByForce() {
        return SystemProperties.get("persist.vendor.bluetooth.setlescan", "")
                               .equals("false");
    }
    /** @} */
    /** @} */
    public static final String TAG_PREFIX = "BtGatt.";
    public static final boolean DEBUG_ADMIN = true;
}
