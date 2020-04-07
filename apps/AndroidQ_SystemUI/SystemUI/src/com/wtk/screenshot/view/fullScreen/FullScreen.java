
package com.wtk.screenshot.view.fullScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wtk.screenshot.util.ShotUtil;
import com.android.systemui.R;

public class FullScreen extends FrameLayout {
    /* Common */
    private Context mContext;

    // Util
    private ShotUtil mShotUtil;

    /* View */
    private ImageView myScreen;
    private FrameLayout instruction;
    private ImageView test;

    public FullScreen(Context context) {
        this(context, null);
    }

    public FullScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (isInEditMode()) {
            return;
        }
    }

    public FullScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mShotUtil = ShotUtil.getInstance(mContext);

        View v = getPartView();
        this.addView(v);

        myScreen = (ImageView) v.findViewById(R.id.full_image);
        instruction = (FrameLayout) v.findViewById(R.id.instruction);
        test = (ImageView) v.findViewById(R.id.test);

        //modify by wangjian for YWSW-858 20190413 start
        //instruction.setVisibility(View.VISIBLE);
        instruction.setVisibility(View.GONE);
        //modify by wangjian for YWSW-858 20190413 end
    }

    public View getPartView() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.full_screen, null);
    }

    public void reset() {
        test.setVisibility(View.GONE);
        myScreen.setImageBitmap(mShotUtil.getFullScreenBitmap());
    }

    public void cancel() {

    }

    public void clear() {

    }

    private void test(Bitmap bitmap) {
        test.setVisibility(View.VISIBLE);
        test.setImageBitmap(bitmap);

    }

    private Bitmap getBitmap() {
        return mShotUtil.getFullScreenBitmap();
    }

    public void saveBitmap() {
        Bitmap saveBitmap = getBitmap();
        if (ShotUtil.IS_TEST) {
            test(saveBitmap);
        } else {
            mShotUtil.saveBitMap(saveBitmap);
        }
    }

    public boolean onMyTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                instruction.setVisibility(View.GONE);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //add by wangjian for YWSW-817 20190410 start
        //Log.e("wangjian","FullScreen onDraw = " + canvas.isHardwareAccelerated() + " / " + canvas.isHwBitmapsInSwModeEnabled());
        canvas.setHwBitmapsInSwModeEnabled(true);
        //add by wangjian for YWSW-817 20190410 end
        super.onDraw(canvas);
    }
}
