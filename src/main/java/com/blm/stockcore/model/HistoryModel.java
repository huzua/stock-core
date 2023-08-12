package com.blm.stockcore.model;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
//"day":"2022-06-28 11:30:00","open":"5.540","high":"5.590","low":"5.530","close":"5.590","volume":"9215479"

public class HistoryModel {
    public String monthDay;
    public Date day;
    public Float open;
    public Float high;
    public Float low;
    public Float close;
    public String volume;
    public String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day =  day;
    }

    public String getMonthDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateNowStr = sdf.format(day);
        return dateNowStr;
    }

    public void setMonthDay(Date day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateNowStr = sdf.format(day);
        this.monthDay =  dateNowStr;
    }


    public Float getOpen() {
        return open;
    }

    public void setOpen(Float open) {
        this.open = open;
    }

    public Float getHigh() {
        return high;
    }

    public void setHigh(Float high) {
        this.high = high;
    }

    public Float getLow() {
        return low;
    }

    public void setLow(Float low) {
        this.low = low;
    }

    public Float getClose() {
        return close;
    }

    public void setClose(Float close) {
        this.close = close;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }
}
