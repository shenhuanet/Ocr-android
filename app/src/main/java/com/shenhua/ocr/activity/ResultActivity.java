package com.shenhua.ocr.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
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
    TextView timeTv;
    @BindView(R.id.resultEt)
    EditText resultEt;
    @BindView(R.id.srcIv)
    ImageView srcIv;
    @BindView(R.id.outputBtn)
    ImageButton outputBtn;
    @BindView(R.id.copyBtn)
    ImageButton copyBtn;
    @BindView(R.id.shareBtn)
    ImageButton shareBtn;
    @BindView(R.id.deleteBtn)
    ImageButton deleteBtn;
    private History history;
    private static final int STATUS_NONE = 0;
    private static final int STATUS_MODIFY = 1;
    private static final int TYPE_DATA = 2;
    private static final int TYPE_VIEW = 3;
    private boolean isDelete;
    private int currentStatus = STATUS_NONE;
    private int currentType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_result);
        ButterKnife.bind(this);

        showData();
        initView();

    }

    private void initView() {
        resultEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentStatus = STATUS_MODIFY;
            }
        });
    }

    /**
     * 装载数据
     */
    private void showData() {
        long id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            history = new HistoryDatabase(this).find(id);
            currentType = TYPE_VIEW;
        } else {
            history = (History) getIntent().getSerializableExtra("data");
            currentType = TYPE_DATA;
        }

        if (history != null) {
            timeTv.setText(String.format(getString(R.string.text_use_time_format), history.getTime()));
            resultEt.setText(history.getResult());
            resultEt.setSelection(history.getResult().length());
            Glide.with(this).load(history.getImg()).into(srcIv);
        }
    }

    @OnClick(R.id.outputBtn)
    void output(View view) {
// TODO: 2017-11-01-0001
    }

    @OnClick(R.id.copyBtn)
    void copy(View view) {
// TODO: 2017-11-01-0001
    }

    @OnClick(R.id.shareBtn)
    void share(View view) {
// TODO: 2017-11-01-0001
    }

    @OnClick(R.id.deleteBtn)
    void delete(View view) {
        // don't save and finish.
        if (history != null && currentType == TYPE_VIEW) {
            new HistoryDatabase(this).remove(history.getId());
        }
        isDelete = true;
        ResultActivity.this.finish();
    }

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

    private void onPreSave() {
        if (history == null) {
            return;
        }
        if (currentType == TYPE_DATA) {
            if (isDelete) {
                return;
            }
            save();
            return;
        }
        if (currentType == TYPE_VIEW) {
            if (currentStatus == STATUS_MODIFY && !isDelete) {
                update();
            }
        }
    }

    private void update() {
        new HistoryDatabase(this).update(history.getId(), resultEt.getText().toString());
//        setResult();
    }

    private void save() {
        history.setResult(resultEt.getText().toString());
        long id = new HistoryDatabase(this).add(history);
        if (id > 0) {
            Toast.makeText(this, "数据已保存", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "数据未保存", Toast.LENGTH_SHORT).show();
        }
    }
}
