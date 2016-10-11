package gui.window.main;

import common.config.ConfigProperties;
import common.config.ConfigPropertiesSections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.val;

import static gui.common.GuiCommonLib.createNewLineLabel;

@Getter
class LoadFileWidget extends VBox {
    private static final String LOAD_FILE_BUTTON_TEXT_CONFIG_PROPERTY = "main.window.load.file.button.text";

    private Label loadFileInfoTextLabel;
    private Button loadFileButton;

    LoadFileWidget() {
        createLoadFileBox();
    }

    private void createLoadFileBox() {
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
        val configProperties = ConfigProperties.getConfigProperties(ConfigPropertiesSections.GUI);
        val propertyValue = configProperties.getPropertyValue(LOAD_FILE_BUTTON_TEXT_CONFIG_PROPERTY);
        loadFileButton = new Button(propertyValue);
    }

}
