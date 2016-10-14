package file.handling.parser;

import common.DataFileType;
import common.logger.LogCategory;
import common.logger.Logger;
import file.handling.model.BaseDataModel;
import file.handling.parser.exception.CellParseException;
import file.handling.parser.exception.FileHeadlinesNotEquals;
import file.handling.util.RegionsUtils;
import lombok.Getter;
import lombok.val;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import server.connector.ftp.FTPConnector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseParser<DataModelType extends BaseDataModel> {

    @Getter
    protected List<DataModelType> data;

    private DataFileType dataFileType;

    BaseParser() {
        data = new ArrayList<>();
    }

    public LocalFileParseResult parseClientLocalFile(File dataFile, DataFileType dataFileType) {
        this.dataFileType = dataFileType;
        val logger = Logger.getLogger(getClass().toString(), "parse");
        try {
            parseDataFromLocalFile(dataFile);
            return LocalFileParseResult.builder().parsedSuccessfully(true).build();
        } catch (CellParseException cpe) {
            val cellCode = cpe.getCellCode();
            logger.log(LogCategory.ERROR, "Error during parsing cell '" + cellCode + "'");
            return LocalFileParseResult.builder().errorCellCode(cellCode).build();
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during parsing file '" + dataFile + "': " + e);
            return LocalFileParseResult.builder().build();
        }
    }

    public ServerFileParseResult parseServerFile(String serverFileName, File localFile) {
        val logger = Logger.getLogger(getClass().toString(), "parse");
        try {
            parseDataFromServerFile(serverFileName, localFile);
            return ServerFileParseResult.builder().parsedSuccessfully(true)
                    .clientHeadlineNotEqualsToServer(true)
                    .build();
        } catch (CellParseException cpe) {
            val cellCode = cpe.getCellCode();
            logger.log(LogCategory.ERROR, "Error during parsing cell '" + cellCode + "'");
            return ServerFileParseResult.builder().parsedSuccessfully(false)
                    .clientHeadlineNotEqualsToServer(true)
                    .cellCode(cellCode)
                    .build();
        } catch (FileHeadlinesNotEquals e) {
            return ServerFileParseResult.builder().parsedSuccessfully(false)
                    .clientHeadlineNotEqualsToServer(false)
                    .build();
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during parsing file '" + serverFileName + "': " + e);
            return ServerFileParseResult.builder().parsedSuccessfully(false)
                    .clientHeadlineNotEqualsToServer(false)
                    .build();
        }
    }


    protected abstract void parseServerFileCell(Row row, Cell cell);

    protected abstract void parseLocalFileCell(int region, Row row, Cell cell);


    private void parseDataFromLocalFile(File dataFile) throws IOException, InvalidFormatException {
        val region = RegionsUtils.getFileRegion(dataFile, dataFileType);
        val woorkbook = WorkbookFactory.create(dataFile);
        val firstSheet = woorkbook.getSheetAt(0); //номер листа в файле
        firstSheet.forEach(row -> parseLocalFileRow(region, row));
    }

    private void parseDataFromServerFile(String serverFileName, File localFile)
    throws IOException, InvalidFormatException, FileHeadlinesNotEquals {
        val logger = Logger.getLogger(getClass().getName(), "parseDataFromServerFile");
        val ftpConnector = new FTPConnector();
        val inputStream = ftpConnector.getInputFileStream(serverFileName);
        val serverFileWorkbook = WorkbookFactory.create(inputStream);
        val serverFileFirstSheet = serverFileWorkbook.getSheetAt(0);
        val serverFileFirstSheetRow = serverFileFirstSheet.getRow(0);
        val serverFileFirstSheetRowCell = serverFileFirstSheetRow.getCell(0);
        val serverFileFirstLine = serverFileFirstSheetRowCell.getStringCellValue();

        val localFileWorkbook = WorkbookFactory.create(localFile);
        val localFileFirstSheet = localFileWorkbook.getSheetAt(0);
        val localFileFirstSheetRow = localFileFirstSheet.getRow(0);
        val localFileFirstSheetRowCell = localFileFirstSheetRow.getCell(0);
        val localFileFirstLine = localFileFirstSheetRowCell.getStringCellValue();
        localFileWorkbook.close();

        val checkHeadsOfFiles = checkEqualityOfHeadlines(serverFileFirstLine, localFileFirstLine);
        if (!checkHeadsOfFiles) {
            throw new FileHeadlinesNotEquals();
        }
        RegionsUtils.readRegionsFromSecondPage(serverFileWorkbook);
        logger.log(LogCategory.INFO, "Parsing server water file");
        serverFileFirstSheet.forEach(row -> parseServerFileRow(row));
    }

    private boolean checkEqualityOfHeadlines(String serverFileFirstLine, String localFileFirstLine) {
        val logger = Logger.getLogger(getClass().getName(), "checkEqualityOfHeadlines");
        logger.log(LogCategory.DEBUG, "Checking equality of headlines");
        try {
            if ((localFileFirstLine == null) || (localFileFirstLine.equals(""))) {
                logger.log(LogCategory.INFO, "Local file is empty");
                return false;
            } else {
                logger.log(LogCategory.INFO, "Local file isn't empty");
                if (localFileFirstLine.equals(serverFileFirstLine)) {
                    logger.log(LogCategory.INFO, "Headlines are equal. Same types of data");
                    return true;
                } else {
                    logger.log(LogCategory.ERROR, "Headlines aren't equal. Different types of data");
                    return false;
                }
            }
        } catch (Exception e) {
            logger.log(LogCategory.ERROR,
                    "IOException/InvalidFormatException. Couldn't compare headlines of files: " + e);
            return false;
        }
    }

    private void parseLocalFileRow(int region, Row row) {
        row.forEach(cell -> parseLocalFileCell(region, row, cell));
    }

    private void parseServerFileRow(Row row) {
        row.forEach(cell -> parseServerFileCell(row, cell));
    }

}
