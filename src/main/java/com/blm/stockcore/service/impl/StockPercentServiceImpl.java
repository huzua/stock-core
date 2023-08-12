package com.blm.stockcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blm.stockcore.crawler.Stock;
import com.blm.stockcore.entity.StockInfo;
import com.blm.stockcore.entity.StockPercent;
import com.blm.stockcore.mapper.StockMapper;
import com.blm.stockcore.mapper.StockPercentMapper;
import com.blm.stockcore.service.StockPercentService;
import com.blm.stockcore.service.StockService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockPercentServiceImpl implements StockPercentService {
    @Autowired
    private StockPercentMapper stockPercentMapper;

    @Override
    public int updateStockPercent(List<StockPercent> list) {
        list.forEach(e->{
//            Map<String,Object> map = new HashMap<>();
//            map.put("code",e.getCode());
//            List<StockPercent> stockPercentList = stockPercentMapper.selectByMap(map);
//            if(!CollectionUtils.isEmpty(stockPercentList)){
//                StockPercent stockPercent = stockPercentList.get(0);
//                stockPercent.setPercent20(e.getPercent20());
//                stockPercent.setPercent50(e.getPercent50());
//                stockPercent.setTotalCount(e.getTotalCount());
//                stockPercentMapper.updateById(stockPercent);
//                System.out.println("update from "+stockPercent.toString()+"  --- to "+e.toString());
//            }
            QueryWrapper<StockPercent> queryWrapper = new QueryWrapper<StockPercent>();
            queryWrapper.eq("code",e.getCode());
            stockPercentMapper.update(e,queryWrapper);
//            stockPercentMapper.insert(e);
        });
        return list.size();

    }

    @Override
    public int saveStockPercent(List<StockPercent> list) {
        list.forEach(e->{
            stockPercentMapper.insert(e);
        });
        return list.size();

    }
}
