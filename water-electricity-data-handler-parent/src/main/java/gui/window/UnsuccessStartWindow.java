package gui.window;


import controller.UnsuccessStartWindowController;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static gui.common.GuiCommonLib.createNewLineLabel;

/**
 * Created by Jay on 07.08.2016.
 */
public class UnsuccessStartWindow extends BaseWindow<UnsuccessStartWindowController> {

    private VBox rootField;
    private Button exitButton;
    private Label errorTextLabel;

    public UnsuccessStartWindow() {
        createRootField();
        createExitButton();

        HBox buttonsBox = createButtonsBox();
        HBox labelBox = createLabelBox();

        rootField.getChildren().addAll(labelBox, createNewLineLabel(), buttonsBox);
    }

    @Override
    public void bindController(UnsuccessStartWindowController controller) {
        super.bindController(controller);
        exitButton.setOnMouseClicked(controller.getExitButtonClickHandler());
        String errorText = controller.getErrorText();
        if(errorText != null) {
            errorTextLabel.setText(errorText);
        }
    }

    public VBox getRootField() {
        return rootField;
    }


    private void createRootField() {
        rootField = new VBox();
    }

    private HBox createLabelBox() {
        HBox labelBox = new HBox();
        errorTextLabel = new Label();
        labelBox.getChildren().addAll(errorTextLabel);
        labelBox.setAlignment(Pos.CENTER);
        return labelBox;
    }

    private HBox createButtonsBox() {
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(20);
        buttonsBox.getChildren().addAll(exitButton);
        buttonsBox.setAlignment(Pos.CENTER);
        return buttonsBox;
    }

    private void createExitButton() {
        exitButton = new Button("Выход");
    }
}
