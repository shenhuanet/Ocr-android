package com.shenhua.ocr.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shenhua.ocr.R;
import com.shenhua.ocr.dao.HistoryDatabase;
import com.shenhua.ocr.utils.Common;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shenhua on 2017-10-26-0026.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class HistoryAdapter extends BaseRecyclerCursorAdapter<HistoryAdapter.ResultViewHolder> {

    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @param flags   Flags used to determine the behavior of the adapter;
     *                Currently it accept {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public HistoryAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        final ResultViewHolder holder = new ResultViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(holder.getLayoutPosition(), (Long) v.getTag());
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onLongClickListener != null) {
                    onLongClickListener.onLongClick(holder.getLayoutPosition(), (Long) v.getTag());
                }
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ResultViewHolder holder, Cursor cursor) {
        holder.itemView.setTag(cursor.getLong(cursor.getColumnIndex(HistoryDatabase.KEY_ID)));
        holder.contentTv.setText(cursor.getString(cursor.getColumnIndex(HistoryDatabase.KEY_RESULT)));
        holder.timeTv.setText(cursor.getString(cursor.getColumnIndex(HistoryDatabase.KEY_TIME)));
        holder.dateTv.setText(Common.formatDate(cursor.getLong(cursor.getColumnIndex(HistoryDatabase.KEY_UPDATE))));
        Glide.with(mContext).load(cursor.getString(cursor.getColumnIndex(HistoryDatabase.KEY_IMG))).into(holder.srcIv);
    }

    @Override
    protected void onContentChanged() {
    }

    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.contentTv)
        TextView contentTv;
        @BindView(R.id.srcIv)
        ImageView srcIv;
        @BindView(R.id.timeTv)
        TextView timeTv;
        @BindView(R.id.dateTv)
        TextView dateTv;

        ResultViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnClickListener {
        /**
         * when user click the item.
         *
         * @param position position
         * @param id       cursor id
         */
        void onClick(int position, long id);
    }

    public interface OnLongClickListener {
        /**
         * when user long click the item.
         *
         * @param position position
         * @param id       cursor id
         */
        void onLongClick(int position, long id);
    }
}
