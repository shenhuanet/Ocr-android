package com.shenhua.ocr.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.shenhua.ocr.R;
import com.shenhua.ocr.dao.History;
import com.shenhua.ocr.dao.HistoryDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shenhua on 2017-10-26-0026.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class ResultActivity extends AppCompatActivity {

    @BindView(R.id.timeTv)
    TextView mTimeTv;
    @BindView(R.id.resultEt)
    EditText mResultEt;
    @BindView(R.id.srcIv)
    ImageView mSrcIv;

    private History mHistory;
    private static final int STATUS_NONE = 0;
    public static final int STATUS_MODIFY = 1;
    public static final int STATUS_DELETE = 2;
    private static final int TYPE_DATA = 3;
    private static final int TYPE_VIEW = 4;
    private boolean isDelete;
    private int mCurrentStatus = STATUS_NONE;
    private int mCurrentType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_result);
        ButterKnife.bind(this);

        showData();
        initView();

    }

    private void initView() {
        mResultEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mCurrentStatus = STATUS_MODIFY;
            }
        });
    }

    /**
     * 装载数据
     */
    private void showData() {
        long id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            mHistory = HistoryDatabase.get(this).find(id);
            mCurrentType = TYPE_VIEW;
        } else {
            mHistory = (History) getIntent().getSerializableExtra("data");
            mCurrentType = TYPE_DATA;
        }

        if (mHistory != null) {
            mTimeTv.setText(String.format(getString(R.string.text_use_time_format), mHistory.getTime()));
            mResultEt.setText(mHistory.getResult());
            mResultEt.setSelection(mHistory.getResult().length());
            Glide.with(this).load(mHistory.getImg()).into(mSrcIv);
        }
    }

    /**
     * 复制内容
     */
    @OnClick(R.id.copyBtn)
    void copy() {
        ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText("content", mResultEt.getText().toString()));
        Toast.makeText(this, R.string.string_clip_done, Toast.LENGTH_SHORT).show();
    }

    /**
     * 分享内容
     */
    @OnClick(R.id.shareBtn)
    void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, mResultEt.getText().toString());
        Intent chooserIntent = Intent.createChooser(intent, getString(R.string.string_choise_intent));
        if (chooserIntent == null) {
            return;
        }
        try {
            startActivity(chooserIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.string_share_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 删除,不存为记录
     */
    @OnClick(R.id.deleteBtn)
    void delete() {
        // don't save and finish.
        if (mHistory != null && mCurrentType == TYPE_VIEW) {
            HistoryDatabase.get(this).remove(mHistory.getId());
        }
        isDelete = true;
        setResult(STATUS_DELETE);
        ResultActivity.this.finish();
    }

    /**
     * 隐藏软键盘
     */
    @OnClick(R.id.resultLayout)
    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // save() if not click the delete button
        onPreSave();
        super.onDestroy();
    }

    /**
     * 保存之前需要做的
     */
    private void onPreSave() {
        if (mHistory == null) {
            return;
        }
        if (mCurrentType == TYPE_DATA) {
            if (isDelete) {
                return;
            }
            save();
            return;
        }
        if (mCurrentType == TYPE_VIEW) {
            if (mCurrentStatus == STATUS_MODIFY && !isDelete) {
                update();
            }
        }
    }

    /**
     * 更新数据
     */
    private void update() {
        HistoryDatabase.get(this).update(mHistory.getId(), mResultEt.getText().toString());
        Toast.makeText(this, R.string.string_record_update, Toast.LENGTH_SHORT).show();
    }

    /**
     * 保存数据
     */
    private void save() {
        mHistory.setResult(mResultEt.getText().toString());
        long id = HistoryDatabase.get(this).add(mHistory);
        if (id > 0) {
            Toast.makeText(this, R.string.string_record_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.string_record_not_save, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentStatus == STATUS_MODIFY) {
            setResult(STATUS_MODIFY);
        }
        super.onBackPressed();
    }
}
