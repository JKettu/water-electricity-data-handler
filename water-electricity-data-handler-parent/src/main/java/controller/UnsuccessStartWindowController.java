package controller;


import controller.common.CommonControllerMethods;
import gui.window.UnsuccessStartWindow;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Created by Jay on 07.08.2016.
 */
public class UnsuccessStartWindowController extends BaseWindowController<UnsuccessStartWindow> {

    private String errorText;

    UnsuccessStartWindowController(UnsuccessStartWindow window, String errorText) {
        super(window);
        this.errorText = errorText;
    }

    public EventHandler<MouseEvent> getExitButtonClickHandler() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                CommonControllerMethods.exit();
            }
        };
    }

    public String getErrorText() {
        return errorText;
    }
}
