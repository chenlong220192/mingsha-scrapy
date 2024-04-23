package site.mingsha.scrapy.test.example;

import com.google.common.collect.Lists;
import site.mingsha.scrapy.test.example.dal.mapper.GoodsMapper;
import site.mingsha.scrapy.test.example.dal.mapper.MediaMapper;
import site.mingsha.scrapy.test.example.dal.model.GoodsDO;
import site.mingsha.scrapy.test.example.dal.model.MediaDO;
import site.mingsha.scrapy.test.example.model.GoodsVO;
import site.mingsha.scrapy.test.example.utils.DownloadUtils;
import site.mingsha.scrapy.test.example.utils.PoiUtils;
import site.mingsha.scrapy.test.example.utils.UserAgentUtils;
import site.mingsha.scrapy.test.example.utils.MingshaUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 利用selenium实现数据爬取
 */
@SpringBootTest
public class LVTest {

    private static final Logger logger           = LoggerFactory.getLogger(LVTest.class);

    /**
     *
     */
    private final static String URL_INDEX        = "https://www.louisvuitton.cn/zhs-cn/search/%E7%9A%AE%E5%B8%A6?page=7";
    private final static String GOODS_TMP_PATH   = "/data/tmp/mingsha/scrapy/LV";
    private final static String GOODS_EXCEL_PATH = "/data/tmp/mingsha/scrapy/goods_lv.xlsx";

    /* ----------------------------------------------------------------------- */
    @Resource
    private GoodsMapper         goodsMapper;
    @Resource
    private MediaMapper         mediaMapper;
    /* ----------------------------------------------------------------------- */

    /**
     *
     */
    @Test
    public void testLVCollector() {
        final String serial = "LV_1";
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

            // 判断是否有“更多”，有则继续点击。
            List<WebElement> goodsUrlList = driver.findElements(By.xpath("//li/div/div/div/div/div/h2/a"));

            //
            if (goodsUrlList.isEmpty()) {
                return;
            }

            List<GoodsVO> targetGoodsList = Lists.newArrayList();
            for (int i = 0; i < goodsUrlList.size(); i++) {
                WebElement goodsUrl = goodsUrlList.get(i);
                targetGoodsList
                    .add(GoodsVO.newInstance().setSerial(serial).setSource("LV").setKeyword(keyword).setGoodsUrl(goodsUrl.getAttribute("href")).setGoodsName(goodsUrl.getText()));
            }

            save(targetGoodsList);
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "LV", e.getMessage()));
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
    public void testLVAnalyzer() {
        final String serial = "LV_1";
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
                    MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(1_000, 2_000));
                    // 页面滚动到底
                    // windowScroll(driver);
                    // ---------- 休眠，模拟人工操作。 ----------
                    MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(1_000, 2_000));

                    {
                        WebElement goodsNo = driver.findElement(By.xpath("//div/div/div/span/strong"));
                        WebElement goodsPrice = driver.findElement(By.xpath("//div[contains(@class, 'lv-product__price')]/span"));

                        goodsDO.setNumber(goodsNo.getText());
                        goodsDO.setPrice(goodsPrice.getText());
                        if (StringUtils.isNotBlank(goodsNo.getText()) || StringUtils.isNotBlank(goodsPrice.getText())) {
                            // goodsMapper.update(goodsDO);
                        }

                        List<MediaDO> mediaDOList = Lists.newArrayList();
                        try {
                            WebElement media4critical = driver.findElement(By.xpath("//video[contains(@class, 'lv-video-loop__video')]"));
                            if (Objects.nonNull(media4critical)) {
                                String url = media4critical.getAttribute("src");

                                MediaDO mediaDO = new MediaDO();
                                mediaDO.setGoodsId(goodsDO.getId());
                                mediaDO.setUrl(url);
                                mediaDOList.add(mediaDO);
                            }
                        } catch (Exception e) {
                            logger.debug(String.format("Moudle:[%s]%n%s", "LV_FOR_1", e.getMessage()));
                        }

                        try {
                            List<WebElement> mediaList = driver.findElements(By.xpath("//ul/li/button/div/picture/img[contains(@class, 'lv-smart-picture__object')]"));
                            if (CollectionUtils.isEmpty(mediaList)) {
                                return;
                            }
                            mediaDOList.addAll(mediaList.stream().map(media -> {
                                MediaDO mediaDO = new MediaDO();
                                mediaDO.setGoodsId(goodsDO.getId());
                                mediaDO.setUrl(media.getAttribute("srcset"));
                                return mediaDO;
                            }).collect(Collectors.toList()));
                        } catch (Exception e) {
                            logger.debug(String.format("Moudle:[%s]%n%s", "LV_FOR_2", e.getMessage()));
                        }

                        if (CollectionUtils.isNotEmpty(mediaDOList)) {
                            mediaDOList = mediaDOList.stream().map(mediaDO -> {
                                mediaDO.setLocalUrl(DownloadUtils.downloadFile(mediaDO.getUrl(), GOODS_TMP_PATH));
                                return mediaDO;
                            }).collect(Collectors.toList());
                            mediaMapper.insert(mediaDOList);
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
    public void testLVGoods4Poi() {
        final String serial = "LV_1";
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
                            goodsDO.getNumber()
                    )
            );
        }
        // poi
        PoiUtils.writeToExcel(
                Lists.newArrayList("商品网页链接","商品标题","价格","货号"),
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
