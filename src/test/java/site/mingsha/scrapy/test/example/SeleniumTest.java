package site.mingsha.scrapy.test.example;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import site.mingsha.scrapy.test.example.utils.MingshaUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;

import site.mingsha.scrapy.test.example.utils.UserAgentUtils;

/**
 * selenium基本功能测试
 */
@SpringBootTest
public class SeleniumTest {

    private final static String CHROMEDRIVER = "/data/chromedriver/chromedriver";

    /**
     * 测试打开&关闭浏览器
     */
    @Test
    public void testOpenAndClose() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                driver = new ChromeDriver();
            }

            driver.get("https://www.baidu.com/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 测试窗口滚动
     */
    @Test
    public void testWindowScroll() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                driver = new ChromeDriver();
                driver.get("https://www.taobao.com/");
                Thread.sleep(1_000);
            }
            {
                // 创建JavaScriptExecutor对象
                JavascriptExecutor js = (JavascriptExecutor) driver;

                // 缓慢滚动到页面底部
                long scrollHeight = (long) js.executeScript("return document.body.scrollHeight");
                long windowHeight = (long) js.executeScript("return window.innerHeight");
                long scrollStep = MingshaUtil.getRandomLongInRange(25, 100); // 每次滚动的步长
                long currentScroll = 0;

                while (currentScroll < scrollHeight) {
                    js.executeScript("window.scrollBy(0, " + scrollStep + ")");
                    try {
                        Thread.sleep(MingshaUtil.getRandomLongInRange(5, 10)); // 每次滚动后等待10毫秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentScroll += scrollStep;
                    scrollHeight = (long) js.executeScript("return document.body.scrollHeight");
                }
            }
            {
                // 获取页面高度
                long pageHeight = (Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");

                // 计算页面一半的位置
                long halfPageHeight = pageHeight / 2;

                // 使用 JavaScript 滚动到页面一半的位置
                String script = "window.scrollTo(0, " + halfPageHeight + ");";
                ((JavascriptExecutor) driver).executeScript(script);
            }
            Thread.sleep(30_000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 测试窗口滚动
     */
    @Test
    public void testMouseMovement() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                driver = new ChromeDriver();
                driver.get("https://www.taobao.com/");
                Thread.sleep(3_000);
            }
            {
                // 获取当前屏幕大小
//                int screenWidth = driver.manage().window().getSize().getWidth();
//                int screenHeight = driver.manage().window().getSize().getHeight();
//
//                // 创建Actions对象
//                Actions actions = new Actions(driver);
//
//                // 随机移动鼠标
//                Random rand = new Random();
//                for (int i = 0; i < 10; i++) { // 重复移动10次
//                    int xOffset = rand.nextInt(screenWidth) - (screenWidth / 2); // 生成屏幕内的随机水平偏移量
//                    int yOffset = rand.nextInt(screenHeight) - (screenHeight / 2); // 生成屏幕内的随机垂直偏移量
//                    actions.moveByOffset(xOffset, yOffset).perform();
//                    Thread.sleep(1000); // 等待1秒钟
//                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 测试打开&关闭浏览器
     */
    @Test
    public void testRemoteOpenAndClose() {
        WebDriver driver = null;
        try {
            {
                //
                final ChromeOptions chromeOptions = new ChromeOptions();
                // 设置无头模式
                chromeOptions.setHeadless(true);
                // 设置窗口大小为全屏模式
                chromeOptions.addArguments("--start-maximized");
                // 在Linux系统下需要添加该选项
                chromeOptions.addArguments("--no-sandbox");
                // 在Linux系统下需要添加该选项
                chromeOptions.addArguments("--disable-dev-shm-usage");
                // 设置随机 User-Agent
                chromeOptions.addArguments("user-agent=" + UserAgentUtils.getRandomUserAgent());

                driver = new RemoteWebDriver(new URL("http://localhost:3000/webdriver"), chromeOptions);
            }

            driver.get("https://www.baidu.com/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 截屏
     */
    @Test
    public void testRemoteScreenshot() {
        WebDriver driver = null;
        try {
            {
                //
                final ChromeOptions chromeOptions = new ChromeOptions();
                // 设置无头模式
                chromeOptions.setHeadless(true);
                // 设置窗口大小为全屏模式
                chromeOptions.addArguments("--start-maximized");
                // 在Linux系统下需要添加该选项
                chromeOptions.addArguments("--no-sandbox");
                // 在Linux系统下需要添加该选项
                chromeOptions.addArguments("--disable-dev-shm-usage");
                // 设置随机 User-Agent
                chromeOptions.addArguments("user-agent=" + UserAgentUtils.getRandomUserAgent());

                driver = new RemoteWebDriver(new URL("http://localhost:3000/webdriver"), chromeOptions);
            }

            {
                // 打开登陆界面
                driver.get("https://www.taobao.com/");
                // echo
                System.out.println("打开登陆界面");
                // 等待一段时间，以便观察结果
                Thread.sleep(3000);
                // 点击二维码按钮
                WebElement qrcodeButton = driver.findElement(By.className("icon-qrcode"));
                qrcodeButton.click();
                // echo
                System.out.println("点击二维码按钮");
            }

            {
                // 窗口滚动
                // 创建 JavascriptExecutor 对象
                JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                // 将页面滚动到最右边
                jsExecutor.executeScript("window.scrollTo(document.body.scrollWidth, 0)");
                // echo
                System.out.println("窗口滚动到最右边");
                // 等待一段时间，以便观察结果
                Thread.sleep(2000);
            }

            {
                // 截取屏幕截图
                File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                // 截图保存路径
                String destinationPath = "/data/tmp/screenshot.png";
                // 将截图保存到指定路径
                FileUtils.copyFile(screenshotFile, new File(destinationPath));
                System.out.println("Screenshot saved to: " + destinationPath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

    }

    /**
     * 代理
     */
    @Test
    public void testRemoteProxy() {
        WebDriver driver = null;
        try {
            {
                // 创建代理对象
                String proxyAddress = "10.0.0.20";
                int proxyPort = 9999;
                Proxy proxy = new Proxy();
                proxy.setHttpProxy(proxyAddress + ":" + proxyPort);
                proxy.setSslProxy(proxyAddress + ":" + proxyPort);

                //
                final ChromeOptions chromeOptions = new ChromeOptions();
                // 代理
                chromeOptions.setProxy(proxy);
                // 设置无头模式
                chromeOptions.setHeadless(true);
                // 设置窗口大小为全屏模式
                chromeOptions.addArguments("--start-maximized");
                // 在Linux系统下需要添加该选项
                chromeOptions.addArguments("--no-sandbox");
                // 在Linux系统下需要添加该选项
                chromeOptions.addArguments("--disable-dev-shm-usage");
                // 设置随机 User-Agent
                chromeOptions.addArguments("user-agent=" + UserAgentUtils.getRandomUserAgent());

                driver = new RemoteWebDriver(new URL("http://localhost:3000/webdriver"), chromeOptions);
            }

            {
                // 打开登陆界面
                driver.get("https://login.taobao.com/");
                // echo
                System.out.println("打开登陆界面");
                // 等待一段时间，以便观察结果
                Thread.sleep(3000);
                // 点击二维码按钮
                WebElement qrcodeButton = driver.findElement(By.className("icon-qrcode"));
                qrcodeButton.click();
                // echo
                System.out.println("点击二维码按钮");
            }

            {
                // 创建 JavascriptExecutor 对象
                JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                // 将页面滚动到最右边
                jsExecutor.executeScript("window.scrollTo(document.body.scrollWidth, 0)");
                // echo
                System.out.println("窗口滚动到最右边");
                // 等待一段时间，以便观察结果
                Thread.sleep(2000);
            }

            {
                // 截取屏幕截图
                File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                // 截图保存路径
                String destinationPath = "/data/tmp/screenshot.png";
                // 将截图保存到指定路径
                FileUtils.copyFile(screenshotFile, new File(destinationPath));
                System.out.println("Screenshot saved to: " + destinationPath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                if (driver != null) {
                    driver.quit();
                }
            }
        }

    }

    /**
     * 测试读取页面能力
     */
    @Test
    public void testReadElement() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                driver = new ChromeDriver();
            }
            driver.get("https://www.taobao.com/");
            WebElement webElement = driver.findElement(By.xpath("/html"));
            System.out.println(webElement.getAttribute("outerHTML"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 测试输入并点击确认按钮
     */
    @Test
    public void testInput() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                driver = new ChromeDriver();
            }
            driver.get("https://www.taobao.com/");
            // 定位输入框
            WebElement inputBox = driver.findElement(By.id("q"));
            // 在输入框中输入信息
            inputBox.sendKeys("nike袜子");
            // 定位并点击按钮
            WebElement button = driver.findElement(By.className("btn-search"));
            button.click();

            // 等待一段时间以确保内容被加载
            Thread.sleep(3000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 测试自动登陆功能
     */
    @Test
    public void testLogin() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                driver = new ChromeDriver();
            }
            driver.get("https://login.taobao.com/");

            // 输入用户名和密码
            WebElement usernameInput = driver.findElement(By.id("fm-login-id"));
            WebElement passwordInput = driver.findElement(By.id("fm-login-password"));
            usernameInput.sendKeys("xxx");
            passwordInput.sendKeys("xxx");

            // 点击登录按钮
            WebElement loginButton = driver.findElement(By.className("password-login"));
            loginButton.click();

            // 等待一段时间以确保登录成功
            Thread.sleep(5000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 测试扫码登陆功能
     *
     * @throws InterruptedException
     */
    @Test
    public void testLogin4Qrcode() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                driver = new ChromeDriver();
            }
            driver.get("https://login.taobao.com/");

            // 点击二维码按钮
            WebElement qrcodeButton = driver.findElement(By.className("icon-qrcode"));
            qrcodeButton.click();

            // 等待一段时间以确保登录成功
            Thread.sleep(30000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 测试打开新tab页
     *
     * @throws InterruptedException
     */
    @Test
    public void testNewTab() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                driver = new ChromeDriver();
            }

            // 1、打开标签页
            // 打开第【1】个标签页
            driver.get("https://www.baidu.com/");
            // mock do something
            Thread.sleep(1_000);

            // 打开第【2】个标签页
            // 在当前标签页中执行 JavaScript 来打开新标签页
            ((JavascriptExecutor) driver).executeScript("window.open('https://www.jd.com/','_blank');");
            // mock do something
            Thread.sleep(1_000);
            // 打开第【3】个标签页
            ((JavascriptExecutor) driver).executeScript("window.open('https://www.taobao.com/','_blank');");
            // mock do something
            Thread.sleep(1_000);
            // 打开第【4】个标签页
            ((JavascriptExecutor) driver).executeScript("window.open('https://www.aliyun.com/','_blank');");
            // mock do something
            Thread.sleep(1_000);

            // 2、获取所有标签页的句柄
            Set<String> handles = driver.getWindowHandles();
            ArrayList<String> tabs = new ArrayList<>(handles);
            // 切换到指定的标签页
            driver.switchTo().window(tabs.get(0));
            System.out.println("切换到第【1】个标签");
            // mock do something
            Thread.sleep(1_000);
            driver.switchTo().window(tabs.get(1));
            System.out.println("切换到第【2】个标签");
            // mock do something
            Thread.sleep(1_000);
            driver.switchTo().window(tabs.get(2));
            System.out.println("切换到第【3】个标签");
            // mock do something
            Thread.sleep(1_000);
            driver.switchTo().window(tabs.get(3));
            System.out.println("切换到第【4】个标签");
            // mock do something
            Thread.sleep(1_000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 加载用户配置
     *
     * @throws InterruptedException
     */
    @Test
    public void testLoadUserContent() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                ChromeOptions chromeOptions = new ChromeOptions();
                // 禁用WebDriver特征
                chromeOptions.addArguments("--disable-blink-features");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                // 加载用户配置
                chromeOptions.addArguments("--user-data-dir=/Users/chenlong/Library/Application Support/Google/Chrome");
                driver = new ChromeDriver(chromeOptions);
            }

            // 1、打开标签页
            // 打开第【1】个标签页
            driver.get("https://www.baidu.com/");
            // mock do something
            Thread.sleep(1000_000);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 禁用特征
     *
     * @throws InterruptedException
     */
    @Test
    public void testInvisible() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                ChromeOptions chromeOptions = new ChromeOptions();
                // 禁用WebDriver特征
                chromeOptions.addArguments("--disable-blink-features");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                // 隐身模式
//                chromeOptions.addArguments("--incognito");
                driver = new ChromeDriver(chromeOptions);
            }

            // 1、打开标签页
            // 打开第【1】个标签页
            driver.get("https://www.baidu.com/");
            // mock do something
            Thread.sleep(1000_000);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * 模拟app
     *
     * @throws InterruptedException
     */
    @Test
    public void testApp() {
        WebDriver driver = null;
        try {
            {
                //
                System.getProperties().setProperty("webdriver.chrome.driver", CHROMEDRIVER);
                ChromeOptions chromeOptions = new ChromeOptions();
                // 禁用WebDriver特征
                chromeOptions.addArguments("--disable-blink-features");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                //模拟iPhone 6
//                chromeOptions.addArguments("user-agent=\"Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1");
                //模拟 android QQ浏览器
                chromeOptions.addArguments("user-agent=\"MQQBrowser/26 Mozilla/5.0 (Linux; U; Android 2.3.7; zh-cn; MB200 Build/GRJ22; CyanogenMod-7) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
                driver = new ChromeDriver(chromeOptions);
            }

            // 1、打开标签页
            // 打开第【1】个标签页
            driver.get("https://www.baidu.com/");
            // mock do something
            Thread.sleep(1000_000);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

}
