package com.shenhua.ocr.helper;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by shenhua on 2017-10-19-0019.
 * Email shenhuanet@126.com
 */
public final class ControlPanel {

    private int panelHeight;
    private static ControlPanel sInstance = null;

    public static ControlPanel get() {
        if (sInstance == null) {
            synchronized (ControlPanel.class) {
                if (sInstance == null) {
                    sInstance = new ControlPanel();
                }
            }
        }
        return sInstance;
    }

    private ControlPanel() {
    }

    public ControlPanel attachView(final View layoutPanel) {
        layoutPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                panelHeight = layoutPanel.getHeight();
                layoutPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        return this;
    }

    public void expand(View panel) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(panel, "translationY", panelHeight - 40, 0);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(500).start();
    }

    public void collapse(View panel) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(panel, "translationY", 0, panelHeight - 40);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(500).start();
    }

}
