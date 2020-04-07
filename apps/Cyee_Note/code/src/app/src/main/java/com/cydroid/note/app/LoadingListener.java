package com.cydroid.note.app;

public interface LoadingListener {
    void onLoadingStarted();

    /**
     * Called when loading is complete or no further progress can be made.
     *
     * @param loadingFailed true if data source cannot provide requested data
     */
    void onLoadingFinished(boolean loadingFailed);
}
