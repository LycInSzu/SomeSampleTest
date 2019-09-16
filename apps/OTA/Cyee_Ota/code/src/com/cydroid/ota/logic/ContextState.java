package com.cydroid.ota.logic;

import android.text.TextUtils;

/**
 * Created by borney on 4/13/15.
 */
public final class ContextState implements IContextState {
    private static final String SPLIT = "&";
    private State mState;
    private boolean isRoot;
    private boolean isBackState = false;
    private int mError = -1;

    private ContextState() {

    }

    protected void setState(State state) {
        this.mState = state;
    }

    protected void setRoot(boolean root) {
        this.isRoot = root;
    }

    protected void setBackstate(boolean backstate) {
        this.isBackState = backstate;
    }

    protected void setError(int error) {
        this.mError = error;
    }

    @Override
    public State state() {
        return mState;
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public boolean isBackState() {
        return isBackState;
    }

    @Override
    public int error() {
        return mError;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IContextState)) {
            return false;
        }
        IContextState contextState = (IContextState) o;
        if (contextState.state() != state()) {
            return false;
        }
        if (contextState.isRoot() != isRoot()) {
            return false;
        }
        if (contextState.isBackState() != isBackState()) {
            return false;
        }
        if (contextState.error() != error()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(state())
                .append(isRoot())
                .append(isBackState())
                .append(error())
                .toHashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("state: ");
        sb.append(mState);
        sb.append("\tisRoot: ");
        sb.append(isRoot);
        sb.append("\tisBackState: ");
        sb.append(isBackState);
        sb.append("\t error: ");
        sb.append(mError);
        return sb.toString();
    }

    protected String storageString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mState.value());
        sb.append(SPLIT);
        sb.append(isRoot);
        sb.append(SPLIT);
        sb.append(isBackState);
        sb.append(SPLIT);
        sb.append(mError);
        return sb.toString();
    }

    protected static ContextState createContextState(String str) {
        ContextState contextState = new ContextState();
        if (TextUtils.isEmpty(str)) {
            contextState.setState(State.ERROR);
            contextState.setRoot(false);
            contextState.setBackstate(false);
            contextState.setError(-1);
        } else {
            String[] strs = str.split(SPLIT);
            contextState.setState(State.getState(Integer.valueOf(strs[0]).intValue()));
            contextState.setRoot(Boolean.valueOf(strs[1]));
            contextState.setBackstate(Boolean.valueOf(strs[2]));
            contextState.setError(Integer.valueOf(strs[3]));
        }
        return contextState;
    }
}
