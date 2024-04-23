package site.mingsha.scrapy.test.example.utils;

import org.apache.commons.io.FilenameUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;

public class DownloadUtils {

    /**
     * 下载文件方法，返回文件完整路径
     *
     * @param fileUrl
     * @param savePath
     * @return
     */
    public static String downloadFile(String fileUrl, String savePath) {
        try {
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();
            String fileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(url.getPath());
            String filePath = savePath + "/" + fileName;
            OutputStream outputStream = new FileOutputStream(filePath);
            byte[] buffer = new byte[2048];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
            return filePath;
        } catch (IOException e) {
            System.err.println("Failed to download file from URL: " + fileUrl);
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(DownloadUtils.downloadFile("https://lv-vod.fl.freecaster.net/vod/louisvuitton/wtuUYb7br6_HD.mp4", "/data/tmp/mingsha/scrapy/LV"));
    }

}
