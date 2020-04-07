package com.cydroid.note.app.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class EditScrollView extends ScrollView {
    public EditScrollView(Context context) {
        super(context);
    }

    public EditScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        //return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
        return true;
    }
}
