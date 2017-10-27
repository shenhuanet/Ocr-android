package com.shenhua.ocr.dao;

import java.io.Serializable;

/**
 * Created by shenhua on 2017-10-26-0026.
 *
 * @author shenhua
 *         Email shenhuanet@126.com
 */
public class History implements Serializable {

    private static final long serialVersionUID = 1519144998296512753L;
    private long id;
    private long date;
    private String result;
    private String time;
    private String img;

    public History() {
    }

    public History(long id, long date, String result, String time, String img) {
        this.id = id;
        this.date = date;
        this.result = result;
        this.time = time;
        this.img = img;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
