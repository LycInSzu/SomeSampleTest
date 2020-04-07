package com.cydroid.note.app;

public interface IYouJuCallback {
    void onEvent(int eventId);

    void onLabelEvent(int eventId, String label);
}
