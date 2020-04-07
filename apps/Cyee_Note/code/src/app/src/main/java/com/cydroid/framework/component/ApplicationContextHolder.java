package com.gionee.framework.component;

import android.content.Context;

public interface ApplicationContextHolder {

    Context APPLICATION_CONTEXT = BaseApplication.sApplication;
    Context CONTEXT = APPLICATION_CONTEXT;
    String PACKAGE_NAME = CONTEXT.getPackageName();

}
