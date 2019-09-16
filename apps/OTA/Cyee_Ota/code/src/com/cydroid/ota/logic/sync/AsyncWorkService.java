
package com.cydroid.ota.logic.sync;

import com.cydroid.ota.execption.SettingUpdateException;
import com.cydroid.ota.Log;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncWorkService {
    private static final String TAG = "AsyncWorkService";
    private static final int THREAD_POOL_SIZE = 5;
    private ExecutorService mExecutorService;

    private static final class AsyncWorkHolder {
        public static final AsyncWorkService INSTANCE = new AsyncWorkService();
    }

    private AsyncWorkService() {
        mExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE,
                new DefaultThreadFactory());
    }

    /**
     * @return an sync Executor contain a threadpool
     */
    public static synchronized AsyncWorkService getInstance() {
        return AsyncWorkHolder.INSTANCE;
    }

    /**
     * @param job Asynchronous execution body
     * @throws com.cydroid.ota.execption.SettingUpdateException
     */
    public void submit(Job job) throws SettingUpdateException {
        Future future = null;
        Log.d(TAG, "ThreadPool isShutdow = " + mExecutorService.isShutdown());
        if (!mExecutorService.isShutdown()) {
            final Job.JobInfo info = job.mJobInfo;
            future = mExecutorService.submit(info.mCallable);
            info.mFuture = future;
        } else {
            throw new SettingUpdateException("ExecutorService is shutdown");
        }
    }

    public void shutdown() {
        mExecutorService.shutdown();
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup mGroup;
        private final AtomicInteger mThreadNumber = new AtomicInteger(1);
        private final String mNamePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            mGroup = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            mNamePrefix = "pool-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(mGroup, r,
                    mNamePrefix + mThreadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable throwable) {
                    Log.e(TAG, t.getName() + " throw exception " + throwable
                            .getMessage());
                }
            });
            return t;
        }

    }
}
