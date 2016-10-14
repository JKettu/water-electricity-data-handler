package gui.controller;

import common.CommonUtils;
import common.DataFileType;
import common.DataType;
import common.logger.LogCategory;
import common.logger.Logger;
import file.handling.handler.FileHandler;
import file.handling.util.HandlingType;
import gui.common.GuiConstants;
import gui.common.WindowsFactory;
import gui.controller.common.CommonControllerMethods;
import gui.window.DeleteRegionFromServerFileWindow;
import gui.window.ErrorWindow;
import gui.window.NewServerFileNameInputWindow;
import gui.window.SuccessWindow;
import gui.window.main.MainWindow;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import server.connector.ftp.FTPConnector;
import server.connector.ftp.FTPErrorCode;
import server.connector.lock.LockFileMonitor;

import java.io.File;
import java.util.List;

import static common.CommonUtils.isNullOrEmpty;

public class MainWindowController extends BaseWindowController<MainWindow> {

    private static final String SELECT_FILE_FOR_UPLOADING_TEXT = "Выберите файл для выгрузки данных";
    private static final String LOADING_SERVER_FILES_TEXT = "Загружаем список серверных файлов...";
    private static final String NETWORK_CONNECTION_ERROR_TEXT_LABEL =
            "Нет подключения к Интернету. Выполните подключение и перезапустите программу";
    private static final String SELECT_SERVER_FILE_TEXT = "Выберите серверный файл";
    private static final String SELECT_FILE_FOR_DOWNLOADING_TEXT = "Выберите файл для загрузки данных";
    private static final String WATER_XLS_FILE_PATTERN = "В.+-\\d+\\.xls";
    private static final String WATER_XLSX_FILE_PATTERN = "В.+-\\d+\\.xlsx";
    private static final String ELECTRICITY_XLS_FILE_PATTERN = "Э.+-\\d+\\.xls";
    private static final String ELECTRICITY_XLSX_FILE_PATTERN = "Э.+-\\d+\\.xlsx";
    private static final String SELECTED_FILE_NOT_MATCH_TO_DATA_TYPE_TEXT =
            "Выбранная категория и загруженный файл не совпадают";
    private static final String SELECTED_FILE_NOT_FOUND_TEXT = "Файл с указанным именем не был найден";
    private static final String FILE_HANDLING_ERROR = "Ошибка обработки файлов";
    private static final String FILE_IS_SENDING = "Файл отправляется...";

    @Getter
    private List<String> serverFileNames;

    @Setter
    @Getter
    private String selectedServerFileName;

    @Setter
    @Getter
    private DataType selectedDataType = DataType.WATER;

    @Setter
    @Getter
    private DataFileType dataFileType;

    @Setter
    @Getter
    private File loadedFile;

    @Getter
    private boolean loadedFileReadyForSend;

    public MainWindowController() {
        serverFileNames.add(GuiConstants.NEW_SERVER_FILE_GUI_TEXT);
        window.setServerFileNames(serverFileNames);
    }


