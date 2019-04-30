package com.mediatek.camera.prize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.mode.professional.IArcProgressBarUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.PaintFlagsDrawFilter;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class PrizeArcProgressBarView extends View {
    /**
     * The angle occupied by the progress bar
     */
    private static final int ARC_FULL_DEGREE = 50;
    /**
     * Width of the arc
     */
    private int STROKE_WIDTH;
    /**
     * The width and height of the components
     */
    private int sWidth, sHeight;

    /**
     * Progress bar maximum and current progress value
     */
    private float max, progress, min;
    /**
     * Progress bar maximum rating
     */
    private int mMaxLevel = 4;

    /**
     * Whether to allow dragging the progress bar
     */
    private boolean draggingEnabled = false;
    /**
     * Draw a rectangular area of the arc
     */
    private RectF circleRectF;
    /**
     * the paint of drawing the arc
     */
    private Paint progressPaint;
    /**
     * the paint of drawing the text
     */
    private Paint textPaint;
    /**
     * the paint of drawing the current progress value
     */
    private Paint thumbPaint;
    /**
     * Radius of the arc
     */
    private int circleRadius;
    /**
     * Arc center position
     */
    private int centerX, centerY;
    /**
     * The color value of the gradient on the arc
     */
    private final int[] colors = {Color.WHITE, Color.WHITE/*,Color.parseColor("#FFE700"),Color.parseColor("#FFD700"),Color.parseColor("#FFC700"),Color.parseColor("#FFB700"),Color.parseColor("#FFA700"),Color.parseColor("#FF9700"),Color.parseColor("#FF7F00")*/};
    /**
     * Currently selected gear
     */
    private int mCurrentLevel = 2;
    private int[] pictures;
    private int[] checkeds;
    private int itemViewWidth;
    private int upBtCenterX, upBtCenterY, downBtCenterx, downBtCenterY;//控制按钮的坐标
    /**
     * The distance from the image to the arc
     */
    private int mDistanceDrawable;
    /**
     * The distance of the text from the arc
     */
    private int mDistanceText;
    /**
     * Record the coordinates of the corresponding points of each gear
     */
    private ConcurrentHashMap<Integer, List<Integer>> mHashMap;
    private float[] position;
    private int buttonRadius;
    private float newProgress;
    private Context mContext;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    /**
     * The flag bit determines whether the current view draws a picture or text
     */
    private static final int CANVASBITMAP = 0;
    private static final int CANVASTEXT = 1;
    private int mTag;
    private String[] mTexts;
    private IArcProgressBarUI mIArcProgressBarUI;

    /**
     * The color of the current drawing text
     */
    private int mColor = Color.WHITE;

    private float mCurrentX;
    private float mCurrentY;
    /**
     * The distance between the click distance and the arc
     */
    private int spaced;
    private PaintFlagsDrawFilter pfd;

    public PrizeArcProgressBarView(Context context) {
        super(context);
        init(context);
    }


    public PrizeArcProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public PrizeArcProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        mHashMap = new ConcurrentHashMap<>();
        mContext = context;

        position = new float[colors.length];
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);


        thumbPaint = new Paint();
        thumbPaint.setAntiAlias(true);
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mDistanceText = (int) getResources().getDimension(R.dimen.progressbar_view_distance_text);
        mDistanceDrawable = (int) getResources().getDimension(R.dimen.progressbar_view_distance_drawable);
    }

    public float getProgress() {
        return progress;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(480, 720);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(480, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, 720);
        } else {
            setMeasuredDimension(widthSize, heightSize);
        }
        sWidth = MeasureSpec.getSize(widthMeasureSpec);
        sHeight = MeasureSpec.getSize(heightMeasureSpec);

        double a = Math.abs(Math.cos((180 - ARC_FULL_DEGREE) / 2));
        circleRadius = (int) ((sWidth / 2) / Math.sin(Math.toRadians(ARC_FULL_DEGREE / 2)));
        circleRadius -= STROKE_WIDTH;

        STROKE_WIDTH = 2;


        centerX = sWidth / 2;
        centerY = (int) (sHeight + circleRadius * Math.cos(Math.toRadians(ARC_FULL_DEGREE / 2)));
        LogHelper.i("", "circleRadius: " + circleRadius + ",centerX: " + centerX + ",centerY: " + centerY);
        circleRectF = new RectF();
        circleRectF.left = centerX - circleRadius;
        circleRectF.top = centerY - circleRadius;
        circleRectF.right = centerX + circleRadius;
        circleRectF.bottom = centerY + circleRadius;


    }


    private Rect textBounds = new Rect();


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(pfd);
        float start = 90 + ((360 - ARC_FULL_DEGREE) >> 1);
        float sweep1 = ARC_FULL_DEGREE * (progress / max);
        float sweep2 = ARC_FULL_DEGREE - sweep1;
        float progressRadians = (float) (((360.0f - ARC_FULL_DEGREE) / 2 + sweep1) / 180 * Math.PI);
        float thumbX = centerX - circleRadius * (float) sin(progressRadians);
        float thumbY = centerY + circleRadius * (float) cos(progressRadians);
        progressPaint.setShadowLayer(1,1,1, Color.GRAY);
        progressPaint.setColor(Color.WHITE);
        progressPaint.setStrokeWidth(0);
        progressPaint.setStyle(Paint.Style.FILL);
        float radians = (float) (((360.0f - ARC_FULL_DEGREE) / 2) / 180 * Math.PI);
        float startX = centerX - circleRadius * (float) sin(radians);
        float startY = centerY + circleRadius * (float) cos(radians);
        System.out.println("startX=" + startX + ";startY=" + startY);
        canvas.drawCircle(startX, startY, STROKE_WIDTH / 2, progressPaint);
        //Draw a progress bar
        for (int i = 0; i < colors.length; i++) {
            position[i] = (float) (0.37 + i * (progressRadians * 100 / 360) / colors.length);
        }
        progressPaint.setStrokeWidth(STROKE_WIDTH);
        progressPaint.setStyle(Paint.Style.STROKE);
        LinearGradient linearGradient = new LinearGradient(startX, startY, thumbX, thumbY, colors, position, TileMode.CLAMP);
