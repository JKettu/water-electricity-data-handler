package server;


import common.ConnectionFailedException;
import common.Result;
import common.lock.TemporaryDeleteOnExitFiles;
import common.logger.LogCategory;
import common.logger.Logger;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jay on 22.07.2016.
 */
public class FTPController {
    private String server = "localhost";
    private String serverFolder = "./Data";
    private String login = "test";
    private String password = "";
    private FTPClient ftpClient;
    private boolean connectionSucceed;
    private boolean loggingInSucceed;
    private int port = 21;
    private Result result;

    public FTPController() {
        ftpClient = new FTPClient();
    }


    public String getServerFolder() {
        return serverFolder;
    }

    public Result getResult() {
        return result;
    }


    private List<LockFile> getLockFiles(String lockServerFileName) {
        List<LockFile> allLocks = LockMonitor.getLockMonitor().getLocks();
        List<LockFile> filteredByServerFileNameLocks = new ArrayList<>();
        for (LockFile lock : allLocks) {
            if (lock.getServerFileName().equals(lockServerFileName)) {
                filteredByServerFileNameLocks.add(lock);
            }
        }
        return filteredByServerFileNameLocks;
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
        endSession();
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


    //Получить список файлов с сервера


    //получения названия файлов в папке
    private List<String> getFilesNames() throws IOException {
        List<String> filesNames = new ArrayList<>();
        tryToConnect();
        if (connectionSucceed) {
            tryToLogIn();
        } else {
            endSession();
            return null;
        }
        if (loggingInSucceed) {
            FTPFile[] files = ftpClient.listFiles(serverFolder);
            if (files.length > 0) {
                for (FTPFile file : files) {
                    filesNames.add(file.getName());
                }
            }
        } else {
            endSession();
            return null;
        }
        endSession();
        return filesNames;
    }

    public List<String> getServerFileNames() throws ConnectionFailedException {
        Logger logger = Logger.getLogger(getClass().getName(), "getServerFileNames");
        List<String> serverFileNames = new ArrayList<>();
        List<String> filteredServerFileNames = new ArrayList<>();
        try {
            serverFileNames = getFilesNames();
            if (serverFileNames != null) {
                for (String serverFileName : serverFileNames) {
                    if (!serverFileName.contains(".lock") && !serverFileName.contains(".txt")) {
                        filteredServerFileNames.add(serverFileName);
                        logger.log(LogCategory.INFO, "Server file name = '" + serverFileName + "'");
                    }
                }
            } else {
                throw new ConnectionFailedException();
            }
            serverFileNames = filteredServerFileNames;
        } catch (IOException e) {
            logger.log(LogCategory.ERROR, "Fail in getting files' names");
        }
        return serverFileNames;
    }

    //получение файла с сервера
    public InputStream getInputFileStream(String serverFileName) throws IOException {
        Logger logger = Logger.getLogger(getClass().getName(), "getInputFileStream");
        logger.log(LogCategory.DEBUG, "Getting stream of server file = '" + serverFileName + "'");
        tryToConnect();
        if (connectionSucceed) {
            tryToLogIn();
            // комментарий для проверки коммита
            if (loggingInSucceed) {
                String serverFilePath = serverFolder + "/" + serverFileName;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ftpClient.retrieveFile(serverFilePath, outputStream);
                outputStream.close();
                InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                endSession();
                return inputStream;
            }
        }
        endSession();
        return null;
    }

    //отправка файла на сервер
    public void sendFile(InputStream localFileData, String serverFileName) throws IOException {
        Logger logger = Logger.getLogger(getClass().getName(), "sendFile");
        logger.log(LogCategory.DEBUG, "Sending file to server");
        tryToConnect();
        if (connectionSucceed) {
            tryToLogIn();
            if (loggingInSucceed) {
                String serverFilePath = serverFolder + "/" + serverFileName;
                boolean done = ftpClient.storeFile(serverFilePath, localFileData);
                localFileData.close();
                if (done) {
                    result = Result.FILE_WAS_LOADED;
                    logger.log(LogCategory.INFO,
                            "Successful sending. File = '" + serverFileName + "' was loaded to the server");
                } else {
                    result = Result.FILE_NOT_LOADED;
                    logger.log(LogCategory.ERROR,
                            "Unsuccessful sending. File = '" + serverFileName + "' wasn't loaded to the server");
                }
            }
        }
        endSession();
    }

    //удаление файла
    public void deleteFile(String fileName) {
        tryToConnect();
        if (connectionSucceed) {
            tryToLogIn();
            Logger logger = Logger.getLogger(getClass().toString(), "deleteFile");
            if (loggingInSucceed) {
                try {
                    String filePath = serverFolder + "/" + fileName;
                    ftpClient.deleteFile(filePath);
                    if (filePath.contains(".lock")) {
                        TemporaryDeleteOnExitFiles.removeFile(fileName);
                    }
                    logger.log(LogCategory.INFO, "Deleting file = '" + fileName + "'");
                } catch (IOException ex) {
                    logger.log(LogCategory.ERROR, "Error during deleting file: " + ex);
                }
            }
        }
    }

    //попытка подключения
    private boolean tryToConnect() {
        Logger logger = Logger.getLogger(getClass().getName(), "tryToConnect");
        try {
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.connect(server, port);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                connectionSucceed = true;
                logger.log(LogCategory.INFO, "Connection succeed");
                result = Result.CONNECTED;
                return true;
            } else {
                connectionSucceed = false;
                logger.log(LogCategory.ERROR, "Connection failed");
                result = Result.DISCONNECTED;
                return false;
            }
        } catch (IOException e) {
            connectionSucceed = false;
            logger.log(LogCategory.ERROR, "IOException. Connection failed: " + e);
            result = Result.DISCONNECTED;
            return false;
        }
    }

    //попытка залогиниться
    private boolean tryToLogIn() {
        Logger logger = Logger.getLogger(getClass().getName(), "tryToLogIn");
        try {
            boolean log = ftpClient.login(login, password);
            if (log) {
                loggingInSucceed = true;
                logger.log(LogCategory.INFO, "Logging in succeed");
                result = Result.LOGIN;
                return true;
            } else {
                loggingInSucceed = false;
                logger.log(LogCategory.ERROR, "Logging in failed");
                result = Result.LOGOUT;
                return false;
            }
        } catch (IOException e) {
            loggingInSucceed = false;
            logger.log(LogCategory.ERROR, "IOException. Logging in failed");
            result = Result.LOGOUT;
            return false;
        }
    }

    //выход
    private void endSession() {
        if (ftpClient.isConnected()) {
            Logger logger = Logger.getLogger(getClass().getName(), "endSession");
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (Exception e) {
                logger.log(LogCategory.ERROR, "End session failed: " + e);
            }
            logger.log(LogCategory.INFO, "Disconnected");
        }
    }
}
