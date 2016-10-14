package file.handling.handler.server.file.modifier;

import common.error.info.ErrorInfo;
import common.error.info.ErrorType;
import common.error.info.WorkbookErrorInfo;
import common.logger.Logger;
import file.handling.handler.server.file.builder.WaterServerFileBuilder;
import file.handling.parser.WaterDataParser;
import lombok.val;

public class WaterServerFileModifier extends BaseServerFileModifier {
    @Override
    public ErrorInfo modifyServerFile() {
        val logger = Logger.getLogger(WaterServerFileModifier.class.toString(), "modifyServerFile");
        val parser = new WaterDataParser();
        val serverFileParseResult = parser.parseServerFileWithHeadlinesCheck(serverFileName, localFile);
        if (!serverFileParseResult.isParsedSuccessfully()) {
            return createErrorInfo(serverFileParseResult);
        }
        val serverFileData = parser.getData();
        val clientFileParseResult = parser.parseClientLocalFile(localFile, dataFileType);
        if (!clientFileParseResult.isParsedSuccessfully()) {
            return ErrorInfo.builder()
                    .errorType(ErrorType.WORKBOOK_LOCAL_FILE_READING_ERROR)
                    .workbookErrorInfo(new WorkbookErrorInfo(clientFileParseResult.getErrorCellCode()))
                    .build();
        }
        val clientFileData = parser.getData();
        serverFileData.addAll(clientFileData);
        val waterServerFileFormatter = new WaterServerFileBuilder(serverFileData, parser.getPeriod());
        val serverFileDataStream = waterServerFileFormatter.build();
        return writeServerFileDataToServer(serverFileDataStream);
    }
}
