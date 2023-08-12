package com.blm.stockcore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("stock_percent_bk")
public class StockPercentBk {
    @TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    @TableField("code")
    private String code;
    @TableField("name")
    private String name;
    @TableField("percent20")
    private float percent20;
    @TableField("percent30")
    private float percent30;
    @TableField("percent40")
    private float percent40;
    @TableField("percent50")
    private float percent50;
    @TableField("percent60")
    private float percent60;
    @TableField("percent70")
    private float percent70;
    @TableField("percent80")
    private float percent80;
    @TableField("percent90")
    private float percent90;
    @TableField("percent100")
    private float percent100;

    public float getPercent30() {
        return percent30;
    }

    public void setPercent30(float percent30) {
        this.percent30 = percent30;
    }

    public float getPercent40() {
        return percent40;
    }

    public void setPercent40(float percent40) {
        this.percent40 = percent40;
    }

    public float getPercent60() {
        return percent60;
    }

    public void setPercent60(float percent60) {
        this.percent60 = percent60;
    }

    public float getPercent70() {
        return percent70;
    }

    public void setPercent70(float percent70) {
        this.percent70 = percent70;
    }

    public float getPercent80() {
        return percent80;
    }

    public void setPercent80(float percent80) {
        this.percent80 = percent80;
    }

    public float getPercent90() {
        return percent90;
    }

    public void setPercent90(float percent90) {
        this.percent90 = percent90;
    }

    public float getPercent100() {
        return percent100;
    }

    public void setPercent100(float percent100) {
        this.percent100 = percent100;
    }

    @TableField("total_count")
    private int totalCount;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public float getPercent50() {
        return percent50;
    }

    public void setPercent50(float percent50) {
        this.percent50 = percent50;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public float getPercent20() {
        return percent20;
    }

    public void setPercent20(float percent20) {
        this.percent20 = percent20;
    }

    @Override
    public String toString() {
        return "StockPercent{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", percent20=" + percent20 +
                ", percent50=" + percent50 +
                ", totalCount=" + totalCount +
                '}';
    }
}
