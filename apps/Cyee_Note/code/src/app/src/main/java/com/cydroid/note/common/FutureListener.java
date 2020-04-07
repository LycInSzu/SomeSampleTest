package com.cydroid.note.common;

public interface FutureListener<T> {
    void onFutureDone(Future<T> future);
}
