package server.connector.lock;

import common.TemporaryDeleteOnExitFiles;
import common.logger.LogCategory;
import common.logger.Logger;
import lombok.val;
import server.connector.ftp.FTPConnector;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LockFileMonitor {
    public static final int LAST_LOG_CHECK_DELAY_MULTIPLIER = 5000;
    public static final Object lockObject = new Object();

    private List<LockFile> locks;

    private static LockFileMonitor instance;

    private Map<String, LockFile> lastClientLocks;


    private LockFileMonitor() {
        locks = new ArrayList<>();
        lastClientLocks = new HashMap<>();
    }

    public static LockFileMonitor getLockMonitor() {
        if (instance == null) {
            instance = new LockFileMonitor();
        }
        return instance;
    }

    public void startMonitoring() {
        val thread = new Thread(this::monitor);
        thread.setDaemon(true);
        thread.start();
    }

    public LockFile getLastClientLock(String serverFileName) {
        return lastClientLocks.get(serverFileName);
    }

    public void forceDeleteLocks() {
        val ftpController = new FTPConnector();
        for (val lockFile : TemporaryDeleteOnExitFiles.currentFiles) {
            val file = new File(lockFile);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            ftpController.deleteFile(lockFile);
            TemporaryDeleteOnExitFiles.removeFile(lockFile);
        }
    }

    public List<LockFile> getServerLockFiles(String lockServerFileName) {
        return locks.stream()
                .filter(lock -> lock.getServerFileName().equals(lockServerFileName))
                .collect(Collectors.toList());
    }


    private void monitor() {
        while (true) {
            val logger = Logger.getLogger(getClass().toString(), "startMonitoring");
            val lockFileController = new LockFileController();
            val ftpController = new FTPConnector();
            locks = lockFileController.getLockFiles();
            List<String> serverFileNames;
            try {
                serverFileNames = ftpController.getServerFileNames();
            } catch (Exception e) {
                logger.log(LogCategory.ERROR, "Error during getting server file names: " + e);
                return;
            }
            for (val serverFileName : serverFileNames) {
                val serverFileNameLocks = locks.stream()
                        .filter(lock -> lock.getServerFileName().equals(serverFileName))
                        .collect(Collectors.toList());
                if (serverFileNameLocks.isEmpty()) {
                    lastClientLocks.remove(serverFileName);
                    continue;
                }
                Collections.sort(serverFileNameLocks);
                val lastClientLock = serverFileNameLocks.get(serverFileNameLocks.size() - 1);
                lastClientLocks.put(serverFileName, lastClientLock);
            }
            synchronized (lockObject) {
                lockObject.notifyAll();
            }
        }
    }
}
