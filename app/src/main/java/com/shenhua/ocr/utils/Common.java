package com.shenhua.ocr.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.text.DecimalFormat;
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

    static final String PROVIDER_MEDIA = "com.android.providers.media.documents";
    static final String PROVIDER_DOWNLOAD = "com.android.providers.media.documents";
    static final String PROVIDER_CONTENT = "content";
    static final String PROVIDER_FILE = "file";

    public static final String TENCENT_APP_ID = "1106498082";
    public static final String TENCENT_POS_ID = "1106498082";
    public static final String TENCENT_BUGLY_ID = "07674e00fe";

    public static final String YOUMI_APP_ID = "85aa56a59eac8b3d";
    public static final String YOUMI_APP_SECRET = "a14006f66f58d5d7";

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

    /**
     * 获取字典文件目录
     *
     * @param context Context
     * @return String
     */
    public static String getTessDataDir(Context context) {
        return context.getFilesDir() + File.separator + "tessdata";
    }

    /**
     * 生成保存的图片路径
     *
     * @return 路径
     */
    public static String getSaveFileName() {
        return "IMG_" + System.currentTimeMillis() + ".jpg";
    }

    /**
     * 获取缓存大小
     *
     * @param context context
     * @return 0KB/23MB
     */
    public static String getCacheSize(Context context) {
        File cache = context.getCacheDir();
        File exCache = context.getExternalCacheDir();
        long size = getFolderSize(cache) + getFolderSize(exCache);
        return formatSize(size);
    }

    /**
     * 清理缓存
     *
     * @param context context
     */
    public static void cleanCache(Context context) {
        File cache = context.getCacheDir();
        File exCache = context.getExternalCacheDir();
        deleteDir(cache);
        deleteDir(exCache);
    }

    private static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] files = file.listFiles();
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    size = size + getFolderSize(file1);
                } else {
                    size = size + file1.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    private static String formatSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String result;
        if (size == 0) {
            return "0KB";
        }
        if (size < 1024) {
            result = df.format((double) size) + "B";
        } else if (size < 1048576) {
            result = df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            result = df.format((double) size / 1048576) + "MB";
        } else {
            result = df.format((double) size / 1073741824) + "GB";
        }
        return result;
    }

    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return true;
        }
        if (dir.isDirectory()) {
            String[] list = dir.list();
            for (String s : list) {
                deleteDir(new File(dir, s));
            }
        }
        return dir.delete();
    }

}
