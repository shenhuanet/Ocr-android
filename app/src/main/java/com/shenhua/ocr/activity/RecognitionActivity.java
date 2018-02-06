package com.shenhua.ocr.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.shenhua.ocr.R;
import com.shenhua.ocr.dao.History;
import com.shenhua.ocr.utils.BitmapUtils;
import com.shenhua.ocr.utils.Common;

import java.io.File;
import java.io.FileInputStream;
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
    ImageView mScanIv;
    @BindView(R.id.srcIv)
    ImageView mSrcIv;
    @BindView(R.id.scanNotifyLayout)
    LinearLayout mScanNotifyLayout;
    @BindView(R.id.scanTimer)
    Chronometer mScanTimer;
    /**
     * tool items
     */
    @BindView(R.id.resolveTv)
    TextView mResolveTv;
    @BindView(R.id.rotationTv)
    TextView mRotationTv;
    @BindView(R.id.langTv)
    TextView mLangTv;
    @BindView(R.id.startBtn)
    ImageButton mStartBtn;
    @BindView(R.id.startTv)
    TextView mStartTv;
    /**
     * tools panel
     */
    @BindView(R.id.toolsLayout)
    LinearLayout mToolsLayout;
    /**
     * root layout
     */
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    private volatile boolean isStarting = false;
    private ExecutorService mExecutor;
    private int mCurrentItem = R.id.originToolTv;
    /**
     * 是否已经准备好,用于控制当图片uri为空时,设置为未准备好
     */
    private boolean isReady = true;
    private boolean isReslove = true;
    private Uri mTempUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_recognition);
        ButterKnife.bind(this);

        mTempUri = getIntent().getParcelableExtra("temp");

        Glide.with(this).load(mTempUri).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (bitmap == null) {
                            isReady = false;
                            Snackbar.make(mStartBtn, R.string.string_null_pic, Snackbar.LENGTH_SHORT).show();
                        }
                        mSrcIv.setImageBitmap(bitmap);
                        itemSelected(mToolsLayout.getChildAt(0));
                        mLangTv.setText(Common.getDisplayLanguage(RecognitionActivity.this));
                        mExecutor = Executors.newSingleThreadExecutor();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 开始
     */
    @OnClick(R.id.startBtn)
    void start() {
        if (!isReady) {
            Snackbar.make(mStartBtn, R.string.string_null_pic, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!isStarting && isReslove) {
            startScanAnim();
            mStartTv.setText(R.string.string_cancel);
            Bitmap bitmap = ((BitmapDrawable) mSrcIv.getDrawable()).getBitmap();
            if (bitmap == null) {
                Snackbar.make(mStartBtn, "图片资源未准备好,请重试或准备小图", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Log.d("RecognitionActivity", "start recognition ...... the bitmap width is "
                    + bitmap.getWidth() + " height is " + bitmap.getHeight());
            /*
             * fixed: 2018/1/23
              * rejected from java.util.concurrent.ThreadPoolExecutor@a9fcf2a[Shutting down, pool size = 1, active threads = 1, queued tasks = 0, completed tasks = 3]
             */
            if (mExecutor == null || mExecutor.isShutdown()) {
                mExecutor = Executors.newSingleThreadExecutor();
            }
            mExecutor.execute(new OcrRunnable(this, bitmap));
            isStarting = true;
        } else {
            stopScanAnim();
            mStartTv.setText(R.string.string_start);
            mExecutor.shutdownNow();
            isStarting = false;
        }
    }

    /**
     * 识别成功时的回调
     *
     * @param result 识别结果
     */
    private void scanSuccess(String result) {
        stopScanAnim();
        History data = new History();
        data.setTime(mScanTimer.getText().toString());
        data.setDate(System.currentTimeMillis());
        data.setResult(result);
        File target = Common.getHistoryPhoto(this, Common.getSaveFileName());
        mExecutor.execute(new CopyFileRunnable(mTempUri, target));
        data.setImg(target.getPath());
        Intent intent = new Intent(this, ResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", data);
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }

    /**
     * 识别失败时回调
     *
     * @param msg 提示的信息
     */
    private void scanFailed(String msg) {
        stopScanAnim();
        Snackbar.make(mStartBtn, msg, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * 开始扫面动画以及控制view的操作
     */
    private void startScanAnim() {
        mScanIv.setVisibility(View.VISIBLE);
        Animation scanAnim = AnimationUtils.loadAnimation(this, R.anim.scan);
        mScanIv.setAnimation(scanAnim);
        scanAnim.start();

        Animation notify = AnimationUtils.loadAnimation(this, R.anim.right_in);
        mScanNotifyLayout.setAnimation(notify);
        notify.start();
        mScanNotifyLayout.setVisibility(View.VISIBLE);

        mScanTimer.setBase(SystemClock.elapsedRealtime());
        mScanTimer.start();

        mToolsLayout.setVisibility(View.GONE);
        mResolveTv.setEnabled(false);
        mRotationTv.setEnabled(false);
        mLangTv.setEnabled(false);
    }

    /**
     * 停止扫描动画以及控制view的操作
     */
    private void stopScanAnim() {
        try {
            if (mScanIv.getAnimation() != null) {
                mScanIv.getAnimation().cancel();
                mScanIv.clearAnimation();
                mScanIv.setVisibility(View.GONE);
            }
        } catch (Exception e) {// NullPointerException
            e.printStackTrace();
        }

        Animation notify = AnimationUtils.loadAnimation(this, R.anim.right_out);
        mScanNotifyLayout.setAnimation(notify);
        notify.start();
        mScanNotifyLayout.setVisibility(View.GONE);

        mScanTimer.stop();
        isStarting = false;
        mStartTv.setText(R.string.string_start);

        mResolveTv.setEnabled(true);
        mRotationTv.setEnabled(true);
        mLangTv.setEnabled(true);
    }

    /**
     * 处置
     */
    @OnClick(R.id.resolveTv)
    void resolve() {
        if (mToolsLayout.getVisibility() == View.GONE) {
            mToolsLayout.setVisibility(View.VISIBLE);
        } else {
            mToolsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 更换当前图片处理选项
     *
     * @param view views
     */
    @OnClick({R.id.originToolTv, R.id.heibaiToolTv, R.id.grayToolTv, R.id.binaryToolTv})
    void changeTool(final View view) {
        if (!isReady) {
            Snackbar.make(mStartBtn, R.string.string_null_pic, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (view.getId() == mCurrentItem) {
            return;
        }

        Glide.with(this).load(mTempUri).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (bitmap != null) {
                            mCurrentItem = view.getId();
                            itemSelected(view);
                     /*
                      *  fixed: 2018/1/23
                      *  rejected from java.util.concurrent.ThreadPoolExecutor@5c1b7b2[Shutting down, pool size = 1, active threads = 1, queued tasks = 0, completed tasks = 1]
                      */
                            if (mExecutor == null || mExecutor.isShutdown()) {
                                mExecutor = Executors.newSingleThreadExecutor();
                            }
                            mExecutor.execute(new BitmapResolveRunnable(bitmap, view.getId()));
                        }
                    }
                });
    }

    /**
     * 改变语言
     */
    @OnClick(R.id.langTv)
    void changeLang(View view) {
        TextView textView = (TextView) view;
        if (getString(R.string.string_chinese).equals(textView.getText().toString())) {
            Common.saveLanguage(this, 1);
            Snackbar.make(mToolsLayout, R.string.string_en_done, Snackbar.LENGTH_SHORT).show();
        } else {
            Common.saveLanguage(this, 0);
            Snackbar.make(mToolsLayout, R.string.string_chinese_done, Snackbar.LENGTH_SHORT).show();
        }
        textView.setText(Common.getDisplayLanguage(this));
    }

    /**
     * 处理图片旋转
     */
    @OnClick(R.id.rotationTv)
    void rotation() {
        if (!isReady) {
            Snackbar.make(mStartBtn, "图片资源为空,操作不能进行", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = ((BitmapDrawable) mSrcIv.getDrawable()).getBitmap();
        mSrcIv.setImageBitmap(BitmapUtils.rotateBitmap(bitmap, 90f));
    }

    private void itemSelected(View view) {
        for (int i = 0; i < mToolsLayout.getChildCount(); i++) {
            mToolsLayout.getChildAt(i).setSelected(false);
        }
        view.setSelected(true);
    }

    /**
     * 获取初始化图片
     *
     * @return bitmap
     */
    private Bitmap getOriginBitmap() {
        try {
            return Glide.with(this).load(mTempUri).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
        } catch (Exception e) {
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
                scanFailed(getString(R.string.string_null_pic_single));
                return;
            }
            if (originBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                scanFailed(getString(R.string.string_pic_not_a888));
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
                        if (getString(R.string.error_tessdata_folder).equals(e.getMessage())) {
                            scanFailed(getString(R.string.string_tess_not_ready));
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
            mProgressBar.setVisibility(View.VISIBLE);
            isReslove = false;
        }

        @Override
        public void run() {
            final Bitmap bitmap;
            switch (type) {
                case R.id.originToolTv:
                    bitmap = getOriginBitmap();
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
                        isReslove = true;
                        mSrcIv.setImageBitmap(bitmap);
                        mProgressBar.setVisibility(View.GONE);
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