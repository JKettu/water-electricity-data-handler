package server;

import common.lock.TemporaryDeleteOnExitFiles;
import common.logger.LogCategory;
import common.logger.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LockFileController {


    private List<LockFile> getLockFiles(String lockServerFileName) {
        List<LockFile> allLocks = LockMonitor.getLockMonitor().getLocks();
        return allLocks.stream()
                .filter(lock -> lock.getServerFileName().equals(lockServerFileName))
                .collect(Collectors.toList());
    }

    List<LockFile> getLockFiles() {
        Logger logger = Logger.getLogger(getClass().toString(), "getLockFiles");
        logger.log(LogCategory.DEBUG, "Getting lock files");
        List<LockFile> locks = new ArrayList<>();
        try {
            List<String> allFiles = getFilesNames();
            if (allFiles == null || allFiles.isEmpty()) {
                return locks;
            }
            for (String fileName : allFiles) {
                if (LockFile.isLockFile(fileName)) {
                    LockFile lockFile = new LockFile(fileName);
                    locks.add(lockFile);
                }
            }
        } catch (IOException e) {
            logger.log(LogCategory.ERROR, "Error during getting lock files: " + e);
        }
        return locks;
    }

    public void lockFile(LockFile lockFile) {
        Logger logger = Logger.getLogger(getClass().toString(), "lockFile");
        if (!tryToConnect()) {
            logger.log(LogCategory.ERROR, "Connection failed");
            return;
        }
        if (!tryToLogIn()) {
            logger.log(LogCategory.ERROR, "Login failed");
            return;
        }
        String lockFileName = lockFile.getLockFileName();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(1);
            String lockFileServerPath = serverFolder + "/" + lockFileName;
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream.close();
            ftpClient.storeFile(lockFileServerPath, inputStream);
            inputStream.close();
            TemporaryDeleteOnExitFiles.addFile(lockFileName);
            logger.log(LogCategory.INFO, "Locked file = '" + lockFile + "'");
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during locking file: " + e);
        }
        disconnect();
    }

    private boolean previousLockExist(LockFile lockFile) {
        String serverFileName = lockFile.getServerFileName();
        List<LockFile> currentLocks = getLockFiles(serverFileName);
        for (LockFile lock : currentLocks) {
            if (lock.getLockClientIndex() == lockFile.getLockClientIndex() - 1) {
                return true;
            }
        }
        return false;

    }

    public boolean updateLock(LockFile lock) {
        while (previousLockExist(lock)) {
        }
        if (lock.getLockClientIndex() != 0) {
            deleteLock(lock);
            lock.decrementClientLockIndex();
            lockFile(lock);
            Logger logger = Logger.getLogger(getClass().toString(), "updateLock");
            logger.log(LogCategory.INFO, "Updated lock file = " + lock);
            return updateLock(lock);
        }
        return false;
    }

    public void deleteLock(LockFile lock) {
        deleteFile(lock.getLockFileName());
    }

}
