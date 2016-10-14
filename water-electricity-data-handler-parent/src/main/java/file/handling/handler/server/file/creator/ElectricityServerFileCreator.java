package file.handling.handler.server.file.creator;

import common.error.info.ErrorInfo;
import common.error.info.ErrorType;
import common.error.info.WorkbookErrorInfo;
import file.handling.handler.server.file.builder.ElectricityServerFileBuilder;
import file.handling.parser.ElectricityDataParser;
import lombok.val;

class ElectricityServerFileCreator extends BaseServerFileCreator {

    @Override
    public ErrorInfo createServerFile() {
        val parser = new ElectricityDataParser();
        val parseResult = parser.parseClientLocalFile(localFile, dataFileType);
        if (!parseResult.isParsedSuccessfully()) {
            return ErrorInfo.builder()
                    .errorType(ErrorType.WORKBOOK_LOCAL_FILE_READING_ERROR)
                    .workbookErrorInfo(new WorkbookErrorInfo(parseResult.getErrorCellCode()))
                    .build();
        }
        val electricityData = parser.getData();
        val firstDate = parser.getFirstDate();
        val secondDate = parser.getSecondDate();
        val electricityServerFileFormatter = new ElectricityServerFileBuilder(electricityData, firstDate, secondDate);
        val serverFileData = electricityServerFileFormatter.build();
        return writeServerFileDataToServer(serverFileData);
    }
}
