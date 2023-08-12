package com.blm.stockcore.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blm.stockcore.crawler.Stock;
import com.blm.stockcore.entity.*;
import com.blm.stockcore.mapper.StockMapper;
import com.blm.stockcore.mapper.StockPercentBkMapper;
import com.blm.stockcore.mapper.StockPercentMapper;
import com.blm.stockcore.model.HistoryModel;
import com.blm.stockcore.model.ModelUpPercent;
import com.blm.stockcore.service.StockHistoryService;
import com.blm.stockcore.service.StockPercentService;
import com.blm.stockcore.service.StockService;
import com.blm.stockcore.utils.MathUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private StockPercentMapper stockPercentMapper;

    @Autowired
    private StockPercentBkMapper stockPercentBkMapper;


    @Autowired
    private StockHistoryService stockHistoryService;

    @Autowired
    private StockPercentService stockPercentService;

    @Autowired
    private RestTemplate restTemplate;

    private static final String SRC_URL ="http://app.finance.ifeng.com/list/stock.php?t=hs";

    private final String baseHisUrl = "https://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol={1}&scale=60&datalen=1024";


    private static final String ENCODING = "utf-8";

    @Override
    public int saveStock(List<StockInfo> list) {
        list.forEach(e->{
            stockMapper.insert(e);
        });
        return list.size();
    }

    @Override
    public int craw() {
        List<Stock> stockList;
        List<StockInfo> stockInfos = new ArrayList<>();
        stockList = new ArrayList<Stock>();
        String url = SRC_URL;

        int idx = 0;
        while (true) {
            System.out.println(url);

            String html = getUrlHtml(url, ENCODING);
            Document doc = Jsoup.parse(html, ENCODING);

            // Find core node
            Element divtab01 = doc.getElementsByClass("tab01").last();

            // Find stocks
            Elements trs = divtab01.getElementsByTag("tr");
            for (Element tr : trs) {
                Elements tds = tr.getElementsByTag("td");
                if (tds.size() > 2) {
                    Element codeElm = tds.get(0).getElementsByTag("a").last();
                    Element nameElm = tds.get(1).getElementsByTag("a").last();

                    Stock s = new Stock(idx++, codeElm.text(), nameElm.text());
                    stockList.add(s);
                }
            }

            // Find next page url
            Element lastLink = divtab01.getElementsByTag("a").last();
            if (lastLink.text().equals("下一页")) {
                url = "http://app.finance.ifeng.com/list/stock.php" + lastLink.attr("href");
            } else {
                break;
            }
        }
        for (Stock s : stockList) {
            StockInfo stockInfo = new StockInfo();
            BeanUtils.copyProperties(s,stockInfo);
            stockInfos.add(stockInfo);
        }
        System.out.println("共找到" + idx + "个股票.");
        return saveStock(stockInfos);

    }

    @Override
    public int updateShizhi() {
        List<StockInfo> stockInfosEastMoney = selectStockAllEastMoney();
        stockInfosEastMoney.forEach(stockInfosEast->{
            String Url = "https://push2.eastmoney.com/api/qt/ulist/get?secids={1}&pn=1&np=1";
            ResponseEntity<String> jsonData = restTemplate.exchange(Url, HttpMethod.GET, null, String.class,stockInfosEast.getCode());
            System.out.println(jsonData.getBody());
            if(!jsonData.getStatusCode().is2xxSuccessful()){
                return;
            }
            //将JSON字符串转换成List对象
            StockEastMoney stockEastMoney = JSONObject.parseObject(jsonData.getBody(), StockEastMoney.class);
            CoreData data = stockEastMoney.getData();
            Long shizhi = data.getDiff()[0].getF20();
            stockInfosEast.setShizhi(shizhi);
            stockMapper.updateById(stockInfosEast);

        });
        return stockInfosEastMoney.size();
    }

    @Override
    public List<StockInfo> selectStockAll() {
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<StockInfo>();
        queryWrapper.eq("name","绿康生化");
        List<StockInfo> stockInfos = stockMapper.selectList(queryWrapper);
        stockInfos.forEach(e->{
            setSZSH(e);
        });
        return stockInfos;
    }

    /**
     * 添加市场标识缩写
     * @param e
     */
    private void setSZSH(StockInfo e){
        if(e.getCode().charAt(0)=='0'){
            e.setCode("sz"+e.getCode());
        }
        if(e.getCode().charAt(0)=='6'){
            e.setCode("sh"+e.getCode());
        }
        if(e.getCode().charAt(0)=='3'){
            e.setCode("sz"+e.getCode());
        }
    }

    @Override
    public List<StockInfo> selectStockByWrapper(QueryWrapper<StockInfo> queryWrapper) {
        List<StockInfo> stockInfos = stockMapper.selectList(queryWrapper);
        stockInfos.forEach(e->{
            setSZSH(e);
        });
        return stockInfos;
    }

    @Override
    public List<StockInfo> selectStockAllEastMoney() {
        QueryWrapper<StockInfo> queryWrapper = new QueryWrapper<StockInfo>();
        List<StockInfo> stockInfos = stockMapper.selectList(null);
        stockInfos.forEach(e->{
            if(e.getCode().charAt(0)=='0'){
                e.setCode("0."+e.getCode());
            }
            if(e.getCode().charAt(0)=='6'){
                e.setCode("1."+e.getCode());
            }
            if(e.getCode().charAt(0)=='3'){
                e.setCode("0."+e.getCode());
            }
        });
        return stockInfos;
    }

    @Override
    public List<StockPercent> selectStockPercentAll() {
        QueryWrapper<StockPercent> queryWrapper = new QueryWrapper<StockPercent>();
        queryWrapper.gt("percent20",90);
        queryWrapper.lt("percent20",100);
        queryWrapper.isNull("percent30");
        List<StockPercent> stockInfos = stockPercentMapper.selectList(queryWrapper);

        return stockInfos;
    }

    @Override
    public List<StockPercent> selectStockPercentByWrapper(QueryWrapper<StockPercent> queryWrapper) {
        List<StockPercent> stockInfos = stockPercentMapper.selectList(queryWrapper);
        return stockInfos;
    }

    @Override
    public List<StockPercentBk> selectStockPercentBkAll() {
        QueryWrapper<StockPercentBk> queryWrapper = new QueryWrapper<StockPercentBk>();
        queryWrapper.eq("percent20",100);
        List<StockPercentBk> stockInfos = stockPercentBkMapper.selectList(queryWrapper);

        return stockInfos;
    }

    @Override
    public List<HistoryModel> getHourHisFromSina(StockInfo stockBase) throws Exception {
        String url = baseHisUrl.replace("{}",stockBase.getCode());
        System.out.println(url);
        ResponseEntity<String> jsonData = restTemplate.exchange(baseHisUrl, HttpMethod.GET, null, String.class,stockBase.getCode());
        System.out.println(jsonData.getStatusCode());
        if(!jsonData.getStatusCode().is2xxSuccessful()){
            throw new Exception(jsonData.getStatusCode().getReasonPhrase());
        }
        //将JSON字符串转换成List对象
        List<HistoryModel> historyModels = JSONObject.parseArray(jsonData.getBody(), HistoryModel.class);
        return historyModels;
    }

    @Override
    public int updateTargetPercent() {
        AtomicInteger updateCount = new AtomicInteger();
        //从备份的数据库获取
        //数据量大，批量操作
        List<StockInfo> stockInfos = this.selectStockAll();
        stockInfos.forEach(s->{
            List<StockHistory> stockHis = stockHistoryService.selectStockHistoryByCode(s.getCode());
            List<HistoryModel> modelsHours = new ArrayList<>();
            stockHis.forEach(stockHisDO->{
                HistoryModel historyModel = new HistoryModel();
                BeanUtils.copyProperties(stockHisDO,historyModel);
                modelsHours.add(historyModel);
            });
            List<HistoryModel> modelsDay = processModel(modelsHours);
            //计算标准分型
            Map<HistoryModel, HistoryModel> historyModelHistoryModelMap = tagCalculate(modelsDay);
            List<ModelUpPercent> modelUpPercents = calculatePercent(historyModelHistoryModelMap);
            modelUpPercents.forEach(e->{
                e.setCode(s.getCode());
                e.setName(s.getName());
            });
            //科创版新浪接口返回数据就一条，这种情况没有底分型数据
            if(!CollectionUtils.isEmpty(modelUpPercents)){
                List<StockPercent> stockPercentList = calculateStockPercent(modelUpPercents);
                updateCount.set(stockPercentService.updateStockPercent(stockPercentList));
            }
        });
        return updateCount.get();
    }


    /**
     * 生成日K数据，包括振幅
     * @param historyModels
     * @return
     */
    private List<HistoryModel> processModel(List<HistoryModel> historyModels){
        //按天归类
        Map<String, List<HistoryModel>> stringListMap = processDay(historyModels);
        //计算当天的最高价和最低价
        List<HistoryModel> models = calculateLength(stringListMap);
        return models;
    }

    /**
     * 将数据按日期分组
     * @param historyModels
     */
    private Map<String,List<HistoryModel>> processDay(List<HistoryModel> historyModels){
        Map<String,List<HistoryModel>> dayModelList = new HashMap<>();
        historyModels.forEach(historyModel -> {
            putDayMapList(dayModelList,historyModel);
        });
        return dayModelList ;

    }

    /**
     * 相同的日期放到同一个map
     * @param dayModelList
     * @param historyModel
     */
    private void putDayMapList(Map<String,List<HistoryModel>> dayModelList,HistoryModel historyModel){
        if(CollectionUtils.isEmpty(dayModelList.get(historyModel.getMonthDay()))){
            List<HistoryModel> models= new ArrayList<>();
            models.add(historyModel);
            dayModelList.put(historyModel.getMonthDay(),models);
        }else{
            dayModelList.get(historyModel.getMonthDay()).add(historyModel);
        }
    }

    /**
     * 获取每根日K当天的震幅
     * @param stringListMap
     * @return
     */
    private List<HistoryModel> calculateLength(Map<String, List<HistoryModel>> stringListMap){
        List<HistoryModel> afterProcessDayModels = new ArrayList<>();
        stringListMap.keySet().forEach(dayModel->{
            List<HistoryModel> models = stringListMap.get(dayModel);
            HistoryModel highModel = models.stream().max(Comparator.comparing(HistoryModel::getHigh)).get();
            HistoryModel lowModel = models.stream().min(Comparator.comparing(HistoryModel::getLow)).get();
            HistoryModel open = models.stream().min(Comparator.comparing(HistoryModel::getDay)).get();
            HistoryModel close = models.stream().max(Comparator.comparing(HistoryModel::getDay)).get();
            HistoryModel dayHistoryModel = new HistoryModel();
            dayHistoryModel.setDay(open.getDay());
            dayHistoryModel.setOpen(open.getOpen());
            dayHistoryModel.setClose(close.getClose());
            dayHistoryModel.setHigh(highModel.getHigh());
            dayHistoryModel.setLow(lowModel.getLow());
            afterProcessDayModels.add(dayHistoryModel);
        });
        List<HistoryModel> afterProcessDaySortModels = afterProcessDayModels.stream()
                .sorted(Comparator.comparing(HistoryModel::getDay))
                .collect(Collectors.toList());
        return afterProcessDaySortModels;
    }

    /**
     * 获取底分型k线组合
     * @param models
     * @return
     */
    private Map<HistoryModel,HistoryModel> tagCalculate(List<HistoryModel> models){
        Map<HistoryModel,HistoryModel> sure = new HashMap<>();
        int first = 0;
        int twice = 1;
        int third = 2;
        HistoryModel firstModel = new HistoryModel();
        HistoryModel twiceModel = new HistoryModel();
        HistoryModel thirdModel = new HistoryModel();
        while (third<models.size()-1){
            firstModel = models.get(first);
            twiceModel = models.get(twice);
            //底分型中间k线判断
            if(twiceModel.getLow()<firstModel.getLow() && twiceModel.getHigh()<firstModel.getHigh()){
                thirdModel = models.get(third);
                if(thirdModel.getLow()>twiceModel.getLow() && thirdModel.getHigh()>twiceModel.getHigh()){
                    //底分型确定
                    first = first+2;
                    twice = twice+2;
                    third = third+2;
                    firstModel = models.get(first);
                    twiceModel = models.get(twice);
                    sure.put(firstModel,twiceModel);
                    continue;
                }
            }
            first = first+1;
            twice = twice+1;
            third = third+1;
        }
        return sure;
    }

    /**
     * 获取底分型参数，eg.收盘价和第二天开盘价
     * @param sure
     * @return
     */
    private List<ModelUpPercent> calculatePercent(Map<HistoryModel,HistoryModel> sure){
        List<ModelUpPercent> modelUpPercents = new ArrayList<>();

        sure.keySet().forEach(e->{
            ModelUpPercent modelUpPercent = new ModelUpPercent();
            HistoryModel afterModel = sure.get(e);
            modelUpPercent.setBeforeClose(e.getClose());
            modelUpPercent.setAfterHigh(afterModel.getHigh());
            modelUpPercent.setDay(afterModel.getMonthDay());
            modelUpPercents.add(modelUpPercent);
        });
        return modelUpPercents;
    }

    private List<StockPercent> calculateStockPercent(List<ModelUpPercent> modelUpPercents){
        List<StockPercent> stockPercentList = new ArrayList<>();
        //后面可优化分档，档次参数调节
        ModelUpPercent upPercentInfo = modelUpPercents.get(0);
        int count20 = 0;
        int count30 = 0;
        int count40 = 0;
        int count50 = 0;
        int count60 = 0;
        int count70 = 0;
        int count80 = 0;
        int count90 = 0;
        int count100 = 0;

        for(ModelUpPercent upPercent : modelUpPercents){
            if(upPercent.getPercent()>0.2){
                count20++;
            }
        }
        for(ModelUpPercent upPercent : modelUpPercents){
            if(upPercent.getPercent()>0.3){
                count30++;
            }
        }
        for(ModelUpPercent upPercent : modelUpPercents){
            if(upPercent.getPercent()>0.4){
                count40++;
            }
        }
        for(ModelUpPercent upPercent : modelUpPercents){
            if(upPercent.getPercent()>0.5){
                count50++;
            }
        }
        for(ModelUpPercent upPercent : modelUpPercents){
            if(upPercent.getPercent()>0.6){
                count60++;
            }
        }
        for(ModelUpPercent upPercent : modelUpPercents){
            if(upPercent.getPercent()>0.7){
                count70++;
            }
        }
        for(ModelUpPercent upPercent : modelUpPercents){
            if(upPercent.getPercent()>0.8){
                count80++;
            }
        }

        for(ModelUpPercent upPercent : modelUpPercents){
            if(upPercent.getPercent()>0.9){
                count90++;
            }
        }
        for(ModelUpPercent upPercent : modelUpPercents){
            if(upPercent.getPercent()>1.0){
                count100++;
            }
        }


        String divide20 = MathUtils.divide(new BigDecimal(count20), new BigDecimal(modelUpPercents.size()));
        String divide30 = MathUtils.divide(new BigDecimal(count30), new BigDecimal(modelUpPercents.size()));
        String divide40 = MathUtils.divide(new BigDecimal(count40), new BigDecimal(modelUpPercents.size()));
        String divide50 = MathUtils.divide(new BigDecimal(count50), new BigDecimal(modelUpPercents.size()));
        String divide60 = MathUtils.divide(new BigDecimal(count60), new BigDecimal(modelUpPercents.size()));
        String divide70 = MathUtils.divide(new BigDecimal(count70), new BigDecimal(modelUpPercents.size()));
        String divide80 = MathUtils.divide(new BigDecimal(count80), new BigDecimal(modelUpPercents.size()));
        String divide90 = MathUtils.divide(new BigDecimal(count90), new BigDecimal(modelUpPercents.size()));
        String divide100 = MathUtils.divide(new BigDecimal(count100), new BigDecimal(modelUpPercents.size()));

        StockPercent stockPercent = new StockPercent();
        stockPercent.setCode(upPercentInfo.getCode());
        stockPercent.setPercent20(Float.parseFloat(divide20));
        stockPercent.setPercent30(Float.parseFloat(divide30));
        stockPercent.setPercent40(Float.parseFloat(divide40));
        stockPercent.setPercent50(Float.parseFloat(divide50));
        stockPercent.setPercent60(Float.parseFloat(divide60));
        stockPercent.setPercent70(Float.parseFloat(divide70));
        stockPercent.setPercent80(Float.parseFloat(divide80));
        stockPercent.setPercent90(Float.parseFloat(divide90));
        stockPercent.setPercent100(Float.parseFloat(divide100));
        stockPercent.setName(upPercentInfo.getName());
        stockPercent.setTotalCount(modelUpPercents.size());
        stockPercentList.add(stockPercent);
        return stockPercentList;
    }

    private String getUrlHtml(String url, String encoding) {
        StringBuffer sb = new StringBuffer();
        URL urlObj = null;
        URLConnection openConnection = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            urlObj = new URL(url);
            openConnection = urlObj.openConnection();
            isr = new InputStreamReader(openConnection.getInputStream(), encoding);
            br = new BufferedReader(isr);
            String temp = null;
            while ((temp = br.readLine()) != null) {
                sb.append(temp + "\n");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


}
