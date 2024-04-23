package site.mingsha.scrapy.test.example;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import site.mingsha.scrapy.test.example.component.MailComponent;
import site.mingsha.scrapy.test.example.dal.mapper.GoodsMapper;
import site.mingsha.scrapy.test.example.dal.model.GoodsDO;
import site.mingsha.scrapy.test.example.model.GoodsVO;
import site.mingsha.scrapy.test.example.utils.ListSplitUtils;
import site.mingsha.scrapy.test.example.utils.PoiUtils;
import site.mingsha.scrapy.test.example.utils.MingshaUtil;
import site.mingsha.scrapy.test.example.utils.UserAgentUtils;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 利用selenium实现数据爬取
 */
@SpringBootTest
public class JdTest {

    private static final Logger  logger                              = LoggerFactory.getLogger(JdTest.class);

    /**
     *
     */
    private final static String  URL_LOGIN                           = "https://passport.jd.com/new/login.aspx?ReturnUrl=https://www.jd.com/";
    private final static String  URL_INDEX                           = "https://www.jd.com/";
    private final static String  GOODS_EXCEL_PATH                    = "/data/tmp/goods_jd.xlsx";

    private final static int     MAX_PAGE                            = 5;

    private final static long    SLEEP_TIME_LEFT                     = 15_000;
    private final static long    SLEEP_TIME_RIGHT                    = 60_000;

    /* ----------------------------------------------------------------------- */

    /**
     *
     */
    @Value("#{'${keyword.jd}'.split(',')}")
    private List<String>         KEYWORD;

    /* ----------------------------------------------------------------------- */

    private final static String  ID_QUERY                            = "key";

    private final static String  CLASSNAME_GOODS_TAB_URL             = "p-name-type-2";
    private final static String  CLASSNAME_GOODS_TAB_PRICE           = "gl-i-wrap";
    private final static String  CLASSNAME_GOODS_TAB_SHOP_URL        = "J_im_icon";
    private final static String  CLASSNAME_GOODS_TAB_EVALUATE        = "p-commit";
    private final static String  CLASSNAME_GOODS_PAGE_NEXT           = "pn-next";

    private final static String  XPATH_LOGIN_QRCODE                  = "//div[contains(@class, 'login-tab') and contains(@class, 'login-tab-l')]";
    private final static String  XPATH_QUERY_BUTTON                  = "(//button[@class='button'])[1]";
    private final static String  XPATH_GOODS_PAGE_PAGINATION_DISPLAY = "(//span[@class='fp-text'])[1]";
    private final static String  XPATH_SORT_BY_SALES                 = "//a[.//text()='销量']";

    /* ----------------------------------------------------------------------- */

    @Resource
    private GoodsMapper          goodsMapper;
    /* ----------------------------------------------------------------------- */

    @Resource
    private MailComponent        mailComponent;

    /* ----------------------------------------------------------------------- */

    protected ThreadPoolExecutor POOL_COLLECTOR                      = new ThreadPoolExecutor(3, 3, 1000, java.util.concurrent.TimeUnit.MILLISECONDS,
        new java.util.concurrent.ArrayBlockingQueue<>(3));

    /* ----------------------------------------------------------------------- */

