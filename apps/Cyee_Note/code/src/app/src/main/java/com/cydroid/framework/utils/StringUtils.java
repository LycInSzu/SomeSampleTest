package com.gionee.framework.utils;

/*
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
*/

public class StringUtils {
    public static final String ENCODING_UTF8 = "utf-8";

    private StringUtils() {
    }

    public static boolean isNull(String content) {
        return content == null || "".equals(content);
    }

    public static boolean isNotNull(String content) {
        return content != null && !"".equals(content);
    }

    public static boolean isBlank(String content) {
        int strLen;
        if (content == null || (strLen = content.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(content.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String content) {
        return !isBlank(content);
    }

    public static String trim(String content) {
        if (content != null) {
            content = content.trim();
        }
        return content;
    }

    public static String toUpperCaseFirstChar(String content) {
        if (isNull(content)) {
            return content;
        }
        char first = content.charAt(0);
        first = Character.toUpperCase(first);
        content = first + content.substring(1, content.length());
        return content;
    }

    public static String toLowerCaseFirstChar(String content) {
        if (isNull(content)) {
            return content;
        }
        char first = content.charAt(0);
        first = Character.toLowerCase(first);
        content = first + content.substring(1, content.length());
        return content;
    }
    
/*    public static String toPinYinHelper(String city) {
        
        if (city == null || "".equals(city)) {
            return city;
        }
        
        final char [] cityCharArr = city.toCharArray();
        final StringBuffer cityBuffer = new StringBuffer();
        final HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
        outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //boolean first = true;
        
        for (char c : cityCharArr) {
            try {
                final String[] all = PinyinHelper.toHanyuPinyinStringArray(c, outputFormat);
                if (all != null) {
                    *//*if (first) {
                        first = false;
                        cityBuffer.append(toUpperCaseFirstChar(all[0]));
                    } else {*//*
                    cityBuffer.append(all[0]);
                    //}
                }
            } catch (Exception e) {
                return city;
            }
        }
        
        return cityBuffer.toString();
    }*/
}
