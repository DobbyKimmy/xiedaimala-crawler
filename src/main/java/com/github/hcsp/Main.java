package com.github.hcsp;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        // 待处理的线程链接池
        List<String> linkPool = new ArrayList<>();
        // 已经处理的链接池
        Set<String> processedLinks = new HashSet<>();
        linkPool.add("https://sina.cn");

        while (true) {
            // 如果链接池为空 直接跳出循环
            if (linkPool.isEmpty()) {
                break;
            }
            // 1. 从链接池种拿一个链接
            // 然后要删除这个链接，remove方法会返回删除的结果，arrayList从尾部删除更有效率
            String link = linkPool.remove(linkPool.size() - 1);

            // 2. 判断从链接池拿到的链接是否被处理过了
            if (processedLinks.contains(link)) {
                continue;
            }

            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);
                storeIntoDatabaseIfItIsNewsPage(doc);
                // 处理完成的数据要放入到processedLinks里面
                processedLinks.add(link);
            } else {
                // 这是我们不感兴趣的，不处理它
                continue;
            }

        }
    }

    private static void storeIntoDatabaseIfItIsNewsPage(Document doc) {
        // 假如这是一个新闻的详情页面，就存入到数据库，否则就什么都不做
        // 假设所有包含article标签的都是新闻
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                System.out.println(title);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        // 这是我们感兴趣的，我们只处理新浪站内的链接
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // 有一些链接是以 “//” 开头的，需要在这样的链接前加上https:
        if (link.startsWith("//")) {
            link = "https:" + link;
        }

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(link);
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);

            return Jsoup.parse(html);
        }
    }

    // 是我们想要的链接吗
    private static boolean isInterestingLink(String link) {
        return (isNotLoginPage(link) && (isNewsPage(link) || isIndexPage(link)));
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }
}
