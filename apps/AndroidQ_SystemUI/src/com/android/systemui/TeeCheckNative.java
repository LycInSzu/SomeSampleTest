/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui;

import android.os.SystemProperties;
import android.util.Log;

public class TeeCheckNative {
    
    // microtrust key 
	public static final String TeeProp = "vendor.soter.teei.init";
    public static final String FaceIdProp = "vendor.soter.teei.active.faceid";
	public static final String FingerPrintProp = "vendor.soter.teei.active.fp";
	public static final String KeyboxProp = "vendor.soter.teei.googlekey.status";
	
    
    static {
        try{
			System.loadLibrary("checkkeyjni");
		}catch(Throwable e){
            Log.e("TeeCheck", "Throwable ....."+e.toString());
		}
    }
    public static boolean checkMultiActive(){
        if("1".equals(SystemProperties.get("ro.mtk_trustkernel_tee_support"))){
            boolean mRet = checkActive();
            Log.e("TeeCheck","TrustKernel ok ? "+mRet);
            return mRet;
        }else if("1".equals(SystemProperties.get("ro.mtk_microtrust_tee_support"))){
            String mTeeStatue = SystemProperties.get(TeeProp, "UNACTIVE");
			String mFpStatue = SystemProperties.get(FingerPrintProp, "UNACTIVE");
			String mKeyboxStatue = SystemProperties.get(KeyboxProp, "FAIL");
			String FaceIdStatue = SystemProperties.get(FaceIdProp, "UNACTIVE");
            Log.e("TeeCheck","MicroTrust  "+mTeeStatue+" "+mFpStatue+" "+mKeyboxStatue+" "+FaceIdStatue);
			if(1==SystemProperties.getInt("ro.tface.support", 0)){
				return "INIT_OK".equals(mTeeStatue) && "ACTIVE".equals(mFpStatue) && "OK".equals(mKeyboxStatue) && "ACTIVE".equals(FaceIdStatue);
			}else{
				return "INIT_OK".equals(mTeeStatue) && "ACTIVE".equals(mFpStatue) && "OK".equals(mKeyboxStatue);
			}
        }
        Log.e("TeeCheck","Others ??  something maybe wrong!!");
        return true;
    }
    public static native boolean checkActive();
}
