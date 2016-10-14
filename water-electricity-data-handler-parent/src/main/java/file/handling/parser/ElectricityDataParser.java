package file.handling.parser;

import common.logger.LogCategory;
import common.logger.Logger;
import file.handling.model.ElectricityDataModel;
import file.handling.parser.exception.CellParseException;
import lombok.Getter;
import lombok.val;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ElectricityDataParser extends BaseParser<ElectricityDataModel> {

    private static final String HAS_NOT_ACCOUNT_DEVICE = "нет";

    @Getter
    private String firstDate;

    @Getter
    private String secondDate;

    private int group;

    @Override
    protected void parseServerFileCell(Row row, Cell cell) {
        val rowNum = row.getRowNum();
        val columnIndex = cell.getColumnIndex();
        val cellRef = new CellReference(rowNum, columnIndex);
        val arr = cellRef.formatAsString().split("\\D");
        int stringNumber = 0; //номер строки, считываемой из файла
        for (int i = 1; i < arr.length; i++) {
            stringNumber +=
                    Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
        }

        if (stringNumber > 5) {
            val electricityDataModel = parseElectricityDataModelForServerFile(cell, cellRef);
            if (electricityDataModel != null) {
                data.add(electricityDataModel);
            }
        }
    }

    @Override
    protected void parseLocalFileCell(int region, Row row, Cell cell) {
        val rowNum = row.getRowNum();
        val columnIndex = cell.getColumnIndex();
        val cellRef = new CellReference(rowNum, columnIndex);
        val arr = cellRef.formatAsString().split("\\D");
        int stringNumber = 0; //номер строки, считываемой из файла
        for (int i = 1; i < arr.length; i++) {
            stringNumber +=
                    Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
        }

        if (rowNum == 3) {
            val date = cell.getStringCellValue();
            if (columnIndex == 6 && firstDate == null) {
                firstDate = date;
            } else if (columnIndex == 7 && secondDate == null) {
                secondDate = date;
            }
        }
        //чтение данных
        if (stringNumber > 5) {
            val electricityDataModel = parseElectricityDataModelForLocalFile(region, cell, cellRef);
            if (electricityDataModel != null) {
                data.add(electricityDataModel);
            }
        }
    }

    private ElectricityDataModel parseElectricityDataModelForServerFile(Cell cell, CellReference cellRef) {
        val electricityDataModel = new ElectricityDataModel();
        val cellCode = cellRef.formatAsString();
        val logger = Logger.getLogger(getClass().toString(), "parseElectricityDataModelForServerFile");
        logger.log(LogCategory.DEBUG,
                "Parsing server xls electricity file. Cell address = '" + cellCode + "'");
        try {
            if (cellRef.formatAsString().matches("A.+")) {
                if (cell.getCellType() == 1 &&
                        cell.getRichStringCellValue().getString().matches("\\d\\..+")) {
                    group = Character.getNumericValue(cell.getRichStringCellValue().getString().charAt(0));
                }
            } else if (cellRef.formatAsString().matches("B.+")) {
                addGroupAndAddressToModel(group, cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("C.+")) {
                parseElectricityBiggestFloor(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("D.+")) {
                parseElectricitySmallestFloor(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("E.+")) {
                parseElectricityJoint(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("F.+")) {
                parseElectricityAccountingDevice(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("G.+")) {
                parseExpenseHouseFirstMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("H.+")) {
                parseExpenseHouseSecondMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("I.+")) {
                parseExpenseNotLivingFirstMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("J.+")) {
                parseExpenseNotLivingSecondMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("K.+")) {
                parseExpenseIndividFirstMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("L.+")) {
                parseExpenseIndividSecondMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("M.+")) {
                parseElectricityRegion(cell, electricityDataModel);
            }
        } catch (CellParseException e) {
            throw new CellParseException(cellCode);
        }
        return electricityDataModel;
    }

    private ElectricityDataModel parseElectricityDataModelForLocalFile(int region, Cell cell, CellReference cellRef) {
        val electricityDataModel = new ElectricityDataModel();
        val cellCode = cellRef.formatAsString();
        try {
            val logger = Logger.getLogger(getClass().toString(), "parseElectricityDataModel");
            logger.log(LogCategory.DEBUG,
                    "Parsing local xls electricity file. Cell address = '" + cellCode + "'");
            if (cellRef.formatAsString().matches("B.+")) {
                if (cell.getCellTypeEnum().equals(CellType.STRING)) {
                    electricityDataModel.setAddress(cell.getRichStringCellValue().getString());
                }
                electricityDataModel.setRegion(region);
            } else if (cellRef.formatAsString().matches("C.+")) {
                parseElectricityBiggestFloor(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("D.+")) {
                parseElectricitySmallestFloor(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("E.+")) {
                parseElectricityJoint(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("F.+")) {
                parseElectricityAccountingDevice(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("G.+")) {
                parseExpenseHouseFirstMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("H.+")) {
                parseExpenseHouseSecondMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("I.+")) {
                parseExpenseNotLivingFirstMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("J.+")) {
                parseExpenseNotLivingSecondMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("K.+")) {
                parseExpenseIndividFirstMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("L.+")) {
                parseExpenseIndividSecondMonth(cell, electricityDataModel);
            } else if (cellRef.formatAsString().matches("M.+")) {
                parseElectricityGroup(cell, electricityDataModel);
                if (electricityDataModel.getGroup() == 0) {
                    return null;
                }
            }
        } catch (Exception e) {
            throw new CellParseException(cellCode);
        }
        return electricityDataModel;
    }

    private void parseExpenseIndividFirstMonth(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                electricityDataModel.setExpenseIndividFirstMonth(
                        new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP)
                                .doubleValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setExpenseIndividFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    electricityDataModel.setExpenseIndividFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseIndividSecondMonth(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                electricityDataModel.setExpenseIndividSecondMonth(
                        new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP)
                                .doubleValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setExpenseIndividSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    electricityDataModel.setExpenseIndividSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseElectricityAccountingDevice(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    electricityDataModel.setHasAccountingDevice(HAS_NOT_ACCOUNT_DEVICE);
                } else {
                    electricityDataModel
                            .setHasAccountingDevice(cell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setHasAccountingDevice(HAS_NOT_ACCOUNT_DEVICE);
                } else {
                    electricityDataModel.setHasAccountingDevice(HAS_NOT_ACCOUNT_DEVICE);
                }
                break;
        }
    }

    private void parseElectricityBiggestFloor(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                electricityDataModel
                        .setBiggestFloor(Integer.valueOf(cell.getStringCellValue()));
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel
                            .setBiggestFloor((int) cell.getNumericCellValue());
                } else {
                    electricityDataModel
                            .setBiggestFloor((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseElectricitySmallestFloor(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                electricityDataModel
                        .setSmallestFloor(Integer.valueOf(cell.getStringCellValue()));
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel
                            .setSmallestFloor((int) cell.getNumericCellValue());
                } else {
                    electricityDataModel
                            .setSmallestFloor((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseExpenseHouseFirstMonth(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    electricityDataModel.setExpenseHouseFirstMonth(0);
                } else {
                    electricityDataModel.setExpenseHouseFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setExpenseHouseFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    electricityDataModel.setExpenseHouseFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseHouseSecondMonth(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    electricityDataModel.setExpenseHouseSecondMonth(0);
                } else {
                    electricityDataModel.setExpenseHouseSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setExpenseHouseSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    electricityDataModel.setExpenseHouseSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseNotLivingFirstMonth(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    electricityDataModel.setExpenseNotLivingFirstMonth(0);
                } else {
                    electricityDataModel.setExpenseNotLivingFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setExpenseNotLivingFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    electricityDataModel.setExpenseNotLivingFirstMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseNotLivingSecondMonth(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    electricityDataModel.setExpenseNotLivingSecondMonth(0);
                } else {
                    electricityDataModel.setExpenseNotLivingSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setExpenseNotLivingSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    electricityDataModel.setExpenseNotLivingSecondMonth(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseElectricityJoint(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    electricityDataModel.setJoint(0);
                } else {
                    electricityDataModel.setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    electricityDataModel.setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseElectricityGroup(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (cell.getStringCellValue().equals("")) {
                    electricityDataModel.setGroup(0);
                } else {
                    electricityDataModel.setGroup((int) cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setGroup((int) cell.getNumericCellValue());
                } else {
                    electricityDataModel.setGroup((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseElectricityRegion(Cell cell, ElectricityDataModel electricityDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                electricityDataModel
                        .setRegion(Integer.valueOf(cell.getStringCellValue()));
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    electricityDataModel.setRegion((int) cell.getNumericCellValue());
                } else {
                    electricityDataModel.setRegion((int) cell.getNumericCellValue());
                }
                break;
        }
    }
}

