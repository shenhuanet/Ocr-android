package com.shenhua.ocr.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.shenhua.ocr.R;
import com.shenhua.ocr.activity.ResultActivity;
import com.shenhua.ocr.adapter.HistoryAdapter;
import com.shenhua.ocr.dao.HistoryDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by shenhua on 2017-10-19-0019.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class HistoryFragment extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    Unbinder unbinder;
    @BindView(R.id.rootLayout)
    LinearLayout rootLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private View mRootView;
    private HistoryAdapter historyAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_history, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        unbinder = ButterKnife.bind(this, mRootView);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Cursor cursor = new HistoryDatabase(getContext()).getAll();
        if (historyAdapter == null) {
            historyAdapter = new HistoryAdapter(getContext(), cursor, 0);
        }
        recyclerView.setAdapter(historyAdapter);
        historyAdapter.setOnClickListener(new HistoryAdapter.OnClickListener() {
            @Override
            public void onClick(int position, long id) {
                Intent intent = new Intent(getContext(), ResultActivity.class);
                intent.putExtra("id", id);
                getContext().startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
