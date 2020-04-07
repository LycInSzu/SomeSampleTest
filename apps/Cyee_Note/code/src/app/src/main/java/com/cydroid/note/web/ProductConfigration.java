package com.cydroid.note.web;

import android.os.Build;

import java.util.Locale;

/**
 * Created by spc on 16-5-6.
 */
public class ProductConfigration {

    public static String getUAString() {

        String brand = Build.BOARD;
        String model = Build.MODEL;
        String language = Locale.getDefault().getLanguage(); // zh
        String country = Locale.getDefault().getCountry().toLowerCase(); // cn

        String uaString = "Mozilla/5.0 (Linux; U; Android " + Build.VERSION.RELEASE + "; " + language + "-"
                + country + ";" + brand + "-" + model + "/" + " Build/IMM76D) AppleWebKit534.30(KHTML,like Gecko)Version/4.0 Mobile Safari/534.30 Id/"
                + "aminote";
        return uaString;
    }
}
