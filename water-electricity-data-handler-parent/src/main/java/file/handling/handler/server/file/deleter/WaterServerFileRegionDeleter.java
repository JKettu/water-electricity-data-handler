package file.handling.handler.server.file.deleter;

import common.error.info.ErrorInfo;
import common.logger.Logger;
import file.handling.handler.server.file.builder.WaterServerFileBuilder;
import file.handling.parser.WaterDataParser;
import lombok.val;

public class WaterServerFileRegionDeleter extends BaseServerFileRegionDeleter {
    @Override
    public ErrorInfo deleteRegionFromServerFile() {
        val logger = Logger.getLogger(WaterServerFileRegionDeleter.class.toString(), "deleteRegionFromServerFile");
        val parser = new WaterDataParser();
        val serverFileParseResult = parser.parseServerFile(serverFileName);
        if (!serverFileParseResult.isParsedSuccessfully()) {
            return createErrorInfo(serverFileParseResult);
        }
        val serverFileData = parser.getData();
        serverFileData.removeIf(model -> model.getRegion() == regionToDelete);
        val waterServerFileFormatter =
                new WaterServerFileBuilder(serverFileData, parser.getPeriod());
        val serverFileDataStream = waterServerFileFormatter.build();
        return writeServerFileDataToServer(serverFileDataStream);
    }
}
