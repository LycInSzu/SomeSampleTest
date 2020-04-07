package com.cydroid.note.app;

import android.app.Activity;

import java.util.Stack;

public class ComponentStack {
    private static final Stack<Activity> ALL_ACTIVITIES = new Stack<Activity>();
    public static final int NO_TASK_ID = Integer.MIN_VALUE;

    private static class Holder {
        private static final ComponentStack INSTANCE = new ComponentStack();
    }

    private ComponentStack() {
    }

    public static ComponentStack obtain() {
        return Holder.INSTANCE;
    }

    public Activity getTopActivity() {
        if (ALL_ACTIVITIES.size() > 0) {
            return ALL_ACTIVITIES.peek();
        } else {
            return null;
        }
    }

    public Activity getFirstActivity() {
        if (ALL_ACTIVITIES.size() > 0) {
            return ALL_ACTIVITIES.firstElement();
        } else {
            return null;
        }
    }
    
    public int getTopActivityTaskId() {
        if (ALL_ACTIVITIES.size() == 0) {
            return NO_TASK_ID;
        }

        return ALL_ACTIVITIES.peek().getTaskId();
    }

    public void addActivity(Activity activity) {
        ALL_ACTIVITIES.push(activity);
    }

    public Activity currentActivity() {
        return ALL_ACTIVITIES.lastElement();
    }

    public void finishActivity() {
        if (ALL_ACTIVITIES.size() > 0) {
            finishActivity(ALL_ACTIVITIES.lastElement());
        }
    }

    public void finishActivity(Activity activity) {
        if (activity != null) {
            ALL_ACTIVITIES.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    public void removeActivity(Activity activity) {
        if (activity != null && ALL_ACTIVITIES.size() > 0) {
            ALL_ACTIVITIES.remove(activity);
            activity = null;
        }
    }

    public void finishActivity(Class<?> cls) {
        for (Activity activity : ALL_ACTIVITIES) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    public void finishAllActivity() {
        for (int i = 0, size = ALL_ACTIVITIES.size(); i < size; i++) {
            final Activity activity = ALL_ACTIVITIES.get(i);
            if (null != activity) {
                activity.finish();
            }
        }
        ALL_ACTIVITIES.clear();
    }

    public void finishAllActivitiesExcludeTop() {

        final Activity top = ALL_ACTIVITIES.pop();
        for (Activity a : ALL_ACTIVITIES) {
            a.finish();
        }
        ALL_ACTIVITIES.clear();
        if (null != top) {
            ALL_ACTIVITIES.push(top);
        }
    }

    public void exitApp() {
        try {
            exitAllUI();
        } catch (Exception e) {

        } finally {
            System.exit(0);
        }
    }

    public void exitAllUI() {
        finishAllActivity();
    }
}
