package com.lyc.newtestapplication.newtestapplication.Regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatches {
    public static void main( String args[] ){

        // 按指定模式在字符串查找
        CharSequence line = "+(56)1 857-664-0037";
        String pattern = "(^\\+|^)[0-9\\(\\) -]{3,}($)";

        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(line);
        if (m.find()) {
            System.out.println("Found value:  " + m.group(0)+"     "+ line.length() );

        } else {
            System.out.println("NO MATCH");
        }




        String key="0|android|17303299|null|1000";
        String temp[]=key.split("\\|");

        if (temp.length>=3&&"android".equals(temp[1])&&"17303299".equals(temp[2])){
            System.out.println("--------lyc--------- temp is  "+temp);
        }
//        for (String temp:key.split("\\|")) {
//            System.out.println("--------lyc--------- temp is  "+temp);
//        }

    }
}
