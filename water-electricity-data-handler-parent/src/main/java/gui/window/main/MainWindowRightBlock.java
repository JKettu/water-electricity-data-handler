package gui.window.main;

import common.config.ConfigProperties;
import common.config.ConfigPropertiesSections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.val;

import static gui.common.GuiCommonLib.createNewLineLabel;

@Getter
public class MainWindowRightBlock extends VBox {
    private static final String SELECT_SERVER_FILE_PROPERTY = "main.window.select.server.file.label.text";

    private ComboBox serverFilesBox;

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
        val configProperties = ConfigProperties.getConfigProperties(ConfigPropertiesSections.GUI);
        val propertyValue = configProperties.getPropertyValue(SELECT_SERVER_FILE_PROPERTY);
        return new Label(propertyValue);
    }

    private void createServerFilesBox() {
        serverFilesBox = new ComboBox<>();
        serverFilesBox.setEditable(false);
        serverFilesBox.setMaxWidth(250);
    }

}
