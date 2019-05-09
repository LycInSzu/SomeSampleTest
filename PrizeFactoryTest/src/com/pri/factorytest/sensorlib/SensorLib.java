package com.pri.factorytest.sensorlib;

public class SensorLib {
    private static final String TAG = "Sensor/SensorLib";
    public static final int RET_SUCCESS = 1;
    public static final int RET_FAILED = 0;
    public static final int RET_STATIC_CALI_SUCCESS = 0;
    public static final int RET_STATIC_CALI_FAILED = 1;

    static {
        System.loadLibrary("prifactory_sensor_jni");
    }

    public static native int getPsensorData();

    /*public static native int getPsensorThreshold(int[] result);

    public static native int calculatePsensorMinValue();

    public static native int getPsensorMinValue();

    public static native int calculatePsensorMaxValue();

    public static native int getPsensorMaxValue();

    public static native int doPsensorCalibration(int min, int max);

    public static native int clearPsensorCalibration();

    public static native int setPsensorThreshold(int high, int low);

    public static native int startGsensorCalibration();

    public static native int getGsensorStaticCalibration(float[] result);

    public static native int startGyroscopeCalibration();

    public static native int getGyroscopeStaticCalibration(float[] result);

    public static native int startLightCalibration();

    public static native int getLightStaticCalibration(float[] result);*/
}
