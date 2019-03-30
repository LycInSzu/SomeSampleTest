/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2016. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.common.debug;

import android.text.TextUtils;/*prize-add-add mode-xiaoping-20180901*/

import com.mediatek.camera.common.debug.LogUtil.Tag;

/**
 * A wrapper class for AOSP Log.java, All other classes logging should use this class.
 */
public class LogHelper {
    private static final String LOG_TAG = "CamAp_";/*prize-add-add mode-xiaoping-20180901*/
    /**
     * Send a ui log message, it will have a prefix for ui log.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void ui(Tag tag, String msg) {
        msg = new StringBuilder("[CamUI] ").append(msg).toString();
        i(tag, msg);
    }

    /**
    * Send a DEBUG log message.
    * @param tag Used to identify the source of a log message.  It usually identifies
    *        the class or activity where the log call occurs.
    * @param msg The message you would like logged.
    */
    public static void d(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(generateTag(), msg);         	/*prize-add-add mode-xiaoping-20180901-begin*/
        }
    }

    /**
     * Send a DEBUG log message.
     * @param tag tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void d(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(generateTag(), LogUtil.addTags(      	/*prize-add-add mode-xiaoping-20180901-begin*/
                    instance, msg));
        }
    }

    /***
     * Send a DEBUG log message.
     * @param tag  Used to identify the source of a log message.
     * @param instance instance Prefixes a message with with a hashcode tag of the object.
     * @param msg  The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void d(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(generateTag(), LogUtil.addTags(  	/*prize-add-add mode-xiaoping-20180901-begin*/
                    instance, msg, tags));
        }
    }

    /**
     * Send a DEBUG log message.
     * @param tag tag tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    public static void d(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(generateTag(), msg, tr);  	/*prize-add-add mode-xiaoping-20180901-begin*/
        }
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void e(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(generateTag(), msg);      	/*prize-add-add mode-xiaoping-20180901-begin*/
        }
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void e(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(generateTag(), LogUtil.addTags( 	/*prize-add-add mode-xiaoping-20180901-begin*/
                    instance, msg));
        }
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void e(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.e(generateTag(), LogUtil.addTags(	/*prize-add-add mode-xiaoping-20180901-begin*/
                    instance, msg, tags));
        }
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    public static void e(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(generateTag(), msg, tr);	/*prize-add-add mode-xiaoping-20180901-begin*/
        }
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void i(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.INFO)) {
            android.util.Log.i(generateTag(), msg);	/*prize-add-add mode-xiaoping-20180901-begin*/
        }
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void i(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.INFO)) {
            android.util.Log.i(generateTag(), LogUtil.addTags(  	/*prize-add-add mode-xiaoping-20180901-begin*/
                    instance, msg));
        }
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void i(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.i(generateTag(), LogUtil.addTags(    	/*prize-add-add mode-xiaoping-20180901-begin*/
                    instance, msg, tags));
        }
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    public static void i(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.INFO)) {
            android.util.Log.i(generateTag(), msg, tr);	/*prize-add-add mode-xiaoping-20180901-begin*/
        }
    }

    /**
     * Send an VERBOSE log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void v(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.VERBOSE)) {
            android.util.Log.v(generateTag(), msg);	/*prize-add-add mode-xiaoping-20180901-begin*/
        }
    }

    /**
     * Send an VERBOSE log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void v(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.VERBOSE)) {
            android.util.Log.v(generateTag(), LogUtil.addTags(instance, msg));
        }
    }

    /**
     * Send an VERBOSE log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void v(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.v(generateTag(), LogUtil.addTags(       	/*prize-add-add mode-xiaoping-20180901*/
                    instance, msg, tags));
        }
    }

    /**
     * Send an VERBOSE log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static void v(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.VERBOSE)) {
            android.util.Log.v(generateTag(), msg, tr);	/*prize-add-add mode-xiaoping-20180901*/
        }
    }

    /**
     * Send an WARNNING log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void w(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(generateTag(), msg);	/*prize-add-add mode-xiaoping-20180901*/
        }
    }

    /**
     * Send an WARNNING log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void w(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(generateTag(), LogUtil.addTags(instance, msg));	/*prize-add-add mode-xiaoping-20180901*/
        }
    }

    /**
     * Send an WARNNING log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void w(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.w(generateTag(), LogUtil.addTags(      	/*prize-add-add mode-xiaoping-20180901*/
                    instance, msg, tags));
        }
    }

    /**
     * Send an WARNNING log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    public static void w(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(generateTag(), msg, tr);	/*prize-add-add mode-xiaoping-20180901*/
        }
    }
	/*prize-add-add mode-xiaoping-20180901-begin*/
    public static void i(String string, String msg) {
            android.util.Log.i(generateTag(string), msg);
    }

    private static String generateTag() {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        tag = TextUtils.isEmpty(LOG_TAG) ? tag : LOG_TAG + ":" + tag;
        return tag;
    }

    private static String generateTag(String string) {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        tag = TextUtils.isEmpty((LOG_TAG+string)) ? tag : (LOG_TAG+string) + ":" + tag;
        return tag;
    }
	/*prize-add-add mode-xiaoping-20180901-end*/
}