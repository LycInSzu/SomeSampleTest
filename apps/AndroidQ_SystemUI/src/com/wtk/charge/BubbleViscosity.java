package com.wtk.charge;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.android.systemui.R;

@SuppressWarnings("ALL")
public class BubbleViscosity extends SurfaceView implements Callback, Runnable {
    private static ScheduledExecutorService scheduledThreadPool;
    private Context context;
    private String paintColor = "#25DA29";
    private String centreColor = "#00000000";
    private String minCentreColor = "#9025DA29";
    private int screenHeight;
    private int screenWidth;


    private float lastRadius;
    private float rate = 0.32f;
    private float rate2 = 0.45f;
    private PointF lastCurveStrat = new PointF();
    private PointF lastCurveEnd = new PointF();
    private PointF centreCirclePoint = new PointF();
    private float centreRadius;
    private float bubbleRadius;


    private PointF[] arcPointStrat = new PointF[8];
    private PointF[] arcPointEnd = new PointF[8];
    private PointF[] control = new PointF[8];
    private PointF arcStrat = new PointF();
    private PointF arcEnd = new PointF();
    private PointF controlP = new PointF();

    List<PointF> bubbleList = new ArrayList<>();
    List<BubbleBean> bubbleBeans = new ArrayList<>();

    private int rotateAngle = 0;
    private float controlrate = 1.66f;
    private float controlrateS = 1.3f;
    private int index = 0;
    private SurfaceHolder mHolder;
    private float scale = 0;

    private Paint arcPaint;
    private Paint minCentrePaint;
    private Paint bubblePaint;
    private Paint centrePaint;
    private Paint lastPaint;
    private Path lastPath;
    private Random random;
    private Paint textPaint;
    private String text = "78 %";
    private Rect rect;

    private Bitmap mBottomBitmap;
    private Bitmap mCircleBitmap;
    private Bitmap mBgBitmap;

    public BubbleViscosity(Context context) {
        this(context, null);
    }

    public BubbleViscosity(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleViscosity(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initTool();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenHeight = getMeasuredHeight();
        screenWidth = getMeasuredWidth();
        setBubbleList();
    }

    private void initTool() {
        rect = new Rect();
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
        lastRadius = dip2Dimension(60f, context);
        centreRadius = dip2Dimension(100f, context);
        bubbleRadius = dip2Dimension(14f, context);
        random = new Random();
        lastPaint = new Paint();
        lastPaint.setAntiAlias(true);
        lastPaint.setStyle(Paint.Style.FILL);
        lastPaint.setColor(Color.parseColor(paintColor));
        lastPaint.setStrokeWidth(2);

        lastPath = new Path();

        centrePaint = new Paint();
        centrePaint.setAntiAlias(true);
        centrePaint.setStyle(Paint.Style.FILL);
        centrePaint.setStrokeWidth(2);
        centrePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        centrePaint.setColor(Color.parseColor(centreColor));
        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.FILL);
        arcPaint.setColor(Color.parseColor(paintColor));
        arcPaint.setStrokeWidth(2);
        minCentrePaint = new Paint();
        minCentrePaint.setAntiAlias(true);
        minCentrePaint.setStyle(Paint.Style.FILL);
        minCentrePaint.setColor(Color.parseColor(paintColor));
        minCentrePaint.setStrokeWidth(2);
        bubblePaint = new Paint();
        bubblePaint.setAntiAlias(true);
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setColor(Color.parseColor(paintColor));
        bubblePaint.setStrokeWidth(2);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.parseColor("#FFFFFF"));
        textPaint.setStrokeWidth(2);
        textPaint.setTextSize(dip2Dimension(40f, context));

        mBottomBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.charge_bottom, new BitmapFactory.Options());

    }


    private void onMDraw() {
        Canvas canvas = mHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        bubbleDraw(canvas);
        lastCircleDraw(canvas);
        //centreCircleDraw(canvas);
        textPaint.getTextBounds(text, 0, text.length(), rect);
        //centreCirclePoint.set(screenWidth / 2, screenHeight / 2);
        //canvas.drawText(text, centreCirclePoint.x - rect.width() / 2, centreCirclePoint.y + rect.height() / 2, textPaint);
        mHolder.unlockCanvasAndPost(canvas);
    }

    private void centreCircleDraw(Canvas canvas) {
        centreCirclePoint.set(screenWidth / 2, screenHeight / 2);
        circleInCoordinateDraw(canvas);
        canvas.drawCircle(centreCirclePoint.x, centreCirclePoint.y, centreRadius, centrePaint);

    }

    private void lastCircleDraw(Canvas canvas) {
        lastCurveStrat.set(screenWidth / 2 - lastRadius, screenHeight);
        lastCurveEnd.set((screenWidth / 2), screenHeight);

        float k = (lastRadius / 2) / lastRadius;

        float aX = lastRadius - lastRadius * rate2;
        float aY = lastCurveStrat.y - aX * k;
        float bX = lastRadius - lastRadius * rate;
        float bY = lastCurveEnd.y - bX * k;

        lastPath.rewind();
        lastPath.moveTo(lastCurveStrat.x, lastCurveStrat.y);
        canvas.drawBitmap(mBottomBitmap,(screenWidth - mBottomBitmap.getWidth()) / 2, screenHeight - mBottomBitmap.getHeight(),null);
        lastPath.cubicTo(lastCurveStrat.x + aX, aY, lastCurveEnd.x - bX, bY, lastCurveEnd.x, lastCurveEnd.y - lastRadius / 2);
        lastPath.cubicTo(lastCurveEnd.x + bX, bY, lastCurveEnd.x + lastRadius - aX, aY, lastCurveEnd.x + lastRadius, lastCurveEnd.y);

        lastPath.lineTo(lastCurveStrat.x, lastCurveStrat.y);
        //canvas.drawPath(lastPath, lastPaint);

    }

    private int bubbleIndex = 0;

    private void bubbleDraw(Canvas canvas) {
        for (int i = 0; i < bubbleBeans.size(); i++) {
            if (bubbleBeans.get(i).getY() <= (int) (screenHeight / 2 + centreRadius)) {
                bubblePaint.setAlpha(000);
                canvas.drawCircle(bubbleBeans.get(i).getX(), bubbleBeans.get(i).getY(), bubbleRadius, bubblePaint);
            } else {
                bubblePaint.setAlpha(255);
                canvas.drawCircle(bubbleBeans.get(i).getX(), bubbleBeans.get(i).getY(), bubbleRadius, bubblePaint);
            }
            /*if (bubbleBeans.get(i).getY() <= bubbleList.get(0).y && bubbleList.get(0).y - bubbleBeans.get(i).getY() < 15) {
                lastPath.moveTo(lastCurveStrat.x, lastCurveStrat.y);
                int y = (int) (screenHeight - (screenHeight - bubbleBeans.get(i).getY()) / 2);
                lastPath.quadTo(lastCurveEnd.x - lastRadius, y, bubbleBeans.get(i).getX() - bubbleRadius, bubbleBeans.get(i).getY());
                lastPath.lineTo(bubbleBeans.get(i).getX() + bubbleRadius, bubbleBeans.get(i).getY());
                lastPath.quadTo(lastCurveEnd.x + lastRadius, y, lastCurveEnd.x + lastRadius, lastCurveStrat.y);
                canvas.drawPath(lastPath, bubblePaint);
            }*/

            /*if (bubbleBeans.get(i).getY() - arcPointStrat[5].y < 110 && rotateAngle > 20 && rotateAngle < 100) {

                lastPath.moveTo(arcPointStrat[5].x, arcPointStrat[5].y);
                float x = Math.abs(arcPointEnd[5].x - arcPointStrat[5].x) / 2;
                float y = screenHeight / 2 + centreRadius + (bubbleBeans.get(i).getY() - arcPointStrat[5].y) / 2;
                lastPath.quadTo(screenWidth / 2 + x, y, bubbleBeans.get(i).getX() + bubbleRadius, bubbleBeans.get(i).getY());
                lastPath.lineTo(bubbleBeans.get(i).getX() - bubbleRadius, bubbleBeans.get(i).getY());
                lastPath.quadTo(screenWidth / 2 - x, y, arcPointEnd[5].x, arcPointEnd[5].y);
                canvas.drawPath(lastPath, bubblePaint);
            }*/
        }

    }

    public float dip2Dimension(float dip, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
    }


    public void circleInCoordinateDraw(Canvas canvas) {
        int angle;
        for (int i = 0; i < arcPointStrat.length; i++) {
            if (i > 3 && i < 6) {
                if (i == 4) {
                    angle = rotateAngle + i * 60;

                } else {
                    angle = rotateAngle + i * 64;
                }
            } else if (i > 5) {
                if (i == 6) {
                    angle = rotateAngle + i * 25;
                } else {
                    angle = rotateAngle + i * 48;
                }

            } else {
                angle = rotateAngle + i * 90;
            }

            float radian = (float) Math.toRadians(angle);
            float adjacent = (float) Math.cos(radian) * centreRadius;
            float right = (float) Math.sin(radian) * centreRadius;
            float radianControl = (float) Math.toRadians(90 - (45 + angle));
            float xStrat = (float) Math.cos(radianControl) * centreRadius;
            float yEnd = (float) Math.sin(radianControl) * centreRadius;
            if (i == 0 || i == 1) {
                if (i == 1) {
                    arcStrat.set(centreCirclePoint.x + adjacent - scale, centreCirclePoint.y + right + scale);
                    arcEnd.set(centreCirclePoint.x - right, centreCirclePoint.y + adjacent);

                } else {
                    arcStrat.set(centreCirclePoint.x + adjacent, centreCirclePoint.y + right);
                    arcEnd.set(centreCirclePoint.x - right - scale, centreCirclePoint.y + adjacent + scale);

                }
                controlP.set(centreCirclePoint.x + yEnd * controlrate, centreCirclePoint.y + xStrat * controlrate);
            } else {
                arcStrat.set(centreCirclePoint.x + adjacent, centreCirclePoint.y + right);
                arcEnd.set(centreCirclePoint.x - right, centreCirclePoint.y + adjacent);
                if (i > 5) {
                    controlP.set(centreCirclePoint.x + yEnd * controlrateS, centreCirclePoint.y + xStrat * controlrateS);
                } else {
                    controlP.set(centreCirclePoint.x + yEnd * controlrate, centreCirclePoint.y + xStrat * controlrate);
                }
            }
            arcPointStrat[i] = arcStrat;
            arcPointEnd[i] = arcEnd;
            control[i] = controlP;

            lastPath.rewind();
            lastPath.moveTo(arcPointStrat[i].x, arcPointStrat[i].y);
            lastPath.quadTo(control[i].x, control[i].y, arcPointEnd[i].x, arcPointEnd[i].y);

            if (i > 3 && i < 6) {
                canvas.drawPath(lastPath, minCentrePaint);
            } else {
                canvas.drawPath(lastPath, arcPaint);
            }
            lastPath.rewind();
        }
    }


    private void setAnimation() {
        setScheduleWithFixedDelay(this, 0, 5);
        setScheduleWithFixedDelay(new Runnable() {
            @SuppressWarnings("AlibabaUndefineMagicConstant")
            @Override
            public void run() {
                if (bubbleIndex > 2) {
                    bubbleIndex = 0;
                }
                if (bubbleBeans.size() < 8) {
                    bubbleBeans.add(new BubbleBean(bubbleList.get(bubbleIndex).x, bubbleList.get(bubbleIndex).y, random.nextInt(4) + 2, bubbleIndex));
                } else {
                    for (int i = 0; i < bubbleBeans.size(); i++) {
                        if (bubbleBeans.get(i).getY() <= (int) (screenHeight / 2 + centreRadius)) {
                            bubbleBeans.get(i).set(bubbleList.get(bubbleIndex).x, bubbleList.get(bubbleIndex).y, random.nextInt(4) + 2, bubbleIndex);
                            if (random.nextInt(bubbleBeans.size()) + 3 == 3 ? true : false) {
                            } else {
                                break;
                            }
                        }
                    }
                }
                bubbleIndex++;
            }
        }, 0, 200);
    }


    private static ScheduledExecutorService getInstence() {
        if (scheduledThreadPool == null) {
            synchronized (BubbleViscosity.class) {
                if (scheduledThreadPool == null) {
                    scheduledThreadPool = Executors.newSingleThreadScheduledExecutor();//newScheduledThreadPool
                }
            }
        }
        return scheduledThreadPool;
    }

    private static void setScheduleWithFixedDelay(Runnable var1, long var2, long var4) {
        getInstence().scheduleWithFixedDelay(var1, var2, var4, TimeUnit.MILLISECONDS);

    }


    public static void onDestroyThread() {
        getInstence().shutdownNow();
        if (scheduledThreadPool != null) {
            scheduledThreadPool = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setAnimation();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        onDestroyThread();
    }

    @SuppressWarnings("AlibabaUndefineMagicConstant")
    @Override
    public void run() {
        index++;
        rotateAngle = index;
        if (index > 90 && index < 180) {
            scale += 0.25;
            if (controlrateS < 1.66) {
                controlrateS += 0.005;
            }
        } else if (index >= 180) {
            scale -= 0.12;
            if (index > 300) {
                controlrateS -= 0.01;
            }
        }
        onMDraw();
        if (index == 360) {
            index = 0;
            rotateAngle = 0;
            controlrate = 1.66f;
            controlrateS = 1.3f;
            scale = 0;
        }

    }


    public void setBubbleList() {
        float radian = (float) Math.toRadians(40);
        float adjacent = (float) Math.cos(radian) * lastRadius / 3;
        float right = (float) Math.sin(radian) * lastRadius / 3;
        if (!bubbleList.isEmpty()) {
            return;
        }
        bubbleList.add(new PointF(screenWidth / 2 - bubbleRadius * 3, screenHeight - right));
        bubbleList.add(new PointF(screenWidth / 2, screenHeight - lastRadius / 4));
        bubbleList.add(new PointF(screenWidth / 2 + bubbleRadius * 3, screenHeight - right));


        setScheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < bubbleBeans.size(); i++) {
                    bubbleBeans.get(i).setMove(screenHeight, (int) (screenHeight / 2 + centreRadius));
                }
            }
        }, 0, 4);

    }

}
