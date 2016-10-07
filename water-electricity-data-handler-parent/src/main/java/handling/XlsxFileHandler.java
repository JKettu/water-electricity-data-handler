
package handling;

import common.DataType;
import common.logger.LogCategory;
import common.logger.Logger;
import handling.util.DataGroupsGetter;
import model.ElectricityDataModel;
import model.WaterDataModel;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import server.FTPController;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jay on 20.07.2016.
 */
public class XlsxFileHandler {

    private List<ElectricityDataModel> arrayForElectricity = new ArrayList<>();
    private List<WaterDataModel> arrayForWater = new ArrayList<>();
    private boolean[] checkRegion;
    private String firstDate = "";
    private String secondDate = "";
    private String period = "";
    private String serverFileName = "";//имя файла с сервера
    private String serverFilePath = "";//путь к файлу с сервера
    private String firstLine; //заголовок файла
    private XlsFileHandler xlsFileHandler;
    private FTPController ftpController;
    private List<String> errors;

    public XlsxFileHandler() {
        this.checkRegion = new boolean[70];
        errors = new ArrayList<>();
        ftpController = new FTPController();
    }

    public List<String> getErrorsArray() {
        return errors;
    }

    private boolean checkEqualtyOfHeadlines(String firstLine, File file) {
        Logger logger = Logger.getLogger(getClass().getName(), "checkEqualtyOfHeadlines");
        logger.log(LogCategory.DEBUG, "Checking equalty of headlines");
        try {
            Workbook wb = WorkbookFactory.create(file);
            Sheet sheet = wb.getSheetAt(0); //номер листа в файле
            Row firstRow = sheet.getRow(0);
            Cell firstCell = firstRow.getCell(0);
            String s = firstCell.getStringCellValue();
            wb.close();

            if ((s == null) || (s.equals(""))) {
                errors.add("EMPTY_FILE");
                logger.log(LogCategory.INFO, "Local file is empty");
                return false;
            } else {
                logger.log(LogCategory.INFO, "Local file isn't empty");
                if (((firstLine.contains("ВОДОСНАБЖЕНИЮ")) && (s.contains("ВОДОСНАБЖЕНИЮ"))) ||
                        ((firstLine.contains("ЭЛЕКТРОСНАБЖЕНИЮ")) && (s.contains("ЭЛЕКТРОСНАБЖЕНИЮ")))) {
                    logger.log(LogCategory.INFO, "Headlines are equal. Same types of data");
                    return true;
                } else {
                    logger.log(LogCategory.ERROR, "Headlines aren't equal. Different types of data");
                    errors.add("WRONG_TYPE");
                    return false;
                }
            }
        } catch (IOException | InvalidFormatException e) {
            logger.log(LogCategory.ERROR, "IOException/InvalidFormatException. Couldn't compare headlines of files");
            errors.add("FILE_NOT_LOADED");
            return false;
        }
    }

