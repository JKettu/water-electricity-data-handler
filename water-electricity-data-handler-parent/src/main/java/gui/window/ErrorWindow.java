package gui.window;


import controller.ErrorWindowController;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.val;

import static gui.common.GuiCommonLib.createNewLineLabel;

public class ErrorWindow extends BaseWindow<ErrorWindowController> {

    private static final String EXIT_BUTTON_TEXT = "Выход";
    private static final int BUTTONS_BOX_SPACING = 20;

    private VBox rootField;
    private Button exitButton;
    private Label errorTextLabel;

    @Override
    public void bindController(ErrorWindowController controller) {
        super.bindController(controller);
        exitButton.setOnMouseClicked(controller.getExitButtonClickHandler());
    }

    @Override
    public void show() {
        createRootField();
        createExitButton();

        val buttonsBox = createButtonsBox();
        val labelBox = createLabelBox();

        rootField.getChildren().addAll(labelBox, createNewLineLabel(), buttonsBox);
    }

    public void setErrorText(String errorText) {
        errorTextLabel.setText(errorText);
    }


    private void createRootField() {
        rootField = new VBox();
    }

    private HBox createLabelBox() {
        val labelBox = new HBox();
        errorTextLabel = new Label();
        labelBox.getChildren().addAll(errorTextLabel);
        labelBox.setAlignment(Pos.CENTER);
        return labelBox;
    }

    private HBox createButtonsBox() {
        val buttonsBox = new HBox();
        buttonsBox.setSpacing(BUTTONS_BOX_SPACING);
        buttonsBox.getChildren().addAll(exitButton);
        buttonsBox.setAlignment(Pos.CENTER);
        return buttonsBox;
    }

    private void createExitButton() {
        exitButton = new Button(EXIT_BUTTON_TEXT);
    }

}