//        progressPaint.setShader(null);
        canvas.drawArc(circleRectF, start, sweep1, false, progressPaint);
        //Draw a progress bar background
//        progressPaint.setShader(null);
        progressPaint.setColor(Color.WHITE);
        canvas.drawArc(circleRectF, start + sweep1, sweep2, false, progressPaint);

        progressPaint.setStrokeWidth(0);
        progressPaint.setStyle(Paint.Style.STROKE);
        float endX = centerX + circleRadius * (float) sin(radians);
        float endY = centerY + circleRadius * (float) cos(radians);
        canvas.drawCircle(endX, endY, STROKE_WIDTH / 2, progressPaint);
        Bitmap indicator = BitmapFactory.decodeResource(getResources(), R.drawable.scence_indicator);
        canvas.drawBitmap(indicator, thumbX - indicator.getWidth() / 2, thumbY - indicator.getHeight() / 2, thumbPaint);
        itemViewWidth = Math.max(indicator.getWidth(), indicator.getHeight());
        if (mTag == CANVASBITMAP) {
            if (pictures == null || checkeds == null) {
                return;
            }
            for (int i = 0; i <= mMaxLevel; i++) {
                if (i != mCurrentLevel) {
                    canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), pictures[i]), getPosition(i)[0] - indicator.getWidth() / 2, getPosition(i)[1] - indicator.getHeight() - mDistanceDrawable, thumbPaint);
                    addValueToMap(i, getPosition(i)[0], getPosition(i)[1] - mDistanceDrawable);
                } else {
                    canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), checkeds[mCurrentLevel]), getPosition(mCurrentLevel)[0] - indicator.getWidth() / 2, getPosition(mCurrentLevel)[1] - indicator.getHeight() - mDistanceDrawable, thumbPaint);
                    addValueToMap(mCurrentLevel, getPosition(mCurrentLevel)[0], getPosition(mCurrentLevel)[1] - mDistanceDrawable);
                }
            }
        } else if (mTag == CANVASTEXT) {
            if (mTexts == null || mTexts.length < 1) {
                return;
            }
            for (int i = 0; i <= mMaxLevel; i++) {
                if (i != mCurrentLevel) {
                    thumbPaint.setColor(mColor);
                    int textSize = (int) getResources().getDimension(R.dimen.progressbar_text_size);
                    thumbPaint.setTextSize(textSize);
                    thumbPaint.setShadowLayer(2,1,1, Color.GRAY);
                    if (i != 0 && Integer.valueOf(mTexts[i]) >= 100) {
                        canvas.drawText(mTexts[i], getPosition(i)[0] - textSize, getPosition(i)[1] - mDistanceText, thumbPaint);
                        addValueToMap(i, getPosition(i)[0] - textSize, getPosition(i)[1] - mDistanceText);
                    } else {
                        canvas.drawText(mTexts[i], getPosition(i)[0] - textSize/2, getPosition(i)[1] - mDistanceText, thumbPaint);
                        addValueToMap(i, getPosition(i)[0] - textSize/2, getPosition(i)[1] - mDistanceText);
                    }
                } else {
                    thumbPaint.setColor(getResources().getColor(R.color.prize_arcprogressbar_text_color));
                    int textSize = (int) getResources().getDimension(R.dimen.progressbar_text_size);
                    thumbPaint.setTextSize(textSize);
                    if (i != 0 && Integer.valueOf(mTexts[i]) >= 100) {
                        canvas.drawText(mTexts[mCurrentLevel], getPosition(i)[0] - textSize, getPosition(mCurrentLevel)[1] - mDistanceText, thumbPaint);
                        addValueToMap(i, getPosition(i)[0] - textSize, getPosition(i)[1] - mDistanceText);
                    } else {
                        canvas.drawText(mTexts[mCurrentLevel], getPosition(i)[0] - textSize/2, getPosition(mCurrentLevel)[1] - mDistanceText, thumbPaint);
                        addValueToMap(i, getPosition(i)[0] - textSize/2, getPosition(i)[1] - mDistanceText);
                    }
                }
            }
        }

    }


    private boolean isDragging = false;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!draggingEnabled) {
            return super.onTouchEvent(event);
        }
        Log.i("onTouchEvent", "getAction: " + event.getAction() + ",getX: " + event.getX() + ",getY: " + event.getY());

        float currentX = event.getX();
        float currentY = event.getY();
        mCurrentX = currentX;
        mCurrentY = currentY;

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //Determine if the progress bar is in the thumb position
                if (checkOnArc(currentX, currentY)) {
                    Log.i("xiaoping", "currentX: " + currentX + ",currentY: " + currentY);
                    newProgress = calDegreeByPosition(currentX, currentY) / ARC_FULL_DEGREE * max;
                    setProgressSync(newProgress);
                    isDragging = true;
                } else if (checkOnButtonUp(currentX, currentY)) {
                    // TODO Auto-generated method stub
                    setProgress(progress + 10);
                    isDragging = false;
                } else if (checkOnButtonDwon(currentX, currentY)) {
                    setProgress(progress - 10);
                    isDragging = false;
                }

                break;


            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    //Determine if it has been removed when dragging
                    if (checkOnArc(currentX, currentY)) {
                        setProgressSync(calDegreeByPosition(currentX, currentY) / ARC_FULL_DEGREE * max);
                    } else {
                        isDragging = false;
                    }
                }
                break;


            case MotionEvent.ACTION_UP:
                if (!checkOnArc(currentX, currentY)) {
                    int selectedlevel = 0;
                    if (mTag == CANVASBITMAP) {
                        spaced = itemViewWidth * 2;
                    } else {
                        if (mMaxLevel <= 4) {
                            spaced = (int) getResources().getDimension(R.dimen.professional_controlview_height);
                        } else
                            spaced = (int) (getResources().getDimension(R.dimen.professional_controlview_height) * 0.5 );
                    }
                    for (int i = 0; i <= mMaxLevel; i++) {
                        if (Math.abs(currentX - mHashMap.get(i).get(0)) <= spaced && Math.abs(currentY - mHashMap.get(i).get(1)) <= spaced) {
                            selectedlevel = i;
                            setProgressSync(min + (max - min * 2) / mMaxLevel * selectedlevel);
                            break;
                        }
                    }

/*                    if (Math.abs(event.getX() - mCurrentX) > sWidth / mMaxLevel * 2) {
                        if (event.getX() - mCurrentX > 0) {
                            setProgressSync(min + (max - min * 2) / mMaxLevel * (mCurrentLevel + 1));
                        } else {
                            setProgressSync(min + (max - min * 2) / mMaxLevel * (mCurrentLevel - 1));
                        }
                    }*/
                }

                isDragging = false;
                break;
        }


        return true;
    }


    private float calDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private boolean checkOnButtonUp(float currentX, float currentY) {
        float distance = calDistance(currentX, currentY, upBtCenterX, upBtCenterY);
        return distance < 1.5 * buttonRadius;
    }

    private boolean checkOnButtonDwon(float currentX, float currentY) {
        float distance = calDistance(currentX, currentY, downBtCenterx, downBtCenterY);
        return distance < 1.5 * buttonRadius;
    }

    /**
     * Determine if the point is on an arc (near)
     */
    private boolean checkOnArc(float currentX, float currentY) {
        float distance = calDistance(currentX, currentY, centerX, centerY);
        float degree = calDegreeByPosition(currentX, currentY);
        return distance > circleRadius - STROKE_WIDTH * 5 && distance < circleRadius + STROKE_WIDTH * 5
                && (degree >= -8 && degree <= ARC_FULL_DEGREE + 8);
    }


    /**
     * Based on the current position, calculate the angle at which the progress bar has been rotated
     */
    private float calDegreeByPosition(float currentX, float currentY) {
        float a1 = (float) (Math.atan(1.0f * (centerX - currentX) / (currentY - centerY)) / Math.PI * 180);
        if (currentY < centerY) {
            a1 += 180;
        } else if (currentY > centerY && currentX > centerX) {
            a1 += 360;
        }

        float rangle = a1 - (360 - ARC_FULL_DEGREE) / 2;
        return rangle;
    }

    private float[] getPosition(int level) {
        float[] position = new float[2];
        float progress = min + level * (1.0f * (max - min * 2) / mMaxLevel);
        float angle = progress / max * ARC_FULL_DEGREE;
        float radin = (float) Math.toRadians(angle);
        float x = (float) (centerX + Math.cos(radin) * circleRadius);
        float y = /*(float) (centerY + Math.sin(radin) * circleRadius)*/getYCoordinate((int) angle, circleRadius);
        float newangle = 180 - (180 - ARC_FULL_DEGREE) / 2 - angle;
        position[0] = (int) (centerX + circleRadius * cos(newangle * 3.14 / 180));
        position[1] = (int) (centerY - circleRadius * sin(newangle * 3.14 / 180));
        return position;
    }


    public void setMax(int max) {
        this.max = max;
        this.min = max / 10;
    }


    public void setProgress(float progress) {
        final float validProgress = checkProgress(progress);
        new Thread(new Runnable() {
            @Override
            public void run() {
                float oldProgress = PrizeArcProgressBarView.this.progress;
                for (int i = 1; i <= 100; i++) {
                    PrizeArcProgressBarView.this.progress = oldProgress + (validProgress - oldProgress) * (1.0f * i / 100);
                    postInvalidate();
                    SystemClock.sleep(3);
                }
            }
        }).start();
    }

    public void setProgressSync(float progress) {
        this.progress = checkProgress(progress);
        invalidate();
    }


    private float checkProgress(float progress) {
        float filterprogress = progress;
        if (filterprogress < min) {
            filterprogress = min;
        }
        int level = (int) ((filterprogress - min) / ((max - min * 2) / mMaxLevel));
        filterprogress = ((max - min * 2) / mMaxLevel) * (level) + min;
        filterprogress = filterprogress > max - min ? max - min : filterprogress;
        Log.i("xiaoping", "filterprogress: " + filterprogress + ",leve: " + level);
        if (level != mCurrentLevel) {
            mCurrentLevel = level;
            updateCurrentLevel(mCurrentLevel);
        }
        return filterprogress;
    }


    public void setDraggingEnabled(boolean draggingEnabled) {
        this.draggingEnabled = draggingEnabled;
    }

    private float getXCoordinate(int angle, double r) {
        angle = angle - 330;
        double sin = Math.sin(Math.PI * angle / 180);
        float x = (float) (sin * r + centerX);
        return x;
    }

    private float getYCoordinate(int angle, double r) {
        angle = angle - 330;
        double cos = -Math.cos(Math.PI * angle / 180);
        float y = (float) (centerY - cos * r);
        return y;
    }


    public interface OnMenuItemClickListener {
        void onItemClick(int item);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener mMenuItemClickListener) {
        this.mOnMenuItemClickListener = mMenuItemClickListener;
    }

    private void addValueToMap(int key, float x, float y) {
        if (mHashMap != null && !mHashMap.containsKey(key)) {
            List list = new ArrayList();
            list.add((int) x);
            list.add((int) y);
            mHashMap.put(key, list);
        }
    }

    private void updateCurrentLevel(int level) {
        mOnMenuItemClickListener.onItemClick(level);

    }

    public void setToTalLevel(int mMaxLevel) {
        this.mMaxLevel = mMaxLevel;
    }

    /**
     * Drawing text
     *
     * @param maxprogress
     * @param mMaxLevel
     * @param mTexts
     * @param tag
     */
    public void setAllParameters(int maxprogress, int mMaxLevel, int defaultlevel, String[] mTexts, int tag) {
        setTag(tag);
        setToTalLevel(mMaxLevel);
        setMax(maxprogress);
        setProgressSync(min + (max - min * 2) / mMaxLevel * defaultlevel);
        this.mTexts = mTexts;
        invalidate();
    }

    /**
     * Draw a picture
     *
     * @param maxprogress
     * @param mMaxLevel
     * @param pictures
     * @param checkeds
     * @param tag
     */
    public void setAllParameters(int maxprogress, int mMaxLevel, int defaultlevel, int[] pictures, int[] checkeds, int tag) {
        setTag(tag);
        setToTalLevel(mMaxLevel);
        setMax(maxprogress);
        setProgressSync(min + (max - min * 2) / mMaxLevel * defaultlevel);
        this.pictures = pictures;
        this.checkeds = checkeds;
        invalidate();
    }

    private void setTag(int tag) {
        mTag = tag;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return true;
    }


    public void reset(int level) {
        setProgressSync(min + (max - min * 2) / mMaxLevel * level);
    }

    public void setTetxtColor(int color) {
        mColor = color;
        invalidate();
    }
}  