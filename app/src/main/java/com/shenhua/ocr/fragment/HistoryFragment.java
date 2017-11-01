package com.shenhua.ocr.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
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
import com.shenhua.ocr.dao.HistoryLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by shenhua on 2017-10-19-0019.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    Unbinder unbinder;
    @BindView(R.id.rootLayout)
    LinearLayout rootLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private View mRootView;
    private HistoryAdapter historyAdapter;
    private static final int CURSOR_LOADER = 1;

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
        getLoaderManager().initLoader(CURSOR_LOADER, null, this);
        if (historyAdapter == null) {
            historyAdapter = new HistoryAdapter(getContext(), null, 0);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(historyAdapter);
        historyAdapter.setOnClickListener(new HistoryAdapter.OnClickListener() {
            @Override
            public void onClick(int position, long id) {
                navResult(position, id);
            }
        });
        historyAdapter.setOnLongClickListener(new HistoryAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(int position, long id) {
                showDialog(position, id);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER, null, this);
//        getLoaderManager().getLoader(CURSOR_LOADER).startLoading();
    }

    private void navResult(int position, long id) {
        Intent intent = new Intent(getContext(), ResultActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, position);
    }

    private void showDialog(final int position, final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(new CharSequence[]{"查看", "删除"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    navResult(position, id);
                }
                if (which == 1) {
                    deleteItem(position, id);
                }
            }
        });
    }

    private void deleteItem(int position, long id) {
        int p = HistoryDatabase.get(getContext()).remove(id);
        if (p > 0) {
            historyAdapter.onItemRemoved(position);
        } else {
            Snackbar.make(mRootView, "删除失败", Snackbar.LENGTH_SHORT).show();
        }
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
        getLoaderManager().getLoader(CURSOR_LOADER).stopLoading();
        unbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ResultActivity.STATUS_MODIFY || resultCode == ResultActivity.STATUS_DELETE) {
            getLoaderManager().restartLoader(CURSOR_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // showProgress
        return new HistoryLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // hideProgress
        historyAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        historyAdapter.swapCursor(null);
    }
}
