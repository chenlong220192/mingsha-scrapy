package site.mingsha.scrapy.test.example;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import site.mingsha.scrapy.test.example.component.MailComponent;
import site.mingsha.scrapy.test.example.utils.PoiUtils;
import site.mingsha.scrapy.test.example.utils.UserAgentUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.common.collect.Lists;

import site.mingsha.scrapy.test.example.dal.mapper.GoodsMapper;
import site.mingsha.scrapy.test.example.dal.model.GoodsDO;
import site.mingsha.scrapy.test.example.model.GoodsVO;
import site.mingsha.scrapy.test.example.utils.ListSplitUtils;
import site.mingsha.scrapy.test.example.utils.MingshaUtil;

/**
 * 利用selenium实现数据爬取
 */
@SpringBootTest
public class TaobaoTest {

    private static final Logger  logger                               = LoggerFactory.getLogger(TaobaoTest.class);

    /**
     *
     */
    private final static String  URL_LOGIN                            = "https://login.taobao.com/";
    private final static String  URL_INDEX                            = "https://www.taobao.com/";
    private final static String  GOODS_EXCEL_PATH                     = "/data/tmp/goods_taobao.xlsx";

    private final static int     MAX_PAGE                             = 10;

    private final static long    SLEEP_TIME_LEFT                      = 15_000;
    private final static long    SLEEP_TIME_RIGHT                     = 60_000;

    /* ----------------------------------------------------------------------- */

    /**
     *
     */
    @Value("#{'${keyword.taobao}'.split(',')}")
    private List<String>         KEYWORD;

    /* ----------------------------------------------------------------------- */

    private final static String  ID_QUERY                             = "q";

    private final static String  CLASSNAME_LOGIN_QRCODE               = "icon-qrcode";
    private final static String  CLASSNAME_QUERY_BUTTON               = "btn-search";
    private final static String  CLASSNAME_GOODS_TAB_URL              = "Card--doubleCardWrapper--L2XFE73";
    private final static String  CLASSNAME_GOODS_TAB_NAME             = "Title--title--jCOPvpf";
    private final static String  CLASSNAME_GOODS_TAB_PRICE_INT        = "Price--priceInt--ZlsSi_M";
    private final static String  CLASSNAME_GOODS_TAB_PRICE_FLOAT      = "Price--priceFloat--h2RR0RK";
    private final static String  CLASSNAME_GOODS_TAB_TAKEDELIVERYDESC = "Price--realSales--FhTZc7U";
    private final static String  CLASSNAME_GOODS_TAB_SHOP_URL         = "ShopInfo--shopName--rg6mGmy";
    private final static String  CLASSNAME_GOODS_PAGE_NEXT            = "next-pagination-icon-next";
    private final static String  CLASSNAME_GOODS_SALESDESC            = "ItemHeader--salesDesc--srlk2Hv";

    private final static String  XPATH_GOODS_PAGE_SHOP_URL            = "(//a[@class='ShopHeader--button--SyE0mSc'])[2]";
    private final static String  XPATH_GOODS_PAGE_PAGINATION_DISPLAY  = "(//span[@class='next-pagination-display'])[1]";
    private final static String  XPATH_SORT_BY_SALES                  = "(//div[@class='next-tabs-tab-inner'])[2]";

    /* ----------------------------------------------------------------------- */

    @Resource
    private GoodsMapper          goodsMapper;

    /* ----------------------------------------------------------------------- */

    @Resource
    private MailComponent        mailComponent;

    /* ----------------------------------------------------------------------- */

    protected ThreadPoolExecutor POOL_COLLECTOR                       = new ThreadPoolExecutor(3, 3, 1000, java.util.concurrent.TimeUnit.MILLISECONDS,
        new java.util.concurrent.ArrayBlockingQueue<>(3));
    protected ThreadPoolExecutor POOL_ANALYZER                        = new ThreadPoolExecutor(5, 5, 1000, java.util.concurrent.TimeUnit.MILLISECONDS,
        new java.util.concurrent.ArrayBlockingQueue<>(5));

    /* ----------------------------------------------------------------------- */

    /**
     * 测试淘宝查询
     *
     */
    @Test
    public void testTaobaoCollector() {
        final String serial = "taobao_2";
        List<List<String>> keywordList = ListSplitUtils.splitWithPart(KEYWORD, 1);
        keywordList.forEach(keywordListTemp -> {
            POOL_COLLECTOR.execute(() -> {
                collectGoodsListPage(serial, keywordListTemp);
            });
        });
        for (;;) {
            if (POOL_COLLECTOR.getActiveCount() == 0) {
                logger.info("任务结束");
                return;
            }
            logger.info("任务正在执行，等待ing...");
            try {
                Thread.sleep(60 * 1_000);
            } catch (InterruptedException e) {
                logger.debug(e.getMessage());
            }
        }
    }

