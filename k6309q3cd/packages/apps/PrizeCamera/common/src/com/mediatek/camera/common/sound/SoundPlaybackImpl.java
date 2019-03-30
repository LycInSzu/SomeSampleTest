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
 * MediaTek Inc. (C) 2015. All rights reserved.
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
package com.mediatek.camera.common.sound;

import android.content.Context;

import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
/*prize-modify-add camera mute and video mute-xiaoping-20190320-start*/
import com.mediatek.camera.portability.SystemProperties;
/*prize-modify-add camera mute and video mute-xiaoping-20190320-end*/
/**
 * Use this to play system-standard sounds for various camera actions or play
 * custom sounds.
 */
public class SoundPlaybackImpl implements ISoundPlayback {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(SoundPlaybackImpl.class.getSimpleName());

    private Context mContext;
    private MediaActionSoundPlayer mMediaActionSoundPlayer;
    private SoundPlayer mSoundPlayer;
    /*prize-modify-add camera mute-xiaoping-20181009-start*/
    private IApp mApp;
    /*prize-modify-add camera mute-xiaoping-20181009-end*/
    /**
     * Construct an instance of ISoundPlayBack.
     *
     * @param appContext app context.
     */
    public SoundPlaybackImpl(Context appContext) {
        mContext = appContext;
        mMediaActionSoundPlayer = new MediaActionSoundPlayer();
        mSoundPlayer = new SoundPlayer(mContext);
    }

    /*prize-modify-add camera mute-xiaoping-20181009-start*/
    public SoundPlaybackImpl(Context appContext, IApp app) {
        mContext = appContext;
        mMediaActionSoundPlayer = new MediaActionSoundPlayer();
        mSoundPlayer = new SoundPlayer(mContext);
        mApp = app;
    }
    /*prize-modify-add camera mute-xiaoping-20181009-end*/

    @Override
    public void play(int action) {
        LogHelper.d(TAG, "[play] play sound with action " + action);
        /*prize-modify-add camera mute and video mute-xiaoping-20190320-start*/
        if (SystemProperties.getInt("ro.pri.current.project",0) == 2 && action == ISoundPlayback.START_VIDEO_RECORDING || action == ISoundPlayback.STOP_VIDEO_RECORDING) {
            mMediaActionSoundPlayer.play(action,mApp.getCameraMuteValue(),mApp.getSettingValue("key_videomute","off",mApp.getAppUi().getCameraId()));
        } else {
            mMediaActionSoundPlayer.play(action,mApp.getCameraMuteValue(),"off");
        }
        /*prize-modify-add camera mute and video mute-xiaoping-20190320-end*/
    }

    @Override
    public void play(int resourceId, float volume) {
        LogHelper.d(TAG, "[play] play sound with resourceId " + resourceId + ",volume " + volume);
        mSoundPlayer.play(resourceId, volume);
    }

    @Override
    public void pause() {
        mSoundPlayer.unloadSound();
    }

    @Override
    public void release() {
        LogHelper.d(TAG, "[release]");
        mMediaActionSoundPlayer.release();
        mSoundPlayer.unloadSound();
        mSoundPlayer.release();
    }
}
