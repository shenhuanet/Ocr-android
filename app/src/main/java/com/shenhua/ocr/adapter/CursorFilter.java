package com.shenhua.ocr.adapter;

import android.database.Cursor;
import android.widget.Filter;

/**
 * Created by shenhua on 2017-10-26-0026.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
class CursorFilter extends Filter {

    private CursorFilterClient mClient;

    interface CursorFilterClient {
        /**
         * convert a cursor to string.
         *
         * @param cursor Cursor
         * @return CharSequence
         */
        CharSequence convertToString(Cursor cursor);

        /**
         * query on a worker thread.
         *
         * @param constraint CharSequence
         * @return Cursor
         */
        Cursor runQueryOnBackgroundThread(CharSequence constraint);

        /**
         * getCursor
         *
         * @return Cursor
         */
        Cursor getCursor();

        /**
         * changeCursor
         *
         * @param cursor Cursor
         */
        void changeCursor(Cursor cursor);
    }

    CursorFilter(CursorFilterClient client) {
        mClient = client;
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
        return mClient.convertToString((Cursor) resultValue);
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        Cursor cursor = mClient.runQueryOnBackgroundThread(constraint);

        FilterResults results = new FilterResults();
        if (cursor != null) {
            results.count = cursor.getCount();
            results.values = cursor;
        } else {
            results.count = 0;
            results.values = null;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        Cursor oldCursor = mClient.getCursor();

        if (results.values != null && results.values != oldCursor) {
            mClient.changeCursor((Cursor) results.values);
        }
    }
}