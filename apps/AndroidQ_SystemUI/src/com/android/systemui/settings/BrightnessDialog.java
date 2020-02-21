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

package com.android.systemui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.SystemUIApplication;
import android.widget.SeekBar;
import android.graphics.Color;

/** A dialog that provides controls for adjusting the screen brightness. */
public class BrightnessDialog extends Activity {

    private BrightnessController mBrightnessController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Window window = getWindow();

        window.setGravity(Gravity.TOP);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.requestFeature(Window.FEATURE_NO_TITLE);
        //add for TEWBW-824 by liyuchong 20200114 begin
        setTheme(R.style.brightnessDialogTheme);
        //add for TEWBW-824 by liyuchong 20200114 end

        View v = LayoutInflater.from(this).inflate(
                R.layout.quick_settings_brightness_dialog, null);
        //add for EJQQQ-354 by liyuchong 20191210 begin
        if (SystemUIApplication.useDgCustomUI) {
            v.setBackgroundColor(getResources().getColor(R.color.dg_qs_expaned_background_color));
        }
        if (SystemUIApplication.useDkCustomUI) {
            v.setBackgroundColor(Color.BLACK);
        }
        //add for EJQQQ-354 by liyuchong 20191210 end
        setContentView(v);

        final ToggleSliderView slider = findViewById(R.id.brightness_slider);
        //add for EJQQQ-354 by liyuchong 20191210 begin
        if (SystemUIApplication.useDgCustomUI) {
            ((SeekBar)slider.findViewById(R.id.slider)).setThumb(getResources().getDrawable(R.drawable.ic_brightness_thumb_dg));
        }
        //add for EJQQQ-354 by liyuchong 20191210 end
        mBrightnessController = new BrightnessController(this, slider);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBrightnessController.registerCallbacks();
        MetricsLogger.visible(this, MetricsEvent.BRIGHTNESS_DIALOG);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MetricsLogger.hidden(this, MetricsEvent.BRIGHTNESS_DIALOG);
        mBrightnessController.unregisterCallbacks();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }
}
