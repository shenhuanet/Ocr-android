package com.shenhua.ocr.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.shenhua.ocr.R;
import com.shenhua.ocr.fragment.HistoryFragment;
import com.shenhua.ocr.fragment.HomeFragment;
import com.shenhua.ocr.fragment.TakePicFragment;
import com.shenhua.ocr.fragment.UserFragment;
import com.shenhua.ocr.helper.ControlPanel;
import com.shenhua.ocr.helper.ToolbarCallback;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shenhua on 2017-10-19-0019.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class MainActivity extends AppCompatActivity implements ToolbarCallback {

    @BindView(R.id.layoutPanel)
    ViewGroup mLayoutPanel;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private static final int CLICK_MIN_TIME = 1000;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        ControlPanel.get().attachView(mLayoutPanel);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    ControlPanel.get().expand(mLayoutPanel);
                }
            }
        });
    }

    @OnClick({R.id.startBtn, R.id.accountBtn, R.id.historyBtn})
    void clicks(View view) {
        // 防止双击
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - mLastClickTime < CLICK_MIN_TIME) {
            return;
        }
        Fragment fragment = null;
        String name = null;
        switch (view.getId()) {
            case R.id.startBtn:
                fragment = new TakePicFragment();
                name = "takePic";
                break;
            case R.id.accountBtn:
                fragment = new UserFragment();
                name = "user";
                break;
            case R.id.historyBtn:
                fragment = new HistoryFragment();
                name = "history";
                break;
            default:
                break;
        }
        ControlPanel.get().collapse(mLayoutPanel);
        replaceFragment(fragment, name);
        mLastClickTime = currentTime;
    }

    private void replaceFragment(Fragment fragment, String name) {
        if (fragment == null) {
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(name)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onShow(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onHide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }
}
