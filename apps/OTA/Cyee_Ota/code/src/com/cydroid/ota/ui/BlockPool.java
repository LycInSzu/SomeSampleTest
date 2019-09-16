package com.cydroid.ota.ui;

import android.os.Handler;
import android.os.Looper;
import com.cydroid.ota.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by borney on 6/16/15.
 */
public class BlockPool<E> {
    private static final String TAG = "BlockPool";
    private volatile boolean isLoop;
    private volatile OnConsumeListener mListener;
    private TheadImpl<E> mThread;
    private final Object mWaitObj = new Object();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    BlockPool() {
        isLoop = true;
        mThread = new TheadImpl<E>();
        mThread.start();
    }

    void registerOnConsumeState(OnConsumeListener listener) {
        mListener = listener;
    }

    void wakeBlock() {
        Log.d(TAG, "wakeBlock()");
        synchronized (mWaitObj) {
            mWaitObj.notifyAll();
        }
    }

    void offer(E e, boolean block) {
        mThread.offer(e, block);
    }

    E peek() {
        return mThread.peek();
    }

    void onDestory() {
        synchronized (mWaitObj) {
            mWaitObj.notifyAll();
        }
        isLoop = false;
		//Chenyee <CY_Bug> <xuyongji> <20180612> modify for SWW1618OTA-481 begin
        mListener = null;
		//Chenyee <CY_Bug> <xuyongji> <20180612> modify for SWW1618OTA-481 end
    }


    interface OnConsumeListener<E> {
        void onConsume(E e);
    }

    private class TheadImpl<E> extends Thread {
        final BlockingQueue<AtomicReference<BlockInfo>> queue = new ArrayBlockingQueue<AtomicReference<BlockInfo>>(10);
//        private volatile boolean block = false;
        private volatile E e;

        void offer(E e, boolean block) {
            Log.d(TAG, "TheadImpl offer(" + e + ")");
            BlockInfo info = new BlockInfo();
            info.e = e;
            info.block = block;
            AtomicReference<BlockInfo> atomicReference = new AtomicReference<BlockInfo>(info);
            boolean result = queue.offer(atomicReference);
            Log.d(TAG, "offer result:" + result);
        }

        E peek() {
            Log.d(TAG, "TheadImpl peek(" + e + ")");
            return e != null ? e : queue.peek().get().e;
        }

        @Override
        public void run() {
            while (isLoop) {
                try {
                    final AtomicReference<BlockInfo> atomicInfo = queue.take();
                    final boolean block = atomicInfo.get().block;
//                    this.block = block;
                    final E e = atomicInfo.get().e;
                    this.e = e;
                    Log.d(TAG, "wait E = " + e + " isBlock = " + block);
                    if (mListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "TheadImpl onConsume(" + e + ")");
								//Chenyee <CY_Bug> <xuyongji> <20180619> modify for CSW1707A-1360 begin
                                if (mListener != null) {
                                    mListener.onConsume(e);
                                }
								//Chenyee <CY_Bug> <xuyongji> <20180619> modify for CSW1707A-1360 end
                            }
                        });
                    }
                    this.e = null;
//                    this.block = false;
                    synchronized (mWaitObj) {
                        if (block) {
                            Log.d(TAG, "--------------block-------------");
                            mWaitObj.wait(Integer.MAX_VALUE);
                        }
                    }
                } catch (InterruptedException exc) {
                    exc.printStackTrace();
                }
            }
        }

        class BlockInfo {
            E e;
            boolean block;
        }
    }
}
