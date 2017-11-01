package com.shenhua.ocr.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.shenhua.ocr.R;
import com.shenhua.ocr.dao.History;
import com.shenhua.ocr.utils.BitmapUtils;
import com.shenhua.ocr.utils.Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shenhua on 2017-10-23-0023.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class RecognitionActivity extends AppCompatActivity {

    /**
     * worker
     */
    @BindView(R.id.scanIv)
    ImageView ivScan;
    @BindView(R.id.srcIv)
    ImageView ivSrc;
    @BindView(R.id.scanNotifyLayout)
    LinearLayout scanNotifyLayout;
    @BindView(R.id.scanTimer)
    Chronometer scanTimer;
    /**
     * tool items
     */
    @BindView(R.id.resolveTv)
    TextView resolveTv;
    @BindView(R.id.rotationTv)
    TextView rotationTv;
    @BindView(R.id.langTv)
    TextView langTv;
    @BindView(R.id.startBtn)
    ImageButton btnStart;
    @BindView(R.id.startTv)
    TextView startTv;
    /**
     * tools panel and item
     */
    @BindView(R.id.toolsLayout)
    LinearLayout toolsLayout;
    @BindView(R.id.originToolTv)
    TextView originToolTv;
    @BindView(R.id.heibaiToolTv)
    TextView heibaiToolTv;
    @BindView(R.id.grayToolTv)
    TextView grayToolTv;
    @BindView(R.id.binaryToolTv)
    TextView binaryToolTv;
    /**
     * root layout
     */
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private volatile boolean isStarting = false;
    private ExecutorService executor;
    private int currentItem = R.id.originToolTv;
    /**
     * 是否已经准备好,用于控制当图片uri为空时,设置为未准备好
     */
    private boolean isReady = true;
    private Uri tempUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_recognition);
        ButterKnife.bind(this);

        tempUri = getIntent().getParcelableExtra("temp");
        Bitmap bitmap = getOriginBitmap();
        if (bitmap == null) {
            isReady = false;
            Snackbar.make(btnStart, "图片资源为空,操作不能进行", Snackbar.LENGTH_SHORT).show();
        }
        ivSrc.setImageBitmap(bitmap);
        itemSelected(originToolTv);
        langTv.setText(Common.getDisplayLanguage(this));
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.startBtn)
    void start() {
        if (!isReady) {
            Snackbar.make(btnStart, "图片资源为空,操作不能进行", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!isStarting) {
            startScanAnim();
            startTv.setText("取消");
            Bitmap bitmap = ((BitmapDrawable) ivSrc.getDrawable()).getBitmap();
            Log.d("RecognitionActivity", "start recognition ...... the bitmap width is " + bitmap.getWidth() + " height is " + bitmap.getHeight());
            executor.execute(new OcrRunnable(this, bitmap));
            isStarting = true;
        } else {
            stopScanAnim();
            startTv.setText("开始");
            executor.shutdownNow();
            isStarting = false;
        }
    }

    private void scanSuccess(String result) {
        stopScanAnim();
        History data = new History();
        data.setTime(scanTimer.getText().toString());
        data.setDate(System.currentTimeMillis());
        data.setResult(result);
        File target = Common.getHistoryPhoto(this,getSaveFileName());
        executor.execute(new CopyFileRunnable(tempUri, target));
        data.setImg(target.getPath());
        Intent intent = new Intent(this, ResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", data);
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }

    private String getSaveFileName() {
        return "IMG_" + System.currentTimeMillis() + ".jpg";
    }

    private void scanFailed(String msg) {
        stopScanAnim();
        Snackbar.make(btnStart, msg, Snackbar.LENGTH_SHORT).show();
    }

    private void startScanAnim() {
        ivScan.setVisibility(View.VISIBLE);
        Animation scanAnim = AnimationUtils.loadAnimation(this, R.anim.scan);
        ivScan.setAnimation(scanAnim);
        scanAnim.start();

        Animation notify = AnimationUtils.loadAnimation(this, R.anim.right_in);
        scanNotifyLayout.setAnimation(notify);
        notify.start();
        scanNotifyLayout.setVisibility(View.VISIBLE);

        scanTimer.setBase(SystemClock.elapsedRealtime());
        scanTimer.start();

        toolsLayout.setVisibility(View.GONE);
        resolveTv.setEnabled(false);
        rotationTv.setEnabled(false);
        langTv.setEnabled(false);
    }

    /**
     * 停止扫描动画
     */
    private void stopScanAnim() {
        try {
            if (ivScan.getAnimation() != null) {
                ivScan.getAnimation().cancel();
                ivScan.clearAnimation();
                ivScan.setVisibility(View.GONE);
            }
        } catch (Exception e) {// NullPointerException
            e.printStackTrace();
        }

        Animation notify = AnimationUtils.loadAnimation(this, R.anim.right_out);
        scanNotifyLayout.setAnimation(notify);
        notify.start();
        scanNotifyLayout.setVisibility(View.GONE);

        scanTimer.stop();
        isStarting = false;
        startTv.setText("开始");

        resolveTv.setEnabled(true);
        rotationTv.setEnabled(true);
        langTv.setEnabled(true);
    }

    /**
     * 处置
     */
    @OnClick(R.id.resolveTv)
    void resolve() {
        if (toolsLayout.getVisibility() == View.GONE) {
            toolsLayout.setVisibility(View.VISIBLE);
        } else {
            toolsLayout.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.originToolTv, R.id.heibaiToolTv, R.id.grayToolTv, R.id.binaryToolTv})
    void changeTool(View view) {
        if (!isReady) {
            Snackbar.make(btnStart, "图片资源为空,操作不能进行", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (view.getId() == currentItem) {
            return;
        }
        Bitmap bitmap = ((BitmapDrawable) ivSrc.getDrawable()).getBitmap();
        currentItem = view.getId();
        itemSelected((TextView) view);
        executor.execute(new BitmapResolveRunnable(bitmap, view.getId()));
    }

    /**
     * 改变语言
     */
    @OnClick(R.id.langTv)
    void changeLang(View view) {
        TextView textView = (TextView) view;
        if ("中文".equals(textView.getText().toString())) {
            Common.saveLanguage(this, 1);
            Snackbar.make(toolsLayout, "已切换为英文模式", Snackbar.LENGTH_SHORT).show();
        } else {
            Common.saveLanguage(this, 0);
            Snackbar.make(toolsLayout, "已切换为中文模式", Snackbar.LENGTH_SHORT).show();
        }
        textView.setText(Common.getDisplayLanguage(this));
    }

    @OnClick(R.id.rotationTv)
    void rotation() {
        if (!isReady) {
            Snackbar.make(btnStart, "图片资源为空,操作不能进行", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = ((BitmapDrawable) ivSrc.getDrawable()).getBitmap();
        ivSrc.setImageBitmap(BitmapUtils.rotateBitmap(bitmap, 90f));
    }

    private void itemSelected(TextView view) {
        originToolTv.setSelected(false);
        heibaiToolTv.setSelected(false);
        grayToolTv.setSelected(false);
        binaryToolTv.setSelected(false);

        view.setSelected(true);
    }

    private Bitmap getOriginBitmap() {
        try {
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(tempUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 处理识别文字的线程
     */
    private class OcrRunnable implements Runnable {

        Bitmap originBitmap;
        Context context;

        OcrRunnable(Context context, Bitmap bitmap) {
            this.context = context;
            this.originBitmap = bitmap;
        }

        @Override
        public void run() {
            if (originBitmap == null) {
                scanFailed("图片为空");
                return;
            }
            if (originBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                scanFailed("图片非ARGB_8888格式");
                return;
            }
            try {
                TessBaseAPI api = new TessBaseAPI();
                api.init(Common.getTessDataDir(context), Common.getLanguage(context));
                api.setImage(originBitmap.copy(Bitmap.Config.ARGB_8888, true));
                final String result = api.getUTF8Text();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isStarting && !isFinishing()) {
                            scanSuccess(result);
                        }
                    }
                });
                originBitmap.recycle();
                api.clear();
                api.end();
            } catch (final Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ("Data path must contain subfolder tessdata!".equals(e.getMessage())) {
                            scanFailed("字典文件没有准备好");
                        } else {
                            scanFailed(e.getMessage());
                        }
                    }
                });
            }
        }
    }

    /**
     * 处理图片优化的线程
     */
    private class BitmapResolveRunnable implements Runnable {
        Bitmap origin;
        int type;

        BitmapResolveRunnable(Bitmap origin, int type) {
            this.origin = origin;
            this.type = type;
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void run() {
            final Bitmap bitmap;
            switch (type) {
                case R.id.originToolTv:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_avatar);
                    break;
                case R.id.heibaiToolTv:
                    bitmap = BitmapUtils.turnBlackWhite(origin);
                    break;
                case R.id.grayToolTv:
                    bitmap = BitmapUtils.lineGray(origin);
                    break;
                case R.id.binaryToolTv:
                    bitmap = BitmapUtils.turnBinary(origin);
                    break;
                default:
                    bitmap = null;
                    break;
            }
            if (bitmap != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivSrc.setImageBitmap(bitmap);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    /**
     * 复制文件的线程
     */
    private class CopyFileRunnable implements Runnable {
        File target;
        Uri uri;

        /**
         * 复制某个文件的线程
         *
         * @param uri    源文件uri
         * @param target 目标文件
         */
        CopyFileRunnable(Uri uri, File target) {
            this.uri = uri;
            this.target = target;
        }

        @Override
        public void run() {
            try {
                File origin = new File(uri.getPath());
                InputStream fis = new FileInputStream(origin);
                OutputStream fos = new FileOutputStream(target);
                byte[] bytes = new byte[1024];
                int c;
                while ((c = fis.read(bytes)) > 0) {
                    fos.write(bytes, 0, c);
                }
                fis.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}