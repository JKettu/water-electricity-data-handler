package file.handling.handler.server.file.builder;

import common.DataType;
import common.logger.LogCategory;
import common.logger.Logger;
import file.handling.model.ElectricityDataModel;
import file.handling.util.DataGroupsGetter;
import file.handling.util.RegionsUtils;
import lombok.val;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import static file.handling.util.RegionsUtils.EMPTY_REGIONS;

public class ElectricityServerFileBuilder extends BaseServerFileBuilder<ElectricityDataModel> {
    private String firstDate;
    private String secondDate;

    public ElectricityServerFileBuilder(List<ElectricityDataModel> data, String firstDate, String secondDate) {
        super(data);
        this.firstDate = firstDate;
        this.secondDate = secondDate;
    }

    @Override
    public ByteArrayOutputStream build() {
        val logger = Logger.getLogger(getClass().getName(), "format");
        logger.log(LogCategory.DEBUG, "Creating server electricity file");
        val workbook = new HSSFWorkbook();
        val sheet = workbook.createSheet("Электроэнергия");
        sheet.setDefaultRowHeight((short) (20 * 15));
        sheet.setColumnWidth(0, 64 * 36);
        sheet.setColumnWidth(1, 199 * 36);
        sheet.setColumnWidth(2, 143 * 36);
        sheet.setColumnWidth(3, 143 * 36);
        sheet.setColumnWidth(4, 161 * 36);
        sheet.setColumnWidth(5, 121 * 36);
        sheet.setColumnWidth(6, 121 * 36);
        sheet.setColumnWidth(7, 121 * 36);
        sheet.setColumnWidth(8, 121 * 36);
        sheet.setColumnWidth(9, 121 * 36);
        sheet.setColumnWidth(10, 121 * 36);
        sheet.setColumnWidth(11, 121 * 36);
        sheet.setColumnWidth(12, 113 * 36);
        val cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_JUSTIFY);
        cellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);
        //ширина строк
        for (int i = 0; i < 6; i++) {
            val row = sheet.createRow((short) i);
            if (i == 0) {
                row.setHeight((short) (118 * 15));
            } else if (i == 2) {
                row.setHeight((short) (79 * 15));
            } else if (i == 3) {
                row.setHeight((short) (130 * 15));
            } else {
                row.setHeight((short) (20 * 15));
            }
            //по столбцам
            for (int j = 0; j < 13; j++) {
                val cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                switch (i) {
                    case 0://первая строчка
                        if (j == 0) {
                            cell.setCellValue(
                                    "ДАННЫЕ ДЛЯ ОПРЕДЕЛЕНИЯ И УСТАНОВЛЕНИЯ НОРМАТИВОВ ПОТРЕБЛЕНИЯ КОММУНАЛЬНЫХ УСЛУГ ПО ЭЛЕКТРОСНАБЖЕНИЮ  НА ОБЩЕДОМОВЫЕ НУЖДЫ");
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));
                        }
                        break;
                    case 2://третья строчка
                        switch (j) {
                            case 0:
                                cell.setCellValue("№ п/п");
                                sheet.addMergedRegion(new CellRangeAddress(2, 3, 0, 0));
                                break;
                            case 1:
                                cell.setCellValue("Адрес многоквартирного дома");
                                sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));
                                break;
                            case 2:
                                sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 4));
                                break;
                            case 5:
                                cell.setCellValue("Наличие коллективного (общедомового) прибора учета");
                                sheet.addMergedRegion(new CellRangeAddress(2, 3, 5, 5));
                                break;
                            case 6:
                                sheet.addMergedRegion(new CellRangeAddress(2, 2, 6, 7));
                                break;
                            case 8:
                                sheet.addMergedRegion(new CellRangeAddress(2, 2, 8, 9));
                                break;
                            case 10:
                                sheet.addMergedRegion(new CellRangeAddress(2, 2, 10, 11));
                                break;
                            case 12:
                                cell.setCellValue("м.р, г.о №");
                                sheet.addMergedRegion(new CellRangeAddress(2, 3, 12, 12));
                                break;
                        }
                    case 3://четвертая строчка
                        switch (j) {
                            case 2:
                                cell.setCellValue(
                                        "Максимальное количество этажей в многоквартирных домах (включая разноуровневые), в отношении которых определяется норматив, ед");
                                break;
                            case 3:
                                cell.setCellValue(
                                        "Наименьшее количество этажей в многоквартирных домах, в отношении которых определяется норматив, ед");
                                break;
                            case 4:
                                cell.setCellValue(
                                        "Общая площадь помещений, входящих в состав общего имущества в многоквартирных домах (кв. м)");
                                sheet.getRow(2).getCell(2)
                                        .setCellValue("Конструктивные и технические параметры многоквартирного дома");
                                break;
                            case 6:
                                cell.setCellValue(firstDate);
                                break;
                            case 7:
                                cell.setCellValue(secondDate);
                                sheet.getRow(2).getCell(6).setCellValue(
                                        "Расход электрической энергии по показаниям коллективного (общедомового) прибора учета (кВт·ч)");
                                break;
                            case 8:
                                cell.setCellValue(firstDate);
                                break;
                            case 9:
                                cell.setCellValue(secondDate);
                                sheet.getRow(2).getCell(8).setCellValue(
                                        "Суммарный расход электрической энергии в нежилых помещениях(кВт·ч)");
                                break;
                            case 10:
                                cell.setCellValue(firstDate);
                                break;
                            case 11:
                                cell.setCellValue(secondDate);
                                sheet.getRow(2).getCell(10).setCellValue(
                                        "Расход электрической энергии по показаниям индивидуального прибора учета в l-м жилом помещении W1, (кВт·ч)");
                                break;
                        }
                        break;
                    case 4://пятая строчка
                        cell.setCellValue(j + 1);
                        break;

                }
            }
        }
        //добавление в файл данных
        addDataToFile(sheet, cellStyle);
        return writeDataToStream(workbook);
    }

    @Override
    protected void addDataToFile(Sheet sheet, CellStyle cellStyle) {
        val logger = Logger.getLogger(getClass().getName(), "addDataToFile");
        logger.log(LogCategory.DEBUG, "Adding data to the server xls electricity file");
        int group = 0;
        int number = 0;
        Collections.sort(data);
        boolean[] regions = EMPTY_REGIONS;
        for (int i = 0; i < data.size(); i++) {
            val electricityDataModel = data.get(i);
            if (electricityDataModel.getGroup() != 0) {
                Row row = sheet.createRow((short) (i + 5 + group));
                for (int j = 0; j < 13; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(cellStyle);
                    if (electricityDataModel.getGroup() > group)//если верно - записать в файл название группы
                    {
                        sheet.addMergedRegion(new CellRangeAddress(i + 5 + group, i + 5 + group, 0, 12));
                        group = electricityDataModel.getGroup();
                        cell.setCellValue(DataGroupsGetter.getGroup(group, DataType.ELECTRICITY));
                        number = 1;
                        i--;
                        break;
                    }
                    switch (j) {
                        case 0:
                            cell.setCellValue(number);
                            number++;
                            break;
                        case 1:
                            cell.setCellValue(electricityDataModel.getAddress());
                            break;
                        case 2:
                            cell.setCellValue(electricityDataModel.getBiggestFloor());
                            break;
                        case 3:
                            cell.setCellValue(electricityDataModel.getSmallestFloor());
                            break;
                        case 4:
                            cell.setCellValue(electricityDataModel.getJoint());
                            break;
                        case 5:
                            cell.setCellValue(electricityDataModel.getHasAccountingDevice());
                            break;
                        case 6:
                            cell.setCellValue(electricityDataModel.getExpenseHouseFirstMonth());
                            break;
                        case 7:
                            cell.setCellValue(electricityDataModel.getExpenseHouseSecondMonth());
                            break;
                        case 8:
                            cell.setCellValue(electricityDataModel.getExpenseNotLivingFirstMonth());
                            break;
                        case 9:
                            cell.setCellValue(electricityDataModel.getExpenseNotLivingSecondMonth());
                            break;
                        case 10:
                            cell.setCellValue(electricityDataModel.getExpenseIndividFirstMonth());
                            break;
                        case 11:
                            cell.setCellValue(electricityDataModel.getExpenseIndividSecondMonth());
                            break;
                        case 12:
                            int region = electricityDataModel.getRegion();
                            cell.setCellValue(region);
                            regions[region - 1] = true;
                            break;
                    }
                }
            }
        }

        RegionsUtils.createRegionsPageInServerFile(sheet.getWorkbook(), regions);
    }
}
