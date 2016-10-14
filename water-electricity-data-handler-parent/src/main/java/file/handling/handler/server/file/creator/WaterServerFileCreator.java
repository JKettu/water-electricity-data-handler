package file.handling.handler.server.file.creator;

import common.error.info.ErrorInfo;
import common.error.info.ErrorType;
import common.error.info.WorkbookErrorInfo;
import file.handling.handler.server.file.formator.WaterServerFileFormatter;
import file.handling.parser.WaterDataParser;
import lombok.val;

class WaterServerFileCreator extends BaseServerFileCreator {
    @Override
    public ErrorInfo createServerFile() {
        val parser = new WaterDataParser();
        val parseResult = parser.parseClientLocalFile(localFile, dataFileType);
        if (!parseResult.isParsedSuccessfully()) {
            return ErrorInfo.builder()
                    .errorType(ErrorType.WORKBOOK_LOCAL_FILE_READING_ERROR)
                    .workbookErrorInfo(new WorkbookErrorInfo(parseResult.getErrorCellCode()))
                    .build();
        }
        val waterData = parser.getData();
        val period = parser.getPeriod();
        val waterServerFileFormatter = new WaterServerFileFormatter(waterData, period);
        val serverFileData = waterServerFileFormatter.format();
        return writeServerFileDataToServer(serverFileData);
    }
}
