package gui.window;

import gui.controller.SuccessWindowController;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import static gui.common.GuiCommonLib.createNewLineLabel;

/**
 * Created by Anton on 20.07.2016.
 */
public class SuccessWindow extends BaseWindow<SuccessWindowController> {

    private static final String LOAD_NEW_FILE_TEXT = "Загрузить новый файл";
    private static final String EXIT_TEXT = "Выход";

    @Getter
    private VBox rootBox;

    private HBox labelBox;
    private HBox buttonsBox;
    private Button againButton;
    private Button exitButton;
    private Label label;


    public void setSuccessText(String text) {
        label.setText(text);
    }

    @Override
    public void bindController(SuccessWindowController controller) {
        super.bindController(controller);

        againButton.setOnMouseClicked(controller::processAgainButtonClick);
        exitButton.setOnMouseClicked(controller::processExitButtonClick);
    }

    @Override
    public void show() {
        rootBox.getChildren().addAll(labelBox, createNewLineLabel(), buttonsBox);
    }


    @Override
    protected void buildWindow() {
        createRootField();
        createExitButton();
        createAgainButton();
        createContainerBoxes();
    }

    private void createRootField() {
        rootBox = new VBox();
    }

    private void createLabelBox() {
        labelBox = new HBox();
        label = new Label();
        labelBox.getChildren().addAll(label);
        labelBox.setAlignment(Pos.CENTER);
    }

    private void createButtonsBox() {
        buttonsBox = new HBox();
        buttonsBox.setSpacing(20);
        buttonsBox.getChildren().addAll(againButton, exitButton);
        buttonsBox.setAlignment(Pos.CENTER);
    }

    private void createExitButton() {
        exitButton = new Button(EXIT_TEXT);
    }

    private void createAgainButton() {
        againButton = new Button(LOAD_NEW_FILE_TEXT);
    }

    private void createContainerBoxes() {
        createButtonsBox();
        createLabelBox();
    }

}
