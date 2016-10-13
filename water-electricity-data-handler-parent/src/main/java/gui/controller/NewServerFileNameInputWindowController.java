package gui.controller;

import gui.window.NewServerFileNameInputWindow;
import javafx.scene.input.MouseEvent;
import lombok.Setter;
import lombok.val;

import static common.CommonUtils.isNullOrEmpty;

@Setter
public class NewServerFileNameInputWindowController extends BaseWindowController<NewServerFileNameInputWindow> {

    private static final String SERVER_FILE_NAME_PATTERN = ".+\\..+\\.xls";
    public static final String INPUT_FILE_NAME_TEXT = "Введите название файла";
    public static final String WRONG_FILE_NAME = "Неверное название файла";

    private MainWindowController mainWindowController;

    NewServerFileNameInputWindowController() {
        window.getStage().setOnCloseRequest(windowEvent -> mainWindowController.enableWindowElements());
    }

    public void processCreateButtonClick(MouseEvent mouseEvent) {
        val fileNameInputTextField = window.getFileNameInputTextField();
        val inputtedFileName = fileNameInputTextField.getText();
        if (isNullOrEmpty(inputtedFileName)) {
            window.setErrorText(INPUT_FILE_NAME_TEXT);
        } else if (inputtedFileName.matches(SERVER_FILE_NAME_PATTERN)) {
            window.setErrorText(WRONG_FILE_NAME);
        } else {
            mainWindowController.setSelectedServerFileName(inputtedFileName + ".xls");
            val stage = window.getStage();
            stage.close();
            mainWindowController.onNewServerFileNameInputted();
        }
    }
}
