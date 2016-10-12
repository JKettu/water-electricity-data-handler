package controller;


import controller.common.CommonControllerMethods;
import gui.window.ErrorWindow;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class ErrorWindowController extends BaseWindowController<ErrorWindow> {

    public EventHandler<MouseEvent> getExitButtonClickHandler() {
        return event -> CommonControllerMethods.exit();
    }

    public void setErrorText(String errorText){
        window.setErrorText(errorText);
    }

    @Override
    public void showWindow() {
        window.show();
    }
}
