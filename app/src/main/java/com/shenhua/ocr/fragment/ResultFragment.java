package com.shenhua.ocr.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.shenhua.ocr.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by shenhua on 2017-10-26-0026.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class ResultFragment extends Fragment {

    @BindView(R.id.resultEt)
    EditText etResult;
    @BindView(R.id.srcIv)
    ImageView ivSrc;
    @BindView(R.id.copyBtn)
    TextView copyTv;
    Unbinder unbinder;
    @BindView(R.id.timeTv)
    TextView timeTv;
    @BindView(R.id.outputBtn)
    TextView outputTv;
    @BindView(R.id.shareBtn)
    TextView shareTv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        HistoryDatabase database = new HistoryDatabase(getContext());
//        History history = new History();
//        history.setDate(System.currentTimeMillis());
//        history.setTime("12:45:30");
//        history.setResult("啦啦啦");
//        history.setImg("");
//        database.add(history);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
