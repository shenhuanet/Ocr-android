package com.shenhua.ocr.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shenhua on 2017-10-26-0026.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class HistoryDatabase extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "History";
    private static final String TABLE_NAME = "items";
    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "_date";
    public static final String KEY_RESULT = "_result";
    public static final String KEY_TIME = "_time";
    public static final String KEY_IMG = "_image";
    public static final String KEY_UPDATE = "_update";

    private HistoryDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private static HistoryDatabase sInstance = null;

    public static HistoryDatabase get(Context context) {
        if (sInstance == null) {
            synchronized (HistoryDatabase.class) {
                if (sInstance == null) {
                    sInstance = new HistoryDatabase(context);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + KEY_ID + " integer primary key autoincrement,"
                + KEY_DATE + " integer,"
                + KEY_RESULT + " text,"
                + KEY_TIME + " text,"
                + KEY_IMG + " text,"
                + KEY_UPDATE + " integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 批量插入本地数据库
     *
     * @param list ResultBean 列表
     */
    public void addAll(List<History> list) {
        for (History item : list) {
            add(item);
        }
    }

    /**
     * 添加到本地数据库
     *
     * @param item History
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long add(History item) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_DATE, item.getDate());
        cv.put(KEY_RESULT, item.getResult());
        cv.put(KEY_TIME, item.getTime());
        cv.put(KEY_IMG, item.getImg());
        cv.put(KEY_UPDATE, item.getDate());
        return getWritableDatabase().insert(TABLE_NAME, KEY_RESULT, cv);
    }

    /**
     * 根据记录id来修改结果
     *
     * @param id     _id
     * @param result 新值
     */
    public void update(long id, String result) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_RESULT, result);
        cv.put(KEY_UPDATE, System.currentTimeMillis());
        String where = KEY_ID + "=?";
        String[] whereValues = {Long.toString(id)};
        getWritableDatabase().update(TABLE_NAME, cv, where, whereValues);
    }

    /**
     * 根据id来移除记录
     *
     * @param id _id
     */
    public int remove(long id) {
        String where = KEY_ID + "=?";
        String[] whereValues = {String.valueOf(id)};
        return getWritableDatabase().delete(TABLE_NAME, where, whereValues);
    }

    /**
     * 清空表中的数据
     */
    public void clean() {
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(this.getWritableDatabase());
        getWritableDatabase().close();
    }

    /**
     * 清空所有记录,使用异常捕获消除
     * {@link android.database.sqlite.SQLiteDatabaseLockedException}
     */
    public void deleteAll() {
        try {
            getWritableDatabase().execSQL("DELETE FROM " + TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库全部记录
     *
     * @return Cursor
     */
    public Cursor getAll() {
        // return getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
        return getReadableDatabase().rawQuery("select * from " + TABLE_NAME + " order by " + KEY_UPDATE + " desc", null);
    }

    /**
     * 根据ID查询一条数据
     *
     * @param id id
     * @return History
     */
    public History find(long id) {
        String sql = "select * from " + TABLE_NAME + " where " + KEY_ID + " = " + id;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        History history = null;
        while (cursor.moveToNext()) {
            String result = cursor.getString(cursor.getColumnIndex(KEY_RESULT));
            long date = cursor.getLong(cursor.getColumnIndex(KEY_UPDATE));
            String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
            String path = cursor.getString(cursor.getColumnIndex(KEY_IMG));
            history = new History(id, date, result, time, path);
        }
        cursor.close();
        return history;
    }

    /**
     * 模糊搜索
     *
     * @param key the key
     * @return History
     */
    public ArrayList<History> find(String key) {
        String sql = "select * from " + TABLE_NAME + " where " + KEY_RESULT
                + " like '%" + key + "%' order by " + KEY_UPDATE + " desc";
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        ArrayList<History> histories = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            String result = cursor.getString(cursor.getColumnIndex(KEY_RESULT));
            long date = cursor.getLong(cursor.getColumnIndex(KEY_UPDATE));
            String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
            String path = cursor.getString(cursor.getColumnIndex(KEY_IMG));
            History history = new History(id, date, result, time, path);
            histories.add(history);
        }
        cursor.close();
        return histories;
    }

}
