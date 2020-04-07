package com.cydroid.note.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.cydroid.note.R;

/**
 * Created by spc on 16-3-31.
 */
public class NoteRootView extends RelativeLayout{

    public NoteRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        NoteContainerView container = null;
        FrameLayout footer = null;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getId() == R.id.abstract_note_activity_layout_footer) {
                footer = (FrameLayout) child;
            } else if (child.getId() == R.id.abstract_note_activity_layout_content) {
                container = (NoteContainerView) child;
            }
        }

        if (container != null && footer!= null) {
            container.setFooterViewTop(footer.getTop());
        }

    }
}
