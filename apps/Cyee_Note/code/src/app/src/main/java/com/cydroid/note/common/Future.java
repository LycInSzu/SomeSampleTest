package com.cydroid.note.common;

// This Future differs from the java.util.concurrent.Future in these aspects:
//
// - Once cancel() is called, isCancelled() always returns true. It is a sticky
//   flag used to communicate to the implementation. The implmentation may
//   ignore that flag. Regardless whether the Future is cancelled, a return
//   value will be provided to get(). The implementation may choose to return
//   null if it finds the Future is cancelled.
//
// - get() does not throw exceptions.
//
public interface Future<T> {
    void cancel();

    boolean isCancelled();

    boolean isDone();

    T get();

    void waitDone();
}
