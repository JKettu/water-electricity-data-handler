package gui.window.main;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;

import static gui.common.GuiCommonLib.createNewLineLabel;

@Getter
public class MainWindowRightBlock extends VBox {
    private static final String SELECT_SERVER_FILE_TEXT_LABEL = "Выберите файл с сервера";
    private static final int SERVER_FILES_BOX_MAX_WIDTH = 250;

    private ComboBox<String> serverFilesBox;

    MainWindowRightBlock() {
        create();
    }

    private void create() {
        Label selectServerFileTextLabel = createSelectServerFileTextLabel();
        createServerFilesBox();
        getChildren().addAll(selectServerFileTextLabel,
                createNewLineLabel(),
                serverFilesBox,
                createNewLineLabel());
    }

    private Label createSelectServerFileTextLabel() {
        return new Label(SELECT_SERVER_FILE_TEXT_LABEL);
    }

    private void createServerFilesBox() {
        serverFilesBox = new ComboBox<>();
        serverFilesBox.valueProperty().addListener((observable, oldValue, newValue) -> {});
        serverFilesBox.setEditable(false);
        serverFilesBox.setMaxWidth(SERVER_FILES_BOX_MAX_WIDTH);
    }

}
