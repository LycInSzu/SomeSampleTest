
package com.wtk.screenshot.view.localScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.wtk.screenshot.util.ShotUtil;
import com.wtk.screenshot.util.imageZoom.CropImageView;
import com.android.systemui.R;

import android.widget.ImageView;

public class LocalScreen extends FrameLayout {
    /* Common */
    // Util
    private Context mContext;
    private ShotUtil mShotUtil;

    //Flag
    private Bitmap saveBitmap;

    /* View */
    private FrameLayout instruction;
    private CropImageView myScreen;
    private ImageView test;

    public LocalScreen(Context context) {
        this(context, null);
    }

    public LocalScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (isInEditMode()) {
            return;
        }
    }

    public LocalScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mShotUtil = ShotUtil.getInstance(mContext);

        View v = getPartView();
        this.addView(v);

        instruction = (FrameLayout) v.findViewById(R.id.instruction);
        myScreen = (CropImageView) v.findViewById(R.id.local_view);
        test = (ImageView) v.findViewById(R.id.test);

        //modify by wangjian for YWSW-858 20190413 start
        //instruction.setVisibility(View.VISIBLE);
        instruction.setVisibility(View.GONE);
        //modify by wangjian for YWSW-858 20190413 end
    }

    public View getPartView() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.local_screen, null);
    }

    public void reset() {
        test.setVisibility(View.GONE);
        myScreen.reset(mShotUtil.getFullScreenBitmap());
    }

    public void cancel() {

    }

    public void clear() {
        if (saveBitmap != null && !saveBitmap.isRecycled()) {
            saveBitmap.recycle();
            saveBitmap = null;
        }
    }

    private void test(Bitmap bitmap) {
        test.setVisibility(View.VISIBLE);
        test.setImageBitmap(bitmap);
    }

    private Bitmap getBitmap() {
        if (myScreen.mCropRectF.isEmpty()) {
            return null;
        }

        if (mShotUtil.getFullScreenBitmap() == null) {
            return null;
        }

        Rect r = new Rect(Math.round(myScreen.mCropRectF.left),
                Math.round(myScreen.mCropRectF.top),
                Math.round(myScreen.mCropRectF.right),
                Math.round(myScreen.mCropRectF.bottom));
        int width = r.width();
        int height = r.height();
        Bitmap croppedImage = null;
        try {
            croppedImage = Bitmap.createBitmap(width, height,
                    Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (croppedImage == null) {
            return null;
        }
        Canvas canvas = new Canvas(croppedImage);
        Rect dstRect = new Rect(0, 0, width, height);
        Bitmap bitmapTemp = mShotUtil.getFullScreenBitmap().copy(Bitmap.Config.RGB_565, true);
        canvas.drawBitmap(bitmapTemp, r, dstRect, null);

        return croppedImage;
    }

    public void saveBitmap() {
        saveBitmap = getBitmap();
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

        myScreen.onMyTouchEvent(event);
        return false;
    }
}
