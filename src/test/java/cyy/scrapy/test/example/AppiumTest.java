package cyy.scrapy.test.example;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URL;

/**
 * appium基本功能测试
 */
@SpringBootTest
public class AppiumTest {

    /**
     *
     */
    @Test
    public void testIOS() {
        // 设置 Appium 服务器的连接信息
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName", "iPhone XR");
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("automationName", "XCUITest"); // 使用 XCUITest 自动化框架
        capabilities.setCapability("udid", "YOUR_DEVICE_UDID"); // iOS 设备的 UDID
        capabilities.setCapability("xcodeOrgId", "YOUR_XCODE_ORG_ID"); // Xcode 组织标识符
        capabilities.setCapability("xcodeSigningId", "YOUR_XCODE_SIGNING_ID"); // Xcode 签名标识符
        capabilities.setCapability("bundleId", "com.yourapp.bundleid"); // 您的应用程序的 bundle ID
        capabilities.setCapability("platformVersion", "16.6"); // iOS 版本号
        capabilities.setCapability("appium:deviceName", "iPhone XR");
        capabilities.setCapability("appium:automationName", "XCUITest");
        capabilities.setCapability("appium:platformVersion", "16.6");
        capabilities.setCapability("appium:platformName", "iOS");

        // 连接到 Appium 服务器
        AppiumDriver<MobileElement> driver = null;
        try {
            driver = new IOSDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities); // 0.0.0.0 是您的 Appium 服务器地址

            // 执行操作，例如查找元素、输入文本、点击按钮等
            // driver.findElementBy...();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                // 关闭连接
                driver.quit();
            }
        }
    }

    /**
     *
     */
    @Test
    public void testAndroid() {
        AppiumDriver driver = null;
        try {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("deviceName", "Android Emulator");
            capabilities.setCapability("platformName", "Android");
            capabilities.setCapability("appPackage", "your.app.package");
            capabilities.setCapability("appActivity", "your.app.activity");

            // 设置虚拟设备的IP地址和端口号
            capabilities.setCapability("udid", "虚拟设备的IP地址:端口号");

            URL appiumServerURL = new URL("http://127.0.0.1:4723/wd/hub");
            driver = new AndroidDriver(appiumServerURL, capabilities);

            // 在此处添加测试代码

            driver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                // 关闭连接
                driver.quit();
            }
        }
    }

}