    public void processServerFilesBoxClick(MouseEvent mouseEvent) {
        showLongTaskProcessingInfo(LOADING_SERVER_FILES_TEXT);
        val mainWindowRightBlock = window.getRightBlock();
        val serverFilesBox = mainWindowRightBlock.getServerFilesBox();
        serverFilesBox.hide();
        disableWindowElements();
        val task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                reloadServerFileNames();
                return null;
            }
        };
        new Thread(task).start();
        task.setOnSucceeded(workerStateEvent -> {
            ObservableList<String> items = serverFilesBox.getItems();
            if (serverFileNames != null) {
                items.setAll(serverFileNames);
            } else {
                items.clear();
            }
            items.add(GuiConstants.NEW_SERVER_FILE_GUI_TEXT);
            enableWindowElements();
            hideLongTaskProcessingInfo();
            serverFilesBox.show();
        });
        task.setOnFailed(workerStateEvent -> showErrorWindow(NETWORK_CONNECTION_ERROR_TEXT_LABEL));
    }

    public void processExitButtonClick(MouseEvent mouseEvent) {
        CommonControllerMethods.exit();
    }

    public void processDeleteRegionButtonClick(MouseEvent mouseEvent) {
        if (CommonUtils.isNullOrEmpty(selectedServerFileName) ||
                selectedServerFileName.equals(GuiConstants.NEW_SERVER_FILE_GUI_TEXT)) {
            window.setCurrentTaskInfoText(SELECT_SERVER_FILE_TEXT);
            return;
        }
        val deleteRegionWindowController = WindowsFactory
                .createWindow(DeleteRegionFromServerFileWindow.class, DeleteRegionFromServerFileWindowController.class);
        deleteRegionWindowController.setMainWindowController(this);
        deleteRegionWindowController.showWindow();
    }

    public void processSendFileButtonClick(MouseEvent clickEvent) {
        if (!isNullOrEmpty(selectedServerFileName)) {
            window.setCurrentTaskInfoText(SELECT_FILE_FOR_UPLOADING_TEXT);
            return;
        }
        if (!loadedFileReadyForSend) {
            window.setCurrentTaskInfoText(SELECT_FILE_FOR_DOWNLOADING_TEXT);
            return;
        }
        if (!isFileMatchingToData()) {
            window.setCurrentTaskInfoText(SELECTED_FILE_NOT_MATCH_TO_DATA_TYPE_TEXT);
            return;
        }
        if (!loadedFile.exists()) {
            window.setCurrentTaskInfoText(SELECTED_FILE_NOT_FOUND_TEXT);
            return;
        }
        if (isSelectedFileNameIsNewFile()) {
            val newServerFileNameInputWindowController = WindowsFactory
                    .createWindow(NewServerFileNameInputWindow.class, NewServerFileNameInputWindowController.class);
            newServerFileNameInputWindowController.setMainWindowController(this);
            newServerFileNameInputWindowController.showWindow();
            return;
        }

        val sendFileTask = new Task<Void>() {
            @Override
            public Void call() {
                Logger logger =
                        Logger.getLogger(MainWindowController.class.toString(), "processSendFileButtonClick");
                try {
                    val fileHandler = FileHandler.builder()
                            .dataFileType(dataFileType)
                            .dataType(selectedDataType)
                            .localFile(loadedFile)
                            .serverFileName(selectedServerFileName)
                            .build();
                    fileHandler.processFileHandling(HandlingType.MODIFY);
                } catch (Exception e) {
                    logger.log(LogCategory.ERROR, "Ошибка обработки файлов: " + e);
                    LockFileMonitor.getLockMonitor().forceDeleteLocks();
                }
                return null;
            }

            @Override
            protected void failed() {
                showErrorWindow(FILE_HANDLING_ERROR);
            }
        };
        sendFileTask.setOnSucceeded(event -> {
            enableWindowElements();
            hideLongTaskProcessingInfo();
        });
        new Thread(sendFileTask).start();
        showLongTaskProcessingInfo(FILE_IS_SENDING);
        disableWindowElements();
    }


    public void disableWindowElements() {
        window.getSendFileAndDeleteRegionButtonsBox().setDisable(true);
        window.getExitButton().setDisable(true);
        window.getLeftBlock().setDisable(true);
        window.getRightBlock().setDisable(true);
    }

    public void enableWindowElements() {
        window.getSendFileButton().setDisable(false);
        window.getExitButton().setDisable(false);
        window.getLeftBlock().setDisable(false);
        window.getRightBlock().setDisable(false);
        if (CommonUtils.isNullOrEmpty(selectedServerFileName) &&
                !selectedServerFileName.equals(GuiConstants.NEW_SERVER_FILE_GUI_TEXT)) {
            window.getDeleteRegionButton().setDisable(false);
        }
    }


    void showSuccessWindow(String text) {
        window.clearWindow();
        val successWindowController = WindowsFactory.createWindow(SuccessWindow.class, SuccessWindowController.class);
        successWindowController.setMainWindowController(this);
        successWindowController.setSuccessText(text);
        successWindowController.showWindow();
    }

    void showErrorWindow(String errorText) {
        window.clearWindow();
        val errorWindowController = WindowsFactory.createWindow(ErrorWindow.class, ErrorWindowController.class);
        errorWindowController.setErrorText(errorText);
        errorWindowController.showWindow();
    }

    void updateWindow() {
        window.reloadWindowElements();
    }

    void onNewServerFileNameInputted() {
        val task = new Task<Void>() {
            @Override
            public Void call() {
                val fileHandler = FileHandler.builder()
                        .serverFileName(selectedServerFileName)
                        .localFile(loadedFile)
                        .dataType(selectedDataType)
                        .dataFileType(dataFileType)
                        .build();
                fileHandler.processFileHandling(HandlingType.CREATE);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            enableWindowElements();
        });
        new Thread(task).start();
        showLongTaskProcessingInfo(FILE_IS_SENDING);
    }


    private void showLongTaskProcessingInfo(String info) {
        window.setCurrentTaskInfoText(info);
        window.showProgressBar();
    }

    private void hideLongTaskProcessingInfo() {
        window.hideProgressBar();
        window.setCurrentTaskInfoText("");
    }

    private void reloadServerFileNames() {
        val ftpController = new FTPConnector();
        serverFileNames = ftpController.getServerFileNames();
        val ftpErrorCode = ftpController.getFtpErrorCode();
        if (ftpErrorCode.equals(FTPErrorCode.CONNECTION_FAILED) || ftpErrorCode.equals(FTPErrorCode.LOGIN_FAILED)) {
            showErrorWindow(NETWORK_CONNECTION_ERROR_TEXT_LABEL);
        }
    }

    private boolean isFileMatchingToData() {
        String loadedFileName = loadedFile.getName();
        switch (selectedDataType) {
            case WATER:
                return loadedFileName.matches(WATER_XLS_FILE_PATTERN) ||
                        loadedFileName.matches(WATER_XLSX_FILE_PATTERN);
            case ELECTRICITY:
                return loadedFileName.matches(ELECTRICITY_XLS_FILE_PATTERN) ||
                        loadedFileName.matches(ELECTRICITY_XLSX_FILE_PATTERN);
        }
        return false;
    }

    private boolean isSelectedFileNameIsNewFile() {
        return selectedServerFileName.equals(GuiConstants.NEW_SERVER_FILE_GUI_TEXT);
    }
}
