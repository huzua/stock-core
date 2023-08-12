package com.blm.stockcore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blm.stockcore.entity.StockInfo;
import com.blm.stockcore.entity.StockPercent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockPercentMapper extends BaseMapper<StockPercent> {
}
