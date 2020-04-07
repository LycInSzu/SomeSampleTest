package com.cydroid.note.app.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import cyee.widget.CyeeTextView;

/**
 * Created by wuguangjie on 16-8-12.
 */
public class MarqueeTextView extends CyeeTextView {

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        if (TextUtils.TruncateAt.MARQUEE.equals(getEllipsize())) {
            return true;
        }
        return super.isFocused();
    }
}
