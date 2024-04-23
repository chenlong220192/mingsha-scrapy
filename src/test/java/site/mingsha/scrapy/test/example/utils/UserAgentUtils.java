package site.mingsha.scrapy.test.example.utils;

import java.util.Random;

/**
 *
 */
public class UserAgentUtils {

    /**
     * 随机选择User-Agent
     * @return
     */
    public static String getRandomUserAgent() {
        String[] userAgents = {
                // Chrome
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                };
        return userAgents[new Random().nextInt(userAgents.length)];
    }

}
