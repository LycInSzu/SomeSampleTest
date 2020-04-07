package com.cydroid.note.app.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;

public class ToastManager {

    private Toast mToast;

    public ToastManager(Context context) {
        mToast = new Toast(context.getApplicationContext());

        LayoutInflater inflate = (LayoutInflater)
                context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.toast, null);

        mToast.setView(v);
        mToast.setDuration(Toast.LENGTH_SHORT);
    }

    /**
     * @param message Toast的信息
     */
    public void showToast(String message) {
        if (null != mToast) {
            mToast.setText(message);
            mToast.show();
        }
    }

    /**
     * @param stringId Toast信息的id
     */
    public void showToast(int stringId) {
        if (null != mToast) {
            mToast.setText(NoteAppImpl.getContext().getResources().getString(stringId));
            mToast.show();
        }
    }

    public void destroy() {
        mToast = null;
    }

}
