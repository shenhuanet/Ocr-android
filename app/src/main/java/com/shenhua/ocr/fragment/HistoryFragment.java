package com.shenhua.ocr.fragment;

import android.content.Context;
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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.shenhua.ocr.R;
import com.shenhua.ocr.activity.ResultActivity;
import com.shenhua.ocr.adapter.HistoryAdapter;
import com.shenhua.ocr.dao.HistoryDatabase;
import com.shenhua.ocr.dao.HistoryLoader;
import com.shenhua.ocr.helper.ToolbarCallback;

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

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private View mRootView;
    private Unbinder mUnBinder;
    private HistoryAdapter mHistoryAdapter;
    private static final int CURSOR_LOADER = 1;
    private ToolbarCallback mCallback;

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
            mCallback.onShow(getString(R.string.string_history));
        }
    }

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
        mUnBinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(CURSOR_LOADER, null, this);
        if (mHistoryAdapter == null) {
            mHistoryAdapter = new HistoryAdapter(getContext(), null, 2);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mHistoryAdapter);
        mHistoryAdapter.setOnClickListener(new HistoryAdapter.OnClickListener() {
            @Override
            public void onClick(int position, long id) {
                navResult(position, id);
            }
        });
        mHistoryAdapter.setOnLongClickListener(new HistoryAdapter.OnLongClickListener() {
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
    }

    /**
     * 跳转至结果详情页面
     *
     * @param position item position
     * @param id       记录id
     */
    private void navResult(int position, long id) {
        Intent intent = new Intent(getContext(), ResultActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, position);
    }

    /**
     * 长按显示对话框
     *
     * @param position item position
     * @param id       记录id
     */
    private void showDialog(final int position, final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(new CharSequence[]{getString(R.string.string_viewer), getString(R.string.string_delete)}, new DialogInterface.OnClickListener() {
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
        builder.show();
    }

    /**
     * 删除一个条目
     *
     * @param position item position
     * @param id       记录id
     */
    private void deleteItem(int position, long id) {
        int p = HistoryDatabase.get(getContext()).remove(id);
        if (p > 0) {
            mHistoryAdapter.onItemRemoved(position);
            getLoaderManager().restartLoader(CURSOR_LOADER, null, this);
        } else {
            Snackbar.make(mRootView, R.string.string_delete_error, Snackbar.LENGTH_SHORT).show();
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().getLoader(CURSOR_LOADER).stopLoading();
        if (mCallback != null) {
            mCallback.onHide();
        }
        mUnBinder.unbind();
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
        mHistoryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHistoryAdapter.swapCursor(null);
    }
}
