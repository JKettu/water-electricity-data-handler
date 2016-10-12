package controller;

import com.sun.javafx.collections.ObservableListWrapper;
import common.CommonUtils;
import common.ConnectionFailedException;
import common.DataType;
import controller.common.CommonControllerMethods;
import gui.common.GuiConstants;
import gui.common.WindowsFactory;
import gui.window.DeleteRegionFromServerFileWindow;
import gui.window.ErrorWindow;
import gui.window.NewServerFileNameInputWindow;
import gui.window.SuccessLoadWindow;
import gui.window.main.MainWindow;
import handling.XlsFileHandler;
import handling.XlsxFileHandler;
import handling.util.HandlingType;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.NoArgsConstructor;
import lombok.val;
import server.FTPController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static common.CommonUtils.isNullOrEmpty;

@NoArgsConstructor
public class MainWindowController extends BaseWindowController<MainWindow> {

    private static final String LOADING_SERVER_FILES_TEXT_LABEL = "Загружаем список серверных файлов...";
    public static final String NETWORK_CONNECTION_ERROR_TEXT_LABEL =
            "Нет подключения к Интернету. Выполните подключение и перезапустите программу";
    public static final String SELECT_SERVER_FILE_TEXT_LABEL = "Выберите серверный файл";

    private XlsxFileHandler xlsxFileHandler;
    private File loadedFile;
    private boolean loadedFileReadyForSend;
    private String selectedServerFileName;
    private DataType selectedDataType = DataType.WATER;
    private FTPController ftpController;
    private List<String> serverFileNames;
    private XlsFileHandler xlsFileHandler;

    public MainWindowController(MainWindow window) {
        xlsxFileHandler = new XlsxFileHandler();
        serverFileNames = new ArrayList<>();
        ObservableListWrapper<String> serverFileNamesObservableList = new ObservableListWrapper<>(serverFileNames);
        serverFileNamesObservableList.add(GuiConstants.NEW_SERVER_FILE_GUI_TEXT);
        val mainWindowRightBlock = window.getRightBlock();
        ComboBox<String> mainWindowServerFilesBox = mainWindowRightBlock.getServerFilesBox();
        mainWindowServerFilesBox.setItems(serverFileNamesObservableList);
    }

    private void showLongTaskProcessingInfo(String info) {
        window.setCurrentTaskInfoText(info);
        window.showProgressBar();
    }

    private void disableWindowElements() {
        window.getSendFileButton().setDisable(true);

    }

    void enableWindowElements() {
        window.getSendFileButton().setDisable(false);
        //        window.getWaterRadioButton().setDisable(false);
        //        window.getElectricityRadioButton().setDisable(false);
        //        window.getLoadFileButton().setDisable(false);
        //        window.getProgressBarBox().getChildren().clear();
        //        window.getCurrentTaskInfoTextLabel().setText("");
        //        window.getServerFilesBox().setDisable(false);
        //        if (selectedServerFileName != null && !selectedServerFileName.isEmpty() &&
        //                !selectedServerFileName.equals("Новый файл")) {
        //            window.getDeleteButton().setDisable(false);
        //        }

    }


    //Получить список файлов с сервера
    private List<String> getServerFileNames() {
        List<String> serverFileNames = new ArrayList<>();
        val ftpController = new FTPController();
        try {
            serverFileNames = ftpController.getServerFileNames();
        } catch (ConnectionFailedException e) {
            showErrorWindow(NETWORK_CONNECTION_ERROR_TEXT_LABEL);
        }
        return serverFileNames;
    }


    void updateWindow() {
        window.reloadWindowElements();
        window.bindController(this);
    }

    void setSelectedServerFileName(String selectedServerFileName) {
        this.selectedServerFileName = selectedServerFileName;
    }


    //окно после успешной загрузки
    void showSuccessLoadWindow(String textForLabel) {
        window.clearWindow();
        selectedServerFileName = "";
        setLoadedFileEqualsNull();
        SuccessLoadWindow successLoadWindow = new SuccessLoadWindow(textForLabel);
        SuccessLoadWindowController successLoadWindowController =
                new SuccessLoadWindowController(successLoadWindow, this);
        successLoadWindow.bindController(successLoadWindowController);
        VBox successLoadWindowRoot = successLoadWindow.getRootField();//место размещения
        //VBox mainWindowRoot = window.getRootField();
        //mainWindowRoot.getChildren().add(successLoadWindowRoot);
    }

