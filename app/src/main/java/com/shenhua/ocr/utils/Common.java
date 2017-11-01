package com.shenhua.ocr.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by shenhua on 2017-10-26-0026.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class Common {

    private static final String[] LANGUAGES_USE = {"chi_sim", "eng"};
    private static final String[] LANGUAGES_DISPLAY = {"中文", "EN"};
    private static final String SP_NAME = "Config";
    private static final String KEY_LANG = "lang";

    public static final int ACTION_PICK = 0;
    public static final int ACTION_CROP = 1;
    public static final int REQUEST_CAMERA_PERMISSION = 1;
    public static final int REQUEST_STORAGE_PERMISSION = 2;
    public static final int REQUEST_PICK_PICTURE = 11;
    public static final int REQUEST_CROP_PICTURE = 12;
    public static final String FRAGMENT_DIALOG = "dialog";

    public static String formatDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA);
        return sdf.format(date);
    }

    /**
     * 获取正确的传入语言
     *
     * @param context context
     * @return "chi_sim", "eng"
     */
    public static String getLanguage(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        int lang = sp.getInt(KEY_LANG, 0);
        return LANGUAGES_USE[lang];
    }

    /**
     * 获取显示语言,用于显示在textView上
     *
     * @param context context
     * @return "中文,""EN"
     */
    public static synchronized String getDisplayLanguage(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        int lang = sp.getInt(KEY_LANG, 0);
        return LANGUAGES_DISPLAY[lang];
    }

    /**
     * 保存上次使用的语言
     *
     * @param context context
     * @param lang    0,1
     */
    public static synchronized void saveLanguage(Context context, int lang) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_LANG, lang).apply();
    }

    /**
     * 获取外部缓存照片的路径
     *
     * @param context context
     * @return File
     */
    public static File getTempPhoto(Context context) {
        return new File(context.getExternalCacheDir(), "temp.jpg");
    }

    /**
     * 获取外部历史记录照片的路径
     *
     * @param context context
     * @param name    name
     * @return File
     */
    public static File getHistoryPhoto(Context context, String name) {
        return new File(context.getExternalFilesDir(null), name);
    }

    public static String getTessDataDir(Context context) {
        return context.getFilesDir() + File.separator + "tessdata";
    }

}
