package com.cydroid.note.ai;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.cydroid.note.common.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.common.Future;
import com.cydroid.note.common.FutureListener;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.ThreadPool;

import java.util.ArrayList;

/**
 * Created by gaojt on 16-1-19.
 */
public class AITipView extends ImageView {

    private static final int MIN_OFFSET_VALUE = 10;

    private int mTop;
    private int mBottom;
    private int mLeft;
    private int mRight;

    private int mStartX;
    private int mStartY;

    private int mLastX;
    private int mLastY;

    private int mScreenHeight;
    private int mScreenWidth;

    private int mMaxBottom;
    private int mButtomHeight;
    private int mPadding;
    private int mPaddingIncrement;
    private int mHideIncrement;

    private boolean mIMEVisable;
    private boolean mIsMovedWhenIMEVisible;

    private int mTopWhenIMEVisble;

    private int mHideOriginPosition;

    private int mState = STATE_DISPLAY;

    private static final String TAG = "AITipView";
    private static final int DELAY_MILLIS = 1000 * 2;
    private static final int MSG_REQUEST_CONTENT = 1;
    private static final int MSG_RESULT_KEY_WORDS = 2;
    private static final int STATE_DISPLAY = 3;
    private static final int STATE_HIDE = 4;
    private AITipCallback mAITipCallback;
    private boolean mDestroy;
    private ArrayList<String> mKeyWords;
    private KeyWordWorker mKeyWordWorker;
    private KeyWordListener mKeyWordListener;
    private AnimationDrawable mHaveKeyWordDw;
    private Drawable mNotHaveKeyWordDw;
    private Handler mHandler;
    private boolean mMoved = false;

    public interface AITipCallback {

        String requestContent();

        void resultKeyWords(ArrayList<String> keywords);
    }

    public AITipView(Context context) {
        super(context);
        initAI(context);
    }

