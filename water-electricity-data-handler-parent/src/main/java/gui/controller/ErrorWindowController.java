package gui.controller;


import gui.controller.common.CommonControllerMethods;
import gui.window.ErrorWindow;
import javafx.scene.input.MouseEvent;
import lombok.Setter;

public class ErrorWindowController extends BaseWindowController<ErrorWindow> {

    @Setter
    private MainWindowController mainWindowController;

    public void setErrorText(String errorText) {
        window.setErrorText(errorText);
    }

    public void processExitButtonClick(MouseEvent mouseEvent) {
        CommonControllerMethods.exit();
    }

}
