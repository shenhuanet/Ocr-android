package com.shenhua.ocr.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by shenhua on 2017-10-26-0026.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class Common {

    private static String[] languages = {"chi_sim", "eng"};

    public static String formatDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA);
        return sdf.format(date);
    }

    public static String getLanguage(int index) {
        return languages[index];
    }


}
