package com.cydroid.note.app.span;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public interface AbstractClickSpan {

    void onClick(View view);

    boolean isClickValid(TextView widget, MotionEvent event, int lineBottom);
}
