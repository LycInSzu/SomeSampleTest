
package com.wtk.screenshot.view.paintScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wtk.screenshot.util.ShotUtil;
import com.wtk.screenshot.util.imageZoom.CropImageView;
import com.android.systemui.R;

public class PaintScreen extends FrameLayout {
    /* Common */
    // Util
    private Context mContext;
    private ShotUtil mShotUtil;

    //Flag
    private Bitmap saveBitmap;

    /* View */
    private CropImageView myScreen;
    private FrameLayout instruction;
    private FrameLayout paintRight;
    private ImageView paintEraser;
    private ImageView test;

    public PaintScreen(Context context) {
        this(context, null);
    }

    public PaintScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (isInEditMode()) {
            return;
        }
    }

    public PaintScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mShotUtil = ShotUtil.getInstance(mContext);

        View v = getPartView();
        this.addView(v);

        myScreen = (CropImageView) v.findViewById(R.id.paint_view);
        instruction = (FrameLayout) v.findViewById(R.id.instruction);
        paintRight = (FrameLayout) v.findViewById(R.id.paint_right);
        paintEraser = (ImageView) v.findViewById(R.id.paint_eraser);
        test = (ImageView) v.findViewById(R.id.test);

        paintRight.setClickable(true);
        paintEraser.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                myScreen.reset();
            }
        });

        // Default
        //modify by wangjian for YWSW-858 20190413 start
        //instruction.setVisibility(View.VISIBLE);
        instruction.setVisibility(View.GONE);
        //modify by wangjian for YWSW-858 20190413 end
        paintRightShow();
    }

    public View getPartView() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.paint_screen, null);
    }

    private void paintRightShow() {
        if (paintRight.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation rightIn = AnimationUtils.loadAnimation(mContext,
                R.anim.select_right_in);
        paintRight.setAnimation(rightIn);
        paintRight.setVisibility(View.VISIBLE);
    }

    private void paintRightGone() {
        if (paintRight.getVisibility() == View.GONE) {
            return;
        }
        Animation rightOut = AnimationUtils.loadAnimation(mContext,
                R.anim.select_right_out);
        paintRight.setAnimation(rightOut);
        paintRight.setVisibility(View.GONE);
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
        if (myScreen.mCropRectF.isEmpty() || myScreen.mCropPath.isEmpty()) {
            return mShotUtil.getFullScreenBitmap();
        }

        if (mShotUtil.getFullScreenBitmap() == null) {
            return null;
        }

        Paint showPaint = new Paint();

        showPaint.setAntiAlias(true);
        showPaint.setDither(true);
        showPaint.setColor(Color.RED);
        showPaint.setStyle(Paint.Style.STROKE);
        showPaint.setStrokeJoin(Paint.Join.ROUND);
        showPaint.setStrokeCap(Paint.Cap.ROUND);
        float widthScale = myScreen.mCropRectF.width()
                / myScreen.mDrawRect.width();
        float heightScale = myScreen.mCropRectF.height()
                / myScreen.mDrawRect.height();
        float scale = (widthScale > heightScale) ? widthScale : heightScale;
        showPaint.setStrokeWidth(8 * scale);

        Rect mFullRect = myScreen.fullScreenRect;
        int fullWidth = mFullRect.width();
        int fullHeight = mFullRect.height();
        Bitmap mFullBitmap = null;
        try {
            mFullBitmap = Bitmap.createBitmap(fullWidth, fullHeight,
                    Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mFullBitmap == null) {
            return null;
        }
        Canvas mFullcanvas = new Canvas(mFullBitmap);
        Rect fullDstRect = new Rect(0, 0, fullWidth, fullHeight);
        Bitmap bitmapTemp = mShotUtil.getFullScreenBitmap().copy(Bitmap.Config.RGB_565, true);
        mFullcanvas.drawBitmap(bitmapTemp, mFullRect, fullDstRect, null);
        mFullcanvas.drawPath(myScreen.mCropPath, showPaint);

        return mFullBitmap;
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
                paintRightGone();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                paintRightShow();
                break;
        }

        myScreen.onMyTouchEvent(event);
        return false;
    }
}
