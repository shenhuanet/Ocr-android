package com.shenhua.ocr.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;
import com.shenhua.ocr.BuildConfig;
import com.shenhua.ocr.R;
import com.shenhua.ocr.utils.Common;

import net.youmi.android.AdManager;
import net.youmi.android.nm.cm.ErrorCode;
import net.youmi.android.nm.sp.SplashViewSettings;
import net.youmi.android.nm.sp.SpotListener;
import net.youmi.android.nm.sp.SpotManager;
import net.youmi.android.nm.sp.SpotRequestListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @author shenhua
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    @BindView(R.id.adView)
    RelativeLayout mAdView;
    @BindView(R.id.infoTv)
    TextView mInfoTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        if (BuildConfig.ENV_TYPE == 1) {
            initGdtAD();
        } else {
            initYoumiAd();
        }
    }

    private void initGdtAD() {
        new SplashAD(this, mAdView, Common.TENCENT_APP_ID, Common.TENCENT_POS_ID, new SplashADListener() {
            @Override
            public void onADDismissed() {
                Log.w(TAG, "onADDismissed: ");
                nav(false);
            }

            @Override
            public void onNoAD(AdError adError) {
                Log.w(TAG, "onNoAD: " + adError.getErrorCode());
                nav(true);
            }

            @Override
            public void onADPresent() {
                Log.w(TAG, "onADPresent: ");
            }

            @Override
            public void onADClicked() {
                Log.w(TAG, "onADClicked: ");
            }

            @Override
            public void onADTick(long l) {
                Log.w(TAG, "onADTick: " + Math.round(l / 1000f));
            }
        });
    }

    private void initYoumiAd() {
        AdManager.getInstance(this).init(Common.YOUMI_APP_ID, Common.YOUMI_APP_SECRET, true);
        /*
         * 预加载ad,注意：不必每次展示插播ad前都请求，只需在应用启动时请求一次
         */
        SpotManager.getInstance(this).requestSpot(new SpotRequestListener() {
            @Override
            public void onRequestSuccess() {
                Log.w(TAG, "onRequestSuccess: >> request success.");
                // 应用安装后首次展示开屏会因为本地没有数据而跳过
                // 如果开发者需要在首次也能展示开屏，可以在请求ad成功之前展示应用的logo，请求成功后再加载开屏
                // setupSplashAd();
            }

            @Override
            public void onRequestFailed(int errorCode) {
                switch (errorCode) {
                    case ErrorCode.NON_NETWORK:
                        Log.w(TAG, "onRequestFailed: 网络异常");
                        break;
                    case ErrorCode.NON_AD:
                        Log.w(TAG, "onRequestFailed: NO AD");
                        break;
                    default:
                        break;
                }
            }
        });
        /*
         * 设置开屏ad
         */
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, R.id.adView);
        // 对开屏进行设置
        SplashViewSettings splashViewSettings = new SplashViewSettings();
        splashViewSettings.setAutoJumpToTargetWhenShowFailed(false);
        splashViewSettings.setTargetClass(MainActivity.class);
        // 设置开屏的容器
        splashViewSettings.setSplashViewContainer(mAdView);
        // 展示开屏ad
        SpotManager.getInstance(this)
                .showSplash(this, splashViewSettings, new SpotListener() {

                    @Override
                    public void onShowSuccess() {
                        Log.w(TAG, "onShowSuccess: success.");
                        nav(false);
                    }

                    @Override
                    public void onShowFailed(int errorCode) {
                        switch (errorCode) {
                            case ErrorCode.NON_NETWORK:
                                Log.w(TAG, "onShowFailed: net error.");
                                break;
                            case ErrorCode.NON_AD:
                                Log.w(TAG, "onShowFailed: no ad.");
                                break;
                            case ErrorCode.RESOURCE_NOT_READY:
                                Log.w(TAG, "onShowFailed: not ready.");
                                break;
                            case ErrorCode.SHOW_INTERVAL_LIMITED:
                                Log.w(TAG, "onShowFailed: time limit.");
                                break;
                            case ErrorCode.WIDGET_NOT_IN_VISIBILITY_STATE:
                                Log.w(TAG, "onShowFailed: invisibility.");
                                break;
                            default:
                                break;
                        }
                        nav(true);
                    }

                    @Override
                    public void onSpotClosed() {
                        Log.w(TAG, "onShowFailed: on closed.");
                    }

                    @Override
                    public void onSpotClicked(boolean isWebPage) {
                        Log.w(TAG, "onShowFailed: on click.");
                    }
                });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        /*
         * 初始化字典
         */
        CopyFiles task = new CopyFiles(this);
        task.execute("", getFilesDir() + "/tessdata");
    }

    /**
     * 复制文件的异步任务
     */
    private class CopyFiles extends AsyncTask<String, String, Void> {

        Context context;

        private CopyFiles(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onProgressUpdate(getString(R.string.string_splash_prepare));
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress(getString(R.string.string_splash_start_copy));
            String src = params[0];
            String dest = params[1];
            if (TextUtils.isEmpty(dest)) {
                throw new RuntimeException(getString(R.string.string_splash_param_error));
            }
            try {
                copy(src, dest);
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress(getString(R.string.string_splash_copy_error));
            }
            return null;
        }

        private void copy(String src, String dest) throws IOException {
            String[] fileNames = context.getAssets().list(src);
            if (fileNames.length > 0) {
                File file = new File(dest);
                if (!file.exists()) {
                    file.mkdirs();
                }
                for (String fileName : fileNames) {
                    // assets 文件夹下的目录
                    if (!TextUtils.isEmpty(src)) {
                        copy(src + File.separator + fileName, dest + File.separator + fileName);
                    } else { // assets 文件夹
                        copy(fileName, dest + File.separator + fileName);
                    }
                }
            } else {
                publishProgress(getString(R.string.string_splash_copy) + src + " ...");
                File outFile = new File(dest);
                InputStream is = context.getAssets().open(src);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isFinishing()) {
                mInfoTv.setText("");
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mInfoTv.setText(values[0]);
        }
    }

    /**
     * 跳转至主页面
     *
     * @param error if error the info showtime is longer.
     */
    private void nav(boolean error) {
        long time = error ? 6000 : 1500;
        mAdView.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                SplashActivity.this.finish();
            }
        }, time);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.ENV_TYPE == 2) {
            SpotManager.getInstance(this).onDestroy();
        }
    }
}
