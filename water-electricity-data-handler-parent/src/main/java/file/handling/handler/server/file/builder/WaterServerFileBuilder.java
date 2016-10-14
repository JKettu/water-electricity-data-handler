package file.handling.handler.server.file.builder;

import common.DataType;
import common.logger.LogCategory;
import common.logger.Logger;
import file.handling.model.WaterDataModel;
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

public class WaterServerFileBuilder extends BaseServerFileBuilder<WaterDataModel> {
    private String period;

    public WaterServerFileBuilder(List<WaterDataModel> data, String period) {
        super(data);
        this.period = period;
    }

    @Override
    public ByteArrayOutputStream build() {
        val logger = Logger.getLogger(getClass().getName(), "format");
        logger.log(LogCategory.DEBUG, "Creating server water file");
        val workbook = new HSSFWorkbook();
        val sheet = workbook.createSheet("Водоснабжение");
        sheet.setDefaultRowHeight((short) (20 * 15));
        sheet.setColumnWidth(0, 64 * 37);
        sheet.setColumnWidth(1, 178 * 37);
        sheet.setColumnWidth(2, 125 * 37);
        sheet.setColumnWidth(3, 125 * 37);
        sheet.setColumnWidth(4, 125 * 37);
        sheet.setColumnWidth(5, 109 * 37);
        sheet.setColumnWidth(6, 76 * 37);
        sheet.setColumnWidth(7, 125 * 37);
        sheet.setColumnWidth(8, 113 * 37);
        sheet.setColumnWidth(9, 84 * 37);
        sheet.setColumnWidth(10, 64 * 37);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_JUSTIFY);
        cellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);

        //ширина строк
        for (int i = 0; i < 6; i++) {
            Row row = sheet.createRow((short) i);
            if (i == 0) {
                row.setHeight((short) (59 * 15));
            } else if (i == 2) {
                row.setHeight((short) (94 * 15));
            } else if (i == 3) {
                row.setHeight((short) (142 * 15));
            } else {
                row.setHeight((short) (20 * 15));
            }
            //по столбцам
            for (int j = 0; j < 11; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                switch (i) {
                    case 0://первая строчка
                        if (j == 0) {
                            cell.setCellValue(
                                    "ДАННЫЕ ДЛЯ ОПРЕДЕЛЕНИЯ И УСТАНОВЛЕНИЯ НОРМАТИВОВ ПОТРЕБЛЕНИЯ КОММУНАЛЬНЫХ УСЛУГ ПО ХОЛОДНОМУ И ГОРЯЧЕМУ ВОДОСНАБЖЕНИЮ НА ОБЩЕДОМОВЫЕ НУЖДЫ");
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
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
                                cell.setCellValue(
                                        "Численность проживающих жителей в i-м многоквартирном доме или жилом доме");
                                sheet.addMergedRegion(new CellRangeAddress(2, 3, 5, 5));
                                break;
                            case 6:
                                sheet.addMergedRegion(new CellRangeAddress(2, 2, 6, 7));
                                break;
                            case 8:
                                sheet.addMergedRegion(new CellRangeAddress(2, 2, 8, 9));
                                break;
                            case 10:
                                cell.setCellValue("м.р, г.о №");
                                sheet.addMergedRegion(new CellRangeAddress(2, 3, 10, 10));
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
                                sheet.getRow(2).getCell(2).setCellValue(
                                        "Конструктивные и технические параметры многоквартирного дома или жилого дома");
                                break;
                            case 6:
                                cell.setCellValue("холодной");
                                break;
                            case 7:
                                sheet.getRow(2).getCell(6)
                                        .setCellValue("Наличие коллективного (общедомового) прибора учета");
                                cell.setCellValue("горячей");
                                break;
                            case 8:
                                cell.setCellValue("холодной");
                                break;
                            case 9:
                                sheet.getRow(2).getCell(8).setCellValue(period);
                                cell.setCellValue("горячей");
                                break;
                        }
                        break;
                    case 4://пятая строчка
                        cell.setCellValue(j + 1);
                        break;

                }
            }
        }
        addDataToFile(sheet, cellStyle);
        return writeDataToStream(workbook);
    }

    @Override
    protected void addDataToFile(Sheet sheet, CellStyle cellStyle) {
        Logger logger = Logger.getLogger(getClass().getName(), "addWaterDataToArray");
        logger.log(LogCategory.DEBUG, "Adding data to the server xls water file");

        int group = 0;//номер группы
        int number = 0;//проставление номера данных в одной из четырёх групп
        Collections.sort(data);//отсортировать в порядке возрастания групп
        boolean[] regions = EMPTY_REGIONS;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getGroup() != 0) {
                Row row = sheet.createRow((short) (i + 5 + group));
                //по столбцам
                for (int j = 0; j < 11; j++) {
                    val cell = row.createCell(j);
                    cell.setCellStyle(cellStyle);
                    val waterDataModel = data.get(i);
                    if (waterDataModel.getGroup() > group)//если верно - записать в файл название группы
                    {
                        sheet.addMergedRegion(new CellRangeAddress(i + 5 + group, i + 5 + group, 0, 10));
                        group = waterDataModel.getGroup();
                        cell.setCellValue(DataGroupsGetter.getGroup(group, DataType.WATER));
                        number = 1;
                        i--;
                        break;
                    }
                    switch (j)//запись данных
                    {
                        case 0:
                            cell.setCellValue(number);
                            number++;
                            break;
                        case 1:
                            cell.setCellValue(waterDataModel.getAddress());
                            break;
                        case 2:
                            cell.setCellValue(waterDataModel.getBiggestFloor());
                            break;
                        case 3:
                            cell.setCellValue(waterDataModel.getSmallestFloor());
                            break;
                        case 4:
                            cell.setCellValue(waterDataModel.getJoint());
                            break;
                        case 5:
                            cell.setCellValue(waterDataModel.getPeople());
                            break;
                        case 6:
                            cell.setCellValue(waterDataModel.getHasColdWaterAccountingDevice());
                            break;
                        case 7:
                            cell.setCellValue(waterDataModel.getHasHotWaterAccountingDevice());
                            break;
                        case 8:
                            cell.setCellValue(waterDataModel.getExpenseHouseCold());
                            break;
                        case 9:
                            cell.setCellValue(waterDataModel.getExpenseHouseHot());
                            break;
                        case 10:
                            int region = waterDataModel.getRegion();
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
