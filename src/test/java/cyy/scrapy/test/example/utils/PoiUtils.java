package cyy.scrapy.test.example.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PoiUtils {

    /**
     *
     * @param headers
     * @param data
     * @param path
     */
    public static void writeToExcel(List<String> headers, List<List<String>> data, String path) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
            }

            for (int i = 0; i < data.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                for (int j = 0; j < data.get(i).size(); j++) {
                    dataRow.createCell(j).setCellValue(data.get(i).get(j));
                }
            }

            // Style the header row
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }

            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}
