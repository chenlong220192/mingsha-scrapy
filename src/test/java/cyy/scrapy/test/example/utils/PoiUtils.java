package cyy.scrapy.test.example.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

    /**
     *
     * @param fileName
     * @param sheetIndex
     * @return
     */
    public static List<List<String>> readExcel(String fileName, int sheetIndex) {
        List<List<String>> data = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(new File(fileName)); Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);

            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                List<String> rowData = new ArrayList<>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case STRING:
                            rowData.add(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            rowData.add(String.valueOf(cell.getNumericCellValue()));
                            break;
                        case BOOLEAN:
                            rowData.add(String.valueOf(cell.getBooleanCellValue()));
                            break;
                        default:
                            rowData.add("");
                    }
                }
                data.add(rowData);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return data;
    }

}
