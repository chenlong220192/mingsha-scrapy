package site.mingsha.scrapy.test.example;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.common.collect.Lists;

import site.mingsha.scrapy.test.example.dal.mapper.GoodsMapper;
import site.mingsha.scrapy.test.example.dal.mapper.MediaMapper;
import site.mingsha.scrapy.test.example.dal.model.GoodsDO;
import site.mingsha.scrapy.test.example.dal.model.MediaDO;
import site.mingsha.scrapy.test.example.model.GoodsVO;
import site.mingsha.scrapy.test.example.utils.DownloadUtils;
import site.mingsha.scrapy.test.example.utils.MingshaUtil;
import site.mingsha.scrapy.test.example.utils.PoiUtils;
import site.mingsha.scrapy.test.example.utils.UserAgentUtils;

/**
 * 利用selenium实现数据爬取
 */
@SpringBootTest
public class GucciTest {

    private static final Logger logger           = LoggerFactory.getLogger(GucciTest.class);

    /**
     *
     */
    private final static String URL_INDEX        = "https://www.gucci.cn/zh/ca/men/accessories/belts?keyword=%E7%9A%AE%E5%B8%A6&search-cat=standard&searchCat=standard&seoSearchKeywords=%E7%9A%AE%E5%B8%A6&pn=1&searchType=click";
    private final static String GOODS_TMP_PATH   = "/data/tmp/mingsha/scrapy/GUCCI";
    private final static String GOODS_EXCEL_PATH = "/data/tmp/mingsha/scrapy/goods_gucci.xlsx";

    /* ----------------------------------------------------------------------- */
    @Resource
    private GoodsMapper         goodsMapper;
    /* ----------------------------------------------------------------------- */

