package controller;

import common.CommonUtils;
import common.ConnectionFailedException;
import common.DataType;
import common.logger.LogCategory;
import common.logger.Logger;
import controller.common.CommonControllerMethods;
import gui.common.GuiConstants;
import gui.common.WindowsFactory;
import gui.window.DeleteRegionFromServerFileWindow;
import gui.window.ErrorWindow;
import gui.window.NewServerFileNameInputWindow;
import gui.window.SuccessWindow;
import gui.window.main.MainWindow;
import handling.XlsFileHandler;
import handling.XlsxFileHandler;
import handling.util.HandlingType;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.input.MouseEvent;
import lombok.Setter;
import lombok.val;
import server.FTPController;
import server.LockMonitor;

import java.io.File;
import java.util.ArrayList;
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

    private XlsxFileHandler xlsxFileHandler;
    private File loadedFile;
    private boolean loadedFileReadyForSend;
    private FTPController ftpController;
    private List<String> serverFileNames;
    private XlsFileHandler xlsFileHandler;

    @Setter
    private String selectedServerFileName;

    @Setter
    private DataType selectedDataType = DataType.WATER;

    public MainWindowController() {
        xlsxFileHandler = new XlsxFileHandler();
        serverFileNames = new ArrayList<>();
        serverFileNames.add(GuiConstants.NEW_SERVER_FILE_GUI_TEXT);
        window.setServerFileNames(serverFileNames);
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
        val ftpController = new FTPController();
        try {
            serverFileNames = ftpController.getServerFileNames();
        } catch (ConnectionFailedException e) {
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
            public Void call() throws Exception {
                sendFileDataToServer();
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            enableWindowElements();
            setSuccessLoadWindow();
        });
        new Thread(task).start();
        showLongTaskProcessingInfo(FILE_IS_SENDING);
    }

    private boolean clientFileWasLoadedCorrectly(String loadedFilePath, String loadedFileName) {
        return ((loadedFilePath.matches(".+\\.xls")) || (loadedFilePath.matches(".+\\.xlsx"))) &&
                ((loadedFileName.matches("[В|в]одоснабжение.+")) || (loadedFileName.matches("[Э|э]лектроснабжение.+")));
    }


    private boolean isSelectedFileNameIsNewFile() {
        return selectedServerFileName.equals(GuiConstants.NEW_SERVER_FILE_GUI_TEXT);
    }


    private void createStageForNewFileName() {
        NewServerFileNameInputWindow newServerFileNameInputWindow = new NewServerFileNameInputWindow(window.getScene());
        NewServerFileNameInputWindowController controller =
                new NewServerFileNameInputWindowController(newServerFileNameInputWindow, this);
        newServerFileNameInputWindow.bindController(controller);
    }

    private boolean sendFileDataToServer() {
        if (selectedServerFileName.matches(".+\\.xls")) {
            return sendXlsFileDataToServer();
        } else if (selectedServerFileName.matches(".+\\.xlsx")) {
            return sendXlsxFileDataToServer();
        }
        return false;
    }

    private boolean sendXlsFileDataToServer() {
        xlsFileHandler = new XlsFileHandler(loadedFile, selectedServerFileName);
        if (selectedDataType.equals(DataType.ELECTRICITY)) {
            return xlsFileHandler.processElectricityFileHandling(HandlingType.CREATE);
        } else {
            return xlsFileHandler.processWaterFileHandling(HandlingType.CREATE);
        }
    }

    private boolean sendXlsxFileDataToServer() {
        xlsxFileHandler.setServerFileName(selectedServerFileName);
        xlsxFileHandler.setServerFilePath(ftpController.getServerFolder() + "/" + selectedServerFileName);
        if (selectedDataType.equals(DataType.ELECTRICITY)) {
            if (xlsxFileHandler.WorkWithXlsxFileElectricity(loadedFile)) {
                return true;
            }
        } else {
            if (xlsxFileHandler.WorkWithXlsxFileWater(loadedFile)) {
                return true;
            }
        }
        return false;
    }

    private boolean readServerFile() {
        if (selectedServerFileName.matches(".+\\.xls")) {
            if (readXlsServerFile(selectedServerFileName)) {
                return true;
            }
        } else if (selectedServerFileName.matches(".+\\.xlsx")) {
            if (readXlsxServerFile(selectedServerFileName)) {
                return true;
            }
        }
        return false;
    }

    private boolean readXlsServerFile(String fileFromServerPath) {
        xlsFileHandler = new XlsFileHandler(loadedFile, fileFromServerPath);
        if (selectedDataType.equals(DataType.ELECTRICITY)) {
            return xlsFileHandler.processElectricityFileHandling(HandlingType.MODIFY);
        } else {
            return xlsFileHandler.processWaterFileHandling(HandlingType.MODIFY);
        }
    }

    private boolean readXlsxServerFile(String fileFromServerPath) {
        xlsxFileHandler.setServerFileName(selectedServerFileName);
        if (selectedDataType.equals(DataType.ELECTRICITY)) {
            xlsxFileHandler.ReadFromServerXlsxFileElectricity(fileFromServerPath, loadedFile);
        } else {
            xlsxFileHandler.ReadFromServerXlsxFileWater(fileFromServerPath, loadedFile);
        }
        return false;
    }

    private void setSuccessLoadWindow() {
        if (xlsFileHandler != null) { //если был xls файл
            List<String> xlsFileResultArray = xlsFileHandler.getErrorsArray();
            if (xlsFileResultArray.size() == 0) {
                showSuccessWindow(
                        "Не удалось отправить файл с компьютера на сервер. Закройте файл и попробуте заново.");
                xlsFileHandler.getErrorsArray().clear();
            } else {
                String xslFileFirstError = xlsFileResultArray.get(0);
                switch (xslFileFirstError) {
                    case "is null":
                        showSuccessWindow("Не удалось считать файл с компьютера.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "DISCONNECTED":
                        showSuccessWindow("Соединение с сервером было разорвано.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "LOGIN_FAILED":
                        showSuccessWindow("Не удалось зайти на сервер.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "WRONG_TYPE":
                        showSuccessWindow(
                                "Структура загружаемого файла не совпадает со структурой файла, хранящегося на сервере.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "EMPTY_FILE":
                        showSuccessWindow("Файл не был добавлен на сервер (пустой файл).");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_FROM_SERVER_WAS_NOT_FOUND":
                        showSuccessWindow("Не удалось получить файл с сервера.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "EXISTED_FILE":
                        showSuccessWindow("Этот файл уже был добавлен ранее.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_NOT_LOADED":
                        showSuccessWindow("Не удалось загрузить файл на сервер.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "WRONG_CELL_TYPE":
                        showSuccessWindow("Ошибка при чтении ячейки " + xlsFileResultArray.get(1));
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_WAS_LOADED":
                        showSuccessWindow("Файл был успешно добавлен на сервер.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                }
            }
        } else if (xlsxFileHandler != null) {
            List<String> xlsxFileHandlerErrorsArray = xlsxFileHandler.getErrorsArray();
            if (xlsxFileHandlerErrorsArray.size() == 0) {
                showSuccessWindow(
                        "Не удалось отправить файл с компьютера на сервер. Закройте файл и попробуте заново.");
                xlsxFileHandler.getErrorsArray().clear();
            } else {
                String firstError = xlsxFileHandlerErrorsArray.get(0);
                switch (firstError) {
                    case "is null":
                        showSuccessWindow("Не удалось считать файл с компьютера.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "DISCONNECTED":
                        showSuccessWindow("Соединение с сервером было разорвано.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "LOGIN_FAILED":
                        showSuccessWindow("Не удалось зайти на сервер.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "WRONG_TYPE":
                        showSuccessWindow(
                                "Структура загружаемого файла не совпадает со структурой файла, хранящегося на сервере.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "EMPTY_FILE":
                        showSuccessWindow("Файл не был добавлен на сервер (пустой файл).");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_FROM_SERVER_WAS_NOT_FOUND":
                        showSuccessWindow("Не удалось получить файл с сервера.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "EXISTED_FILE":
                        showSuccessWindow("Этот файл уже был добавлен ранее.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_NOT_LOADED":
                        showSuccessWindow("Не удалось загрузить файл на сервер.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "WRONG_CELL_TYPE":
                        showSuccessWindow("Ошибка при чтении ячейки " + xlsxFileHandlerErrorsArray.get(1));
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_WAS_LOADED":
                        showSuccessWindow("Файл был успешно добавлен на сервер.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                }
            }
        } else {
            showSuccessWindow("Возникла ошибка на сервере. Попробуйте загрузить файл ещё раз.");
        }
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
                    readServerFile();
                } catch (Exception e) {
                    logger.log(LogCategory.ERROR, "Ошибка обработки файлов: " + e);
                    LockMonitor.getLockMonitor().forceDeleteLocks();
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
            setSuccessLoadWindow();
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


/*
    private static final class EventHandlerProducer {
        public EventHandler<MouseEvent> getWaterRadioButtonClickHandler() {
            return event -> {
                RadioButton electricityButton = window.getElectricityRadioButton();
                electricityButton.setSelected(false);
                RadioButton waterButton = window.getWaterRadioButton();
                waterButton.setSelected(true);
                selectedDataType = DataType.WATER;
            };
        }

        public EventHandler<MouseEvent> getElectricityRadioButtonClickHandler() {
            return new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    RadioButton waterButton = window.getWaterRadioButton();
                    waterButton.setSelected(false);
                    RadioButton electricityButton = window.getElectricityRadioButton();
                    electricityButton.setSelected(true);
                    selectedDataType = DataType.ELECTRICITY;
                }
            };
        }

        public EventHandler<MouseEvent> getLoadFileButtonClickHandler() {

            return new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent mouseEvent) {
                    //Загрузка файла с компьютера
                    ExcelFileChooser excelFileChooser = new ExcelFileChooser();
                    int result = excelFileChooser.showDialog(null, "Открыть файл");
                    if (result == ExcelFileChooser.APPROVE_OPTION) {

                        loadedFile = excelFileChooser.getSelectedFile();
                        String loadedFilePath = loadedFile.getAbsolutePath();
                        String loadedFileName = loadedFile.getName();
                        Label loadFileErrorTextLabel = window.getLoadFileErrorTextLabel();
                        Label sendFileInfoTextLabel = window.getCurrentTaskInfoTextLabel();

                        if (clientFileWasLoadedCorrectly(loadedFilePath, loadedFileName)) {
                            loadFileErrorTextLabel.setText("Файл загружен");
                            sendFileInfoTextLabel.setText("");
                            loadedFileReadyForSend = true;
                        } else {
                            loadFileErrorTextLabel.setText("Неверный формат или имя файла");
                            loadedFileReadyForSend = false;
                        }

                    }
                }

            };
        }

        //кнопка "отправить"
        public EventHandler<MouseEvent> getSendButtonClickHandler() {
            return new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    Label sendFileInfoTextLabel = window.getCurrentTaskInfoTextLabel();
                    //Отправка загруженного файла в БД

                    //если не выбрали файл, в который будет идти запись
                    if (!isSelectedFileNameEmpty()) {
                        sendFileInfoTextLabel.setText("Выберите файл для выгрузки данных");
                        return;
                    }
                    //если файл НЕ был загружен
                    if (!loadedFileReadyForSend) {
                        sendFileInfoTextLabel.setText("Выберите файл для загрузки данных");
                        return;
                    }

                    if (((loadedFile.getName().matches("В.+-\\d+\\.xls")) ||
                            (loadedFile.getName().matches("В.+-\\d+\\.xlsx"))) &&
                            (selectedDataType.equals(DataType.ELECTRICITY))) {
                        sendFileInfoTextLabel.setText("Выбранная категория и загруженный файл не совпадают");
                        return;
                    }

                    if (((loadedFile.getName().matches("Э.+-\\d+\\.xls")) ||
                            (loadedFile.getName().matches("Э.+-\\d+\\.xlsx"))) &&
                            (selectedDataType.equals(DataType.WATER))) {
                        sendFileInfoTextLabel.setText("Выбранная категория и загруженный файл не совпадают");
                        return;
                    }

                    if (!loadedFile.exists()) {
                        sendFileInfoTextLabel.setText("Файл с указанным именем не был найден");
                        return;
                    }

                    //если файл есть изначально
                    if (!isSelectedFileNameIsNewFile()) {
                        Task<Void> task = new Task<Void>() {
                            @Override
                            public Void call() {
                                Logger logger = Logger.getLogger(getClass().toString(), "call");
                                try {
                                    readServerFile();
                                } catch (Exception e) {
                                    logger.log(LogCategory.ERROR, "Ошибка обработки файлов: " + e);
                                    LockMonitor.getLockMonitor().forceDeleteLocks();
                                }
                                return null;
                            }

                            @Override
                            protected void failed() {
                                enableWindow();
                                showUnsuccessfulStartWindow("Ошибка обработки файлов");
                            }
                        };

                        task.setOnSucceeded(event1 -> {
                            enableWindow();
                            setSuccessLoadWindow();
                        });
                        new Thread(task).start();
                        showLongTaskProcessingInfo("Файл отправляется...");

                    } else //если файла нет
                    {
                        createStageForNewFileName();
                    }
                    disableWindow();
                }
            };
        }

        public EventHandler<MouseEvent> getExitButtonClickHandler() {
            return event -> {CommonControllerMethods.exit();};
        }

        public EventHandler<MouseEvent> getDeleteRegionButtonClickHandler() {
            return new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (selectedServerFileName == null || selectedServerFileName.isEmpty() ||
                            selectedServerFileName.equals(GuiConstants.NEW_SERVER_FILE_GUI_TEXT)) {
                        window.getCurrentTaskInfoTextLabel().setText("Выберите серверный файл");
                        return;
                    }
                    createStageForRegions();
                }
            };
        }

        public ChangeListener<String> getServerFileNamesComboBoxChangeListener() {
            return new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                    selectedServerFileName = observableValue.getValue();
                    Button deleteButton = window.getDeleteButton();
                    if (selectedServerFileName == null || selectedServerFileName.equals("Новый файл")) {
                        deleteButton.setDisable(true);
                        return;
                    }
                    deleteButton.setDisable(false);
                }
            };
        }
    }
*/
}
