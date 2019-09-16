package com.cydroid.ota.logic.parser;

import com.cydroid.ota.execption.SettingUpdateParserException;

/**
 * Created by liuyanfeng on 15-4-16.
 */
public interface IParser<T, R> {
    R parser(T t) throws SettingUpdateParserException;
}
