package com.shenhua.ocr.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.shenhua.ocr.R;

/**
 * Created by shenhua on 2017-10-10-0010.
 * Email shenhuanet@126.com
 *
 * @author shenhua
 */
public class ArcView extends View {

    private Paint mArcPaint;
    private float x, y;

    public ArcView(Context context) {
        this(context, null);
    }

    public ArcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        mArcPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        x = w / 2;
        y = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, (float) (y * 1.8), (float) (x * 1.8), mArcPaint);
    }
}
