package com.shenhua.ocr.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.shenhua.ocr.R;
import com.shenhua.ocr.dao.History;
import com.shenhua.ocr.utils.BitmapUtils;
import com.shenhua.ocr.utils.Common;

import java.io.File;
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

    private volatile boolean isStarting = false;
    private ExecutorService executor;
    private int currentItem = R.id.originToolTv;
    private String lang;
    private String tessDir;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        getWindow().setStatusBarColor(Color.BLACK);
        ButterKnife.bind(this);

        ivSrc.setImageResource(R.drawable.img_sample);
        itemSelected(originToolTv);
        lang = getSharedPreferences("config", Context.MODE_PRIVATE).getString("lang", Common.getLanguage(0));
        langTv.setText(lang.equals(Common.getLanguage(0)) ? "中文" : "EN");
        tessDir = getFilesDir() + File.separator + "tessdata";
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.startBtn)
    void start(View view) {
        if (!isStarting) {
            startScanAnim();
            startTv.setText("取消");
            executor = Executors.newSingleThreadExecutor();

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_sample);
            executor.execute(new OCR(bitmap));
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
        //constraintLayout.setVisibility(View.GONE);
        //getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        //.replace(R.id.container, new ResultFragment()).commit();

        Log.d("shenhuaLog -- " + RecognitionActivity.class.getSimpleName(), "scanSuccess: >> " + result);
        Log.d("shenhuaLog -- " + RecognitionActivity.class.getSimpleName(), "scanSuccess: >> " + scanTimer.getText().toString());

        History data = new History();
        data.setTime(scanTimer.getText().toString());
        data.setDate(System.currentTimeMillis());
        data.setResult(result);
        data.setImg("");

        Intent intent = new Intent(this, ResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", data);
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }

    private void scanFailed(String msg) {
        stopScanAnim();
        Snackbar.make(btnStart, msg, Snackbar.LENGTH_SHORT).show();
    }

    private class OCR implements Runnable {

        Bitmap originBitmap;

        OCR(Bitmap bitmap) {
            originBitmap = bitmap;
        }

        @Override
        public void run() {
            if (originBitmap == null || originBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                scanFailed("图片不能为空或非ARGB_8888格式");
                return;
            }
            try {
                TessBaseAPI api = new TessBaseAPI();
                api.init(tessDir, lang);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_sample);
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
                        scanFailed(e.getMessage());
                    }
                });
            }
        }
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
        if (view.getId() == currentItem) {
            return;
        }
        switch (view.getId()) {
            case R.id.originToolTv:
                break;
            case R.id.heibaiToolTv:
                break;
            case R.id.grayToolTv:
                break;
            case R.id.binaryToolTv:
                break;
            default:
                break;
        }
        currentItem = view.getId();
        itemSelected((TextView) view);
    }

    /**
     * 改变语言
     */
    @OnClick(R.id.langTv)
    void changeLang(View view) {
        TextView textView = (TextView) view;
        if ("中文".equals(textView.getText().toString())) {
            textView.setText("EN");
            Snackbar.make(toolsLayout, "已切换为英文模式", Snackbar.LENGTH_SHORT).show();
            lang = Common.getLanguage(1);
        } else {
            textView.setText("中文");
            Snackbar.make(toolsLayout, "已切换为中文模式", Snackbar.LENGTH_SHORT).show();
            lang = Common.getLanguage(0);
        }
    }

    @OnClick(R.id.rotationTv)
    void rotation() {
        Bitmap bitmap = ((BitmapDrawable) ivSrc.getDrawable()).getBitmap();
        ivSrc.setImageBitmap(BitmapUtils.rotateBitmap(bitmap, 90f));
    }

    private void itemSelected(TextView view) {
        originToolTv.setSelected(false);
        heibaiToolTv.setSelected(false);
        grayToolTv.setSelected(false);
        binaryToolTv.setSelected(false);

        view.setSelected(true);
//        Snackbar.make(toolsLayout, "okokokok", Snackbar.LENGTH_SHORT).show();
    }

}