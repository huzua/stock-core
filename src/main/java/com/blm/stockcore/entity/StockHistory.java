package com.blm.stockcore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.text.SimpleDateFormat;
import java.util.Date;
//"day":"2022-06-28 11:30:00","open":"5.540","high":"5.590","low":"5.530","close":"5.590","volume":"9215479"
@TableName("stock_history")
public class StockHistory {
    @TableId(value = "id")
    public Long id;
    @TableField("code")
    public String code;
    @TableField("name")
    public String name;
    @TableField("day")
    public Date day;
    @TableField("open")
    public Float open;
    @TableField("high")
    public Float high;
    @TableField("low")
    public Float low;
    @TableField("close")
    public Float close;
    @TableField("volume")
    public String volume;
    @TableField("create_time")
    public Date createTime;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day =  day;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
