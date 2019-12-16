package com.cydroid.util;

public class BallPosition {

    public static final String TAG = "BallPosition";

    /**
     * 是从屏幕顶部到小球中心圆点的位子,注意上下小球，位置计算不同
     */
    private static int mSmallBallPossion = 0;

    public static int getmSmallBallPossion() {
        return mSmallBallPossion;
    }

    /**
     * 获得从屏幕顶部到小球中心的位子
     */
    public static void setmSmallBallPossion(int mSmallBallPossion) {
        Log.d(TAG, "set smallBallPosition from " + BallPosition.mSmallBallPossion + " to " + mSmallBallPossion);
        // Log.v(TAG, "before set ball position mSmallBallPossion = " + BallPosition.mSmallBallPossion);
        BallPosition.mSmallBallPossion = mSmallBallPossion;
        // Log.v(TAG, "after set ball position mSmallBallPossion = " + mSmallBallPossion);
    }


}
