package controller;


import controller.common.CommonControllerMethods;
import gui.window.ErrorWindow;
import javafx.scene.input.MouseEvent;

public class ErrorWindowController extends BaseWindowController<ErrorWindow> {

    public void setErrorText(String errorText) {
        window.setErrorText(errorText);
    }

    public void processExitButtonClick(MouseEvent mouseEvent) {
        CommonControllerMethods.exit();
    }
}
