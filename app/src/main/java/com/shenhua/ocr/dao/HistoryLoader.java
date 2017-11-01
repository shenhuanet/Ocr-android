package com.shenhua.ocr.dao;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

/**
 * Created by shenhua on 2017-11-01-0001.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class HistoryLoader extends CursorLoader {

    public HistoryLoader(Context context) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        return HistoryDatabase.get(getContext()).getAll();
    }
}
