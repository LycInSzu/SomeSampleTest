package com.mediatek.camera.common.prize;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PrizeReadNodeValue  {
    private static final String UP_PATH = "/proc/hall1/m1120_up";
    private static final String DOWN_PATH = "/proc/hall2/m1120_down";
    /***
     * 获取驱动节点值
     *
     * @param 节点路径
     * @return 节点值
     */
    private static String getBGFuzzyNodeValue(String path) {
        // Log.i(TAG, "getBGFuzzyNodeValue path: "+path);
        String prop = "0";// 默认值
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            prop = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    public String getUpValue() {
        return getBGFuzzyNodeValue(UP_PATH);
    }

    public String getDownValue() {
        return getBGFuzzyNodeValue(DOWN_PATH);
    }
}
