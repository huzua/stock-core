package com.blm.stockcore.service;

import com.blm.stockcore.entity.StockInfo;
import com.blm.stockcore.entity.StockPercent;

import java.util.List;

public interface StockPercentService {
    int saveStockPercent(List<StockPercent> list);
    int updateStockPercent(List<StockPercent> list);
}
