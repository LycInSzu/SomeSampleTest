package com.pri.factorytest;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by prize on 2018/11/21.
 */

public class PrizeListview extends ListView {

    public PrizeListview(Context context) {
        super(context);
    }

    public PrizeListview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrizeListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
