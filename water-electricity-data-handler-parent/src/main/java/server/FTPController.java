package server;


import common.ConnectionFailedException;
import common.Result;
import common.config.ConfigProperties;
import common.config.ConfigPropertiesSections;
import common.lock.TemporaryDeleteOnExitFiles;
import common.logger.LogCategory;
import common.logger.Logger;
import lombok.val;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FTPController {
    private final static String CONFIG_HOST_PROPERTY_KEY = "host";
    private final static String CONFIG_FOLDER_PROPERTY_KEY = "folder";
    private final static String CONFIG_LOGIN_PROPERTY_KEY = "login";
    private final static String CONFIG_PASSWORD_PROPERTY_KEY = "password";
    private final static String CONFIG_PORT_PROPERTY_KEY = "port";
    private final static String UTF_8 = "UTF-8";

    private FTPClient ftpClient;
    private FtpResult ftpResult;

    public FTPController() {
        ftpClient = new FTPClient();
    }

    public List<String> getServerFileNames() {
        val logger = Logger.getLogger(getClass().getName(), "getServerFileNames");
        List<String> serverFileNames;
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


    private boolean tryToConnect() {
        val logger = Logger.getLogger(getClass().getName(), "tryToConnect");
        val config = ConfigProperties.getConfigProperties(ConfigPropertiesSections.FTP);
        try {
            ftpClient.setControlEncoding(UTF_8);
            String host = config.getPropertyValue(CONFIG_HOST_PROPERTY_KEY);
            int port = config.getPropertyValue(CONFIG_PORT_PROPERTY_KEY);
            ftpClient.connect(host, port);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                logger.log(LogCategory.INFO, "Connection succeed");
                ftpResult = FtpResult.CONNECTED;
                return true;
            } else {
                logger.log(LogCategory.ERROR, "Connection failed");
                ftpResult = FtpResult.DISCONNECTED;
                return false;
            }
        } catch (IOException e) {
            logger.log(LogCategory.ERROR, "IOException. Connection failed: " + e);
            ftpResult = FtpResult.DISCONNECTED;
            return false;
        }
    }

    private boolean tryToLogIn() {
        val logger = Logger.getLogger(getClass().getName(), "tryToLogIn");
        try {
            val config = ConfigProperties.getConfigProperties(ConfigPropertiesSections.FTP);
            String login = config.getPropertyValue(CONFIG_LOGIN_PROPERTY_KEY);
            String password = config.getPropertyValue(CONFIG_PASSWORD_PROPERTY_KEY);
            val log = ftpClient.login(login, password);
            if (log) {
                logger.log(LogCategory.INFO, "Logging in succeed");
                ftpResult = FtpResult.LOGIN;
                return true;
            } else {
                logger.log(LogCategory.ERROR, "Logging in failed");
                ftpResult = FtpResult.LOGOUT;
                return false;
            }
        } catch (IOException e) {
            logger.log(LogCategory.ERROR, "IOException. Logging in failed");
            ftpResult = FtpResult.LOGOUT;
            return false;
        }
    }

    private void disconnect() {
        if (!ftpClient.isConnected()) {
            return;
        }
        val logger = Logger.getLogger(getClass().getName(), "endSession");
        try {
            ftpClient.logout();
            ftpClient.disconnect();
            ftpResult = FtpResult.DISCONNECTED;
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Disconnect failed: " + e);
        }
        logger.log(LogCategory.INFO, "Disconnected");
    }


    private List<String> getFilesNames() {
        List<String> filesNames = new ArrayList<>();
        if (!tryToConnect()) {
            return null;
        }

        if (!tryToLogIn()) {
            return null;
        }

        val config = ConfigProperties.getConfigProperties(ConfigPropertiesSections.FTP);
        String serverFolder = config.getPropertyValue(CONFIG_FOLDER_PROPERTY_KEY);
        val logger = Logger.getLogger(getClass().getName(), "endSession");
        try {
            val files = ftpClient.listFiles(serverFolder);
            if (files.length > 0) {
                Arrays.stream(files).forEach(file -> filesNames.add(file.getName()));
            }
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error during getting server files: " + e);
        }
        disconnect();
        return filesNames;
    }


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


    //Получить список файлов с сервера


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
                disconnect();
                return inputStream;
            }
        }
        disconnect();
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
        disconnect();
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

}
