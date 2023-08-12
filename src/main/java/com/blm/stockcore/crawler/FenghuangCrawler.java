package com.blm.stockcore.crawler;

import com.blm.stockcore.entity.StockInfo;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FenghuangCrawler {
    private static final String SRC_URL ="http://app.finance.ifeng.com/list/stock.php?t=hs";
    private static final String ENCODING = "utf-8";

    // Used to save stock code names
    private List<Stock> stockList;
    private List<StockInfo> stockInfos = new ArrayList<>();

    public FenghuangCrawler() {
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

    public List<Stock> getStockList() {
        return stockList;
    }

    public List<StockInfo> execute() {
        // 根据需要设置代理
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");

        new FenghuangCrawler();
        return stockInfos;
    }
}