    void afterFileCreation() {
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                sendFileDataToServer();
                return null;
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                enableWindowElements();
                setSuccessLoadWindow();
            }
        });
        new Thread(task).start();
        showLongTaskProcessingInfo("Файл отправляется...");
    }

    String getServerFileName() {
        return selectedServerFileName;
    }


    private void setLoadedFileEqualsNull() {
        loadedFileReadyForSend = false;
        loadedFile = null;
    }

    private boolean clientFileWasLoadedCorrectly(String loadedFilePath, String loadedFileName) {
        return ((loadedFilePath.matches(".+\\.xls")) || (loadedFilePath.matches(".+\\.xlsx"))) &&
                ((loadedFileName.matches("[В|в]одоснабжение.+")) || (loadedFileName.matches("[Э|э]лектроснабжение.+")));
    }

    private boolean isSelectedFileNameEmpty() {
        return !isNullOrEmpty(selectedServerFileName);
    }

    private boolean isSelectedFileNameIsNewFile() {
        return selectedServerFileName.equals(GuiConstants.NEW_SERVER_FILE_GUI_TEXT);
    }


    private void showErrorWindow(String errorText) {
        window.clearWindow();
        selectedServerFileName = "";
        setLoadedFileEqualsNull();
        val errorWindowController = WindowsFactory.createWindow(ErrorWindow.class, ErrorWindowController.class);
        errorWindowController.setErrorText(errorText);
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
                showSuccessLoadWindow(
                        "Не удалось отправить файл с компьютера на сервер. Закройте файл и попробуте заново.");
                xlsFileHandler.getErrorsArray().clear();
            } else {
                String xslFileFirstError = xlsFileResultArray.get(0);
                switch (xslFileFirstError) {
                    case "is null":
                        showSuccessLoadWindow("Не удалось считать файл с компьютера.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "DISCONNECTED":
                        showSuccessLoadWindow("Соединение с сервером было разорвано.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "LOGOUT":
                        showSuccessLoadWindow("Не удалось зайти на сервер.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "WRONG_TYPE":
                        showSuccessLoadWindow(
                                "Структура загружаемого файла не совпадает со структурой файла, хранящегося на сервере.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "EMPTY_FILE":
                        showSuccessLoadWindow("Файл не был добавлен на сервер (пустой файл).");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_FROM_SERVER_WAS_NOT_FOUND":
                        showSuccessLoadWindow("Не удалось получить файл с сервера.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "EXISTED_FILE":
                        showSuccessLoadWindow("Этот файл уже был добавлен ранее.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_NOT_LOADED":
                        showSuccessLoadWindow("Не удалось загрузить файл на сервер.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "WRONG_CELL_TYPE":
                        showSuccessLoadWindow("Ошибка при чтении ячейки " + xlsFileResultArray.get(1));
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_WAS_LOADED":
                        showSuccessLoadWindow("Файл был успешно добавлен на сервер.");
                        xlsFileHandler.getErrorsArray().clear();
                        break;
                }
            }
        } else if (xlsxFileHandler != null) {
            List<String> xlsxFileHandlerErrorsArray = xlsxFileHandler.getErrorsArray();
            if (xlsxFileHandlerErrorsArray.size() == 0) {
                showSuccessLoadWindow(
                        "Не удалось отправить файл с компьютера на сервер. Закройте файл и попробуте заново.");
                xlsxFileHandler.getErrorsArray().clear();
            } else {
                String firstError = xlsxFileHandlerErrorsArray.get(0);
                switch (firstError) {
                    case "is null":
                        showSuccessLoadWindow("Не удалось считать файл с компьютера.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "DISCONNECTED":
                        showSuccessLoadWindow("Соединение с сервером было разорвано.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "LOGOUT":
                        showSuccessLoadWindow("Не удалось зайти на сервер.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "WRONG_TYPE":
                        showSuccessLoadWindow(
                                "Структура загружаемого файла не совпадает со структурой файла, хранящегося на сервере.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "EMPTY_FILE":
                        showSuccessLoadWindow("Файл не был добавлен на сервер (пустой файл).");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_FROM_SERVER_WAS_NOT_FOUND":
                        showSuccessLoadWindow("Не удалось получить файл с сервера.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "EXISTED_FILE":
                        showSuccessLoadWindow("Этот файл уже был добавлен ранее.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_NOT_LOADED":
                        showSuccessLoadWindow("Не удалось загрузить файл на сервер.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "WRONG_CELL_TYPE":
                        showSuccessLoadWindow("Ошибка при чтении ячейки " + xlsxFileHandlerErrorsArray.get(1));
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                    case "FILE_WAS_LOADED":
                        showSuccessLoadWindow("Файл был успешно добавлен на сервер.");
                        xlsxFileHandler.getErrorsArray().clear();
                        break;
                }
            }
        } else {
            showSuccessLoadWindow("Возникла ошибка на сервере. Попробуйте загрузить файл ещё раз.");
        }
    }

    private void createDeleteRegionStage() {
        val deleteRegionWindowController = WindowsFactory
                .createWindow(DeleteRegionFromServerFileWindow.class, DeleteRegionFromServerFileWindowController.class);
        deleteRegionWindowController.setMainWindowController(this);
        deleteRegionWindowController.showWindow();
    }


    public void processServerFilesBoxClick(MouseEvent mouseEvent) {
        showLongTaskProcessingInfo(LOADING_SERVER_FILES_TEXT_LABEL);
        val mainWindowRightBlock = window.getRightBlock();
        val serverFilesBox = mainWindowRightBlock.getServerFilesBox();
        serverFilesBox.hide();
        disableWindowElements();
        val task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                serverFileNames = getServerFileNames();
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
            window.setCurrentTaskInfoText(SELECT_SERVER_FILE_TEXT_LABEL);
            return;
        }
        createDeleteRegionStage();
    }

    public void processSendFileButtonClick(MouseEvent clickEvent) {

    }

    @Override
    public void showWindow() {
        window.show();
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
