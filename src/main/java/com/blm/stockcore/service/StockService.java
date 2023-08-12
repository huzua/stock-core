package com.blm.stockcore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blm.stockcore.crawler.Stock;
import com.blm.stockcore.entity.StockInfo;
import com.blm.stockcore.entity.StockPercent;
import com.blm.stockcore.entity.StockPercentBk;
import com.blm.stockcore.model.HistoryModel;

import java.util.List;

public interface StockService {
    int saveStock(List<StockInfo> list);
    int craw();
    int updateShizhi();
    List<StockInfo> selectStockAll();
    List<StockInfo> selectStockByWrapper(QueryWrapper<StockInfo> queryWrapper);

    List<StockInfo> selectStockAllEastMoney();
    List<StockPercent> selectStockPercentAll();
    List<StockPercent> selectStockPercentByWrapper(QueryWrapper<StockPercent> queryWrapper);

    List<StockPercentBk> selectStockPercentBkAll();

    List<HistoryModel> getHourHisFromSina(StockInfo stockBase) throws Exception;

    int updateTargetPercent();
}
