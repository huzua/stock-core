package com.blm.stockcore.entity;


public class StockInfo {
    private Integer id;
    private String code;
    private String name;
    private Long shizhi;

    public Long getShizhi() {
        return shizhi;
    }

    public void setShizhi(Long shizhi) {
        this.shizhi = shizhi;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public StockInfo(Integer id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public StockInfo() {
    }
}