    public AITipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AITipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAI(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void resume() {
        mHandler.sendEmptyMessageDelayed(MSG_REQUEST_CONTENT, DELAY_MILLIS);
    }

    public void pause() {
        mHandler.removeMessages(MSG_REQUEST_CONTENT);
    }

    private void initAI(Context context) {
        initData(context);
        //GIONEE wanghaiyan 2016-12-07 modify 40315 for begin
        //initDrawable();
        //GIONEE wanghaiyan 2016-12-07 modify 40315 for end
        updateTipDrawable();
        initHelper();
        setOnTouchListener(mOnTouchListener);
    }

    private void initData(Context context) {
        mScreenHeight = NoteUtils.sScreenHeight;
        mScreenWidth = NoteUtils.sScreenWidth;
        Resources resources = context.getResources();
        mButtomHeight = resources.
                getDimensionPixelOffset(R.dimen.abstract_note_activity_footer_height);
        mPadding = context.getResources().
                getDimensionPixelOffset(R.dimen.edit_note_content_padding_left);
        mPaddingIncrement = resources.
                getDimensionPixelOffset(R.dimen.ai_tipview_increment_padding);
        mHideIncrement = resources.
                getDimensionPixelOffset(R.dimen.ai_tipview_hide_to_left_threshold);
    }

    private void initHelper() {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_REQUEST_CONTENT: {
                        handlerRequestContent();
                        break;
                    }
                    case MSG_RESULT_KEY_WORDS: {
                        handlerResultKeyWords((ArrayList<String>) msg.obj);
                        break;
                    }
                    default:
                        break;
                }
            }
        };
        mHandler = handler;
        mKeyWordWorker = new KeyWordWorker();
        mKeyWordWorker.setView(this);
        mKeyWordListener = new KeyWordListener();
        mKeyWordListener.setHandler(handler);
    }

    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = (int) event.getRawX();
                    mStartY = (int) event.getRawY();

                    mLastX = (int) event.getRawX();
                    mLastY = (int) event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    mMoved = true;

                    int moveX = (int) event.getRawX();
                    int moveY = (int) event.getRawY();

                    int dx = moveX - mLastX;
                    int dy = moveY - mLastY;

                    mTop = getTop() + dy;
                    mBottom = getBottom() + dy;
                    mLeft = getLeft() + dx;
                    mRight = getRight() + dx;

                    // adjustingBoundary
                    if (mLeft < 0) {
                        mLeft = 0;
                        mRight = mLeft + getWidth();
                    }

                    if (mRight > mScreenWidth) {
                        mRight = mScreenWidth;
                        mLeft = mRight - getWidth();
                    }

                    if (mTop < 0) {
                        mTop = 0;
                        mBottom = mTop + getHeight();
                    }

                    int limitBottom = getLimitBottom();
                    if (mBottom >= limitBottom) {
                        mBottom = limitBottom;
                        mTop = mBottom - getHeight();
                    }

                    // update View layout.
                    layout(mLeft, mTop, mRight, mBottom);
                    if (mIMEVisable) {
                        mIsMovedWhenIMEVisible = true;
                    } else {
                        mIsMovedWhenIMEVisible = false;
                    }
                    mLastX = (int) event.getRawX();
                    mLastY = (int) event.getRawY();
                    break;

                case MotionEvent.ACTION_UP:
                    int offsetX = Math.abs(mLastX - mStartX);
                    int offsetY = Math.abs(mLastY - mStartY);

                    boolean isOnClick = (offsetX <= MIN_OFFSET_VALUE)
                            && (offsetY <= MIN_OFFSET_VALUE);
                    if (isOnClick) {
                        handleOnClickEvent();
                    }
                    if (mMoved) {
                        checkToHide();
                    }
                    mMoved = false;
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private void checkToHide() {
        int position = getRight();
        int hideToLeftXThreshold = getWidth() + mHideIncrement;
        if (position <= hideToLeftXThreshold) {
            hideToLeft();
            return;
        }

        position = getLeft();
        int hideToRightXThreshold = mScreenWidth - getWidth() - mHideIncrement;
        if (position >= hideToRightXThreshold) {
            hideToRight();
            return;
        }
        mState = STATE_DISPLAY;
    }

    private void hideToLeft() {
        mState = STATE_HIDE;
        int leftStart = mPadding + mPaddingIncrement - getWidth();
        mHideOriginPosition = leftStart;
        layout(leftStart, mTop, leftStart + getWidth(), mBottom);
    }

    private void hideToRight() {
        mState = STATE_HIDE;
        int leftStart = mScreenWidth - mPadding - mPaddingIncrement;
        mHideOriginPosition = leftStart;
        layout(leftStart, mTop, leftStart + getWidth(), mBottom);
    }

    private void handleOnClickEvent() {
        AITipCallback callback = mAITipCallback;
        if (callback == null) {
            return;
        }
        callback.resultKeyWords(mKeyWords);
    }

    public void updatePosition(boolean keepIMEVisbleState) {
        if (isOriginalState()) {
            return;
        }
        int left = mLeft;
        int right = mRight;
        int top = mTop;
        int bottom = mBottom;

        if (mState == STATE_HIDE) {
            left = mHideOriginPosition;
            right = mHideOriginPosition + getWidth();
        }
        if (keepIMEVisbleState) {
            top = mTopWhenIMEVisble;
            bottom = mTopWhenIMEVisble + getHeight();
        }
        layout(left, top, right, bottom);
    }

    public void updatePosition(double ratio) {
        if (isOriginalState()) {
            return;
        }

        int left = mLeft;
        int right = mRight;
        if (mState == STATE_HIDE) {
            left = mHideOriginPosition;
            right = mHideOriginPosition + getWidth();
        }

        int top = (int) (mTop * ratio) - getHeight();
        if (top < 0) {
            top = 0;
        }
        mTopWhenIMEVisble = top;
        int bottom = top + getHeight();
        layout(left, top, right, bottom);
    }

    public boolean isMovedWhenIMEVisible() {
        return mIsMovedWhenIMEVisible;
    }

    public void setIMEVisable(boolean imeVisable) {
        mIMEVisable = imeVisable;
    }

    public void setMaxBottom(int maxBottom) {
        mMaxBottom = maxBottom;
    }

    private int getLimitBottom() {
        int limitBottom = 0;
        if (mMaxBottom > 0) {
            limitBottom = mMaxBottom - getHeight();
        } else {
            limitBottom = mScreenHeight - mButtomHeight - getHeight();
        }
        return limitBottom;
    }

    private boolean isOriginalState() {
        return mLeft == 0 && mTop == 0 && mRight == 0
                && mBottom == 0;
    }

    private void handlerRequestContent() {
        if (mDestroy) {
            return;
        }
        if (mAITipCallback != null) {
            String content = mAITipCallback.requestContent();
            if (!TextUtils.isEmpty(content)) {
                String targetContent = new String(content);//NOSONAR
                mKeyWordWorker.setContent(targetContent);
                NoteAppImpl.getContext().getThreadPool().submit(mKeyWordWorker,
                        mKeyWordListener);
            } else {
                mKeyWords = null;
                updateTipDrawable();
            }
            mHandler.sendEmptyMessageDelayed(MSG_REQUEST_CONTENT, DELAY_MILLIS);
        }
    }

    private void handlerResultKeyWords(ArrayList<String> keywords) {
        if (mDestroy) {
            return;
        }
        mKeyWords = keywords;
        updateTipDrawable();
    }
    //GIONEE wanghaiyan 2016-12-07 modify 40315 for begin
    /*
    private void initDrawable() {
        AnimationDrawable animationDrawable = (AnimationDrawable) ContextCompat.
                getDrawable(getContext(), R.drawable.ai_tip_drawable);
        mHaveKeyWordDw = animationDrawable;
        mNotHaveKeyWordDw = ContextCompat.getDrawable(getContext(), R.drawable.ai_tip_dw_no);
    }
    */
    //GIONEE wanghaiyan 2016-12-07 modify 40315 for end
    private static class KeyWordListener implements FutureListener<ArrayList<String>> {

        private Handler mHandler;
        private Object mLock = new Object();

        public void setHandler(Handler handler) {
            synchronized (mLock) {
                mHandler = handler;
            }
        }

        @Override
        public void onFutureDone(Future<ArrayList<String>> future) {
            final ArrayList<String> keyWords = future.get();
            synchronized (mLock) {
                if (mHandler != null) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_RESULT_KEY_WORDS, keyWords));
                }
            }
        }
    }

    private static class KeyWordWorker implements ThreadPool.Job<ArrayList<String>> {
        private IntelligentAssistant mAI;
        private Object mLock = new Object();
        private View mView;
        private String mContent;

        public void setView(View view) {
            synchronized (mLock) {
                mView = view;
            }
        }

        public void setContent(String content) {
            synchronized (mLock) {
                mContent = content;
            }
        }

        @Override
        public ArrayList<String> run(ThreadPool.JobContext jc) {
            try {
                IntelligentAssistant ai = mAI;
                if (ai == null) {
                    View view;
                    synchronized (mLock) {
                        view = mView;
                    }
                    if (view == null) {
                        return null;
                    }
                    ai = new IntelligentAssistant(view.getContext());
                    mAI = ai;
                }

                View view;
                synchronized (mLock) {
                    view = mView;
                }

                if (view == null) {
                    return null;
                }
                String content;
                synchronized (mLock) {
                    content = mContent;
                }

                if (TextUtils.isEmpty(content)) {
                    return null;

                }
                ArrayList<String> keywords = ai.getKeyWords(content);
                return keywords;
            } catch (Exception e) {
                Log.w(TAG, "error", e);
            }
            return null;
        }
    }

    public void setAICallback(AITipCallback callback) {
        mAITipCallback = callback;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHaveKeyWordDw.isRunning()) {
            mHaveKeyWordDw.stop();
            mHaveKeyWordDw = null;
        }
        mKeyWordWorker.setView(null);
        mKeyWordListener.setHandler(null);
        mKeyWordWorker = null;
        mKeyWordListener = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mDestroy = true;
        mAITipCallback = null;
        if (null != mKeyWords) {
            mKeyWords.clear();
        }
        mHaveKeyWordDw = null;
        mNotHaveKeyWordDw = null;
    }

    private void updateTipDrawable() {
        if (mKeyWords != null && mKeyWords.size() > 0) {
            setImageDrawable(mHaveKeyWordDw);
            if (!mHaveKeyWordDw.isRunning()) {
                mHaveKeyWordDw.start();
            }
        } else {
            if (mHaveKeyWordDw.isRunning()) {
                mHaveKeyWordDw.stop();
            }
            setImageDrawable(mNotHaveKeyWordDw);
        }
    }
}
