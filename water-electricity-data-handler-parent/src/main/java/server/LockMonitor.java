package server;

import common.lock.TemporaryDeleteOnExitFiles;
import common.logger.LogCategory;
import common.logger.Logger;
import lombok.Getter;
import lombok.val;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LockMonitor {
    public static final int LAST_LOG_CHECK_DELAY_MULTIPLIER = 5000;
    public static final Object lock = new Object();

    @Getter
    private List<LockFile> locks;

    private static LockMonitor instance;

    private Map<String, LockFile> lastClientLocks;


    private LockMonitor() {
        locks = new ArrayList<>();
        lastClientLocks = new HashMap<>();
    }

    public static LockMonitor getLockMonitor() {
        if (instance == null) {
            instance = new LockMonitor();
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
        val ftpController = new FTPController();
        for (val lockFile : TemporaryDeleteOnExitFiles.currentFiles) {
            val file = new File(lockFile);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            ftpController.deleteFile(lockFile);
            TemporaryDeleteOnExitFiles.removeFile(lockFile);
        }
    }


    private void monitor() {
        while (true) {
            val logger = Logger.getLogger(getClass().toString(), "startMonitoring");
            val ftpController = new FTPController();
            locks = ftpController.getLockFiles();
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
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
}