    //чтение из файла с компа в массив
    public boolean WorkWithXlsxFileWater(File filePath) {
        Logger logger = Logger.getLogger(getClass().getName(), "WorkWithXlsxFileWater");
        logger.log(LogCategory.DEBUG, "Reading local xlsx water file");
        try {
            int region = getRegion(filePath); //считывание региона
            if (region == 0) {
                ftpController.sendFile(new FileInputStream(filePath), serverFilePath);
                logger.log(LogCategory.INFO, "Rewriting water server file");
                errors.add(ftpController.getResult().toString());
                return true;
            }
            if (!checkRegion[region - 1]) {//если региона нет
                logger.log(LogCategory.INFO, region + " region hasn't been loaded yet to the server water file");
                XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(filePath));
                Sheet sheet = wb.getSheetAt(0); //номер листа в файле
                String cellCode = "";
                try {
                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                            String arr[] = cellRef.formatAsString().split("\\D");
                            int stringNumber = 0;//номер строки, читаемой из файла
                            for (int i = 1; i < arr.length; i++) {
                                stringNumber = stringNumber +
                                        Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
                            }
                            //считывание периода, за который идёт расчёт
                            if (row.getRowNum() == 2) {
                                if (cell.getColumnIndex() == 8 && (period.equals(""))) {
                                    period = cell.getStringCellValue();
                                }
                            }
                            //считывание строк с данными
                            if (stringNumber > 5) {
                                logger.log(LogCategory.DEBUG, "Parsing local xlsx water file");
                                if (cellRef.formatAsString().matches("B.+")) {
                                    switch (cell.getCellType()) {
                                        case Cell.CELL_TYPE_STRING:
                                            WaterDataModel data = new WaterDataModel();
                                            data.setAddress(cell.getRichStringCellValue().getString());
                                            data.setRegion(region);
                                            arrayForWater.add(data);
                                            break;
                                        case Cell.CELL_TYPE_NUMERIC:
                                            if (DateUtil.isCellDateFormatted(cell)) {
                                                WaterDataModel data1 = new WaterDataModel();
                                                data1.setAddress(null);
                                                data1.setRegion(region);
                                                arrayForWater.add(data1);
                                            } else {
                                                WaterDataModel data2 = new WaterDataModel();
                                                data2.setAddress(null);
                                                data2.setRegion(region);
                                                arrayForWater.add(data2);
                                            }
                                            break;
                                    }
                                } else if (cellRef.formatAsString().matches("C.+")) {
                                    parseWaterBiggestFloor(cell);
                                } else if (cellRef.formatAsString().matches("D.+")) {
                                    parseWaterSmallestFloor(cell);
                                } else if (cellRef.formatAsString().matches("E.+")) {
                                    parseWaterJoint(cell);
                                } else if (cellRef.formatAsString().matches("F.+")) {
                                    parseWaterPeople(cell);
                                } else if (cellRef.formatAsString().matches("G.+")) {
                                    parseColdWaterAccountingDevice(cell);
                                } else if (cellRef.formatAsString().matches("H.+")) {
                                    parseHotWaterAccountingDevice(cell);
                                } else if (cellRef.formatAsString().matches("I.+")) {
                                    parseExpenseHouseCold(cell);
                                } else if (cellRef.formatAsString().matches("J.+")) {
                                    parseExpenseHouseHot(cell);
                                } else if (cellRef.formatAsString().matches("K.+")) {
                                    parseWaterGroup(cell);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(LogCategory.ERROR, "Cell = '" + cellCode + "' + can't be read");
                    errors.add("WRONG_CELL_TYPE");
                    errors.add(cellCode);
                    return false;
                }
                if (serverFileName.matches(".+\\.xls")) {
                    xlsFileHandler = new XlsFileHandler();
                    xlsFileHandler.setArrayForWater(arrayForWater);
                    xlsFileHandler.createServerWaterFile();
                } else {
                    CreatingXlsxFileWater();
                    return true;
                }
                return true;
            } else {
                errors.add("EXISTED_FILE");
                logger.log(LogCategory.ERROR, "This file had been already loaded");
                for (int i = 0; i < checkRegion.length - 1; i++) {
                    checkRegion[i] = false;
                }
                arrayForWater.clear();
                return false;
            }
        } catch (IOException e) {
            errors.add("is null");
            logger.log(LogCategory.ERROR, "IOException/InvalidFormatException. Couldn't read local xlsx water file");
            return false;
        }
    }

    //чтение из файла с компа в массив
    public boolean WorkWithXlsxFileElectricity(File filePath) {
        Logger logger = Logger.getLogger(getClass().getName(), "WorkWithXlsxFileElectricity");
        logger.log(LogCategory.DEBUG, "Reading local xlsx electricity file");
        try {
            int region = getRegion(filePath); //считывание региона
            if (region == 0) {
                ftpController.sendFile(new FileInputStream(filePath), serverFilePath);
                logger.log(LogCategory.INFO, "Rewriting server electricity file");
                errors.add(ftpController.getResult().toString());
                return true;
            }
            if (!checkRegion[region - 1]) {//если региона ещё не было
                logger.log(LogCategory.INFO, region + " region hasn't been loaded yet to the electricity file");
                XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(filePath));
                Sheet sheet = wb.getSheetAt(0); //номер листа в файле
                String cellCode = "";
                try {
                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                            String arr[] = cellRef.formatAsString().split("\\D");
                            int stringNumber = 0; //номер строки, считываемой из файла
                            for (int i = 1; i < arr.length; i++) {
                                stringNumber = stringNumber +
                                        Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
                            }
                            //чтение дат, за которые идёт рассчёт
                            if (row.getRowNum() == 3) {
                                if (cell.getColumnIndex() == 6 && (firstDate.equals(""))) {
                                    firstDate = cell.getStringCellValue();
                                } else if (cell.getColumnIndex() == 7 && (secondDate.equals(""))) {
                                    secondDate = cell.getStringCellValue();
                                }
                            }
                            //чтение данных
                            if (stringNumber > 5) {
                                cellCode = cellRef.formatAsString();
                                logger.log(LogCategory.DEBUG, "Parsing local xls electricity file");
                                if (cellRef.formatAsString().matches("B.+")) {
                                    switch (cell.getCellType()) {
                                        case Cell.CELL_TYPE_STRING:
                                            ElectricityDataModel data = new ElectricityDataModel();
                                            data.setAddress(cell.getRichStringCellValue().getString());
                                            data.setRegion(region);
                                            arrayForElectricity.add(data);
                                            break;
                                        case Cell.CELL_TYPE_NUMERIC:
                                            if (DateUtil.isCellDateFormatted(cell)) {
                                                ElectricityDataModel data1 = new ElectricityDataModel();
                                                data1.setAddress(null);
                                                data1.setRegion(region);
                                                arrayForElectricity.add(data1);
                                            } else {
                                                ElectricityDataModel data2 = new ElectricityDataModel();
                                                data2.setAddress(null);
                                                data2.setRegion(region);
                                                arrayForElectricity.add(data2);
                                            }
                                            break;
                                    }
                                } else if (cellRef.formatAsString().matches("C.+")) {
                                    parseElectricityBiggestFloor(cell);
                                } else if (cellRef.formatAsString().matches("D.+")) {
                                    parseElectricitySmallestFloor(cell);
                                } else if (cellRef.formatAsString().matches("E.+")) {
                                    parseElectricityJoint(cell);
                                } else if (cellRef.formatAsString().matches("F.+")) {
                                    parseElectricityAccountingDevice(cell);
                                } else if (cellRef.formatAsString().matches("G.+")) {
                                    parseExpenseHouseFirstMonth(cell);
                                } else if (cellRef.formatAsString().matches("H.+")) {
                                    parseExpenseHouseSecondMonth(cell);
                                } else if (cellRef.formatAsString().matches("I.+")) {
                                    parseExpenseNotLivingFirstMonth(cell);
                                } else if (cellRef.formatAsString().matches("J.+")) {
                                    parseExpenseNotLivingSecondMonth(cell);
                                } else if (cellRef.formatAsString().matches("K.+")) {
                                    parseExpenseIndividFirstMonth(cell);
                                } else if (cellRef.formatAsString().matches("L.+")) {
                                    parseExpenseIndividSecondMonth(cell);
                                } else if (cellRef.formatAsString().matches("M.+")) {
                                    parseElectricityGroup(cell);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(LogCategory.ERROR, "Cell = '" + cellCode + "' + can't be read");
                    errors.add("WRONG_CELL_TYPE");
                    errors.add(cellCode);
                    return false;
                }
                //конец чтения файла
                if (serverFileName.matches(".+\\.xls")) {
                    xlsFileHandler = new XlsFileHandler();
                    xlsFileHandler.setArrayForElectricity(arrayForElectricity);
                    xlsFileHandler.setFirstDate(firstDate);
                    xlsFileHandler.setSecondDate(secondDate);
                    xlsFileHandler.createServerElectricityFile();
                } else {
                    CreatingXlsxFileElectricity();
                    return true;
                }
                return true;
            } else {
                errors.add("EXISTED_FILE");
                logger.log(LogCategory.ERROR, "This file had been already loaded");
                for (int i = 0; i < checkRegion.length - 1; i++) {
                    checkRegion[i] = false;
                }
                arrayForElectricity.clear();
                return false;
            }
        } catch (IOException e) {
            errors.add("is null");
            logger.log(LogCategory.ERROR,
                    "IOException/InvalidFormatException. Couldn't read local xlsx electricity file");
            return false;
        }
    }

    //чтение из файла по электроэнергии с сервера в массив
    public boolean ReadFromServerXlsxFileElectricity(String fileFromServerPath, File filePath) {
        Logger logger = Logger.getLogger(getClass().getName(), "ReadFromServerXlsxFileElectricity");
        logger.log(LogCategory.DEBUG, "Reading server electricity file");
        try {
            if (fileFromServerPath != null) {
                InputStream inputStream = ftpController.getInputFileStream(fileFromServerPath);
                XSSFWorkbook wb = new XSSFWorkbook(inputStream);
                Sheet sheet = wb.getSheetAt(0); //номер листа в файле
                Row firstRow = sheet.getRow(0);
                Cell firstCell = firstRow.getCell(0);
                firstLine = firstCell.getStringCellValue();
                boolean checkHeadsOfFiles = checkEqualtyOfHeadlines(firstLine, filePath);

                if (checkHeadsOfFiles) {
                    logger.log(LogCategory.INFO, "Parsing server electricity file");
                    ReadSecondPageFromServerFile(wb);
                    int group = 0;
                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                            String arr[] = cellRef.formatAsString().split("\\D");
                            int stringNumber = 0;//номер строки, считываемой из файла
                            for (int i = 1; i < arr.length; i++) {
                                stringNumber = stringNumber +
                                        Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
                            }

                            //чтение данных
                            if (stringNumber > 5) {
                                if (cellRef.formatAsString().matches("A.+")) {
                                    if (cell.getCellType() == 1) {
                                        if (cell.getRichStringCellValue().getString().matches("\\d\\..+")) {
                                            group++;
                                        }
                                    }
                                } else if (cellRef.formatAsString().matches("B.+")) {
                                    addNewElectricityDataModel(group, cell);
                                } else if (cellRef.formatAsString().matches("C.+")) {
                                    parseElectricityBiggestFloor(cell);
                                } else if (cellRef.formatAsString().matches("D.+")) {
                                    parseElectricitySmallestFloor(cell);
                                } else if (cellRef.formatAsString().matches("E.+")) {
                                    parseElectricityJoint(cell);
                                } else if (cellRef.formatAsString().matches("F.+")) {
                                    parseElectricityAccountingDevice(cell);
                                } else if (cellRef.formatAsString().matches("G.+")) {
                                    parseExpenseHouseFirstMonth(cell);
                                } else if (cellRef.formatAsString().matches("H.+")) {
                                    parseExpenseHouseSecondMonth(cell);
                                } else if (cellRef.formatAsString().matches("I.+")) {
                                    parseExpenseNotLivingFirstMonth(cell);
                                } else if (cellRef.formatAsString().matches("J.+")) {
                                    parseExpenseNotLivingSecondMonth(cell);
                                } else if (cellRef.formatAsString().matches("K.+")) {
                                    parseExpenseIndividFirstMonth(cell);
                                } else if (cellRef.formatAsString().matches("L.+")) {
                                    parseExpenseIndividSecondMonth(cell);
                                } else if (cellRef.formatAsString().matches("M.+")) {
                                    parseElectricityRegion(cell);
                                }
                            }
                        }
                    }
                    //конец чтения данных с сервера в массив и закрытие
                    wb.close();
                    if (filePath.getAbsolutePath().matches(".+\\.xls")) {
                        xlsFileHandler = new XlsFileHandler();
                        if (xlsFileHandler
                                .setWorkWithElectricityFileFromXlsx(firstDate, secondDate,
                                        arrayForElectricity)) {
                            errors.add(ftpController.getResult().toString());
                            return true;
                        } else {
                            errors.add(ftpController.getResult().toString());
                            return false;
                        }
                    } //чтение данных из файла с компьютера в массив
                    else {
                        return WorkWithXlsxFileElectricity(filePath);
                    }
                } else {
                    return false;
                }
            } else {
                errors.add("DISCONNECTED");
                for (int i = 0; i < checkRegion.length - 1; i++) {
                    checkRegion[i] = false;
                }
                arrayForElectricity.clear();
                logger.log(LogCategory.ERROR, "Connection failed");
                return false;
            }
        } catch (IOException e) {
            errors.add(ftpController.getResult().toString());
            logger.log(LogCategory.ERROR, "IOException. Couldn't parse server electricity file");
            return false;
        }
    }

