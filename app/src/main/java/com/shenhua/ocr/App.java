package com.shenhua.ocr;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by shenhua on 2017-10-31-0031.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "07674e00fe", true);
    }
}
