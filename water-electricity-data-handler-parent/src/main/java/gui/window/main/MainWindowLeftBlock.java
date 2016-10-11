package gui.window.main;

import common.config.ConfigProperties;
import common.config.ConfigPropertiesSections;
import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.val;

import static gui.common.GuiCommonLib.createNewLineLabel;

@Getter
class MainWindowLeftBlock extends VBox {
    private static final String WATER_BUTTON_TEXT_CONFIG_PROPERTY = "main.window.water.button.text";
    private static final String ELECTRICITY_BUTTON_TEXT_CONFIG_PROPERTY = "main.window.electricity.button.text";
    private static final String SELECT_RESOURCE_TEXT_LABEL_PROPERTY = "main.window.select.resource.label.text";

    private RadioButton waterRadioButton;
    private RadioButton electricityRadioButton;
    private LoadFileWidget loadFileWidget;

    MainWindowLeftBlock() {
        create();
    }

    private void create() {
        createElectricityRadioButton();
        createWaterRadioButton();
        loadFileWidget = new LoadFileWidget();
        loadFileWidget.setAlignment(Pos.CENTER);

        val configProperties = ConfigProperties.getConfigProperties(ConfigPropertiesSections.GUI);
        val propertyValue = configProperties.getPropertyValue(SELECT_RESOURCE_TEXT_LABEL_PROPERTY);
        val selectResourceTextLabel = new javafx.scene.control.Label(propertyValue + "\n");
        getChildren().addAll(selectResourceTextLabel,
                waterRadioButton,
                electricityRadioButton,
                createNewLineLabel(),
                loadFileWidget);
    }

    private void createWaterRadioButton() {
        val configProperties = ConfigProperties.getConfigProperties(ConfigPropertiesSections.GUI);
        val propertyValue = configProperties.getPropertyValue(WATER_BUTTON_TEXT_CONFIG_PROPERTY);
        waterRadioButton = new RadioButton(propertyValue);
        waterRadioButton.setAlignment(Pos.BOTTOM_LEFT);
        waterRadioButton.setSelected(true);
        waterRadioButton.setStyle(".radio");
    }

    private void createElectricityRadioButton() {
        val configProperties = ConfigProperties.getConfigProperties(ConfigPropertiesSections.GUI);
        val propertyValue = configProperties.getPropertyValue(ELECTRICITY_BUTTON_TEXT_CONFIG_PROPERTY);
        electricityRadioButton = new RadioButton(propertyValue);
        electricityRadioButton.setAlignment(Pos.BOTTOM_LEFT);
        electricityRadioButton.setSelected(false);
        electricityRadioButton.setStyle(".radio");
    }

}
