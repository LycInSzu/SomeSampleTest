package com.example.administrator.myapplication.AboutSurfaceView;

import android.content.ContentResolver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

public class SurfaceViewThread extends Thread {

    private boolean stop=false;
    private SurfaceHolder holder;


    public SurfaceViewThread(SurfaceHolder holder) {
        this.holder=holder;
    }

    /**
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see #start()
     * @see #stop()
     * @see #Thread(ThreadGroup, Runnable, String)
     */
    @Override
    public void run() {
        super.run();


        try {
//            while (!stop) {
                checkSurfaceHolder();
                drawMyView();
//                Thread.sleep(10000000);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawMyView() {
        Canvas canvas=this.holder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
        Paint paint= getMyPaint();
        canvas.drawCircle(400,500,200,paint);
        canvas.drawText("Surfaceview text",260,500,paint);
        holder.unlockCanvasAndPost(canvas);
    }

    private Paint getMyPaint() {
        Paint paint= new Paint();
        paint.setColor(Color.BLUE);
        paint.setAntiAlias(true);
        paint.setTextSize(40);
        paint.setStyle(Paint.Style.STROKE);

        return paint;
    }

    private void checkSurfaceHolder() throws Exception {
        if (this.holder == null){
            throw new Exception("SurfaceHolder can not be null");
        }
    }


    public void stopMe(boolean stop) {
        this.stop = stop;
    }
}
