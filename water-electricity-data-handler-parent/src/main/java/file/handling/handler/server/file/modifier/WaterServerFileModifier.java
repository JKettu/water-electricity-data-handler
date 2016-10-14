package file.handling.handler.server.file.modifier;

import common.error.info.ErrorInfo;
import common.logger.LogCategory;
import common.logger.Logger;
import file.handling.util.RegionsUtils;
import lombok.val;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.RegionUtil;
import server.connector.ftp.FTPConnector;

import java.io.IOException;
import java.io.InputStream;

public class WaterServerFileModifier extends BaseServerFileModifier {
    @Override
    public ErrorInfo modifyServerFile() {
        val logger = Logger.getLogger(getClass().getName(), "modifyServerFile");
        logger.log(LogCategory.DEBUG, "Reading server water file");
        try {
            val ftpConnector = new FTPConnector();
            val inputStream = ftpConnector.getInputFileStream(serverFileName);
            RegionsUtils.readRegionsFromSecondPage(inputStream);
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
                return workWithWaterFile();
                //}
            } else {
                return false;
            }

            errors.add("DISCONNECTED");
            for (int i = 0; i < existedRegions.length - 1; i++) {
                existedRegions[i] = false;
            }
            arrayForWater.clear();
            logger.log(LogCategory.ERROR, "Connection failed");
            return false;
        }
    } catch(IOException |
    InvalidFormatException e)

    {
        errors.add(ftpConnector.getResult().toString());
        logger.log(LogCategory.ERROR, "IOException/InvalidFormatException. Couldn't parse server water file");
        return false;
    }
}
}
