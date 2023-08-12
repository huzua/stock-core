package com.blm.stockcore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blm.stockcore.entity.StockHistory;
import com.blm.stockcore.entity.StockInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockHistoryMapper extends BaseMapper<StockHistory> {
}
