package com.gionee.framework.log;

import android.os.Message;

/*package*/interface ILog {

    void println(Message message);

    void printStack(Message message);

}
