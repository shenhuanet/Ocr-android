package com.shenhua.ocr.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by shenhua on 2017-10-25-0025.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class BitmapUtils {

    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
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
}
