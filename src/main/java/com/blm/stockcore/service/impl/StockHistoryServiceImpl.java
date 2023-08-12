package com.blm.stockcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blm.stockcore.entity.StockHistory;
import com.blm.stockcore.mapper.StockHistoryMapper;
import com.blm.stockcore.service.StockHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class StockHistoryServiceImpl implements StockHistoryService {
    @Autowired
    private StockHistoryMapper stockHistoryMapper;

    @Override
    public int saveStockHistory(List<StockHistory> list) {
        list.forEach(e->{
            stockHistoryMapper.insert(e);
        });
        return 0;
    }

    @Override
    public List<StockHistory> selectStockHistoryAll() {

        return stockHistoryMapper.selectList(null);
    }

    @Override
    public List<StockHistory> selectStockHistoryByCode(String code) {
        QueryWrapper<StockHistory> queryWrapper = new QueryWrapper();
        queryWrapper.eq("code",code);
        return stockHistoryMapper.selectList(queryWrapper);
    }
}
