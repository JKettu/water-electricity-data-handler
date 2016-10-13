package server.connector.lock;

import common.TemporaryDeleteOnExitFiles;
import common.logger.LogCategory;
import common.logger.Logger;
import lombok.val;
import server.connector.ftp.FTPConnector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LockFileController {

    private FTPConnector ftpConnector;

    public LockFileController() {
        ftpConnector = new FTPConnector();
    }

    public List<LockFile> getLockFiles() {
        val logger = Logger.getLogger(getClass().toString(), "getLockFiles");
        logger.log(LogCategory.DEBUG, "Getting lock files");
        List<LockFile> locks = new ArrayList<>();
        List<String> allFiles = ftpConnector.getFilesNames();
        if (allFiles == null || allFiles.isEmpty()) {
            return locks;
        }
        allFiles.stream()
                .filter(LockFile::isLockFile)
                .forEach(fileName -> {
                    LockFile lockFile = new LockFile(fileName);
                    locks.add(lockFile);
                });
        return locks;
    }

    public void lockFile(LockFile lockFile) {
        val logger = Logger.getLogger(getClass().toString(), "lockFile");
        val lockFileName = lockFile.getLockFileName();
        try {
            val outputStream = new ByteArrayOutputStream();
            outputStream.write(1);
            val inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream.close();
            ftpConnector.sendFile(inputStream, lockFileName);
            TemporaryDeleteOnExitFiles.addFile(lockFileName);
            logger.log(LogCategory.INFO, "Locked file = '" + lockFile + "'");
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during locking file: " + e);
        }
    }

    public boolean updateLock(LockFile lock) {
        while (previousLockExist(lock)) {}
        if (lock.getLockClientIndex() != 0) {
            deleteLock(lock);
            lock.decrementClientLockIndex();
            lockFile(lock);
            val logger = Logger.getLogger(getClass().toString(), "updateLock");
            logger.log(LogCategory.INFO, "Updated lock file '" + lock + "'");
            return updateLock(lock);
        }
        return false;
    }

    public void deleteLock(LockFile lock) {
        ftpConnector.deleteFile(lock.getLockFileName());
    }


    private boolean previousLockExist(LockFile lockFile) {
        val serverFileName = lockFile.getServerFileName();
        LockFileMonitor lockFileMonitor = LockFileMonitor.getLockMonitor();
        List<LockFile> currentLocks = lockFileMonitor.getServerLockFiles(serverFileName);
        for (val lock : currentLocks) {
            if (lock.getLockClientIndex() == lockFile.getLockClientIndex() - 1) {
                return true;
            }
        }
        return false;
    }


}
