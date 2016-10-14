package file.handling.handler.server.file.modifier;

import common.error.info.ErrorInfo;
import common.error.info.ErrorType;
import common.error.info.WorkbookErrorInfo;
import common.logger.Logger;
import file.handling.parser.WaterDataParser;
import file.handling.util.RegionsUtils;
import lombok.val;

public class WaterServerFileModifier extends BaseServerFileModifier {
    @Override
    public ErrorInfo modifyServerFile() {
        val logger = Logger.getLogger(WaterServerFileModifier.class.toString(), "modifyServerFile");
        val regions = RegionsUtils.readRegionsFromSecondPage()
        val serverFileParser = new WaterDataParser();
        val serverFileParseResult = serverFileParser.parseServerFile(serverFileName, localFile);
        if (!serverFileParseResult.isParsedSuccessfully()) {
            if (serverFileParseResult.isClientHeadlineNotEqualsToServer()) {
                return ErrorInfo.builder()
                        .errorType(ErrorType.SERVER_FILE_AND_LOCAL_FILE_STRUCTURE_NOT_EQUALS)
                        .build();
            }
            return ErrorInfo.builder()
                    .errorType(ErrorType.WORKBOOK_SERVER_FILE_READING_ERROR)
                    .workbookErrorInfo(new WorkbookErrorInfo(serverFileParseResult.getCellCode()))
                    .build();
        }
        val data = serverFileParser.getData();
    }
}
