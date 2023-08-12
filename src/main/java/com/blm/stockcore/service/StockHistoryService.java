package com.blm.stockcore.service;

import com.blm.stockcore.entity.StockHistory;
import com.blm.stockcore.entity.StockInfo;
import com.blm.stockcore.entity.StockPercent;
import com.blm.stockcore.entity.StockPercentBk;
import org.springframework.stereotype.Service;

import java.util.List;


public interface StockHistoryService {
    int saveStockHistory(List<StockHistory> list);

    List<StockHistory> selectStockHistoryAll();

    List<StockHistory> selectStockHistoryByCode(String code);
}
