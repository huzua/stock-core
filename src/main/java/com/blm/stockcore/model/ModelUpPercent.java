package com.blm.stockcore.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ModelUpPercent {
    private String code;
    private String name;
    private String day;
    private float beforeClose;
    private float afterHigh;
    private float percent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public float getPercent() {
        BigDecimal differ = new BigDecimal(afterHigh-beforeClose);
        BigDecimal divideBeforeClose = new BigDecimal(beforeClose);
        float percent = differ.divide(divideBeforeClose, 4,RoundingMode.HALF_DOWN).floatValue();
        return percent*100;

    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public float getBeforeClose() {
        return beforeClose;
    }

    public void setBeforeClose(float beforeClose) {
        this.beforeClose = beforeClose;
    }

    public float getAfterHigh() {
        return afterHigh;
    }

    public void setAfterHigh(float afterHigh) {
        this.afterHigh = afterHigh;
    }

}
