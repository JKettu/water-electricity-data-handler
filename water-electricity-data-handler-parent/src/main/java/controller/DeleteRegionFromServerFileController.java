package controller;

import com.sun.javafx.collections.ObservableListWrapper;
import common.DataType;
import common.ServerFilesUtils;
import gui.window.DeleteRegionFromServerFileWindow;
import handling.XlsFileHandler;
import handling.util.HandlingType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import server.FTPController;

import java.util.List;

/**
 * Created by Jay on 22.09.2016.
 */
public class DeleteRegionFromServerFileController extends BaseWindowController<DeleteRegionFromServerFileWindow> {
    private final String serverFileName;
    private Integer selectedRegion;
    private MainWindowController mainWindowController;
    private List<Integer> regions;

    DeleteRegionFromServerFileController(DeleteRegionFromServerFileWindow window,
            MainWindowController mainWindowController) {
        super(window);
        this.mainWindowController = mainWindowController;
        serverFileName = mainWindowController.getServerFileName();
    }

    private void disableWindow() {
        window.getRegions().setDisable(true);
        window.getDeleteButton().setDisable(true);
    }

    private void enableWindow() {
        window.getRegions().setDisable(false);
        window.getDeleteButton().setDisable(false);
        window.getProgressBarBox().getChildren().clear();
        window.getErrorTextLabel().setText("");
    }

    public EventHandler<MouseEvent> getDeleteButtonClickHandler() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Label errorTextLabel = window.getErrorTextLabel();
                if (selectedRegion == null) {
                    errorTextLabel.setText("Выберите удаляемый регион");
                } else {
                    createTaskProgressBar("Удаляем регион. Пожалуйста, подождите");
                    disableWindow();

                    Task<Void> task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            XlsFileHandler xlsFileHandler = new XlsFileHandler(selectedRegion, serverFileName);
                            DataType fileType = xlsFileHandler.getHeadlineFromServerFile(serverFileName);
                            if (fileType == null) {
                                xlsFileHandler.getErrorsArray().add("FAILED_REGION_DELETION");
                            } else if (fileType.equals(DataType.WATER)) {
                                xlsFileHandler.processWaterFileHandling(HandlingType.DELETE_REGION);
                            } else if (fileType.equals(DataType.ELECTRICITY)) {
                                xlsFileHandler.processElectricityFileHandling(HandlingType.DELETE_REGION);
                            }
                            return null;
                        }
                    };
                    task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            mainWindowController.showSuccessLoadWindow("Выбранный регион был удалён из файла");
                            window.getStage().close();
                        }
                    });
                    task.setOnFailed(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            mainWindowController.showSuccessLoadWindow("Не удалось удалить регион из файла");
                            window.getStage().close();
                        }
                    });
                    new Thread(task).start();
                }
            }
        };
    }

    private void createTaskProgressBar(final String taskName) {
        window.getErrorTextLabel().setText(taskName);
        ProgressBar progressBar = new ProgressBar();
        progressBar.isIndeterminate();
        VBox progressBarBox = window.getProgressBarBox();
        ObservableList<Node> children = progressBarBox.getChildren();
        children.clear();
        children.addAll(progressBar, new Label("\n"));

    }

    public ChangeListener<Integer> getRegionsComboBoxChangeListener() {
        return new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                selectedRegion = observableValue.getValue();
            }
        };
    }

    public EventHandler<MouseEvent> getRegionsComboBoxClickHandler() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                getRegions();
            }
        };
    }

    private void getRegions() {
        createTaskProgressBar("Получаем список регионов. Пожалуйста, подождите");
        final ComboBox<Integer> regionsComboBox = window.getRegions();
        regionsComboBox.hide();
        disableWindow();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                regions = ServerFilesUtils.getRegions(serverFileName);
                return null;
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                regionsComboBox.setItems(new ObservableListWrapper<>(regions));
                enableWindow();
                regionsComboBox.show();
            }
        });
        task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                mainWindowController.showSuccessLoadWindow("Не удалось получить список регионов");
                window.getStage().close();
            }
        });
        new Thread(task).start();
    }


}
