package com.blm.stockcore.model;

import java.util.List;

public class HistoryModels {
    public String name;
    public String code;
    public String now;
    public List<HistoryModel> list;

    public HistoryModels(String name, String code, String now, List<HistoryModel> list) {
        this.name = name;
        this.code = code;
        this.now = now;
        this.list = list;
    }
}