    /**
     * 测试jd查询
     *
     */
    @Test
    public void testJdCollector() {
        final String serial = "jd_2";
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
    public void testTaobaoGoodsMail() {
        final String serial = "jd_2";
        List<GoodsDO> goodsDOList = goodsMapper.findBySerial(serial);
        if (CollectionUtils.isEmpty(goodsDOList)) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<List<String>> data = Lists.newArrayList();
        for (int i = 0; i < goodsDOList.size(); i++) {
            GoodsDO goodsDO = goodsDOList.get(i);
            data.add(Lists.newArrayList(String.valueOf((i + 1)), goodsDO.getSource(), goodsDO.getKeyword(), goodsDO.getName(), goodsDO.getEvaluateDesc(), goodsDO.getPrice(),
                goodsDO.getUrl(), goodsDO.getShopName(), String.format("cn.jd.%s", goodsDO.getShopUrl().replace(".html?from=pc", "").split("index-")[1]), goodsDO.getShopUrl(),
                sdf.format(goodsDO.getGmtCreated())));
        }
        // poi
        PoiUtils.writeToExcel(Lists.newArrayList("序号", "平台", "搜索词", "商品名称", "评价描述", "商品价格", "商品链接", "店铺名称", "店铺ID", "店铺链接", "采集时间"), data, GOODS_EXCEL_PATH);
        // mail
        mailComponent.sendMail4Mime(String.format("【mingsha】This is auto mail for %s", "goods_jd"), String.format("LOVE YOU~~~%n总数:%d条%n", data.size()), GOODS_EXCEL_PATH);
    }

    /**
     *
     */
    @Test
    public void testJdGoodsMatchShopInfo() {
        final String serial = "jd_2";
        List<GoodsDO> goodsDOList = goodsMapper.findBySerial(serial);
        if (CollectionUtils.isEmpty(goodsDOList)) {
            return;
        }
        //
        List<List<String>> shopData = PoiUtils.readExcel("/data/tmp/scrapy.xlsx",4);
        Map<String, List<List<String>>> groupedByShopId = shopData.stream().collect(Collectors.groupingBy(row -> row.get(0)));

        List<List<String>> data = Lists.newArrayList();
        for (int i = 0; i < goodsDOList.size(); i++) {
            GoodsDO goodsDO = goodsDOList.get(i);
            String shopId = StringUtils.isNotBlank(goodsDO.getShopUrl()) ? String.format("cn.jd.%s", goodsDO.getShopUrl().replace("https://mall.jd.com/index-","").replace(".html?from=pc", "")) : "";
            List<List<String>> shopDataByShopId = StringUtils.isNotBlank(shopId) ? groupedByShopId.get(shopId) : null;
            data.add(
                    Lists.newArrayList(
                            String.valueOf((i + 1)),
                            goodsDO.getSource(),
                            goodsDO.getKeyword(),
                            goodsDO.getName(),
                            goodsDO.getEvaluateDesc(),
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
                Lists.newArrayList("序号", "平台", "搜索词", "商品名称", "评价描述", "商品价格", "商品链接", "店铺名称", "店铺链接", "采集时间", "店铺id", "店铺状态", "是否黑榜", "会否配饰抓取", "会否白牌店铺", "是否类目隐藏"),
                data,
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

            // 1、登陆
            login(driver);
            // ---------- 显式等待直到元素可见 ----------
            wait(driver, ID_QUERY, 10, MingshaUtil.getRandomLongInRange(1_000, 10_000));
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
     */
    private void login(WebDriver driver) {
        // 打开登陆界面
        driver.get(URL_LOGIN);
        // echo
        logger.info("打开登陆界面");
        // 点击二维码按钮
        try {
            WebElement qrcodeButton = findElementByXpath(driver, XPATH_LOGIN_QRCODE);
            // 使用 Actions 类模拟鼠标点击操作
            Actions actions = new Actions(driver);
            actions.click(qrcodeButton).perform();
            // echo
            logger.info("点击二维码按钮");
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "login", e.getMessage()));
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
                if (CollectionUtils.isEmpty(goodsList4ThisPage)) {
                    logger.info(String.format("!!!失败!!!【keyword=%s】列表页：%s", keyword, driver.getCurrentUrl()));
                    break;
                }
                //logger.info(String.format("解析【keyword=%s】列表页：%s", keyword, driver.getCurrentUrl()));
                // save goods info to db
                save(goodsList4ThisPage);
                logger.info(String.format("保存解析数据，【第%d页】【共%s条】", i, goodsList4ThisPage.size()));
                if (i == pageCount) {
                    break;
                }
                // 下一页
                next(driver);
                // ---------- 休眠，模拟人工操作。 ----------
                MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(1_000, 3_000));
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
        // echo
        logger.info("打开jd.com首页");
        // ---------- 显式等待直到元素可见 ----------
        wait(driver, ID_QUERY, 10, MingshaUtil.getRandomLongInRange(1_000, 10_000));

        // input keyword
        inputKeyword(driver, keyword);
        // echo
        logger.info("输入关键词：" + keyword);
        // ---------- 休眠，模拟人工操作。 ----------
        MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(500, 5_000));
        /* +++++++++++++++++++++++++ */
        verifyPage(driver);
        /* +++++++++++++++++++++++++ */

        // 排序
        sortBySales(driver);
        // echo
        logger.info("点击按销量排序");
        // ---------- 休眠，模拟人工操作。 ----------
        MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(500, 5_000));
        /* +++++++++++++++++++++++++ */
        verifyPage(driver);
        /* +++++++++++++++++++++++++ */
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
            WebElement queryButton = findElementByXpath(driver, XPATH_QUERY_BUTTON);
            // 使用 Actions 类模拟鼠标点击操作
            Actions actions = new Actions(driver);
            actions.click(queryButton).perform();
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "login", e.getMessage()));
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
            // 使用 Actions 类模拟鼠标点击操作
            Actions actions = new Actions(driver);
            actions.click(filterButton).perform();
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "sortBySales", e.getMessage()));
        }
    }

    /**
     * 点击下一页
     * @param driver
     */
    private void next(WebDriver driver) {
        try {
            WebElement pageButton = driver.findElement(By.className(CLASSNAME_GOODS_PAGE_NEXT));
            // 点击按钮
            // 使用 Actions 类模拟鼠标点击操作
            Actions actions = new Actions(driver);
            actions.click(pageButton).perform();
            // echo
            logger.info("点击下一页");
            // ---------- 休眠，模拟人工操作。 ----------
            MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(1_500, 3_000));
            /* +++++++++++++++++++++++++ */
            verifyPage(driver);
            /* +++++++++++++++++++++++++ */
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "next", e.getMessage()));
        }
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
            //
            // windowScroll(driver);
            //
            repairPage(driver);
            //
            List<WebElement> goodsUrlList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_URL));
            List<WebElement> goodsPriceList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_PRICE));
            List<WebElement> goodsShopUrlList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_SHOP_URL));
            List<WebElement> goodsEvaluateList = driver.findElements(By.className(CLASSNAME_GOODS_TAB_EVALUATE));

            //
            if (goodsUrlList.isEmpty() || goodsUrlList.size() == 30) {
                return null;
            }

            for (int i = 0; i < goodsUrlList.size(); i++) {
                WebElement goodsUrl = goodsUrlList.get(i).findElement(By.xpath(".//a"));
                WebElement goodsPrice = goodsPriceList.get(i).findElement(By.className("p-price"));
                WebElement goodsShopUrl = goodsShopUrlList.get(i).findElement(By.className("hd-shopname"));
                WebElement goodsEvaluate = goodsEvaluateList.get(i).findElement(By.xpath(".//a"));
                targetGoodsList.add(GoodsVO.newInstance().setSerial(serial).setSource("jd").setKeyword(keyword).setGoodsUrl(goodsUrl.getAttribute("href"))
                    .setGoodsName(goodsUrl.getText()).setPrice(goodsPrice.getText().replace("￥", "")).setShopName(goodsShopUrl.getText())
                    .setShopUrl(goodsShopUrl.getAttribute("href")).setEvaluateDesc(goodsEvaluate.getText()));
            }
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "parseSingleGoods", e.getMessage()));
        }
        return targetGoodsList;
    }

    /**
     *
     * @param driver
     */
    private void repairPage(WebDriver driver) {
        AtomicInteger count = new AtomicInteger(0);
        for (;;) {
            // 检查页面源代码中是否包含目标文本
            if (driver.getPageSource().contains("正在加载中")) {
                // logger.warn(String.format("页面加载ing... URL:%s", driver.getCurrentUrl()));
                int times = count.getAndIncrement();
                if(times == 0){
                    // scrollHere((JavascriptExecutor) driver, 2);
                }
                // ---------- 休眠，模拟人工操作。 ----------
                MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(1_000, 2_000));
                continue;
            }
            //
            List<WebElement> elements = driver.findElements(By.xpath("//a[.//text()='重试']"));
            if (elements.isEmpty()) {
                break;
            }
            {
                // 使用 Actions 类模拟鼠标点击操作
                Actions actions = new Actions(driver);
                actions.click(elements.get(0)).perform();
                // logger.warn(String.format("页面重试ing... URL:%s", driver.getCurrentUrl()));
                // ---------- 休眠，模拟人工操作。 ----------
                MingshaUtil.sleep(MingshaUtil.getRandomLongInRange(500, 1_000));
                /* +++++++++++++++++++++++++ */
                verifyPage(driver);
                /* +++++++++++++++++++++++++ */
            }
        }
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
                goodsDO.setEvaluateDesc(goodsVO.getEvaluateDesc());
                return goodsDO;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "save", e.getMessage()));
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

    /* ----------------------------------------------------------------------- */

    /**
     *
     * @param driver
     */
    private void verifyPage(WebDriver driver) {
        try {
            List<WebElement> elements = driver.findElements(By.xpath("//div[contains(@class, 'verifyBtn')]"));
            if (elements.isEmpty()) {
                return;
            }
            // ---------- 显式等待直到元素可见 ----------
            wait(driver, ID_QUERY, 10, MingshaUtil.getRandomLongInRange(30_000, 60_000));
        } catch (Exception e) {
            logger.debug(String.format("Moudle:[%s]%n%s", "wait", e.getMessage()));
        }
    }
    /* ----------------------------------------------------------------------- */

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
                Thread.sleep(MingshaUtil.getRandomLongInRange(10, 100)); // 每次滚动后等待10毫秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentScroll += scrollStep;
            scrollHeight = (long) js.executeScript("return document.body.scrollHeight");
        }

        scrollHere((JavascriptExecutor) driver, 2);
    }

    /**
     *
     * @param driver
     * @param p
     */
    private static void scrollHere(JavascriptExecutor driver, int p) {
        // 获取页面高度
        long pageHeight = (Long) driver.executeScript("return document.body.scrollHeight");

        // 计算页面一半的位置
        long halfPageHeight = pageHeight / p;

        // 使用 JavaScript 滚动到页面一半的位置
        String script = "window.scrollTo(0, " + halfPageHeight + ");";
        driver.executeScript(script);
    }

}
