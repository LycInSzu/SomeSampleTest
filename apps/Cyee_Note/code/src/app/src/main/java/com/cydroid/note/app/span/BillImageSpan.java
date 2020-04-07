package com.cydroid.note.app.span;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.cydroid.note.common.Log;
import com.cydroid.note.R;
import com.cydroid.note.app.Config;

public class BillImageSpan extends ReplacementSpan implements AbstractClickSpan {
    private static final String TAG = "BillImageSpan";
    private final Context mContext;
    private final BillItem mBillItem;
    private int mImageWidth;
    private TextPaint mBluePaint;
    private TextPaint mRedPaint;
    private Drawable mCheckedDrawable = null;
    private Drawable mUncheckedDrawable = null;
    private int LINE_PADDING_TOP;
    private int LINE_PADDING_BOTTOM;

    public BillImageSpan(Context context, BillItem item) {
        mContext = context;
        mBillItem = item;
        init();
    }

    private void init() {
        mImageWidth = Config.EditPage.get(mContext).mBillWidth;
        LINE_PADDING_TOP = mContext.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_top);
        LINE_PADDING_BOTTOM = mContext.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_bottom);
        mBluePaint = new TextPaint();
        mBluePaint.setColor(Color.BLUE);

        mRedPaint = new TextPaint();
        mRedPaint.setColor(Color.RED);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Drawable d = getDrawable();
        Rect rect = d.getBounds();
        Paint.FontMetricsInt fmi = new Paint.FontMetricsInt();
        if (fm != null) {
            fm.ascent = fmi.ascent;
            fm.descent = fmi.descent;

            fm.top = fmi.top;
            fm.bottom = fmi.bottom;
        }

        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int baseLine, int bottom, Paint paint) {
        Drawable drawable = getDrawable();
        int visualTop = top - LINE_PADDING_TOP;
        int visualBottom = baseLine + LINE_PADDING_BOTTOM;
        int transY = visualTop + ((visualBottom - visualTop) - drawable.getBounds().height()) / 2;
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.translate(-x, -transY);
    }

    public Drawable getDrawable() {
        boolean checked = mBillItem.isChecked();
        if (checked) {
            if (mCheckedDrawable == null) {
                mCheckedDrawable = decodeDrawable(mContext, R.drawable.bill_complete);
            }
            return mCheckedDrawable;
        } else {
            if (mUncheckedDrawable == null) {
                mUncheckedDrawable = decodeDrawable(mContext, R.drawable.bill_uncomplete);
            }
            return mUncheckedDrawable;
        }
    }

    public static Drawable decodeDrawable(Context context, int resId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, resId);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            return drawable;
        } catch (Exception e) {
            Log.d(TAG, "Unable to find resource: " + resId);
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        if (mBillItem != null) {
            mBillItem.onBillClick(view);
        }
    }

    @Override
    public boolean isClickValid(TextView widget, MotionEvent event, int lineBottom) {
        int paddingLeft = widget.getTotalPaddingLeft();
        int minX = paddingLeft;
        int maxX = paddingLeft + mImageWidth;
        int clickX = (int) event.getX();
        int clickY = (int) event.getY();
	    //GIONEE wanghaiyan 2017-1-4 modify for #58898 begin
	    if(widget.isLayoutRtl()){
	 	   int width = mContext.getResources().getDisplayMetrics().widthPixels;
		   minX = width - paddingLeft -mImageWidth;
		   maxX = width - paddingLeft;
	    }
	    //GIONEE wanghaiyan 2017-1-4 modify for #58898 end
        return !(clickX < minX || clickX > maxX || clickY >= lineBottom);
    }
}
