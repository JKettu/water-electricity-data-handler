package controller;

import gui.window.NewServerFileNameInputWindow;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import static common.CommonUtils.isNullOrEmpty;

/**
 * Created by Jay on 20.07.2016.
 */
public class NewServerFileNameInputWindowController extends BaseWindowController<NewServerFileNameInputWindow> {

    private MainWindowController mainWindowController;

    NewServerFileNameInputWindowController(NewServerFileNameInputWindow window, final MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
        window.getStage().setOnCloseRequest(windowEvent -> mainWindowController.enableWindowElements());

    }

    public EventHandler<MouseEvent> getCreateButtonClickHandler() {
        return event -> {
            TextField fileNameInputTextField = window.getFileNameInputTextField();
            Label errorTextLabel = window.getErrorTextLabel();
            String inputtedFileName = fileNameInputTextField.getText();
            if (isNullOrEmpty(inputtedFileName)) {
                errorTextLabel.setText("Введите название файла");
            } else if (checkInputtedName(inputtedFileName)) {
                errorTextLabel.setText("Неверное название файла");
            } else {
                mainWindowController.setSelectedServerFileName(inputtedFileName + ".xls");
                Stage stage = window.getStage();
                stage.close();
                mainWindowController.afterFileCreation();
            }
        };
    }

    private boolean checkInputtedName(String inputtedFileName) {
        return inputtedFileName.matches(".+\\..+\\.xls");
    }

}