    /**
     *
     */
    @Test
    public void testLVCollector() {
        final String serial = "GUCCI_1";
        final String keyword = "皮带";
        WebDriver driver = null;
        try {
            {
                //
                final ChromeOptions chromeOptions = new ChromeOptions();
                // 在Linux系统下需要添加该选项
                // chromeOptions.addArguments("--disable-dev-shm-usage");
                // 设置随机 User-Agent
                chromeOptions.addArguments("user-agent=" + UserAgentUtils.getRandomUserAgent());
                // 禁用WebDriver特征
                chromeOptions.addArguments("--disable-blink-features");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                //
                System.getProperties().setProperty("webdriver.chrome.driver", "/data/chromedriver/chromedriver");
                driver = new ChromeDriver(chromeOptions);
            }
            driver.get(URL_INDEX);
            // ---------- 休眠，模拟人工操作。 ----------
            MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(1_000, 2_000));
            // 页面滚动到底
            windowScroll(driver);

            List<WebElement> goodsUrlList = driver.findElements(By.xpath("//div[contains(@class, 'product-cont')]/a"));

            //
            if (goodsUrlList.isEmpty()) {
                return;
            }

            List<GoodsVO> targetGoodsList = Lists.newArrayList();
            for (int i = 0; i < goodsUrlList.size(); i++) {
                WebElement goodsUrl = goodsUrlList.get(i);
                targetGoodsList.add(
                        GoodsVO.newInstance()
                                .setSerial(serial)
                                .setSource("GUCCI")
                                .setKeyword(keyword)
                                .setGoodsUrl(String.format("%s", goodsUrl.getAttribute("href")))
                );
            }

            save(targetGoodsList);
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "GUCCI", e.getMessage()));
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     *
     */
    @Test
    public void testGucciAnalyzer() {
        final String serial = "GUCCI_1";
        List<GoodsDO> goodsDOList = goodsMapper.findBySerial(serial);
        if (CollectionUtils.isEmpty(goodsDOList)) {
            return;
        }
        WebDriver driver = null;
        try {
            {
                //
                final ChromeOptions chromeOptions = new ChromeOptions();
                // 在Linux系统下需要添加该选项
                // chromeOptions.addArguments("--disable-dev-shm-usage");
                // 设置随机 User-Agent
                chromeOptions.addArguments("user-agent=" + UserAgentUtils.getRandomUserAgent());
                // 禁用WebDriver特征
                chromeOptions.addArguments("--disable-blink-features");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                //
                System.getProperties().setProperty("webdriver.chrome.driver", "/data/chromedriver/chromedriver");
                driver = new ChromeDriver(chromeOptions);
            }
            for (int i = 0; i < goodsDOList.size(); i++) {
                GoodsDO goodsDO = goodsDOList.get(i);
                try {
                    driver.get(goodsDO.getUrl());
                    // ---------- 休眠，模拟人工操作。 ----------
                    MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(2_000, 5_000));

                    {
                        List<WebElement> elements = driver.findElements(By.xpath("//span[.//text()='商品详情']"));
                        if (elements.isEmpty()) {
                            break;
                        }
                        // 使用 Actions 类模拟鼠标点击操作
                        Actions actions = new Actions(driver);
                        actions.click(elements.get(0)).perform();
                    }

                    {
                        WebElement goodsName = driver.findElement(By.xpath("//h1[contains(@class, 'spice-product-name')]"));
                        WebElement goodsPrice = driver.findElement(By.xpath("//span[contains(@class, 'goods-price')]"));
                        WebElement goodsDesc = driver.findElement(By.xpath("//span[contains(@class, 'des')]"));
                        WebElement goodsNo = driver.findElement(By.xpath("//span[contains(@class, 'spucode')]"));
                        WebElement goodsDesc2 = driver.findElement(By.xpath("//div[contains(@class, 'detail-list')]"));

                        goodsDO.setName(goodsName.getText());
                        goodsDO.setPrice(goodsPrice.getText());
                        goodsDO.setSaleDesc(goodsDesc.getText());
                        goodsDO.setEvaluateDesc(goodsDesc2.getText());
                        goodsDO.setNumber(goodsNo.getText());
                        if (StringUtils.isNotBlank(goodsName.getText())
                                || StringUtils.isNotBlank(goodsPrice.getText())
                                || StringUtils.isNotBlank(goodsDesc.getText())
                                || StringUtils.isNotBlank(goodsDesc2.getText())
                                || StringUtils.isNotBlank(goodsNo.getText())) {
                            goodsMapper.update(goodsDO);
                        }
                    }
                } catch (Exception e) {
                    // echo
                    logger.error(String.format("商品链接：%s", goodsDO.getUrl()));
                    logger.debug(String.format("Moudle:[%s]%n%s", "LV_FOR", e.getMessage()));
                }
            }
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "LV", e.getMessage()));
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Test
    public void testGucciGoods4Poi() {
        final String serial = "GUCCI_1";
        List<GoodsDO> goodsDOList = goodsMapper.findBySerial(serial);
        if (CollectionUtils.isEmpty(goodsDOList)) {
            return;
        }
        //
        List<List<String>> data = Lists.newArrayList();
        for (int i = 0; i < goodsDOList.size(); i++) {
            GoodsDO goodsDO = goodsDOList.get(i);
            data.add(
                    Lists.newArrayList(
                            goodsDO.getUrl(),
                            goodsDO.getName(),
                            goodsDO.getPrice(),
                            goodsDO.getNumber(),
                            goodsDO.getSaleDesc(),
                            goodsDO.getEvaluateDesc()
                    )
            );
        }
        // poi
        PoiUtils.writeToExcel(
                Lists.newArrayList("商品网页链接","商品标题","价格","货号","商品介绍","商品详情"),
                data,
                GOODS_EXCEL_PATH
        );
    }

    /**
     *
     * @param goodsList
     */
    private void save(List<GoodsVO> goodsList) {
        if (CollectionUtils.isEmpty(goodsList)) {
            return;
        }
        try {
            goodsMapper.insert(goodsList.stream().map(goodsVO -> {
                GoodsDO goodsDO = new GoodsDO();
                goodsDO.setSerial(goodsVO.getSerial());
                goodsDO.setSource(goodsVO.getSource());
                goodsDO.setKeyword(goodsVO.getKeyword());
                goodsDO.setUrl(goodsVO.getGoodsUrl());
                goodsDO.setName(goodsVO.getGoodsName());
                goodsDO.setPrice(goodsVO.getPrice());
                return goodsDO;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "save", e.getMessage()));
        }
    }

    /**
     *
     * @param driver
     */
    private static void windowScroll(WebDriver driver) {
        // 创建JavaScriptExecutor对象
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 缓慢滚动到页面底部
        long scrollHeight = (long) js.executeScript("return document.body.scrollHeight");
        long windowHeight = (long) js.executeScript("return window.innerHeight");
        long scrollStep = MingshaUtil.getRandomLongInRange(25, 150); // 每次滚动的步长
        long currentScroll = 0;

        while (currentScroll < scrollHeight) {
            js.executeScript("window.scrollBy(0, " + scrollStep + ")");
            try {
                Thread.sleep(MingshaUtil.getRandomLongInRange(50, 500)); // 每次滚动后等待xx毫秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentScroll += scrollStep;
            scrollHeight = (long) js.executeScript("return document.body.scrollHeight");
        }
    }

}
