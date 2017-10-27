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

import com.shenhua.ocr.R;

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

    @BindView(R.id.adView)
    RelativeLayout adView;
    @BindView(R.id.infoTv)
    TextView infoTv;
    private boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        // AdManager.getInstance(this).init("e8f4d225e6c9b04a", "c649f87dae735c7b", true);
        AdManager.getInstance(this).init("85aa56a59eac8b3d", "a14006f66f58d5d7", true);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        preloadAd();
        setupSplashAd();
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

    private class CopyFiles extends AsyncTask<String, String, Void> {

        Context context;

        private CopyFiles(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onProgressUpdate("正在准备字典文件");
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("开始拷贝字典文件");
            String src = params[0];
            String dest = params[1];
            if (TextUtils.isEmpty(dest)) {
                throw new RuntimeException("参数错误 params[1] is dest dir.");
            }
            try {
                copy(src, dest);
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress("拷贝字典出错");
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
                publishProgress("拷贝: " + src + " ...");
                File outFile = new File(dest);
                Log.d("shenhuaLog -- " + CopyFiles.class.getSimpleName(), "copy: >>> " + outFile);
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
            infoTv.setText("");
            if (ready) {
                nav();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            infoTv.setText(values[0]);
        }
    }

    /**
     * 预加载广告
     */
    private void preloadAd() {
        // 注意：不必每次展示插播广告前都请求，只需在应用启动时请求一次
        SpotManager.getInstance(this).requestSpot(new SpotRequestListener() {
            @Override
            public void onRequestSuccess() {
                Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onRequestSuccess: >> 请求插播广告成功");
                // 应用安装后首次展示开屏会因为本地没有数据而跳过
                // 如果开发者需要在首次也能展示开屏，可以在请求广告成功之前展示应用的logo，请求成功后再加载开屏
                // setupSplashAd();
            }

            @Override
            public void onRequestFailed(int errorCode) {
                switch (errorCode) {
                    case ErrorCode.NON_NETWORK:
                        Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onRequestFailed: 网络异常");
                        break;
                    case ErrorCode.NON_AD:
                        Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onRequestFailed: NO AD");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 设置开屏广告
     */
    private void setupSplashAd() {
        // 创建开屏容器
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, R.id.adView);
        // 对开屏进行设置
        SplashViewSettings splashViewSettings = new SplashViewSettings();
        splashViewSettings.setAutoJumpToTargetWhenShowFailed(false);
        splashViewSettings.setTargetClass(MainActivity.class);
        // 设置开屏的容器
        splashViewSettings.setSplashViewContainer(adView);
        // 展示开屏广告
        SpotManager.getInstance(this)
                .showSplash(this, splashViewSettings, new SpotListener() {

                    @Override
                    public void onShowSuccess() {
                        Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onShowSuccess: 开屏展示成功");
                    }

                    @Override
                    public void onShowFailed(int errorCode) {
                        switch (errorCode) {
                            case ErrorCode.NON_NETWORK:
                                Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onShowFailed: 网络异常");
                                break;
                            case ErrorCode.NON_AD:
                                Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onShowFailed: 暂无开屏广告");
                                break;
                            case ErrorCode.RESOURCE_NOT_READY:
                                Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onShowFailed: 开屏资源还没准备好");
                                break;
                            case ErrorCode.SHOW_INTERVAL_LIMITED:
                                Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onShowFailed: 开屏展示间隔限制");
                                break;
                            case ErrorCode.WIDGET_NOT_IN_VISIBILITY_STATE:
                                Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onShowFailed: 开屏控件处在不可见状态");
                                break;
                            default:
                                break;
                        }
                        ready = true;
                    }

                    @Override
                    public void onSpotClosed() {
                        Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onShowFailed: 开屏被关闭");
                    }

                    @Override
                    public void onSpotClicked(boolean isWebPage) {
                        Log.d("shenhuaLog -- " + SplashActivity.class.getSimpleName(), "onShowFailed: 开屏被点击");
                    }
                });
    }

    private void nav() {
        adView.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                SplashActivity.this.finish();
            }
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SpotManager.getInstance(this).onDestroy();
    }
}
