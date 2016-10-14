package file.handling.util;

import common.DataFileType;
import lombok.val;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RegionsUtils {
    public static final boolean[] EMPTY_REGIONS = new boolean[70];

    public static int getFileRegion(File file, DataFileType dataFileType) {
        int region = 0;
        if (!file.getAbsolutePath().matches(".+-\\d+\\" + dataFileType.getFileType())) {
            return 0;
        }
        val arr = file.getAbsolutePath().split("\\D+|\\s");
        for (int i = 1; i < arr.length; i++) {
            region = region + Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
        }
        return region;
    }

    public static  List<Integer> readRegionsFromSecondPage(Workbook workbook) {
        val existedRegions = EMPTY_REGIONS;
        List<Integer> regions = new ArrayList<>();
        val secondPage = workbook.getSheetAt(1);
        for (int i = 0; i < 14; i++) {
            Row row = secondPage.getRow(i);
            if (i % 2 != 0) {
                for (int j = 0; j < 10; j++) {
                    Cell cell = row.getCell(j);
                    if (cell.getBooleanCellValue()) {
                        existedRegions[(i - 1) * 5 + j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < existedRegions.length - 1; i++) {
            if (existedRegions[i]) {
                regions.add(i + 1);
            }
        }
        return regions;
    }

    public static void createRegionsPageInServerFile(Workbook workbook, boolean[] existedRegions) {
        Sheet sheet = workbook.createSheet("м.р, г.о");
        for (int i = 0; i < 14; i++) {
            Row row = sheet.createRow(i);
            if (i % 2 == 0) {
                for (int j = 0; j < 10; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(i * 5 + j + 1);
                }
            } else {
                for (int j = 0; j < 10; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(existedRegions[(i - 1) * 5 + j]);
                }
            }
        }
    }
}