    /**
     *
     */
    @Test
    public void testTaobaoAnalyzer() {
        final String serial = "taobao_2";
        List<GoodsDO> goodsDOList = goodsMapper.findAllShopUrlIsEmpty(serial);
        if (CollectionUtils.isEmpty(goodsDOList)) {
            logger.info("任务结束");
            return;
        }
        List<List<GoodsDO>> goodsDOListList = ListSplitUtils.splitWithPart(goodsDOList, 5);
        goodsDOListList.forEach(goodsDOListTemp -> {
            POOL_ANALYZER.execute(() -> {
                analysisGoodsPage(goodsDOListTemp);
            });
        });
        for (;;) {
            if (POOL_ANALYZER.getActiveCount() == 0) {
                logger.info("任务结束");
                return;
            }
            logger.info("任务正在执行，等待ing...");
            try {
                Thread.sleep(60 * 1_000);
            } catch (InterruptedException e) {
                logger.debug(e.getMessage());
            }
        }
    }

    /**
     *
     */
    @Test
    public void testTaobaoGoodsMail() {
        final String serial = "taobao_2";
        List<GoodsDO> goodsDOList = goodsMapper.findBySerial(serial);
        if (CollectionUtils.isEmpty(goodsDOList)) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<List<String>> data = Lists.newArrayList();
        for (int i = 0; i < goodsDOList.size(); i++) {
            GoodsDO goodsDO = goodsDOList.get(i);
            data.add(Lists.newArrayList(
                    String.valueOf((i + 1)),
                    goodsDO.getSource(),
                    goodsDO.getKeyword(),
                    goodsDO.getName(),
                    goodsDO.getSaleDesc(),
                    goodsDO.getTakeDeliveryDesc(),
                    goodsDO.getPrice(),
                    goodsDO.getUrl(),
                    goodsDO.getShopName(),
                    goodsDO.getShopUrl(),
                    sdf.format(goodsDO.getGmtCreated())));
        }
        // poi
        PoiUtils.writeToExcel(
                Lists.newArrayList("序号", "平台", "搜索词", "商品名称", "销售描述", "收货描述", "商品价格", "商品链接", "店铺名称", "店铺链接", "采集时间"),
                data,
                GOODS_EXCEL_PATH
        );
        // mail
        mailComponent.sendMail4Mime(
                String.format("【mingsha】This is auto mail for %s","goods_taobao"),
                String.format("LOVE YOU~~~%n总数:%d条%n", data.size()),
                GOODS_EXCEL_PATH
        );
    }

    /**
     *
     */
    @Test
    public void testTaobaoGoodsMatchShopInfo() {
        final String serial = "taobao_2";

        List<GoodsDO> goodsDOList = goodsMapper.findBySerial(serial);
        if (CollectionUtils.isEmpty(goodsDOList)) {
            return;
        }

        List<List<String>> shopData = PoiUtils.readExcel("/data/tmp/scrapy.xlsx",4);
        Map<String, List<List<String>>> groupedByShopId = shopData.stream().collect(Collectors.groupingBy(row -> row.get(0)));

        List<List<String>> goodsData = Lists.newArrayList();
        for (int i = 0; i < goodsDOList.size(); i++) {
            GoodsDO goodsDO = goodsDOList.get(i);
            String shopId = StringUtils.isNotBlank(goodsDO.getShopUrl()) ? String.format("cn.taobao.%s",goodsDO.getShopUrl().replace("https://shop","").replace(".taobao.com/","").replace("/","").replace(" ","")) : "";
            List<List<String>> shopDataByShopId = StringUtils.isNotBlank(shopId) ? groupedByShopId.get(shopId) : null;

            goodsData.add(
                    Lists.newArrayList(
                            String.valueOf((i + 1)),
                            goodsDO.getSource(),
                            goodsDO.getKeyword(),
                            goodsDO.getName(),
                            goodsDO.getSaleDesc(),
                            goodsDO.getTakeDeliveryDesc(),
                            goodsDO.getPrice(),
                            goodsDO.getUrl(),
                            goodsDO.getShopName(),
                            goodsDO.getShopUrl(),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(goodsDO.getGmtCreated()),
                            shopId,
                            shopDataByShopId != null ? shopDataByShopId.get(0).get(1) : "",
                            shopDataByShopId != null ? shopDataByShopId.get(0).get(2) : "",
                            shopDataByShopId != null ? shopDataByShopId.get(0).get(3) : "",
                            shopDataByShopId != null ? shopDataByShopId.get(0).get(4) : "",
                            shopDataByShopId != null ? shopDataByShopId.get(0).get(5) : ""
                    )
            );
        }
        // poi
        PoiUtils.writeToExcel(
                Lists.newArrayList("序号", "平台", "搜索词", "商品名称", "销售描述", "收货描述", "商品价格", "商品链接", "店铺名称", "店铺链接", "采集时间", "店铺id", "店铺状态", "是否黑榜", "会否配饰抓取", "会否白牌店铺", "是否类目隐藏"),
                goodsData,
                GOODS_EXCEL_PATH
        );
    }

