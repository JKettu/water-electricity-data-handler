package common;

import common.logger.LogCategory;
import common.logger.Logger;
import handling.XlsFileHandler;
import lombok.val;

import java.util.List;

public class ServerFilesUtils {

    public static List<Integer> getRegions(String serverFileName) {
        val logger = Logger.getLogger(ServerFilesUtils.class.toString(), "getRegions");
        logger.log(LogCategory.INFO, "Getting regions from file = '" + serverFileName + "'");
        val xlsFileHandler = new XlsFileHandler(serverFileName);
        return xlsFileHandler.getServerFileRegions();
    }

}
