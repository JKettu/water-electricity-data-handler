package gui.window;


import gui.controller.ErrorWindowController;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import static gui.common.GuiCommonLib.createNewLineLabel;

public class ErrorWindow extends BaseWindow<ErrorWindowController> {

    private static final String EXIT_BUTTON_TEXT = "Выход";

    @Getter
    private VBox rootBox;

    private Button exitButton;
    private Label errorTextLabel;
    private HBox buttonsBox;
    private HBox labelBox;

    public void setErrorText(String errorText) {
        errorTextLabel.setText(errorText);
    }

    @Override
    public void bindController(ErrorWindowController controller) {
        super.bindController(controller);
        exitButton.setOnMouseClicked(controller::processExitButtonClick);
    }

    @Override
    public void show() {
        rootBox.getChildren().addAll(labelBox, createNewLineLabel(), buttonsBox);
    }


    @Override
    protected void buildWindow() {
        createRootBox();
        createExitButton();
        createButtonsBox();
        createLabelBox();
    }


    private void createRootBox() {
        rootBox = new VBox();
    }

    private void createLabelBox() {
        labelBox = new HBox();
        errorTextLabel = new Label();
        labelBox.getChildren().addAll(errorTextLabel);
        labelBox.setAlignment(Pos.CENTER);
    }

    private void createButtonsBox() {
        buttonsBox = new HBox();
        buttonsBox.setSpacing(20);
        buttonsBox.getChildren().addAll(exitButton);
        buttonsBox.setAlignment(Pos.CENTER);
    }

    private void createExitButton() {
        exitButton = new Button(EXIT_BUTTON_TEXT);
    }
}