    /* ----------------------------------------------------------------------- */

    /**
     *
     * @param serial
     * @param keywordList
     */
    private void collectGoodsListPage(String serial, List<String> keywordList) {
        WebDriver driver = null;
        try {
            {
                //
                final ChromeOptions chromeOptions = new ChromeOptions();
                // 在Linux系统下需要添加该选项
                chromeOptions.addArguments("--disable-dev-shm-usage");
                // 设置随机 User-Agent
                chromeOptions.addArguments("user-agent=" + UserAgentUtils.getRandomUserAgent());
                // 禁用WebDriver特征
                chromeOptions.addArguments("--disable-blink-features");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                //
                System.getProperties().setProperty("webdriver.chrome.driver", "/data/chromedriver/chromedriver");
                driver = new ChromeDriver(chromeOptions);
            }

            // 1、登陆
            login(driver);
            // ---------- 显式等待直到元素可见 ----------
            wait(driver, ID_QUERY, 60, MingshaUtil.getRandomLongInRange(10, 3000));
            // echo
            logger.info("登陆成功");
            // collect target goods
            for (int i = 0; i < keywordList.size(); i++) {
                final String keyword = keywordList.get(i);
                try {
                    // 采集商品信息
                    collectGoodsListByKeyword(driver, serial, keyword);
                } catch (Exception e) {
                    logger.debug(String.format("Moudle:[%s]%n%s", "KEYWORD", e.getMessage()));
                }
            }
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "collectorGoodsList", e.getMessage()));
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     *
     * @param driver
     * @param keyword
     * @return
     */
    private void collectGoodsListByKeyword(WebDriver driver, String serial, String keyword) {
        try {
            int pageCount = MAX_PAGE;
            // 搜索商品
            searchGoods(driver, keyword);
            WebElement element = findElementByXpath(driver, XPATH_GOODS_PAGE_PAGINATION_DISPLAY);
            if (Objects.nonNull(element)) {
                String pagination = element.getText(); // 1/100
                if (StringUtils.isNotBlank(pagination) && pagination.contains("1/")) {
                    int totalPageCount = Integer.valueOf(pagination.split("1/")[1]);
                    pageCount = totalPageCount > pageCount ? pageCount : totalPageCount;
                }
            }
            for (int i = 1; i <= pageCount; i++) {
                //
                List<GoodsVO> goodsList4ThisPage = parseThisGoodsPage(driver, serial, keyword);
                logger.info(String.format("解析【keyword=%s】列表页：%s", keyword, driver.getCurrentUrl()));
                // save goods info to db
                save(goodsList4ThisPage);
                logger.info("保存解析数据");
                if (i == pageCount) {
                    break;
                }
                // 下一页
                next(driver);
                // ---------- 休眠，模拟人工操作。 ----------
                MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(SLEEP_TIME_LEFT, SLEEP_TIME_RIGHT));
            }
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "collectGoodsList", e.getMessage()));
        }
    }

    /**
     *
     * @param driver
     * @param keyword
     */
    private void searchGoods(WebDriver driver, String keyword) {
        // 2、查询
        // 打开首页
        driver.get(URL_INDEX);
        // ---------- 显式等待直到元素可见 ----------
        wait(driver, ID_QUERY, 10, MingshaUtil.getRandomLongInRange(1000, 3000));
        // echo
        logger.info("打开taobao.com首页");
        // input keyword
        inputKeyword(driver, keyword);
        // echo
        logger.info("输入关键词：" + keyword);
        // ---------- 休眠，模拟人工操作。 ----------
        MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(3_000, 10_000));
        // 排序
        sortBySales(driver);
        // echo
        logger.info("点击按销量排序");
        // ---------- 休眠，模拟人工操作。 ----------
        MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(3_000, 10_000));
    }

    /**
     *
     * @param driver
     * @param keyword
     * @return
     */
    private List<GoodsVO> parseThisGoodsPage(WebDriver driver, String serial, String keyword) {
        List<GoodsVO> targetGoodsList = Lists.newArrayList();
        try {
            List<WebElement> linkElements4GoodsList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_URL));
            List<WebElement> linkElements4GoodsNameList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_NAME));
            List<WebElement> linkElements4GoodsPriceIntList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_PRICE_INT));
            List<WebElement> linkElements4GoodsPriceFloatList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_PRICE_FLOAT));
            List<WebElement> linkElements4GoodsTakeDeliveryDescList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_TAKEDELIVERYDESC));
            List<WebElement> linkElements4GoodsShopList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_SHOP_URL));

            for (int i = 0; i < linkElements4GoodsList.size(); i++) {
                WebElement webElement4Goods = linkElements4GoodsList.get(i);
                WebElement webElement4GoodsName = linkElements4GoodsNameList.get(i);
                WebElement webElement4GoodsPriceInt = linkElements4GoodsPriceIntList.get(i);
                WebElement webElement4GoodsPriceFloat = linkElements4GoodsPriceFloatList.get(i);
                WebElement webElement4GoodsTakeDeliveryDesc = linkElements4GoodsTakeDeliveryDescList.get(i);
                WebElement webElement4GoodsShop = linkElements4GoodsShopList.get(i);
                targetGoodsList.add(GoodsVO.newInstance().setSerial(serial).setSource("taobao").setKeyword(keyword).setGoodsUrl(webElement4Goods.getAttribute("href"))
                    .setGoodsName(webElement4GoodsName.getText()).setPrice(String.format("%s%s", webElement4GoodsPriceInt.getText(), webElement4GoodsPriceFloat.getText()))
                    .setTakeDeliveryDesc(webElement4GoodsTakeDeliveryDesc.getText()).setSaleDesc(StringUtils.EMPTY).setShopUrl(StringUtils.EMPTY)
                    .setShopName(webElement4GoodsShop.getText()));
            }
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "parseSingleGoods", e.getMessage()));
        }
        return targetGoodsList;
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
                goodsDO.setShopUrl(goodsVO.getShopUrl());
                goodsDO.setShopName(goodsVO.getShopName());
                goodsDO.setPrice(goodsVO.getPrice());
                goodsDO.setSaleDesc(goodsVO.getSaleDesc());
                goodsDO.setTakeDeliveryDesc(goodsVO.getTakeDeliveryDesc());
                return goodsDO;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "save", e.getMessage()));
        }
    }

    /**
     * 解析商品页面
     *
     * @param driver
     * @param goods
     */
    private GoodsVO parseGoodsPage(WebDriver driver, GoodsVO goods) {
        try {
            if (StringUtils.isBlank(goods.getGoodsUrl())) {
                return null;
            }
            // 4、进入商品页面采集信息
            open(driver, goods.getGoodsUrl());
            // echo
            logger.info("打开商品信息页面：" + goods.getGoodsUrl());
            // ---------- 休眠，模拟人工操作。 ----------
            MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(SLEEP_TIME_LEFT, SLEEP_TIME_RIGHT));
            //
            WebElement element = findElementByXpath(driver, XPATH_GOODS_PAGE_SHOP_URL);
            if (Objects.nonNull(element)) {
                goods.setShopUrl(element.getAttribute("href"));
            }
            goods.setSaleDesc(MingshaUtil.warpNull4Text(findElementByClassName(driver, CLASSNAME_GOODS_SALESDESC)));
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "parseGoodsPage", e.getMessage()));
        }
        return goods;
    }

    /**
     *
     * @param goodsDOList
     */
    private void analysisGoodsPage(List<GoodsDO> goodsDOList) {
        WebDriver driver = null;
        try {
            {
                //
                final ChromeOptions chromeOptions = new ChromeOptions();
                // 在Linux系统下需要添加该选项
                chromeOptions.addArguments("--disable-dev-shm-usage");
                // 设置随机 User-Agent
                chromeOptions.addArguments("user-agent=" + UserAgentUtils.getRandomUserAgent());
                // 禁用WebDriver特征
                chromeOptions.addArguments("--disable-blink-features");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                //
                System.getProperties().setProperty("webdriver.chrome.driver", "/data/chromedriver/chromedriver");
                driver = new ChromeDriver(chromeOptions);
            }

            // 1、登陆
            login(driver);
            // ---------- 显式等待直到元素可见 ----------
            wait(driver, ID_QUERY, 180, MingshaUtil.getRandomLongInRange(1_000, 15_000));
            // echo
            logger.info("登陆成功");

            for (GoodsDO goodsDO : goodsDOList) {
                if (StringUtils.isNotBlank(goodsDO.getShopUrl())) {
                    continue;
                }
                GoodsVO goodsVO = parseGoodsPage(driver, GoodsVO.newInstance().setKeyword(goodsDO.getKeyword()).setGoodsUrl(goodsDO.getUrl()));
                if (Objects.isNull(goodsVO) || StringUtils.isBlank(goodsVO.getSaleDesc())) {
                    continue;
                }
                if (StringUtils.isNotBlank(goodsVO.getShopUrl())) {
                    goodsDO.setShopUrl(goodsVO.getShopUrl());
                }
                if (StringUtils.isNotBlank(goodsVO.getSaleDesc())) {
                    goodsDO.setSaleDesc(goodsVO.getSaleDesc());
                }
                if (StringUtils.isNotBlank(goodsVO.getShopUrl()) || StringUtils.isNotBlank(goodsVO.getSaleDesc())) {
                    goodsMapper.update(goodsDO);
                }
            }
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "analysisGoodsPage", e.getMessage()));
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /* ----------------------------------------------------------------------- */

    /**
     * 点击下一页
     * @param driver
     */
    private void next(WebDriver driver) {
        try {
            WebElement pageButton = driver.findElement(By.className(CLASSNAME_GOODS_PAGE_NEXT));
            // 点击按钮
            pageButton.click();
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "next", e.getMessage()));
        }
    }

    /**
     *
     * @param driver
     */
    private void sortBySales(WebDriver driver) {
        try {
            // 定位并点击排序按钮
            WebElement filterButton = driver.findElement(By.xpath(XPATH_SORT_BY_SALES));
            filterButton.click();
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "sortBySales", e.getMessage()));
        }
    }

    /**
     *
     * @param driver
     * @param keyword
     */
    private void inputKeyword(WebDriver driver, String keyword) {
        try {
            // (1)定位输入框
            WebElement inputBox = driver.findElement(By.id(ID_QUERY));
            // (2)在输入框中输入信息
            inputBox.sendKeys(keyword);
            // (3)定位并点击按钮
            WebElement queryButton = driver.findElement(By.className(CLASSNAME_QUERY_BUTTON));
            queryButton.click();
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "login", e.getMessage()));
        }
    }

    /**
     *
     * @param driver
     */
    private void login(WebDriver driver) {
        // 打开登陆界面
        driver.get(URL_LOGIN);
        // echo
        logger.info("打开登陆界面");
        // 点击二维码按钮
        try {
            WebElement qrcodeButton = driver.findElement(By.className(CLASSNAME_LOGIN_QRCODE));
            qrcodeButton.click();
            // echo
            logger.info("点击二维码按钮");
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "login", e.getMessage()));
        }
    }

    /**
     *
     * @param driver
     */
    private void open(WebDriver driver, String url) {
        try {
            // 打开url
            driver.get(url);
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "open", e.getMessage()));
        }
    }

    /* ----------------------------------------------------------------------- */

    /**
     *
     * @param driver
     * @param id
     * @return
     */
    private WebElement findElementById(WebDriver driver, String id) {
        WebElement element = null;
        try {
            element = driver.findElement(By.id(id));
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "findElementById", e.getMessage()));
        }
        return element;
    }

    /**
     *
     * @param driver
     * @param className
     * @return
     */
    private WebElement findElementByClassName(WebDriver driver, String className) {
        WebElement element = null;
        try {
            element = driver.findElement(By.className(className));
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "findElementByClassName", e.getMessage()));
        }
        return element;
    }

    /**
     *
     * @param driver
     * @param xpath
     * @return
     */
    private WebElement findElementByXpath(WebDriver driver, String xpath) {
        WebElement element = null;
        try {
            element = driver.findElement(By.xpath(xpath));
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "findElementByXpath", e.getMessage()));
        }
        return element;
    }

    /* ----------------------------------------------------------------------- */

    /**
     *
     * @param driver
     */
    private void wait(WebDriver driver, String id, long timeOutInSeconds, long sleepInMillis) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds, sleepInMillis);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "wait", e.getMessage()));
        }
    }

}
