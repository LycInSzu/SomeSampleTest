package com.cydroid.note.common;

import com.gionee.framework.log.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ErnestWorker {
    private final static String TAG = "ErnestWorker";
    private final Object mWorkLock = new Object();
    private ConcurrentLinkedQueue<JobProvider> mJobProviders = new ConcurrentLinkedQueue<JobProvider>();
    private ArrayList<WorkThread> mThreads = new ArrayList<WorkThread>();

    public ErnestWorker(int poolSize) {
        for (int i = 0; i < poolSize; ++i) {
            mThreads.add(new WorkThread("WorkThread-" + i));
        }

    }

    public void startWorking() {
        for (WorkThread thread : mThreads) {
            thread.start();
        }
    }

    public void addJobProvider(JobProvider provider) {
        mJobProviders.add(provider);

        for (WorkThread thread : mThreads) {
            thread.mBProvidersAdded = true;
        }

        wakeup();
    }

    public void removeJobProvider(JobProvider provider) {
        mJobProviders.remove(provider);
    }

    public void wakeup() {
        synchronized (mWorkLock) {
            mWorkLock.notifyAll();
        }
    }

    public interface FancyJob extends Runnable {
    }

    public interface JobProvider {
        FancyJob provide();
    }

    private class WorkThread extends Thread {
        volatile boolean mBQuit = false;
        volatile boolean mBProvidersAdded;

        public WorkThread(String name) {
            super(name);
            setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        }

        public void quit() {
            mBQuit = true;
            synchronized (mWorkLock) {
                mWorkLock.notifyAll();
            }
        }

        @Override
        public void run() {
            while (!mBQuit) {
                Iterator<JobProvider> iter = mJobProviders.iterator();
                mBProvidersAdded = false;
                boolean jobProvided = false;
                while (iter.hasNext()) {
                    JobProvider provider = iter.next();
                    FancyJob job = provider.provide();
                    if (job != null) {
                        job.run();
                        jobProvided = true;
                    }
                    if (mBQuit) {
                        return;
                    }
                }
                if (!jobProvided) {
                    synchronized (mWorkLock) {
                        if (!mBProvidersAdded) {
                            try {
                                mWorkLock.wait();
                            } catch (InterruptedException e) {
                                Logger.printLog(TAG, "unexpected interrupt: " + mWorkLock);
                            }
                        }
                    }
                }
            }
        }
    }

    //------------------------------------------------------------------------------
    public interface StreamlinedJob extends FancyJob {
        int getId();
    }

    public static class StreamlinedJobProvider implements JobProvider {
        final JobProvider mProvider;
        final ConcurrentLinkedQueue<StreamlinedJob> pendingWorkFinishers =
                new ConcurrentLinkedQueue<>();
        final IntSortedSet mMarks = new IntSortedSet();

        public StreamlinedJobProvider(JobProvider provider) {
            mProvider = provider;
        }

        @Override
        public FancyJob provide() {
            StreamlinedJob job = pendingWorkFinishers.poll();
            if (job != null) {
                if (queryAndMark(job) == false) {
                    return new MyFancyJob(job);
                }
                pendingWorkFinishers.offer(job);
            }

            StreamlinedJob newJob = (StreamlinedJob) mProvider.provide();
            if (newJob == null) {
                return null;
            }
            if (queryAndMark(newJob) == false) {
                return new MyFancyJob(newJob);
            }
            pendingWorkFinishers.offer(newJob);
            return null;
        }

        private boolean queryAndMark(StreamlinedJob job) {
            synchronized (mMarks) {
                int id = job.getId();
                return mMarks.findAndInsert(id);
            }
        }

        private void unMark(StreamlinedJob job) {
            synchronized (mMarks) {
                mMarks.delete(job.getId());
            }
        }

        class MyFancyJob implements FancyJob {
            final StreamlinedJob mJob;

            MyFancyJob(StreamlinedJob job) {
                mJob = job;
            }

            @Override
            public void run() {
                mJob.run();
                unMark(mJob);
            }
        }
    }
}
