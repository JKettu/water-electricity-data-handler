package server.connector.ftp;


import common.TemporaryDeleteOnExitFiles;
import common.config.ConfigProperties;
import common.config.ConfigPropertiesSections;
import common.logger.LogCategory;
import common.logger.Logger;
import lombok.Getter;
import lombok.val;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FTPConnector {
    private final static String CONFIG_HOST_PROPERTY_KEY = "host";
    private final static String CONFIG_FOLDER_PROPERTY_KEY = "folder";
    private final static String CONFIG_LOGIN_PROPERTY_KEY = "login";
    private final static String CONFIG_PASSWORD_PROPERTY_KEY = "password";
    private final static String CONFIG_PORT_PROPERTY_KEY = "port";
    private final static String UTF_8 = "UTF-8";
    private final static String LOCK_FILE_TYPE = ".lockObject";
    private final static String ID_FLE_TYPE = ".txt";

    @Getter
    private FTPErrorCode ftpErrorCode;

    private FTPClient ftpClient;

    public FTPConnector() {
        ftpClient = new FTPClient();
    }


    public List<String> getServerFileNames() {
        val logger = Logger.getLogger(getClass().getName(), "getServerFileNames");
        List<String> filteredServerFileNames = new ArrayList<>();
        List<String> serverFileNames = getFilesNames();
        if (serverFileNames == null || serverFileNames.isEmpty()) {
            return filteredServerFileNames;
        }
        serverFileNames.stream()
                .filter(serverFileName ->
                        !serverFileName.contains(LOCK_FILE_TYPE) && !serverFileName.contains(ID_FLE_TYPE))
                .forEach(serverFileName -> {
                    filteredServerFileNames.add(serverFileName);
                    logger.log(LogCategory.INFO, "Server file name = '" + serverFileName + "'");
                });
        return filteredServerFileNames;
    }

    public InputStream getInputFileStream(String serverFileName) {
        val logger = Logger.getLogger(getClass().getName(), "getInputFileStream");
        logger.log(LogCategory.DEBUG, "Getting stream of server file '" + serverFileName + "'");
        if (!tryToConnect()) {
            return null;
        }
        if (!tryToLogIn()) {
            return null;
        }
        val serverFilePath = formatServerFilePath(serverFileName);
        val outputStream = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {
            if (!ftpClient.retrieveFile(serverFilePath, outputStream)) {
                ftpErrorCode = FTPErrorCode.FILE_NOT_FOUND;
                logger.log(LogCategory.ERROR,
                        "Unsuccessful retrieving. File = '" + serverFileName + "' wasn't retrieved from server");
            } else {
                logger.log(LogCategory.INFO,
                        "Successful retrieving. File = '" + serverFileName + "' was retrieved from server");
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                outputStream.close();
                }
        } catch (Exception e) {
            ftpErrorCode = FTPErrorCode.FILE_RETRIEVE_ERROR;
            logger.log(LogCategory.ERROR, "Error during getting stream of server file '" + serverFileName + "': " + e);
        } finally {
            disconnect();
        }
        return inputStream;
    }

    public boolean sendFile(InputStream localFileStream, String serverFileName) {
        val logger = Logger.getLogger(getClass().getName(), "sendFile");
        logger.log(LogCategory.DEBUG, "Sending file '" + serverFileName + "' to server");
        if (!tryToConnect()) {
            return false;
        }

        if (!tryToLogIn()) {
            return false;
        }
        val serverFilePath = formatServerFilePath(serverFileName);
        boolean fileStoredSuccessfully = false;
        try {
            fileStoredSuccessfully = ftpClient.storeFile(serverFilePath, localFileStream);
            localFileStream.close();
            if (!fileStoredSuccessfully) {
                ftpErrorCode = FTPErrorCode.FILE_ALREADY_EXIST;
                logger.log(LogCategory.ERROR,
                        "Unsuccessful sending. File = '" + serverFileName + "' wasn't loaded to the server");
            } else {
                logger.log(LogCategory.INFO,
                        "Successful sending. File = '" + serverFileName + "' was loaded to the server");
            }
        } catch (Exception e) {
            fileStoredSuccessfully = false;
            ftpErrorCode = FTPErrorCode.FILE_SENDING_ERROR;
            logger.log(LogCategory.ERROR, "Error during sending file '" + serverFileName + "' to server: " + e);
        } finally {
            disconnect();
        }
        return fileStoredSuccessfully;
    }

    public boolean deleteFile(String serverFileName) {
        if (tryToConnect()) {
            return false;
        }
        if (!tryToLogIn()) {
            return false;
        }
        val logger = Logger.getLogger(getClass().toString(), "deleteFile");
        val serverFilePath = formatServerFilePath(serverFileName);
        boolean fileWasSuccessfullyDeleted = false;
        try {
            fileWasSuccessfullyDeleted = ftpClient.deleteFile(serverFilePath);
            if (!fileWasSuccessfullyDeleted) {
                ftpErrorCode = FTPErrorCode.FILE_NOT_FOUND;
                logger.log(LogCategory.ERROR,
                        "Unsuccessful deleting. File = '" + serverFileName + "' wasn't found on the server");
            } else {
                logger.log(LogCategory.INFO,
                        "Successful deleting. File = '" + serverFileName + "' was deleted from the server");
                if (serverFilePath.contains(LOCK_FILE_TYPE)) {
                    TemporaryDeleteOnExitFiles.removeFile(serverFilePath);
                }
            }
        } catch (Exception e) {
            ftpErrorCode = FTPErrorCode.FILE_DELETING_ERROR;
            logger.log(LogCategory.ERROR, "Error during deleting file + '" + serverFileName + "' from server: " + e);
        } finally {
            disconnect();
        }
        return fileWasSuccessfullyDeleted;
    }

    public List<String> getFilesNames() {
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
            ftpErrorCode = FTPErrorCode.FILE_NAMES_GETTING_ERROR;
            logger.log(LogCategory.ERROR, "Error during getting server files: " + e);
        } finally {
            disconnect();
        }
        return filesNames;
    }


    private boolean tryToConnect() {
        val logger = Logger.getLogger(getClass().getName(), "tryToConnect");
        val config = ConfigProperties.getConfigProperties(ConfigPropertiesSections.FTP);
        try {
            ftpClient.setControlEncoding(UTF_8);
            String host = config.getPropertyValue(CONFIG_HOST_PROPERTY_KEY);
            int port = Integer.parseInt(config.getPropertyValue(CONFIG_PORT_PROPERTY_KEY));
            ftpClient.connect(host, port);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                logger.log(LogCategory.INFO, "Connection succeed");
                return true;
            } else {
                logger.log(LogCategory.ERROR, "Connection failed");
                ftpErrorCode = FTPErrorCode.CONNECTION_FAILED;
                return false;
            }
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error. Connection failed: " + e);
            ftpErrorCode = FTPErrorCode.CONNECTION_FAILED;
            return false;
        }
    }

    private boolean tryToLogIn() {
        val logger = Logger.getLogger(getClass().getName(), "tryToLogIn");
        try {
            val config = ConfigProperties.getConfigProperties(ConfigPropertiesSections.FTP);
            String login = config.getPropertyValue(CONFIG_LOGIN_PROPERTY_KEY);
            String password = config.getPropertyValue(CONFIG_PASSWORD_PROPERTY_KEY);
            val loginWasSuccessful = ftpClient.login(login, password);
            if (loginWasSuccessful) {
                logger.log(LogCategory.INFO, "Logging in succeed");
                return true;
            } else {
                logger.log(LogCategory.ERROR, "Logging in failed");
                ftpErrorCode = FTPErrorCode.LOGIN_FAILED;
                return false;
            }
        } catch (Exception e) {
            logger.log(LogCategory.ERROR, "Error. Logging in failed: " + e);
            ftpErrorCode = FTPErrorCode.LOGIN_FAILED;
            return false;
        }
    }

    private void disconnect() {
        if (!ftpClient.isConnected()) {
            return;
        }
        val logger = Logger.getLogger(getClass().getName(), "disconnect");
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Exception e) {
            ftpErrorCode = FTPErrorCode.DISCONNECTION_FAILED;
            logger.log(LogCategory.ERROR, "Disconnect failed: " + e);
        }
        logger.log(LogCategory.INFO, "Disconnected");
    }


    private String formatServerFilePath(String serverFileName) {
        val config = ConfigProperties.getConfigProperties(ConfigPropertiesSections.FTP);
        val serverFolder = config.getPropertyValue(CONFIG_FOLDER_PROPERTY_KEY);
        return serverFolder + "/" + serverFileName;
    }
}
