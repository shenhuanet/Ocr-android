package com.shenhua.ocr.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * Created by shenhua on 2017-10-25-0025.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class BitmapUtils {

    /**
     * 旋转bitmap
     *
     * @param origin bitmap
     * @param alpha  旋转角度
     * @return 旋转后的bitmap
     */
    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null || origin.isRecycled()) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 将彩色图转换为纯黑白二色
     *
     * @param origin bitmap
     * @return Bitmap
     */
    @SuppressWarnings("NumericOverflow")
    public static Bitmap turnBlackWhite(Bitmap origin) {
        int w = origin.getWidth();
        int h = origin.getHeight();
        int[] pixels = new int[w * h];
        origin.getPixels(pixels, 0, w, 0, 0, w, h);
        int alpha = 0xFF << 24;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int gray = pixels[w * i + j];
                int red = ((gray & 0x00FF0000) >> 16);
                int green = ((gray & 0x000FF00) >> 8);
                int blue = (gray & 0x000000FF);
                gray = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                gray = alpha | (gray << 16) | (gray << 8) | gray;
                pixels[w * i + j] = gray;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return ThumbnailUtils.extractThumbnail(bitmap, w, h);
    }

    /**
     * 实现对图像进行线性灰度化处理
     *
     * @param origin bitmap
     * @return Bitmap
     */
    public static Bitmap lineGray(Bitmap origin) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        Bitmap bitmap = origin.copy(Bitmap.Config.ARGB_8888, true);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int col = origin.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                red = (int) (1.1 * red + 30);
                green = (int) (1.1 * green + 30);
                blue = (int) (1.1 * blue + 30);
                if (red >= 255) {
                    red = 255;
                }
                if (green >= 255) {
                    green = 255;
                }
                if (blue >= 255) {
                    blue = 255;
                }
                int newColor = alpha | (red << 16) | (green << 8) | blue;
                bitmap.setPixel(i, j, newColor);
            }
        }
        return bitmap;
    }

    /**
     * 实现对图像进行二值化处理
     *
     * @param origin bitmap
     * @return Bitmap
     */
    public static Bitmap turnBinary(Bitmap origin) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        Bitmap bitmap = origin.copy(Bitmap.Config.ARGB_8888, true);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int col = bitmap.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                int gray = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                if (gray <= 95) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                bitmap.setPixel(i, j, newColor);
            }
        }
        return bitmap;
    }

    /**
     * 7.0 获取文件真实路径
     *
     * @param context Context
     * @param data    Intent
     * @return String path
     */
    public String getImagePath(Context context, Intent data) {
        if (data == null) {
            return null;
        }

        Uri uri = data.getData();
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);

            if (Common.PROVIDER_MEDIA.equals(uri.getAuthority())) {
                // 解析出数字格式的id
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if (Common.PROVIDER_DOWNLOAD.equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(context, contentUri, null);
            }
        } else if (Common.PROVIDER_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(context, uri, null);
        } else if (Common.PROVIDER_FILE.equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    /**
     * 普通方式获取文件真实路径
     *
     * @param context   context
     * @param uri       uri
     * @param selection 选择参数,可空
     * @return String path
     */
    private String getImagePath(Context context, Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


}