    //чтение из файла по воде с сервера в массив
    public boolean ReadFromServerXlsxFileWater(String fileFromServerPath, File filePath) {
        Logger logger = Logger.getLogger(getClass().getName(), "ReadFromServerXlsxFileWater");
        logger.log(LogCategory.DEBUG, "Reading server water file");
        try {
            if (fileFromServerPath != null) {
                InputStream inputStream = ftpController.getInputFileStream(fileFromServerPath);
                XSSFWorkbook wb = new XSSFWorkbook(inputStream);
                ReadSecondPageFromServerFile(wb);
                int group = 0;
                Sheet sheet = wb.getSheetAt(0); //номер листа в файле

                Row firstRow = sheet.getRow(0);
                Cell firstCell = firstRow.getCell(0);
                firstLine = firstCell.getStringCellValue();
                boolean checkHeadsOfFiles = checkEqualtyOfHeadlines(firstLine, filePath);
                if (checkHeadsOfFiles) {
                    logger.log(LogCategory.INFO, "Parsing server water file");
                    for (Row row : sheet) {
                        for (Cell cell : row) {
                            CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                            String arr[] = cellRef.formatAsString().split("\\D");
                            int stringNumber = 0;//номер строки, читаемой из файла
                            for (int i = 1; i < arr.length; i++) {
                                stringNumber = stringNumber +
                                        Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
                            }

                            //считывание строк с данными
                            if (stringNumber > 5) {
                                if (cellRef.formatAsString().matches("A.+")) {
                                    if (cell.getCellType() == 1) {
                                        if (cell.getRichStringCellValue().getString().matches("\\d\\..+")) {
                                            group++;
                                        }
                                    }
                                } else if (cellRef.formatAsString().matches("B.+")) {
                                    addNewWaterDataModel(group, cell);
                                } else if (cellRef.formatAsString().matches("C.+")) {
                                    parseWaterBiggestFloor(cell);
                                } else if (cellRef.formatAsString().matches("D.+")) {
                                    parseWaterSmallestFloor(cell);
                                } else if (cellRef.formatAsString().matches("E.+")) {
                                    parseWaterJoint(cell);
                                } else if (cellRef.formatAsString().matches("F.+")) {
                                    parseWaterPeople(cell);
                                } else if (cellRef.formatAsString().matches("G.+")) {
                                    parseColdWaterAccountingDevice(cell);
                                } else if (cellRef.formatAsString().matches("H.+")) {
                                    parseHotWaterAccountingDevice(cell);
                                } else if (cellRef.formatAsString().matches("I.+")) {
                                    parseExpenseHouseCold(cell);
                                } else if (cellRef.formatAsString().matches("J.+")) {
                                    parseExpenseHouseHot(cell);
                                } else if (cellRef.formatAsString().matches("K.+")) {
                                    parseWaterRegion(cell);
                                }
                            }
                        }
                    }
                    //конец чтения данных с сервера в массив и закрытие
                    wb.close();

                    if (filePath.getAbsolutePath().matches(".+\\.xls")) {
                        xlsFileHandler = new XlsFileHandler();
                        if (xlsFileHandler.setWorkWithWaterFileFromXlsx(arrayForWater)) {
                            errors.add(ftpController.getResult().toString());
                            return true;
                        } else {
                            errors.add(ftpController.getResult().toString());
                            return false;
                        }
                    } //чтение данных из файла с компьютера в массив
                    else {
                        return WorkWithXlsxFileWater(filePath);
                    }
                } else {
                    return false;
                }
            } else {
                errors.add("DISCONNECTED");
                for (int i = 0; i < checkRegion.length - 1; i++) {
                    checkRegion[i] = false;
                }
                arrayForWater.clear();
                logger.log(LogCategory.ERROR, "Connection failed");
                return false;
            }
        } catch (IOException e) {
            errors.add(ftpController.getResult().toString());
            logger.log(LogCategory.ERROR, "IOException. Couldn't parse server water file");
            return false;
        }
    }

