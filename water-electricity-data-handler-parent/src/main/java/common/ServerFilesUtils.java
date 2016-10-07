package common;

import common.logger.LogCategory;
import common.logger.Logger;
import handling.XlsFileHandler;

import java.util.List;

public class ServerFilesUtils {

    public static List<Integer> getRegions(String serverFileName) {
        Logger logger = Logger.getLogger(ServerFilesUtils.class.toString(), "getRegions");
        logger.log(LogCategory.INFO, "Getting regions from file = '" + serverFileName + "'");
        XlsFileHandler xlsFileHandler = new XlsFileHandler(serverFileName);
        return xlsFileHandler.getServerFileRegions();
    }

}
