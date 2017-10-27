package com.shenhua.ocr.helper;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.util.AttributeSet;

/**
 * Created by shenhua on 2017-10-19-0019.
 * Email shenhuanet@126.com
 *
 * @author shenhua
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class FragmentTransition extends TransitionSet {

    public FragmentTransition() {
        init();
    }

    public FragmentTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());
    }

}
