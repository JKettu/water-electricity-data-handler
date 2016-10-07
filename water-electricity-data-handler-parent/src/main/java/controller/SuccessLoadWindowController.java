package controller;

import common.DataType;
import controller.common.CommonControllerMethods;
import gui.window.SuccessLoadWindow;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Created by Anton on 20.07.2016.
 */
public class SuccessLoadWindowController extends BaseWindowController<SuccessLoadWindow> {

    private MainWindowController mainWindowController;

    SuccessLoadWindowController(SuccessLoadWindow window, MainWindowController mainWindowController) {
        super(window);
        this.mainWindowController = mainWindowController;
    }

    public EventHandler<MouseEvent> getAgainButtonClickHandler() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mainWindowController.setSelectedDataType(DataType.WATER);
                mainWindowController.updateWindow();
            }
        };
    }

    public EventHandler<MouseEvent> getExitButtonClickHandler() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                CommonControllerMethods.exit();
            }
        };
    }
}
