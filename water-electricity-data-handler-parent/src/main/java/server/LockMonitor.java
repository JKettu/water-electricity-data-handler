package server;

import common.ConnectionFailedException;
import common.lock.TemporaryDeleteOnExitFiles;
import common.logger.LogCategory;
import common.logger.Logger;

import java.io.File;
import java.util.*;

public class LockMonitor {
    private static LockMonitor instance;
    private List<LockFile> locks;
    private Map<String, LockFile> lastClientLocks;
    public static final int LAST_LOG_CHECK_DELAY_MULTIPLIER = 5000;
    public static final Object lock = new Object();

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
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Logger logger = Logger.getLogger(getClass().toString(), "startMonitoring");
                    FTPController ftpController = new FTPController();
                    locks = ftpController.getLockFiles();
                    List<String> serverFileNames;
                    try {
                        serverFileNames = ftpController.getServerFileNames();
                    } catch (ConnectionFailedException e) {
                        logger.log(LogCategory.ERROR, "Error during getting server file names: " + e);
                        return;
                    }
                    for (String serverFileName : serverFileNames) {
                        List<LockFile> serverFileNameLocks = new ArrayList<>();
                        for (LockFile lock : locks) {
                            if (lock.getServerFileName().equals(serverFileName)) {
                                serverFileNameLocks.add(lock);
                            }
                        }
                        if (serverFileNameLocks.isEmpty()) {
                            lastClientLocks.remove(serverFileName);
                            continue;
                        }
                        Collections.sort(serverFileNameLocks);
                        LockFile lastClientLock = serverFileNameLocks.get(serverFileNameLocks.size() - 1);
                        lastClientLocks.put(serverFileName, lastClientLock);
                    }
                    synchronized (lock) {
                            lock.notifyAll();
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    List<LockFile> getLocks() {
        return locks;
    }

    public LockFile getLastClientLock(String serverFileName) {
        return lastClientLocks.get(serverFileName);
    }

    public void forceDeleteLocks(){
        FTPController ftpController = new FTPController();
        for (String lockFile : TemporaryDeleteOnExitFiles.currentFiles) {
            File file = new File(lockFile);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            ftpController.deleteFile(lockFile);
            TemporaryDeleteOnExitFiles.removeFile(lockFile);
        }
    }
}
