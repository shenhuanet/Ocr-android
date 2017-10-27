package com.shenhua.ocr.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    private boolean isDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_result);
        ButterKnife.bind(this);

        showData();

    }

    /**
     * 装载数据
     */
    private void showData() {
        long id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            history = new HistoryDatabase(this).find(id);
        } else {
            history = (History) getIntent().getSerializableExtra("data");
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

    }

    @OnClick(R.id.copyBtn)
    void copy(View view) {

    }

    @OnClick(R.id.shareBtn)
    void share(View view) {

    }

    @OnClick(R.id.deleteBtn)
    void delete(View view) {
        // don't save and finish.
        isDelete = true;
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
        save();
        super.onDestroy();
    }

    private void save() {
        if (isDelete && history == null) {
            return;
        }
        long id = new HistoryDatabase(this).add(history);
        if (id > 0) {
            Toast.makeText(this, "数据已保存", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "数据未保存", Toast.LENGTH_SHORT).show();
        }
    }
}
