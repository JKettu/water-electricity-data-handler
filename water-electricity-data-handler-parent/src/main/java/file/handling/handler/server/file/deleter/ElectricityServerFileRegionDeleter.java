package file.handling.handler.server.file.deleter;

import common.error.info.ErrorInfo;
import common.logger.Logger;
import file.handling.handler.server.file.builder.ElectricityServerFileBuilder;
import file.handling.parser.ElectricityDataParser;
import lombok.val;

public class ElectricityServerFileRegionDeleter extends BaseServerFileRegionDeleter {

    @Override
    public ErrorInfo deleteRegionFromServerFile() {
        val logger = Logger.getLogger(WaterServerFileRegionDeleter.class.toString(), "modifyServerFile");
        val parser = new ElectricityDataParser();
        val serverFileParseResult = parser.parseServerFile(serverFileName);
        if (!serverFileParseResult.isParsedSuccessfully()) {
            return createErrorInfo(serverFileParseResult);
        }
        val serverFileData = parser.getData();
        serverFileData.removeIf(model -> model.getRegion() == regionToDelete);
        val waterServerFileFormatter =
                new ElectricityServerFileBuilder(serverFileData, parser.getFirstDate(), parser.getSecondDate());
        val serverFileDataStream = waterServerFileFormatter.build();
        return writeServerFileDataToServer(serverFileDataStream);
    }
}
