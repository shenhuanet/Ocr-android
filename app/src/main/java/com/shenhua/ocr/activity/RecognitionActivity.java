package com.shenhua.ocr.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.shenhua.ocr.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shenhua on 2017-10-23-0023.
 * Email shenhuanet@126.com
 */
public class RecognitionActivity extends AppCompatActivity {

    @BindView(R.id.ivScan)
    ImageView ivScan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScanAnim();
    }

    private void startScanAnim() {
        Animation scanAnim = AnimationUtils.loadAnimation(this, R.anim.scan);
        ivScan.setAnimation(scanAnim);
        scanAnim.start();
    }

    // 停止扫描动画
    private void stopScanAnim() {
        try {
            if (ivScan.getAnimation() != null) {
                ivScan.getAnimation().cancel();
                ivScan.clearAnimation();
            }
        } catch (Exception e) {// NullPointerException
            e.printStackTrace();
        }
    }
}