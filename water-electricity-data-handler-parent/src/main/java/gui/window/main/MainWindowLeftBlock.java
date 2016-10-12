package gui.window.main;

import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.val;

import static gui.common.GuiCommonLib.createNewLineLabel;

@Getter
class MainWindowLeftBlock extends VBox {
    private static final String RADIO_BUTTON_STYLE = ".radio";
    private static final String WATER_BUTTON_TEXT = "Вода";
    private static final String ELECTRICITY_BUTTON_TEXT = "Электричество";
    private static final String SELECT_RESOURCE_TEXT_LABEL = "Выберите ресурс:";

    private RadioButton waterRadioButton;
    private RadioButton electricityRadioButton;
    private LoadFileWidget loadFileWidget;

    MainWindowLeftBlock() {
        createElectricityRadioButton();
        createWaterRadioButton();
        loadFileWidget = new LoadFileWidget();
        loadFileWidget.setAlignment(Pos.CENTER);

        val selectResourceTextLabel = new javafx.scene.control.Label(SELECT_RESOURCE_TEXT_LABEL + "\n");
        getChildren().addAll(selectResourceTextLabel,
                waterRadioButton,
                electricityRadioButton,
                createNewLineLabel(),
                loadFileWidget);
    }

    private void createWaterRadioButton() {
        waterRadioButton = new RadioButton(WATER_BUTTON_TEXT);
        waterRadioButton.setAlignment(Pos.BOTTOM_LEFT);
        waterRadioButton.setSelected(true);
        waterRadioButton.setStyle(RADIO_BUTTON_STYLE);
    }

    private void createElectricityRadioButton() {
        electricityRadioButton = new RadioButton(ELECTRICITY_BUTTON_TEXT);
        electricityRadioButton.setAlignment(Pos.BOTTOM_LEFT);
        electricityRadioButton.setSelected(false);
        electricityRadioButton.setStyle(RADIO_BUTTON_STYLE);
    }

}
