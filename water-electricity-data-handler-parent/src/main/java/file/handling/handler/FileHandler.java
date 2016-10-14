package file.handling.handler;

import common.DataFileType;
import common.DataType;
import common.error.info.ErrorInfo;
import common.error.info.ErrorType;
import common.logger.LogCategory;
import common.logger.Logger;
import file.handling.handler.server.file.creator.ServerFileCreatorBuilder;
import file.handling.handler.server.file.deleter.ServerFileRegionDeleterBuilder;
import file.handling.handler.server.file.modifier.ServerFileModifierBuilder;
import file.handling.util.HandlingType;
import lombok.Builder;
import lombok.val;
import server.connector.ClientService;
import server.connector.lock.LockFile;
import server.connector.lock.LockFileController;
import server.connector.lock.LockFileMonitor;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

@Builder
public class FileHandler {

    private static final int FIRST_LOCK_CLIENT_INDEX = 0;

    private String serverFileName;
    private File localFile;
    private DataFileType dataFileType;
    private DataType dataType;
    private int regionToDelete;

    public ErrorInfo processFileHandling(HandlingType handlingType) {
        val logger = Logger.getLogger(getClass().toString(), "processWaterFileHandling");
        try {
            val threadSleepDelay = ClientService.CLIENT_ID * LockFileMonitor.LAST_LOG_CHECK_DELAY_MULTIPLIER;
            logger.log(LogCategory.DEBUG, "Waiting for: '" + threadSleepDelay + "'");
            Thread.sleep(threadSleepDelay + ThreadLocalRandom.current().nextInt(5000, 10000));
            synchronized (LockFileMonitor.lockObject) {
                LockFileMonitor.lockObject.wait();
            }
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during waiting: " + e);
            return ErrorInfo.builder().errorType(ErrorType.LOCK_ERROR).build();
        }
        val lastClientLock = LockFileMonitor.getLockMonitor().getLastClientLock(serverFileName);
        LockFile lock;
        if (lastClientLock == null) {
            lock = new LockFile(serverFileName, FIRST_LOCK_CLIENT_INDEX);
        } else {
            lock = new LockFile(serverFileName, lastClientLock.getLockClientIndex() + 1);
        }
        val lockFileController = new LockFileController();
        lockFileController.lockFile(lock);
        while (lockFileController.updateLock(lock)) {}
        ErrorInfo result = null;
        switch (handlingType) {
            case CREATE:
                result = new ServerFileCreatorBuilder()
                        .setServerFileName(serverFileName)
                        .setLocalFile(localFile)
                        .setDataFileType(dataFileType)
                        .build(dataType)
                        .createServerFile();
                break;
            case MODIFY:
                result = new ServerFileModifierBuilder()
                        .setServerFileName(serverFileName)
                        .setLocalFile(localFile)
                        .setDataFileType(dataFileType)
                        .build(dataType)
                        .modifyServerFile();
                break;
            case DELETE_REGION:
                result = new ServerFileRegionDeleterBuilder()
                        .setServerFileName(serverFileName)
                        .setRegion(regionToDelete)
                        .build(dataType)
                        .deleteRegionFromServerFile();
                break;
        }
        lockFileController.deleteLock(lock);
        return result;
    }
}
