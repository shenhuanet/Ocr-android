package com.shenhua.ocr.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.shenhua.ocr.R;
import com.shenhua.ocr.fragment.CameraFragment;
import com.shenhua.ocr.fragment.HistoryFragment;
import com.shenhua.ocr.fragment.HomeFragment;
import com.shenhua.ocr.fragment.UserFragment;
import com.shenhua.ocr.helper.ControlPanel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.layoutPanel)
    FrameLayout layoutPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ControlPanel.get().attachView(layoutPanel);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    ControlPanel.get().expand(layoutPanel);
                }
            }
        });
    }

    @OnClick({R.id.btnStart, R.id.btnAccount, R.id.btnHistory})
    void clicks(View view) {
        Fragment fragment = null;
        String name = null;
        switch (view.getId()) {
            case R.id.btnStart:
                fragment = new CameraFragment();
                name = "takePic";
                break;
            case R.id.btnAccount:
                fragment = new UserFragment();
                name = "user";
                break;
            case R.id.btnHistory:
                fragment = new HistoryFragment();
                name = "history";
                break;
        }
        ControlPanel.get().collapse(layoutPanel);
        replaceFragment(fragment, name);
    }

    private void replaceFragment(Fragment fragment, String name) {
        if (fragment == null) return;
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(name)
                .commit();
    }

}
