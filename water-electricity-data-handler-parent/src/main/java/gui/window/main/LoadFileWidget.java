package gui.window.main;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.val;

import static gui.common.GuiCommonLib.createNewLineLabel;

@Getter
public class LoadFileWidget extends VBox {
    private static final String LOAD_FILE_BUTTON_TEXT = "Загрузить файл с данными";

    private Label loadFileInfoTextLabel;

    @Getter
    private Button loadFileButton;

    LoadFileWidget() {
        createLoadFileInfoTextLabel();
        createLoadFileButton();
        val loadFileInfoTextBox = new HBox();
        loadFileInfoTextBox.getChildren().add(loadFileInfoTextLabel);
        loadFileInfoTextBox.setAlignment(Pos.CENTER);
        getChildren().addAll(loadFileButton, loadFileInfoTextBox);

    }

    private void createLoadFileInfoTextLabel() {
        loadFileInfoTextLabel = createNewLineLabel();
    }

    private void createLoadFileButton() {
        loadFileButton = new Button(LOAD_FILE_BUTTON_TEXT);
    }

}
