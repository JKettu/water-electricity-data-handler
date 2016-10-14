package file.handling.parser;

import common.logger.LogCategory;
import common.logger.Logger;
import file.handling.model.WaterDataModel;
import file.handling.parser.exception.CellParseException;
import lombok.Getter;
import lombok.val;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WaterDataParser extends BaseParser<WaterDataModel> {

    @Getter
    private String period;
    private int group;

    @Override
    protected void parseLocalFileCell(int region, Row row, Cell cell) {
        val rowNum = row.getRowNum();
        val columnIndex = cell.getColumnIndex();
        val cellRef = new CellReference(rowNum, columnIndex);
        val arr = cellRef.formatAsString().split("\\D");
        int stringNumber = 0;//номер строки, читаемой из файла
        for (int i = 1; i < arr.length; i++) {
            stringNumber += Integer.valueOf(arr[i]) * ((int) Math.pow(10, arr.length - i - 1));
        }
        if (rowNum == 2) {
            if (columnIndex == 8 && period == null) {
                period = cell.getStringCellValue();
            }
        }
        //считывание строк с данными
        if (stringNumber > 5) {
            val waterDataModel = parseWaterDataModelForLocalFile(region, cell, cellRef);
            if (waterDataModel != null) {
                data.add(waterDataModel);
            }
        }
    }

    @Override
    protected void parseServerFileCell(Row row, Cell cell) {
        val rowNum = row.getRowNum();
        val columnIndex = cell.getColumnIndex();
        val cellRef = new CellReference(rowNum, columnIndex);
        val cellCodeStringArray = cellRef.formatAsString().split("\\D");
        int stringNumber = 0;//номер строки, читаемой из файла
        for (int i = 1; i < cellCodeStringArray.length; i++) {
            stringNumber = stringNumber +
                    Integer.valueOf(cellCodeStringArray[i]) * ((int) Math.pow(10, cellCodeStringArray.length - i - 1));
        }

        //считывание строк с данными
        if (stringNumber > 5) {
            val waterDataModel = parseWaterDataModelForServerFile(cell, cellRef);
            if (waterDataModel != null) {
                data.add(waterDataModel);
            }
        }
    }

    private WaterDataModel parseWaterDataModelForLocalFile(int region, Cell cell, CellReference cellRef) {
        val waterDataModel = new WaterDataModel();
        val cellCode = cellRef.formatAsString();
        try {
            val logger = Logger.getLogger(getClass().toString(), "parseWaterDataModelForLocalFile");
            logger.log(LogCategory.DEBUG,
                    "Parsing local xls water file. Cell address = '" + cellCode + "'");
            waterDataModel.setRegion(region);
            if (cellRef.formatAsString().matches("B.+")) {
                if (cell.getCellTypeEnum().equals(CellType.STRING)) {
                    waterDataModel.setAddress(cell.getRichStringCellValue().getString());
                }
            } else if (cellRef.formatAsString().matches("C.+")) {
                parseWaterBiggestFloor(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("D.+")) {
                parseWaterSmallestFloor(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("E.+")) {
                parseWaterJoint(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("F.+")) {
                parseWaterPeople(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("G.+")) {
                parseColdWaterAccountingDevice(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("H.+")) {
                parseHotWaterAccountingDevice(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("I.+")) {
                parseExpenseHouseCold(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("J.+")) {
                parseExpenseHouseHot(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("K.+")) {
                parseWaterGroup(cell, waterDataModel);
                if (waterDataModel.getGroup() == 0) {
                    return null;
                }
            }
        } catch (Exception e) {
            throw new CellParseException(cellCode);
        }
        return waterDataModel;
    }

    private WaterDataModel parseWaterDataModelForServerFile(Cell cell, CellReference cellRef) {
        val waterDataModel = new WaterDataModel();
        val cellCode = cellRef.formatAsString();
        val logger = Logger.getLogger(getClass().toString(), "parseWaterDataModelForServerFile");
        logger.log(LogCategory.DEBUG,
                "Parsing server water file. Cell address = '" + cellCode + "'");
        try {
            if (cellRef.formatAsString().matches("A.+")) {
                if (cell.getCellType() == 1) {
                    if (cell.getRichStringCellValue().getString().matches("\\d\\..+")) {
                        group = Character.getNumericValue(
                                cell.getRichStringCellValue().getString().charAt(0));
                    }
                }
            } else if (cellRef.formatAsString().matches("B.+")) {
                addGroupAndAddressToModel(group, cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("C.+")) {
                parseWaterBiggestFloor(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("D.+")) {
                parseWaterSmallestFloor(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("E.+")) {
                parseWaterJoint(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("F.+")) {
                parseWaterPeople(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("G.+")) {
                parseColdWaterAccountingDevice(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("H.+")) {
                parseHotWaterAccountingDevice(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("I.+")) {
                parseExpenseHouseCold(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("J.+")) {
                parseExpenseHouseHot(cell, waterDataModel);
            } else if (cellRef.formatAsString().matches("K.+")) {
                parseWaterRegion(cell, waterDataModel);
            }
        } catch (CellParseException e) {
            throw new CellParseException(cellCode);
        }
        return waterDataModel;
    }

    private void parseWaterBiggestFloor(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel.setBiggestFloor(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setBiggestFloor((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseWaterSmallestFloor(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel
                            .setSmallestFloor(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setSmallestFloor((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseWaterJoint(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel.setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setJoint(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseWaterGroup(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel.setGroup(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setGroup((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseWaterPeople(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel.setPeople(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setPeople((int) cell.getNumericCellValue());
                }
                break;
        }
    }

    private void parseColdWaterAccountingDevice(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel
                            .setHasColdWaterAccountingDevice(cell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setHasColdWaterAccountingDevice(null);
                }
                break;
        }
    }

    private void parseHotWaterAccountingDevice(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel
                            .setHasHotWaterAccountingDevice(cell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setHasColdWaterAccountingDevice(null);
                }
                break;
        }
    }

    private void parseExpenseHouseCold(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel.setExpenseHouseCold(
                            new BigDecimal(Double.valueOf(cell.getStringCellValue())).setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setExpenseHouseCold(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseExpenseHouseHot(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel.setExpenseHouseHot(
                            new BigDecimal(Double.valueOf(cell.getStringCellValue())).setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue());
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setExpenseHouseHot(
                            new BigDecimal(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }
                break;
        }
    }

    private void parseWaterRegion(Cell cell, WaterDataModel waterDataModel) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                if (!cell.getStringCellValue().equals("")) {
                    waterDataModel.setRegion(Integer.valueOf(cell.getStringCellValue()));
                }
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (cell.getNumericCellValue() != 0) {
                    waterDataModel.setRegion((int) cell.getNumericCellValue());
                }
                break;
        }
    }


}