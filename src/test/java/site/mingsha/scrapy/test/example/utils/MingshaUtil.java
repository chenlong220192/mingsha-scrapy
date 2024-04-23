package site.mingsha.scrapy.test.example.utils;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Random;

import org.openqa.selenium.WebElement;

public class MingshaUtil {

    /**
     *
     * @param millis
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            System.err.println(String.format("Moudle:[%s]%n%s", "sleep", e.getMessage()));
        }
    }

    /**
     * 获取随机数
     *
     * @param min
     * @param max
     * @return
     */
    public static long getRandomLongInRange(long min, long max) {
        if (min >= max) {
            throw new IllegalArgumentException("最小值必须小于最大值");
        }
        Random random = new Random();
        // 计算范围内随机数的上限
        long range = max - min + 1;
        // 生成随机 long 值
        long randomLong = min + (long) (random.nextDouble() * range);
        return randomLong;
    }

    /**
     * 计算百分比
     *
     * @param left
     * @param right
     * @return
     */
    public static String percentage(long left, long right) {
        // 计算百分比
        double percentage = (Double.valueOf(left) / Double.valueOf(right)) * 100;
        // 使用 DecimalFormat 格式化输出，保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("#.######");
        String formattedPercentage = decimalFormat.format(percentage);
        //
        return formattedPercentage + "%";
    }

    /**
     * 毫秒转时分秒
     *
     * @param millis
     * @return
     */
    public static String millisToHMS(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     *
     * @param element
     * @return
     */
    public static String warpNull4Text(WebElement element){
        return Objects.nonNull(element) ? element.getText() : null;
    }

}
