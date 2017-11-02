package com.shenhua.ocr.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenhua.ocr.R;
import com.shenhua.ocr.dao.HistoryDatabase;
import com.shenhua.ocr.helper.ToolbarCallback;
import com.shenhua.ocr.utils.Common;
import com.shenhua.ocr.widget.ConfirmDialog;
import com.tencent.bugly.beta.Beta;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by shenhua on 2017-10-19-0019.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class UserFragment extends Fragment {

    @BindView(R.id.avatarIv)
    ImageView mIvAvatar;
    @BindView(R.id.cacheTv)
    TextView mCacheTv;
    private View mRootView;
    private ToolbarCallback mCallback;
    private Unbinder mUnBinder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context != null) {
            mCallback = (ToolbarCallback) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mCallback != null) {
            mCallback.onShow(getString(R.string.string_user));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_user, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        mUnBinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_avatar_default);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        drawable.setCircular(true);
        mIvAvatar.setImageDrawable(drawable);
        bitmap.recycle();
        mCacheTv.setText(String.format(getString(R.string.string_setting_cache), Common.getCacheSize(getContext())));
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCallback != null) {
            mCallback.onHide();
        }
        mUnBinder.unbind();
    }

    /**
     * 个人设置
     */
    @OnClick({R.id.avatarIv, R.id.userSettingTv})
    void user() {
        Toast.makeText(getContext(), R.string.string_thanks, Toast.LENGTH_SHORT).show();
    }

    /**
     * 删除全部
     */
    @OnClick(R.id.deleteTv)
    void deleteAll() {
        ConfirmDialog dialog = new ConfirmDialog(getContext());
        dialog.setMessage(getString(R.string.string_confirm_delete_all));
        dialog.show();
        dialog.setPositiveListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HistoryDatabase.get(getContext()).deleteAll(getContext());
            }
        });
    }

    /**
     * 关于
     */
    @OnClick(R.id.aboutTv)
    void about() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.dialog_about);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        dialog.show();
    }

    /**
     * 清理缓存
     */
    @OnClick(R.id.cacheTv)
    void cleanCache() {
        Common.cleanCache(getContext());
        mCacheTv.setText(String.format(getString(R.string.string_setting_cache), "0KB"));
    }

    /**
     * 意见反馈
     */
    @OnClick(R.id.feedbackTv)
    void feedback() {
        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.dialog_feedback);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        dialog.show();
        EditText et = dialog.getWindow().findViewById(R.id.feedbackEt);
        dialog.getWindow().findViewById(R.id.cancelTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().findViewById(R.id.sureTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @OnClick(R.id.updateTv)
    void update() {
        // 参数一:用户手动点击,参数二:有提示
        Beta.checkUpgrade(true, false);
    }
}