    //чтение страницы с отмеченными регионами
    private void ReadSecondPageFromServerFile(Workbook wb) {
        Sheet secondPage = wb.getSheetAt(1);
        for (int i = 0; i < 14; i++) {
            Row row = secondPage.getRow(i);
            if (i % 2 != 0) {
                for (int j = 0; j < 10; j++) {
                    Cell cell = row.getCell(j);
                    if (cell.getBooleanCellValue()) {
                        checkRegion[(i - 1) * 5 + j] = true;
                    }
                }
            }
        }
    }

    boolean setWorkWithElectricityFileFromXls(String name, String firstDate, String secondDate, File filePath,
            List<ElectricityDataModel> arrayForElectricity) {
        this.serverFileName = name;
        this.arrayForElectricity = arrayForElectricity;
        this.firstDate = firstDate;
        this.secondDate = secondDate;
        return WorkWithXlsxFileElectricity(filePath);
    }

    boolean setWorkWithWaterFileFromXls(String name, File filePath, List<WaterDataModel> arrayForWater) {
        this.serverFileName = name;
        this.arrayForWater = arrayForWater;
        return WorkWithXlsxFileElectricity(filePath);
    }

    //создание файла по электроэнергии
    public void CreatingXlsxFileElectricity() throws IOException {
        Logger logger = Logger.getLogger(getClass().getName(), "CreatingXlsxFileElectricity");
        logger.log(LogCategory.DEBUG, "Creating xlsx electricity file");
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Электроэнергия");

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
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_JUSTIFY);
        cellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);

        //ширина строк
        for (int i = 0; i < 6; i++) {
            Row row = sheet.createRow((short) i);
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
                Cell cell = row.createCell(j);
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
                            case 1:
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
        AddingXlsxDataElectricity(sheet, cellStyle);
        //запись в файл
        FileOutputStream fileOut = new FileOutputStream(serverFileName);
        wb.write(fileOut);
        fileOut.close();
        File fileForSend = new File(serverFileName);
        ftpController.sendFile(new FileInputStream(fileForSend), serverFileName);
        fileForSend.delete();
        errors.add(ftpController.getResult().toString());
    }

    //создание файла по воде
    public void CreatingXlsxFileWater() throws IOException {
        Logger logger = Logger.getLogger(getClass().getName(), "CreatingXlsxFileWater");
        logger.log(LogCategory.DEBUG, "Creating xlsx water file");
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Водоснабжение");
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
        CellStyle cellStyle = wb.createCellStyle();
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
                                        "Количество этажей в многоквартирных домах (включая разноуровневые), в отношении которых определяется норматив, ед");
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
        //добавление данных в файл
        AddingXlsxDataWater(sheet, cellStyle);
        //запись в файл
        FileOutputStream fileOut = new FileOutputStream(serverFileName);
        wb.write(fileOut);
        fileOut.close();
        File fileForSend = new File(serverFileName);
        ftpController.sendFile(new FileInputStream(fileForSend), serverFilePath);
        fileForSend.delete();
        errors.add(ftpController.getResult().toString());
    }

    //создание второй страницы с добавленными регионами
    private void creatingPageWithRegions(Workbook wb) {
        Sheet sheet = wb.createSheet("м.р, г.о");
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
                    cell.setCellValue(checkRegion[(i - 1) * 5 + j]);
                }
            }
        }
    }

    //заполнение данных из массива в файл
    private void AddingXlsxDataElectricity(Sheet sheet, CellStyle cellStyle) {
        Logger logger = Logger.getLogger(getClass().getName(), "AddingXlsxDataElectricity");
        logger.log(LogCategory.DEBUG, "Adding data to the xlsx electricity file");
        int group = 0;//номер группы
        int number = 0;//проставление номера данных в одной из четырёх групп
        Collections.sort(arrayForElectricity);//отсортировать в порядке возрастания групп

        for (int i = 0; i < arrayForElectricity.size(); i++) {
            Row row = sheet.createRow((short) (i + 5 + group));
            //по столбцам
            for (int j = 0; j < 13; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                if (arrayForElectricity.get(i).getGroup() == group + 1)//если верно - записать в файл название группы
                {
                    sheet.addMergedRegion(new CellRangeAddress(i + 5 + group, i + 5 + group, 0, 12));
                    group++;
                    cell.setCellValue(DataGroupsGetter.getGroup(group, DataType.ELECTRICITY));
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
                        cell.setCellValue(arrayForElectricity.get(i).getAddress());
                        break;
                    case 2:
                        cell.setCellValue(arrayForElectricity.get(i).getBiggestFloor());
                        break;
                    case 3:
                        cell.setCellValue(arrayForElectricity.get(i).getSmallestFloor());
                        break;
                    case 4:
                        cell.setCellValue(arrayForElectricity.get(i).getJoint());
                        break;
                    case 5:
                        cell.setCellValue(arrayForElectricity.get(i).getAccountingDevice());
                        break;
                    case 6:
                        cell.setCellValue(arrayForElectricity.get(i).getExpenseHouseFirstMonth());
                        break;
                    case 7:
                        cell.setCellValue(arrayForElectricity.get(i).getExpenseHouseSecondMonth());
                        break;
                    case 8:
                        cell.setCellValue(arrayForElectricity.get(i).getExpenseNotLivingFirstMonth());
                        break;
                    case 9:
                        cell.setCellValue(arrayForElectricity.get(i).getExpenseNotLivingSecondMonth());
                        break;
                    case 10:
                        cell.setCellValue(arrayForElectricity.get(i).getExpenseIndividFirstMonth());
                        break;
                    case 11:
                        cell.setCellValue(arrayForElectricity.get(i).getExpenseIndividSecondMonth());
                        break;
                    case 12:
                        cell.setCellValue(arrayForElectricity.get(i).getRegion());
                        checkRegion[arrayForElectricity.get(i).getRegion() - 1] = true;
                        break;
                }
            }
        }

        arrayForElectricity.clear();//очистить массив на случай, если будут добаляться ещё файлы за один раз
        creatingPageWithRegions(sheet.getWorkbook());
        for (int i = 0; i < checkRegion.length - 1; i++) {
            checkRegion[i] = false;
        }
    }

    //заполнение данных из массива в файл
    public void AddingXlsxDataWater(Sheet sheet, CellStyle cellStyle) {
        Logger logger = Logger.getLogger(getClass().getName(), "AddingXlsxDataWater");
        logger.log(LogCategory.DEBUG, "Adding data to the xlsx water file");
        int group = 0;//номер группы
        int number = 0;//проставление номера данных в одной из четырёх групп
        Collections.sort(arrayForWater);//отсортировать в порядке возрастания групп
        for (int i = 0; i < arrayForWater.size(); i++) {
            Row row = sheet.createRow((short) (i + 5 + group));
            //по столбцам
            for (int j = 0; j < 11; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                if (arrayForWater.get(i).getGroup() == group + 1)//если верно - записать в файл название группы
                {
                    sheet.addMergedRegion(new CellRangeAddress(i + 5 + group, i + 5 + group, 0, 10));
                    group++;
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
                        cell.setCellValue(arrayForWater.get(i).getAddress());
                        break;
                    case 2:
                        cell.setCellValue(arrayForWater.get(i).getBiggestFloor());
                        break;
                    case 3:
                        cell.setCellValue(arrayForWater.get(i).getSmallestFloor());
                        break;
                    case 4:
                        cell.setCellValue(arrayForWater.get(i).getJoint());
                        break;
                    case 5:
                        cell.setCellValue(arrayForWater.get(i).getPeople());
                        break;
                    case 6:
                        cell.setCellValue(arrayForWater.get(i).getColdWaterAccountingDevice());
                        break;
                    case 7:
                        cell.setCellValue(arrayForWater.get(i).getHotWaterAccountingDevice());
                        break;
                    case 8:
                        cell.setCellValue(arrayForWater.get(i).getExpenseHouseCold());
                        break;
                    case 9:
                        cell.setCellValue(arrayForWater.get(i).getExpenseHouseHot());
                        break;
                    case 10:
                        cell.setCellValue(arrayForWater.get(i).getRegion());
                        checkRegion[arrayForWater.get(i).getRegion() - 1] = true;
                        break;
                }
            }
        }
        arrayForWater.clear();//очистить массив на случай, если будут добаляться ещё файлы за один раз
        creatingPageWithRegions(sheet.getWorkbook());
        for (int i = 0; i < checkRegion.length - 1; i++) {
            checkRegion[i] = false;
        }
    }

    public void setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
    }

    public void setServerFilePath(String serverFilePath) {
        this.serverFilePath = serverFilePath;
    }

    public String getServerFilePath() {
        return this.serverFilePath;
    }

    //считывание региона
    private int getRegion(File filePath) {
        int region = 0;
        if (filePath.getAbsolutePath().matches(".+-\\d+\\.xls")) {
            String[] arr = filePath.getAbsolutePath().split("\\D+|\\s");
            for (int i = 1; i < arr.length; i++) {
                region = region + Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
            }
        }
        return region;
    }

    private void addNewElectricityDataModel(int group, Cell cell) {
        ElectricityDataModel model = new ElectricityDataModel();

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                model.setAddress(cell.getRichStringCellValue().getString());
                model.setGroup(group);
                break;
            case Cell.CELL_TYPE_NUMERIC:
                model.setAddress(null);
                break;
        }
        arrayForElectricity.add(model);

    }

    private void parseElectricityRegion(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                arrayForElectricity.get(arrayForElectricity.size() - 1)
                        .setRegion(Integer.valueOf(cell.getStringCellValue()));
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setRegion((int) cell.getNumericCellValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setRegion((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseElectricityAccountingDevice(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                arrayForElectricity.get(arrayForElectricity.size() - 1).setAccountingDevice(cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setAccountingDevice(null);
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setAccountingDevice(null);
                }
                break;
        }
    }

    private void parseExpenseIndividFirstMonth(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseIndividFirstMonth(
                        new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP)
                                .doubleValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseIndividFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseIndividFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseIndividSecondMonth(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseIndividSecondMonth(
                        new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP)
                                .doubleValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseIndividSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseIndividSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseElectricityBiggestFloor(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                arrayForElectricity.get(arrayForElectricity.size() - 1)
                        .setBiggestFloor(Integer.valueOf(cell.getStringCellValue()));
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1)
                            .setBiggestFloor((int) cell.getNumericCellValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1)
                            .setBiggestFloor((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseElectricitySmallestFloor(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                arrayForElectricity.get(arrayForElectricity.size() - 1)
                        .setSmallestFloor(Integer.valueOf(cell.getStringCellValue()));
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1)
                            .setSmallestFloor((int) cell.getNumericCellValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1)
                            .setSmallestFloor((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseExpenseHouseFirstMonth(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseHouseFirstMonth(0);
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseHouseFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseHouseFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseHouseFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseHouseSecondMonth(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseHouseSecondMonth(0);
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseHouseSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseHouseSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseHouseSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseNotLivingFirstMonth(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseNotLivingFirstMonth(0);
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseNotLivingFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseNotLivingFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseNotLivingFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseNotLivingSecondMonth(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseNotLivingSecondMonth(0);
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseNotLivingSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseNotLivingSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setExpenseNotLivingSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseElectricityJoint(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setJoint(0);
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseElectricityGroup(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setGroup(0);
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setGroup((int) cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setGroup((int) cell.getNumericCellValue());
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setGroup((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void addNewWaterDataModel(int group, Cell cell) {
        WaterDataModel model = new WaterDataModel();

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                model.setAddress(cell.getRichStringCellValue().getString());
                model.setGroup(group);
                break;
            case Cell.CELL_TYPE_NUMERIC:
                model.setAddress(null);
                break;
        }
        if (!model.getAddress().equals("")) {
            arrayForWater.add(model);
        }

    }

    private void parseWaterBiggestFloor(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1)
                            .setBiggestFloor(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setBiggestFloor((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseWaterSmallestFloor(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1)
                            .setSmallestFloor(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setSmallestFloor((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseWaterJoint(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1).setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseWaterGroup(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1).setGroup(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setGroup((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseWaterRegion(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1).setRegion(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setRegion((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseWaterPeople(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1).setPeople(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setPeople((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseColdWaterAccountingDevice(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1).setColdWaterAccountingDevice(cell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setColdWaterAccountingDevice(null);
                }
                break;
        }
    }

    private void parseHotWaterAccountingDevice(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1).setHotWaterAccountingDevice(cell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setHotWaterAccountingDevice(null);
                }
                break;
        }
    }

    private void parseExpenseHouseCold(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1).setExpenseHouseCold(
                            new BigDecimal(Double.valueOf(cell.getStringCellValue())).setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setExpenseHouseCold(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseHouseHot(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1).setExpenseHouseHot(
                            new BigDecimal(Double.valueOf(cell.getStringCellValue())).setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setExpenseHouseHot(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    void setSecondDate(String secondDate) {
        this.secondDate = secondDate;
    }

    void setFirstDate(String firstDate) {
        this.firstDate = firstDate;
    }

    void setArrayForElectricity(List<ElectricityDataModel> arrayForElectricity) {
        this.arrayForElectricity = arrayForElectricity;
    }

    void setArrayForWater(List<WaterDataModel> arrayForWater) {
        this.arrayForWater = arrayForWater;
    }
}
