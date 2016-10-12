package controller;

import common.DataType;
import controller.common.CommonControllerMethods;
import gui.window.SuccessWindow;
import javafx.scene.input.MouseEvent;
import lombok.Setter;

@Setter
public class SuccessWindowController extends BaseWindowController<SuccessWindow> {

    private MainWindowController mainWindowController;

    void setSuccessText(String text) {
        window.setSuccessText(text);
    }

    public void processAgainButtonClick(MouseEvent mouseEvent) {
        mainWindowController.setSelectedDataType(DataType.WATER);
        mainWindowController.updateWindow();
    }

    public void processExitButtonClick(MouseEvent mouseEvent) {
        CommonControllerMethods.exit();
    }
}
