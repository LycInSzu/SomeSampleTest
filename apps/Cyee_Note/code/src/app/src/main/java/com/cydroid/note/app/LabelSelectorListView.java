package com.cydroid.note.app;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.cydroid.note.R;

public class LabelSelectorListView extends ListView {
    private int mMaxHeight;

    public LabelSelectorListView(Context context) {
        this(context, null);
    }

    public LabelSelectorListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public LabelSelectorListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMaxHeight = context.getResources().getDimensionPixelSize(R.dimen.label_selector_dialog_list_max_height);
    }

/*
    public LabelSelectorListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
*/

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
