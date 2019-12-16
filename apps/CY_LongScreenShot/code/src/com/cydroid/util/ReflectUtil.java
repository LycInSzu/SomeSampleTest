package com.cydroid.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author lixh
 * @since 2017-12-02
 */
public class ReflectUtil {
    //===============================================================================================//
    /**
     * Class<?> cls
     * getFields(),getMethods(),getConstructors()将分别返回类提供的public域、方法和构造器数组，其中包含超类的公有成员.
     * getDeclaredFields(),getDeclaredMethods(),getDeclaredConstructors()将分别返回类中声明的全部域、方法和构造器，
     * 其中包含私有和受保护成员，但不包括超类的成员
     */
    //===============================================================================================//
    static final boolean DEBUG = true;
    static final String TAG = ReflectUtil.class.getSimpleName();

    /**
     * 根据指定类名、方法名及其对应参数Class类型数组获取方法对象
     *
     * @param clsName        the class name
     * @param methodName     the method name
     * @param parameterTypes the parameter array
     * @return
     */
    public static Class<?> getClassByClsName(String clsName) {
        try {
            Class<?> cls = Class.forName(clsName);
            return cls;
        } catch (ClassNotFoundException e) {
            if (DEBUG) Log.e(TAG, "getClassByClsName() occur exception.");
        }
        return null;
    }

    /**
     * 根据指定类名、方法名及其对应参数Class类型数组获取方法对象
     *
     * @param clsName        the class name
     * @param methodName     the method name
     * @param parameterTypes the parameter array
     * @return
     */
    public static Method getMethodByParameters(String clsName, String methodName, Class<?>... parameterTypes) {
        try {
            Class<?> cls = Class.forName(clsName);
            return cls.getDeclaredMethod(methodName, parameterTypes);
        } catch (ClassNotFoundException | NoSuchMethodException | NullPointerException e) {
            if (DEBUG) Log.e(TAG, "getMethodByParameters() occur exception.");
        }

        return null;
    }

    /**
     * 根据指定方法及参数值数组返回该方法通过反射机制后的结果
     *
     * @param mMethod  it will be called
     * @param receiver the object the underlying method is invoked from
     * @param args     the arguments used for the method call
     * @return
     */
    public static Object invokeMultiArgumentsMethod(Method mMethod, Object receiver, Object... args) {
        try {
            if (mMethod != null && receiver != null) {
                mMethod.setAccessible(true);
                return mMethod.invoke(receiver, args);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            if (DEBUG) Log.e(TAG, "invokeMultiArgumentsMethod() occur exception.");
        }
        return null;
    }

    /**
     * 不关心调用结果
     *
     * @param mMethod  it will be called
     * @param receiver the object the underlying method is invoked from
     * @param args     the arguments used for the method call
     */
    public static void invokeNoReturnMethod(Method mMethod, Object receiver, Object... args) {
        try {
            if (mMethod != null && receiver != null) {
                mMethod.setAccessible(true);
                mMethod.invoke(receiver, args);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            if (DEBUG) Log.e(TAG, "invokeNoReturnMethod() occur exception.");
        }
    }
}
