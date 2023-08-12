package com.blm.stockcore.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blm.stockcore.common.CommonAttributes;
import com.blm.stockcore.entity.StockHistory;
import com.blm.stockcore.entity.StockInfo;
import com.blm.stockcore.entity.StockPercent;
import com.blm.stockcore.entity.StockPercentBk;
import com.blm.stockcore.model.HistoryModel;
import com.blm.stockcore.model.ModelUpPercent;
import com.blm.stockcore.service.StockHistoryService;
import com.blm.stockcore.service.StockPercentService;
import com.blm.stockcore.service.StockService;
import com.blm.stockcore.utils.MathUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

//https://blog.csdn.net/withkai44/article/details/131345208   csdn地址
@RestController
public class StockController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockHistoryService stockHistoryService;

    @Autowired
    private StockPercentService stockPercentService;

    private final String baseHisUrl = "https://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol={1}&scale=60&datalen=1024";

    /**
     * 更新底分型胜率
     * @return
     */
    @RequestMapping("/updateTargetPercent")
    @ResponseBody
    public String updateTargetPercent(){
        stockService.updateTargetPercent();
        return "success";
    }


    private Float targetProcess(List<HistoryModel> modelsHours){
            List<HistoryModel> modelsDay = processModel(modelsHours);
            //获取底分型确定的当天k线列表
            List<HistoryModel>  historyTimeKLineList = tagCalculateTimeKLine(modelsDay);
            Float timeKLineUpPercent = calculateTimeKLine(historyTimeKLineList,modelsHours);
            System.out.println(timeKLineUpPercent);
            return timeKLineUpPercent;


    }

    private Float calculateTimeKLine(List<HistoryModel> historyTimeKLineList,List<HistoryModel> modelsHours) {
        AtomicInteger countUp = new AtomicInteger();
        for (HistoryModel historyModel : historyTimeKLineList) {
            String day = historyModel.getMonthDay();
            modelsHours.forEach(modelsHour->{
                if(modelsHour.getMonthDay().equals(day) && modelsHour.getDay().getHours()==15){
                    if(modelsHour.getOpen()<modelsHour.getClose()){
                        countUp.getAndIncrement();
                    }
                }
            });
        }
        String countUpPercent = MathUtils.divide(new BigDecimal(String.valueOf(countUp)), new BigDecimal(historyTimeKLineList.size()));
        return Float.parseFloat(countUpPercent);
    }


    private List<HistoryModel> processModel(List<HistoryModel> historyModels){
        //按天归类
        Map<String, List<HistoryModel>> stringListMap = processDay(historyModels);
        //计算当天的最高价和最低价
        List<HistoryModel> models = calculateLength(stringListMap);
        return models;

    }

    /**
     * 按天归类
     * @param historyModels
     */
    private Map<String,List<HistoryModel>> processDay(List<HistoryModel> historyModels){
        Map<String,List<HistoryModel>> dayModelList = new HashMap<>();
        historyModels.forEach(historyModel -> {
            putDayMapList(dayModelList,historyModel);
        });
        return dayModelList ;

    }

    //按天存放map
    private void putDayMapList(Map<String,List<HistoryModel>> dayModelList,HistoryModel historyModel){
        if(CollectionUtils.isEmpty(dayModelList.get(historyModel.getMonthDay()))){
            List<HistoryModel> models= new ArrayList<>();
            models.add(historyModel);
            dayModelList.put(historyModel.getMonthDay(),models);
        }else{
            dayModelList.get(historyModel.getMonthDay()).add(historyModel);
        }
    }

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

    //标准底分型计算
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


    //底分型当天最后一根分时k线列表
    private List<HistoryModel>  tagCalculateTimeKLine(List<HistoryModel> models){
        //底分型当天最后一根分时k线列表
        List<HistoryModel> timeKLineList = new ArrayList<>();
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
                    timeKLineList.add(firstModel);
                    continue;
                }
            }
            first = first+1;
            twice = twice+1;
            third = third+1;
        }
        return timeKLineList;
    }

    public static void main(String[] args) {
        Float f1 = new Float("17.44");
        Float f2 = new Float("17.4");
        if(f1<f2){
            System.out.printf("222");
        }
        System.out.printf("211122");
    }
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

    @RequestMapping("/crawler")
    @ResponseBody
    public String crawler() {
        stockService.craw();
        return "success";
    }


    @RequestMapping("/updateshizhi")
    @ResponseBody
    public String updateshizhi() {
        stockService.updateShizhi();
        return "success";
    }

    @RequestMapping("/dealDup")
    @ResponseBody
    public int dealDup(){
        List<StockPercentBk> stockInfos = stockService.selectStockPercentBkAll();
        List<StockPercent> stockNewInfos = new ArrayList<>();
        Map<String,StockPercentBk> map = new HashMap<>();
        stockInfos.forEach(e->{
            map.put(e.getCode(),e);
        });
        map.keySet().forEach(e->{
            StockPercentBk stockPercentBk = map.get(e);
            StockPercent stockPercent = new StockPercent();
            BeanUtils.copyProperties(stockPercentBk,stockPercent);
            stockNewInfos.add(stockPercent);
        });
        return stockPercentService.saveStockPercent(stockNewInfos);
    }


    @RequestMapping("/saveStock")
    @ResponseBody
    public String saveStockHistoryFromSina(){
        AtomicReference<String> error = new AtomicReference<>("");
        List<StockInfo> stockInfos = stockService.selectStockAll();

        stockInfos.forEach(stockBase->{
            String name = stockBase.getName();
            String url = baseHisUrl.replace("{}",stockBase.getCode());
            System.out.println(url);
            ResponseEntity<String> jsonData = restTemplate.exchange(baseHisUrl, HttpMethod.GET, null, String.class,stockBase.getCode());
            System.out.println(jsonData.getStatusCode());
            if(!jsonData.getStatusCode().is2xxSuccessful()){
                error.set(jsonData.getStatusCode().getReasonPhrase());
                return ;
            }
            //将JSON字符串转换成List对象
            List<HistoryModel> historyModels = JSONObject.parseArray(jsonData.getBody(), HistoryModel.class);
            List<StockHistory> historyStocks = new ArrayList<>();
            historyModels.forEach(e->{
                StockHistory stockHistory = new StockHistory();
                BeanUtils.copyProperties(e,stockHistory);
                stockHistory.setCreateTime(new Date());
                stockHistory.setCode(stockBase.getCode());
                stockHistory.setName(name);
                historyStocks.add(stockHistory);
            });
            stockHistoryService.saveStockHistory(historyStocks);


            try {
                Thread.sleep(1000+(int)(Math.random()*1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println("finish");
//        List<ModelUpPercent> afterProcessDaySortModels = modelUpPercents.stream()
//                .sorted(Comparator.comparing(ModelUpPercent::getDay))
//                .collect(Collectors.toList());
//        System.out.println(modelUpPercents.size());
        if(StringUtil.isBlank(error.get())){
            return "success :" + stockInfos.size();
        }
        return error.get();

    }


    /**
     * 计算标的股票购买时段的胜率
     * @return
     */
    @RequestMapping("/stockTimeUp")
    @ResponseBody
    public String stockTimeUp(){
        List<StockPercent> percentList = new ArrayList<>();
        //从数据库取出标的股票
        //计算每只股票两点半的价格和三点价格上涨的概率
        //保存每只股票的上涨率
        AtomicReference<String> error = new AtomicReference<>("");
        QueryWrapper<StockPercent> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt("percent60",90);
        List<StockPercent> stockInfos = stockService.selectStockPercentByWrapper(queryWrapper);

        stockInfos.forEach(stockBase->{
            String name = stockBase.getName();
            String url = CommonAttributes.base30HisUrl.replace("{}",stockBase.getCode());
//            System.out.println(url);
            ResponseEntity<String> jsonData = restTemplate.exchange(baseHisUrl, HttpMethod.GET, null, String.class,stockBase.getCode());
//            System.out.println(jsonData.getStatusCode());
            if(!jsonData.getStatusCode().is2xxSuccessful()){
                error.set(jsonData.getStatusCode().getReasonPhrase());
                return ;
            }
            //将JSON字符串转换成List对象
            List<HistoryModel> historyModels = JSONObject.parseArray(jsonData.getBody(), HistoryModel.class);
            historyModels.forEach(e->{
//                e.setCreateTime(new Date());
                e.setCode(stockBase.getCode());
//                e.setName(name);
//
            });
            StockPercent stockPercent = new StockPercent();
            stockPercent.setCode(stockBase.getCode());
            stockPercent.setPercent50(targetProcess(historyModels));
            percentList.add(stockPercent);
            try {
                Thread.sleep(100+(int)(Math.random()*1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        AtomicInteger count50 = new AtomicInteger();
        percentList.forEach(e->{
            if(e.getPercent50()>50f){
                count50.getAndIncrement();
                System.out.println(e.getCode()+":      "+e.getPercent50());
            }
        });
        System.out.println(count50.toString());
        System.out.println("finish");

        if(StringUtil.isBlank(error.get())){
            return "success :" + stockInfos.size();
        }
        return error.get();

    }

}
