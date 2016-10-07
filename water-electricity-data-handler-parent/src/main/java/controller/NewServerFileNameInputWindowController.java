package controller;

import gui.window.NewServerFileNameInputWindow;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


import static common.CommonUtils.isNullOrEmpty;

/**
 * Created by Jay on 20.07.2016.
 */
public class NewServerFileNameInputWindowController extends BaseWindowController<NewServerFileNameInputWindow> {

    private MainWindowController mainWindowController;

    NewServerFileNameInputWindowController(NewServerFileNameInputWindow window, final MainWindowController mainWindowController) {
        super(window);
        this.mainWindowController = mainWindowController;
        window.getStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                mainWindowController.enableWindow();
            }
        });

    }

    public EventHandler<MouseEvent> getCreateButtonClickHandler() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
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
            }
        };
    }

    private boolean checkInputtedName(String inputtedFileName) {
        return inputtedFileName.matches(".+\\..+\\.xls");
    }

}
