package gui.window;

import controller.SuccessLoadWindowController;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import static gui.common.GuiCommonLib.*;

/**
 * Created by Anton on 20.07.2016.
 */
public class SuccessLoadWindow extends BaseWindow<SuccessLoadWindowController> {

    private VBox rootField;
    private Button againButton;
    private Button exitButton;

    public SuccessLoadWindow(String text) {
        createRootField();
        createExitButton();
        createAgainButton();

        HBox buttonsBox = createButtonsBox();
        HBox labelBox = createLabelBox(text);

        rootField.getChildren().addAll(labelBox, createNewLineLabel(), buttonsBox);
    }

    @Override
    public void bindController(SuccessLoadWindowController controller) {
        super.bindController(controller);

        againButton.setOnMouseClicked(controller.getAgainButtonClickHandler());
        exitButton.setOnMouseClicked(controller.getExitButtonClickHandler());
    }

    public VBox getRootField() {
        return rootField;
    }


    private void createRootField() {
        rootField = new VBox();
    }

    private HBox createLabelBox(String text) {
        HBox labelBox = new HBox();
        Label label = new Label(text);
        labelBox.getChildren().addAll(label);
        labelBox.setAlignment(Pos.CENTER);
        return labelBox;
    }

    private HBox createButtonsBox() {
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(20);
        buttonsBox.getChildren().addAll(againButton, exitButton);
        buttonsBox.setAlignment(Pos.CENTER);
        return buttonsBox;
    }

    private void createExitButton() {
        exitButton = new Button("Выход");
    }

    private void createAgainButton() {
        againButton = new Button("Загрузить новый файл");
    }

}
