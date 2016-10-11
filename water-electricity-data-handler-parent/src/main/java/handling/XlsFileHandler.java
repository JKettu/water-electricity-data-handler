package handling;

import common.DataType;
import common.logger.LogCategory;
import common.logger.Logger;
import handling.util.DataGroupsGetter;
import handling.util.HandlingType;
import model.ElectricityDataModel;
import model.WaterDataModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import server.ClientService;
import server.FTPController;
import server.LockFile;
import server.LockMonitor;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class XlsFileHandler {

    private int regionToDelete;
    private File localFile;
    private String serverFileName = "";//имя файла с сервера
    private List<ElectricityDataModel> arrayForElectricity = new ArrayList<>();
    private List<WaterDataModel> arrayForWater = new ArrayList<>();
    private boolean[] existedRegions;
    private String firstDate = "";
    private String secondDate = "";
    private String period = "";
    private String firstLine; //заголовок файла
    private XlsxFileHandler xlsxFileHandler;
    private FTPController ftpController;
    private List<String> errors;

    XlsFileHandler() {
        this.existedRegions = new boolean[70];
        ftpController = new FTPController();
        errors = new ArrayList<>();
    }

    public XlsFileHandler(File localFile, String serverFileName) {
        this();
        this.localFile = localFile;
        this.serverFileName = serverFileName;

    }

    public XlsFileHandler(int region, String serverFileName) {
        this();
        this.regionToDelete = region;
        this.serverFileName = serverFileName;

    }

    public XlsFileHandler(String serverFileName) {
        this();
        this.serverFileName = serverFileName;
    }


    public boolean processWaterFileHandling(HandlingType handlingType) {
        Logger logger = Logger.getLogger(getClass().toString(), "processWaterFileHandling");
        try {
            int threadSleepDelay = ClientService.CLIENT_ID * LockMonitor.LAST_LOG_CHECK_DELAY_MULTIPLIER;
            logger.log(LogCategory.DEBUG, "Waiting for: '" + threadSleepDelay + "'");
            Thread.sleep(threadSleepDelay + ThreadLocalRandom.current().nextInt(5000, 10000));
            synchronized (LockMonitor.lock) {
                LockMonitor.lock.wait();
            }
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during waiting: " + e);
        }
        LockFile lastClientLock = LockMonitor.getLockMonitor().getLastClientLock(serverFileName);
        LockFile lock;
        if (lastClientLock == null) {
            lock = new LockFile(serverFileName, 0);
        } else {
            lock = new LockFile(serverFileName, lastClientLock.getLockClientIndex() + 1);
        }
        ftpController.lockFile(lock);
        while (ftpController.updateLock(lock)) {
        }
        boolean result = false;
        switch (handlingType) {
            case CREATE:
                result = workWithWaterFile();
                break;
            case MODIFY:
                result = readServerWaterFile();
                break;
            case DELETE_REGION:
                result = deleteWaterRegion();
                break;
        }
        ftpController.deleteLock(lock);
        return result;
    }

    public boolean processElectricityFileHandling(HandlingType handlingType) {
        Logger logger = Logger.getLogger(getClass().toString(), "processElectricityFileHandling");
        try {
            int threadSleepDelay = ClientService.CLIENT_ID * LockMonitor.LAST_LOG_CHECK_DELAY_MULTIPLIER;
            logger.log(LogCategory.DEBUG, "Waiting for: " + "'" + threadSleepDelay + "'");
            Thread.sleep(threadSleepDelay + ThreadLocalRandom.current().nextInt(5000, 10000));
            synchronized (LockMonitor.lock) {
                LockMonitor.lock.wait();
            }
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during waiting: " + e);
        }
        LockFile lastClientLock = LockMonitor.getLockMonitor().getLastClientLock(serverFileName);
        LockFile lock;
        if (lastClientLock == null) {
            lock = new LockFile(serverFileName, 0);
        } else {
            lock = new LockFile(serverFileName, lastClientLock.getLockClientIndex() + 1);
        }
        ftpController.lockFile(lock);
        while (ftpController.updateLock(lock)) {
        }

        boolean result = false;
        switch (handlingType) {
            case CREATE:
                result = workWithElectricityFile();
                break;
            case MODIFY:
                result = readServerElectricityFile();
                break;
            case DELETE_REGION:
                result = deleteElectricityRegion();
                break;
        }
        ftpController.deleteLock(lock);
        return result;
    }
    //чтение страницы с отмеченными регионами

    public List<Integer> getServerFileRegions() {
        Logger logger = Logger.getLogger(getClass().toString(), "readRegionsPageFromServerFile");
        try {
            int threadSleepDelay = ClientService.CLIENT_ID * LockMonitor.LAST_LOG_CHECK_DELAY_MULTIPLIER;
            logger.log(LogCategory.DEBUG, "Waiting for: " + "'" + threadSleepDelay + "'");
            Thread.sleep(threadSleepDelay + ThreadLocalRandom.current().nextInt(5000, 10000));
            synchronized (LockMonitor.lock) {
                LockMonitor.lock.wait();
            }
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during waiting: " + e);
        }
        LockFile lastClientLock = LockMonitor.getLockMonitor().getLastClientLock(serverFileName);
        LockFile lock;
        if (lastClientLock == null) {
            lock = new LockFile(serverFileName, 0);
        } else {
            lock = new LockFile(serverFileName, lastClientLock.getLockClientIndex() + 1);
        }
        ftpController.lockFile(lock);
        while (ftpController.updateLock(lock)) {
        }
        List<Integer> regions = new ArrayList<>();
        FTPController ftpController = new FTPController();
        Workbook wb = null;
        try {
            InputStream inputFileStream = ftpController.getInputFileStream(serverFileName);
            wb = WorkbookFactory.create(inputFileStream);
            regions = readRegionsFromSecondPage(wb);
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during regions reading: " + e);
            try {
                if (wb != null) {
                    wb.close();
                }
            } catch (Exception wbCloseE) {
                logger.log(LogCategory.ERROR, "Error during closing wb after regions reading error: " + wbCloseE);
            }
            ftpController.deleteLock(lock);
        }
        ftpController.deleteLock(lock);
        return regions;
    }


    private boolean deleteWaterRegion() {
        Logger logger = Logger.getLogger(getClass().getName(), "deleteWaterRegion");
        logger.log(LogCategory.DEBUG, "Deleting region = '" + regionToDelete + "' from water file");
        try {
            InputStream inputStream = ftpController.getInputFileStream(serverFileName);
            Workbook wb = WorkbookFactory.create(inputStream);
            int group = 0;
            Sheet sheet = wb.getSheetAt(0); //номер листа в файле
            for (Row row : sheet) {
                for (int j = 10; j > -1; j--) {
                    int currentRegion;
                    Cell cell = row.getCell(j);
                    if (cell == null) {
                        continue;
                    }
                    CellReference cellRef = new CellReference(row.getRowNum(), j);
                    String arr[] = cellRef.formatAsString().split("\\D");
                    int stringNumber = 0;//номер строки, читаемой из файла
                    for (int i = 1; i < arr.length; i++) {
                        stringNumber = stringNumber +
                                Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
                    }

                    //считывание строк с данными
                    if (stringNumber > 5) {
                        if (cellRef.formatAsString().matches("A.+")) {
                            if (row.getCell(j).getCellType() == 1) {
                                if (row.getCell(j).getRichStringCellValue().getString().matches("\\d\\..+")) {
                                    group = Character.getNumericValue(
                                            row.getCell(j).getRichStringCellValue().getString().charAt(0));
                                }
                            }
                        } else if (cellRef.formatAsString().matches("B.+")) {
                            parseAddressWater(group, cell);
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
                            currentRegion = getRegionToDelete(cell);
                            if (currentRegion == regionToDelete) {
                                break;
                            } else {
                                addNewWaterDataModelForDelete(currentRegion, row.getCell(j));
                            }
                        }
                    }
                }
            }
            //конец чтения данных с сервера в массив и закрытие
            wb.close();
            if (!createServerWaterFile()) {
                return false;
            }
            errors.clear();
            errors.add("DELETION_SUCCESSED");
            return true;
        } catch (Exception e) {
            errors.add("FAILED_REGION_DELETION");
            logger.log(LogCategory.ERROR, "IOException/InvalidFormatException. Couldn't parse server water file");
            return false;
        }
    }

    private boolean deleteElectricityRegion() {
        Logger logger = Logger.getLogger(getClass().getName(), "deleteElectricityRegion");
        logger.log(LogCategory.DEBUG, "Deleting region = '" + regionToDelete + "' from electricity file");
        try {
            InputStream inputStream = ftpController.getInputFileStream(serverFileName);
            Workbook wb = WorkbookFactory.create(inputStream);
            int group = 0;
            Sheet sheet = wb.getSheetAt(0); //номер листа в файле
            for (Row row : sheet) {
                for (int j = 12; j > -1; j--) {
                    int currentRegion = 0;
                    Cell cell = row.getCell(j);
                    if (cell == null) {
                        continue;
                    }
                    CellReference cellRef = new CellReference(row.getRowNum(), j);
                    String arr[] = cellRef.formatAsString().split("\\D");
                    int stringNumber = 0;//номер строки, читаемой из файла
                    for (int i = 1; i < arr.length; i++) {
                        stringNumber = stringNumber +
                                Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
                    }

                    //считывание строк с данными
                    if (stringNumber > 5) {
                        if (cellRef.formatAsString().matches("A.+")) {
                            if (row.getCell(j).getCellType() == 1) {
                                if (row.getCell(j).getRichStringCellValue().getString().matches("\\d\\..+")) {
                                    group = Character.getNumericValue(
                                            row.getCell(j).getRichStringCellValue().getString().charAt(0));
                                }
                            }
                        } else if (cellRef.formatAsString().matches("B.+")) {
                            parseAddressElectricity(group, cell);
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
                            currentRegion = getRegionToDelete(cell);
                            if (currentRegion == regionToDelete) {
                                break;
                            } else {
                                addNewElectricityDataModelForDelete(currentRegion, row.getCell(j));
                            }
                        }
                    }
                }
            }
            //конец чтения данных с сервера в массив и закрытие
            wb.close();
            if (!createServerElectricityFile()) {
                return false;
            }
            errors.clear();
            errors.add("DELETION_SUCCESSED");
            return true;
        } catch (IOException | InvalidFormatException e) {
            errors.add("FAILED_REGION_DELETION");
            logger.log(LogCategory.ERROR, "IOException/InvalidFormatException. Couldn't parse server water file");
            return false;
        }
    }

    //чтение из файла с компа в массив
    private boolean workWithWaterFile() {
        Logger logger = Logger.getLogger(getClass().getName(), "readLocalWaterFile");
        logger.log(LogCategory.DEBUG, "Reading local xls water file");
        try {
            int region = getRegion(localFile); //считывание региона
            if (!existedRegions[region - 1]) {//если региона нет
                logger.log(LogCategory.INFO, region + " region hasn't been loaded yet to the server water file");
                Workbook wb = WorkbookFactory.create(localFile);
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
                                cellCode = cellRef.formatAsString();
                                logger.log(LogCategory.DEBUG,
                                        "Parsing local xls water file. Cell address = '" + cellCode + "'");
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
                                    if (arrayForWater.get(arrayForWater.size() - 1).getGroup() == 0) {
                                        arrayForWater.remove(arrayForWater.size() - 1);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(LogCategory.ERROR, "Cell = '" + cellCode + "' can't be read");
                    errors.add("WRONG_CELL_TYPE");
                    errors.add(cellCode);
                    return false;
                }
                if (serverFileName.matches(".+\\.xlsx")) {
                    xlsxFileHandler = new XlsxFileHandler();
                    xlsxFileHandler.setArrayForWater(arrayForWater);
                    xlsxFileHandler.setServerFileName(serverFileName);
                    xlsxFileHandler.CreatingXlsxFileWater();
                } else {
                    return createServerWaterFile();

                }
                return true;
            } else {
                errors.add("EXISTED_FILE");
                logger.log(LogCategory.ERROR, "This file had been already loaded");
                for (int i = 0; i < existedRegions.length - 1; i++) {
                    existedRegions[i] = false;
                }
                arrayForWater.clear();
                return false;
            }
        } catch (IOException | InvalidFormatException e) {
            errors.add("is null");
            logger.log(LogCategory.ERROR,
                    "IOException/InvalidFormatException. Couldn't read and send local xls water file");
            return false;
        }
    }

    //чтение из файла с компа в массив
    private boolean workWithElectricityFile() {
        Logger logger = Logger.getLogger(getClass().getName(), "readLocalElectricityFile");
        logger.log(LogCategory.DEBUG, "Reading local xls electricity file");
        try {
            int region = getRegion(localFile); //считывание региона

            if (!existedRegions[region - 1]) {//если региона ещё не было
                logger.log(LogCategory.INFO, region + " region hasn't been loaded yet to the electricity file");
                Workbook wb = WorkbookFactory.create(localFile);
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
                                logger.log(LogCategory.DEBUG,
                                        "Parsing local xls electricity file. Cell address = '" + cellCode + "'");
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
                                    if (arrayForElectricity.get(arrayForElectricity.size() - 1).getGroup() == 0) {
                                        arrayForElectricity.remove(arrayForElectricity.size() - 1);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(LogCategory.ERROR, "Cell = '" + cellCode + "' can't be read");
                    errors.add("WRONG_CELL_TYPE");
                    errors.add(cellCode);
                    return false;
                }
                if (serverFileName.matches(".+\\.xlsx")) {
                    xlsxFileHandler = new XlsxFileHandler();
                    xlsxFileHandler.setArrayForElectricity(arrayForElectricity);
                    xlsxFileHandler.setFirstDate(firstDate);
                    xlsxFileHandler.setSecondDate(secondDate);
                    xlsxFileHandler.setServerFileName(serverFileName);
                    xlsxFileHandler.CreatingXlsxFileElectricity();
                }
                //конец чтения файла
                else {
                    return createServerElectricityFile();
                }
                return true;
            } else {
                errors.add("EXISTED_FILE");
                logger.log(LogCategory.ERROR, "This file had been already loaded");
                for (int i = 0; i < existedRegions.length - 1; i++) {
                    existedRegions[i] = false;
                }
                arrayForElectricity.clear();
                return false;
            }
        } catch (IOException | InvalidFormatException e) {
            errors.add("is null");
            logger.log(LogCategory.ERROR,
                    "IOException/InvalidFormatException. Couldn't read local xls electricity file");
            return false;
        }
    }

    //чтение из файла по электроэнергии с сервера в массив
    private boolean readServerElectricityFile() {
        Logger logger = Logger.getLogger(getClass().getName(), "readServerElectricityFile");
        logger.log(LogCategory.DEBUG, "Reading server electricity file");
        try {
            if (serverFileName != null) {
                InputStream inputStream = ftpController.getInputFileStream(serverFileName);
                Workbook wb = WorkbookFactory.create(inputStream);
                Sheet sheet = wb.getSheetAt(0); //номер листа в файле

                Row firstRow = sheet.getRow(0);
                Cell firstCell = firstRow.getCell(0);
                firstLine = firstCell.getStringCellValue();
                boolean checkHeadsOfFiles = checkEqualityOfHeadlines(firstLine, localFile);

                if (checkHeadsOfFiles) {
                    logger.log(LogCategory.INFO, "Parsing server electricity file");
                    readRegionsFromSecondPage(wb);
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
                                            group = Character.getNumericValue(
                                                    cell.getRichStringCellValue().getString().charAt(0));
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
                    /*if (localFile.getAbsolutePath().matches(".+\\.xlsx")) {
                        xlsxFileHandler = new XlsxFileHandler();
                        if (xlsxFileHandler
                                .setWorkWithElectricityFileFromXls(serverFilePath, firstDate, secondDate, localFile,
                                        arrayForElectricity)) {
                            errors.add(ftpController.getResult().toString());
                            return true;
                        } else {
                            errors.add(ftpController.getResult().toString());
                            return false;
                        }
                    }
                    //чтение данных из файла с компьютера в массив
                    else {*/
                    return workWithElectricityFile();
                    //}
                } else {
                    return false;
                }
            } else {
                errors.add("DISCONNECTED");
                for (int i = 0; i < existedRegions.length - 1; i++) {
                    existedRegions[i] = false;
                }
                arrayForElectricity.clear();
                logger.log(LogCategory.ERROR, "Connection failed");
                return false;
            }
        } catch (IOException | InvalidFormatException e) {
            errors.add(ftpController.getResult().toString());
            logger.log(LogCategory.ERROR, "IOException/InvalidFormatException. Couldn't parse server electricity file");
            return false;
        }
    }

    //чтение из файла по воде с сервера в массив
    private boolean readServerWaterFile() {
        Logger logger = Logger.getLogger(getClass().getName(), "readServerWaterFile");
        logger.log(LogCategory.DEBUG, "Reading server water file");
        try {
            if (serverFileName != null) {
                InputStream inputStream = ftpController.getInputFileStream(serverFileName);
                Workbook wb = WorkbookFactory.create(inputStream);
                readRegionsFromSecondPage(wb);
                int group = 0;
                Sheet sheet = wb.getSheetAt(0); //номер листа в файле

                Row firstRow = sheet.getRow(0);
                Cell firstCell = firstRow.getCell(0);
                firstLine = firstCell.getStringCellValue();
                boolean checkHeadsOfFiles = checkEqualityOfHeadlines(firstLine, localFile);
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
                                            group = Character.getNumericValue(
                                                    cell.getRichStringCellValue().getString().charAt(0));
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
                    /*if (localFile.getAbsolutePath().matches(".+\\.xlsx")) {
                        xlsxFileHandler = new XlsxFileHandler();
                        if (xlsxFileHandler.setWorkWithWaterFileFromXls(serverFilePath, file, arrayForWater)) {
                            errors.add(ftpController.getResult().toString());
                            return true;
                        } else {
                            errors.add(ftpController.getResult().toString());
                            return false;
                        }
                    }
                    //чтение данных из файла с компьютера в массив
                    else {*/
                    return workWithWaterFile();
                    //}
                } else {
                    return false;
                }
            } else {
                errors.add("DISCONNECTED");
                for (int i = 0; i < existedRegions.length - 1; i++) {
                    existedRegions[i] = false;
                }
                arrayForWater.clear();
                logger.log(LogCategory.ERROR, "Connection failed");
                return false;
            }
        } catch (IOException | InvalidFormatException e) {
            errors.add(ftpController.getResult().toString());
            logger.log(LogCategory.ERROR, "IOException/InvalidFormatException. Couldn't parse server water file");
            return false;
        }

    }

    //создание файла по электроэнергии
    boolean createServerElectricityFile() {
        Logger logger = Logger.getLogger(getClass().getName(), "createServerElectricityFile");
        logger.log(LogCategory.DEBUG, "Creating server xls electricity file");
        Workbook wb = new HSSFWorkbook();
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
        addElectricityDataToArray(sheet, cellStyle);
        ByteArrayOutputStream serverFileData;
        try {
            serverFileData = new ByteArrayOutputStream();
            wb.write(serverFileData);
            serverFileData.close();
            wb.close();
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during writing server file on local machine: " + e);
            return false;
        }
        try {
            InputStream is = new ByteArrayInputStream(serverFileData.toByteArray());
            ftpController.sendFile(is, serverFileName);
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during sending file to server: " + e);
            errors.clear();
            errors.add(ftpController.getResult().toString());
            return false;
        }

        errors.add(ftpController.getResult().toString());
        return true;
    }

    //создание файла по воде
    boolean createServerWaterFile() {
        Logger logger = Logger.getLogger(getClass().getName(), "createServerWaterFile");
        logger.log(LogCategory.DEBUG, "Creating server xls water file");

        Workbook wb = new HSSFWorkbook();
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
        //добавление данных в файл
        try {
            addWaterDataToArray(sheet, cellStyle);
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error in adding data to array: " + e);
        }
        //запись в файл
        ByteArrayOutputStream serverFileData;
        try {
            serverFileData = new ByteArrayOutputStream();
            wb.write(serverFileData);
            serverFileData.close();
            wb.close();
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during writing server file on local machine: " + e);
            return false;
         }
        try {
            InputStream is = new ByteArrayInputStream(serverFileData.toByteArray());
            ftpController.sendFile(is, serverFileName);
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during sending file to server: " + e);
            errors.clear();
            errors.add(ftpController.getResult().toString());
            return false;
        }

        errors.add(ftpController.getResult().toString());
        return true;
    }

    public DataType getHeadlineFromServerFile(String serverFileName) {
        Logger logger = Logger.getLogger(getClass().getName(), "getHeadlineFromServerFile");
        try {
            InputStream inputStream = ftpController.getInputFileStream(serverFileName);
            Workbook wb = WorkbookFactory.create(inputStream);
            Sheet sheet = wb.getSheetAt(0); //номер листа в файле
            Row firstRow = sheet.getRow(0);
            Cell firstCell = firstRow.getCell(0);
            String s = firstCell.getStringCellValue();
            if (s.contains("ЭЛЕКТРОСНАБЖЕНИЮ")) {
                wb.close();
                return DataType.ELECTRICITY;
            } else if (s.contains("ВОДОСНАБЖЕНИЮ")) {
                wb.close();
                return DataType.WATER;
            }
        } catch (InvalidFormatException | IOException e) {
            logger.log(LogCategory.ERROR, "Get headline from server file error: " + e.getMessage());
        }
        return null;
    }

    private boolean checkEqualityOfHeadlines(String firstLine, File file) {
        Logger logger = Logger.getLogger(getClass().getName(), "checkEqualityOfHeadlines");
        logger.log(LogCategory.DEBUG, "Checking equality of headlines");
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


    private List<Integer> readRegionsFromSecondPage(Workbook wb) {
        List<Integer> regions = new ArrayList<>();
        Sheet secondPage = wb.getSheetAt(1);
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


    boolean setWorkWithElectricityFileFromXlsx(String firstDate, String secondDate,
            List<ElectricityDataModel> arrayForElectricity) {
        this.firstDate = firstDate;
        this.secondDate = secondDate;
        this.arrayForElectricity = arrayForElectricity;
        return workWithElectricityFile();
    }

    boolean setWorkWithWaterFileFromXlsx(List<WaterDataModel> arrayForWater) {
        this.arrayForWater = arrayForWater;
        return workWithElectricityFile();
    }


    //заполнение данных из массива в файл
    private void addElectricityDataToArray(Sheet sheet, CellStyle cellStyle) {
        Logger logger = Logger.getLogger(getClass().getName(), "addElectricityDataToArray");
        logger.log(LogCategory.DEBUG, "Adding data to the server xls electricity file");
        int group = 0;//номер группы
        int number = 0;//проставление номера данных в одной из четырёх групп
        Collections.sort(arrayForElectricity);//отсортировать в порядке возрастания групп
        for (int i = 0; i < arrayForElectricity.size(); i++) {
            if (arrayForElectricity.get(i).getGroup() != 0) {
                Row row = sheet.createRow((short) (i + 5 + group));
                for (int j = 0; j < 13; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(cellStyle);
                    if (arrayForElectricity.get(i).getGroup() > group)//если верно - записать в файл название группы
                    {
                        sheet.addMergedRegion(new CellRangeAddress(i + 5 + group, i + 5 + group, 0, 12));
                        group = arrayForElectricity.get(i).getGroup();
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
                            cell.setCellValue(arrayForElectricity.get(i).getHasAccountingDevice());
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
                            existedRegions[arrayForElectricity.get(i).getRegion() - 1] = true;
                            break;
                    }
                }
            }
        }
        arrayForElectricity.clear();//очистить массив на случай, если будут добаляться ещё файлы за один раз
        createRegionsPageInServerFile(sheet.getWorkbook());
        for (int i = 0; i < existedRegions.length - 1; i++) {
            existedRegions[i] = false;
        }
    }

    //заполнение данных из массива в файл
    private void addWaterDataToArray(Sheet sheet, CellStyle cellStyle) throws IOException, InvalidFormatException {
        Logger logger = Logger.getLogger(getClass().getName(), "addWaterDataToArray");
        logger.log(LogCategory.DEBUG, "Adding data to the server xls water file");

        int group = 0;//номер группы
        int number = 0;//проставление номера данных в одной из четырёх групп
        Collections.sort(arrayForWater);//отсортировать в порядке возрастания групп

        for (int i = 0; i < arrayForWater.size(); i++) {
            if (arrayForWater.get(i).getGroup() != 0) {
                Row row = sheet.createRow((short) (i + 5 + group));
                //по столбцам
                for (int j = 0; j < 11; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(cellStyle);
                    if (arrayForWater.get(i).getGroup() > group)//если верно - записать в файл название группы
                    {
                        sheet.addMergedRegion(new CellRangeAddress(i + 5 + group, i + 5 + group, 0, 10));
                        group = arrayForWater.get(i).getGroup();
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
                            cell.setCellValue(arrayForWater.get(i).getHasColdWaterAccountingDevice());
                            break;
                        case 7:
                            cell.setCellValue(arrayForWater.get(i).getHasHotWaterAccountingDevice());
                            break;
                        case 8:
                            cell.setCellValue(arrayForWater.get(i).getExpenseHouseCold());
                            break;
                        case 9:
                            cell.setCellValue(arrayForWater.get(i).getExpenseHouseHot());
                            break;
                        case 10:
                            cell.setCellValue(arrayForWater.get(i).getRegion());
                            existedRegions[arrayForWater.get(i).getRegion() - 1] = true;
                            break;
                    }
                }
            }
        }
        arrayForWater.clear();//очистить массив на случай, если будут добаляться ещё файлы за один раз
        createRegionsPageInServerFile(sheet.getWorkbook());
        for (int i = 0; i < existedRegions.length - 1; i++) {
            existedRegions[i] = false;
        }
    }


    public List<String> getErrorsArray() {
        return errors;
    }

    public boolean[] getExistedRegions() {
        return existedRegions;
    }

    //создание второй страницы с добавленными регионами
    private void createRegionsPageInServerFile(Workbook wb) {
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
                    cell.setCellValue(existedRegions[(i - 1) * 5 + j]);
                }
            }
        }
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

    private int getRegionToDelete(Cell cell) {
        return (int) cell.getNumericCellValue();
    }

    private void addNewElectricityDataModelForDelete(int region, Cell cell) {
        ElectricityDataModel model = new ElectricityDataModel();

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                model.setRegion(region);
                break;
            case Cell.CELL_TYPE_NUMERIC:
                model.setRegion(region);
                break;
        }
        arrayForElectricity.add(model);
    }

    private void parseAddressElectricity(int group, Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                arrayForElectricity.get(arrayForElectricity.size() - 1).setAddress(cell.getStringCellValue());
                arrayForElectricity.get(arrayForElectricity.size() - 1).setGroup(group);
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setAddress(null);
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setAddress(null);
                }
                break;
        }
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
                if (cell.getStringCellValue().equals("")) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setHasAccountingDevice("нет");
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1)
                            .setHasAccountingDevice(cell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setHasAccountingDevice("нет");
                } else {
                    arrayForElectricity.get(arrayForElectricity.size() - 1).setHasAccountingDevice("нет");
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

    private void addNewWaterDataModelForDelete(int region, Cell cell) {
        WaterDataModel model = new WaterDataModel();
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                model.setRegion(region);
                break;
            case Cell.CELL_TYPE_NUMERIC:
                model.setRegion(region);
                break;
        }
        arrayForWater.add(model);
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

    private void parseAddressWater(int group, Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                arrayForWater.get(arrayForWater.size() - 1).setAddress(cell.getStringCellValue());
                arrayForWater.get(arrayForWater.size() - 1).setGroup(group);
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    arrayForWater.get(arrayForWater.size() - 1).setAddress(null);
                } else {
                    arrayForWater.get(arrayForWater.size() - 1).setAddress(null);
                }
                break;
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
                    arrayForWater.get(arrayForWater.size() - 1).setHasColdWaterAccountingDevice(cell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setHasColdWaterAccountingDevice(null);
                }
                break;
        }
    }

    private void parseHotWaterAccountingDevice(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    arrayForWater.get(arrayForWater.size() - 1).setHasHotWaterAccountingDevice(cell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    arrayForWater.get(arrayForWater.size() - 1).setHasColdWaterAccountingDevice(null);
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

    XlsFileHandler setArrayForElectricity(List<ElectricityDataModel> arrayForElectricity) {
        this.arrayForElectricity = arrayForElectricity;
        return this;
    }

    XlsFileHandler setArrayForWater(List<WaterDataModel> arrayForWater) {
        this.arrayForWater = arrayForWater;
        return this;
    }

    void setFirstDate(String firstDate) {
        this.firstDate = firstDate;
    }

    void setSecondDate(String secondDate) {
        this.secondDate = secondDate;
    }
}
