package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.systemui.R;

import java.text.SimpleDateFormat;

/**
 * 悬浮球控制类
 *
 * @author eli chang
 */
public class FloatingBallManager implements View.OnTouchListener {
    private static final String TAG = "FloatingBallManager";

    private Context mContext;
    private WindowManager windowManager;
    private LayoutParams layoutParams;
    private static FloatingBallManager floatBallManager;
    //悬浮球最小化的尺寸
    private static final int minWidth = 120;
    //悬浮球最大化的尺寸
    private static final int maxWidth = 500;
    //手机状态栏高度
    private float statusBarHeight;
    private float windowWidth;
    private float windowHeight;
    //触摸组件按下的位置
    private float touchDownX;
    private float touchDownY;
    //触摸组件按下的时间
    private long touchDownTime;
    //控制状态标记
    private boolean isControlMode = false;

    private FrameLayout floatView;
    private View recordBtn;
    private TextView recordTimeText;
    private int recordTime = 0;
    private int viewWidth;
    private int viewHeight;
    private final int HANDLER_TIME_COUNTING = 1;
    private final int HANDLER_TIME_STOP = 2;

    /**
     * @param context
     */
    private FloatingBallManager(Context context) {
        mContext = context;
        //获取系统窗体服务
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //获取手机窗体像素值
        windowWidth = getWindowWidth(context);
        windowHeight = getWindowHeight(context);
        statusBarHeight = getStatusBarHeight(context);

        floatView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.float_record_control, null);
        floatView.setOnTouchListener(this);
        recordBtn = floatView.findViewById(R.id.record_btn);
        recordTimeText = floatView.findViewById(R.id.record_time);
        if (recordBtn != null) {
            recordBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent service = new Intent(mContext, RecordService.class);
                    service.putExtra("status", false);
                    mContext.startService(service);
                }
            });
        }
        viewWidth = context.getResources().getDimensionPixelOffset(R.dimen.float_control_width);
        viewHeight = context.getResources().getDimensionPixelOffset(R.dimen.float_control_height);
    }

    /**
     * 获取本类的实例化对象
     *
     * @param context
     * @return
     */
    public static FloatingBallManager getInstance(Context context) {
        if (floatBallManager == null)
            floatBallManager = new FloatingBallManager(context);
        Log.i(TAG, "getInstance");
        return floatBallManager;
    }

    /**
     * 初始化并显示悬浮球
     */
    public void show() {
        Log.i(TAG, "show");
        if (layoutParams == null) {
            //设置悬浮球布局信息
            layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = viewWidth;
            layoutParams.height = viewHeight;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            layoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
            layoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams.format = PixelFormat.TRANSPARENT;
            //读取并设置存储的位置信息
            Coordinate position = readPosition(mContext);
            layoutParams.x = (int) (position.getRawX() - viewWidth / 2);
            layoutParams.y = (int) (position.getRawY() - viewHeight / 2);
        }
        try {
            //将悬浮球添加到窗口
            windowManager.addView(floatView, layoutParams);
            timeHandler.sendEmptyMessageDelayed(HANDLER_TIME_COUNTING, 800);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 隐藏悬浮球
     */
    public void close() {
        if (windowManager != null && floatView != null) {
            try {
                timeHandler.sendEmptyMessage(HANDLER_TIME_STOP);
                windowManager.removeView(floatView);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        //获取触摸的位置
        float x = event.getRawX();
        float y = event.getRawY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下时，记录当前时间和位置
            touchDownTime = System.currentTimeMillis();
            touchDownX = x;
            touchDownY = y;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //时，进一步判断
            /**
             * 满足以下两个条件时，进入拖拽状态
             *
             * 1.当手指移动距离相对于按下的位置偏移10个像素以上
             *
             * 2.当按下并且静止的时间大于100毫秒
             */
            float offset = (float) Math.sqrt(((x - touchDownX) * (x - touchDownX) + (y - touchDownY) * (y - touchDownY)));
            if (offset > 10 && (System.currentTimeMillis() - touchDownTime) > 100) {
                //重新更新悬浮球的位置
                setPosition((int) x - viewWidth / 2, (int) y - viewHeight / 2);
            } else if ((System.currentTimeMillis() - touchDownTime) <= 100) {
                //touchDownTime = System.currentTimeMillis();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            /**
             * 手指离开时，同时满足以下两个条件：
             *
             * 1.距离手指按下的时间小于100毫秒
             *
             * 2.相对于手指按下的位置没有变化
             *
             * 触发点击事件
             */
            /*if ((System.currentTimeMillis() - touchDownTime) <= 100 && (x == touchDownX) && (y == touchDownY)) {
                if (isControlMode) {
                    setPosition(x - maxWidth / 2, y - maxWidth / 2);
                } else {
                    setPosition(x - minWidth / 2, y - minWidth / 2);
                }
            }*/
            //存储当前悬浮球的位置
            writePosition((int) x, (int) y);
        }
        return false;
    }

    /**
     * 设置组件的位置
     *
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        layoutParams.x = (int) x;
        layoutParams.y = (int) (y - statusBarHeight);
        windowManager.updateViewLayout(floatView, layoutParams);
    }

    /**
     * 设置悬浮球是否处于控制状态
     *
     * @param isControlMode
     */
    private void setControlMode(boolean isControlMode) {
        if (isControlMode) {
            //控制状态下最大化悬浮球
            layoutParams.width = maxWidth;
            layoutParams.height = maxWidth;
            windowManager.updateViewLayout(floatView, layoutParams);
        } else {
            //移动状态下最小化悬浮球
            layoutParams.width = minWidth;
            layoutParams.height = minWidth;
            windowManager.updateViewLayout(floatView, layoutParams);
        }
    }

    /**
     * 计算并存储悬浮球当前位置
     *
     * @param x
     * @param y
     */
    private void writePosition(float x, float y) {
        //根据当前状态，选择当前组件的大小
        float width = isControlMode ? maxWidth : minWidth;
        //将坐标偏移到左上角
        x = (int) (x - width / 2);
        y = (int) (y - width / 2);
        //计算坐标是否溢出窗口，溢出则重新计算位置
        if (x < 0) {
            x = 0;
        } else if (x + width > windowWidth) {
            x = (int) (windowWidth - width);
        }

        if (y - width < 0) {
            y = 0;
        } else if (y + width > windowHeight) {
            y = (int) (windowHeight - width);
        }

        //将欧标偏移到组件中间
        x = (int) (x + width / 2);
        y = (int) (y + width / 2);
        writePosition(mContext, x, y);
    }

    public class Coordinate {

        private float rawX;
        private float rawY;

        public Coordinate(float rawX, float rawY) {
            this.rawX = rawX;
            this.rawY = rawY;
        }

        public void setRawX(float rawX) {
            this.rawX = rawX;
        }

        public void setRawY(float rawY) {
            this.rawY = rawY;
        }

        public float getRawX() {
            return this.rawX;
        }

        public float getRawY() {
            return this.rawY;
        }
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    private float getWindowWidth(Context context) {
        float width = 0;
        try {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            width = displayMetrics.widthPixels;
        } catch (Exception e) {
        }

        return width;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    private float getWindowHeight(Context context) {
        float height = 0;
        try {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            height = displayMetrics.heightPixels;
        } catch (Exception e) {
        }

        return height;
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    private float getStatusBarHeight(Context context) {
        float statusBarHeight = 0;
        try {
            int resourceID = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceID > 0) {
                statusBarHeight = context.getResources().getDimensionPixelSize(resourceID);
            }
        } catch (Exception e) {
        }
        return statusBarHeight;
    }

    /**
     * 获取存储的位置
     *
     * @param context
     * @return
     */
    private final Coordinate readPosition(Context context) {
        Coordinate position = null;
        float defaultRawX = 100;
        float defaultRawY = getWindowHeight(context) - getStatusBarHeight(context) - 200;
        try {
            SharedPreferences preferences = context.getSharedPreferences("ControlBallPosition", Context.MODE_PRIVATE);
            if (preferences != null) {
                float rawX = preferences.getFloat("rawX", defaultRawX);
                float rawY = preferences.getFloat("rawY", defaultRawY);
                position = new Coordinate(rawX, rawY);
            }
        } catch (Exception e) {
            position = new Coordinate(defaultRawX, defaultRawY);
        }
        return position;
    }

    /**
     * 写入当前位置
     *
     * @param context
     * @param rawX
     * @param rawY
     */
    private final void writePosition(Context context, float rawX, float rawY) {
        try {
            SharedPreferences preferences = context.getSharedPreferences("ControlBallPosition", Context.MODE_PRIVATE);
            if (preferences != null) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putFloat("rawX", rawX);
                editor.putFloat("rawY", rawY);
                editor.commit();
            }
        } catch (Exception e) {
        }
    }

    Handler timeHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == HANDLER_TIME_COUNTING) {
                recordTime ++;
                timeHandler.sendEmptyMessageDelayed(HANDLER_TIME_COUNTING, 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                String timeText = sdf.format(recordTime * 1000);
                recordTimeText.setText(timeText);
            } else if (msg.what == HANDLER_TIME_STOP) {
                recordTime = 0;
                recordTimeText.setText("00:00");
                timeHandler.removeMessages(HANDLER_TIME_COUNTING);
            }
            return false;
        }
    });
}