package gui.window;

import gui.controller.NewServerFileNameInputWindowController;
import gui.common.GuiCommonLib;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import static gui.common.GuiCommonLib.createNewLineLabel;
import static gui.common.GuiCommonLib.wrapNodeToCenteredHBox;

public class NewServerFileNameInputWindow extends BaseWindow<NewServerFileNameInputWindowController> {
    @Getter
    private TextField fileNameInputTextField;

    @Getter
    private Label errorTextLabel;

    @Getter
    private Stage stage;

    @Setter
    private Scene ownerScene;

    private static final String INPUT_NEW_FILE_NAME_TEXT_LABEL =
            "    Введите название нового файла.\nВнимание! Название вводится без расширения.\n";
    private static final String INPUT_TEXT_FIELD_PROMPT_TEXT = "Введите название файла без расширения";
    private static final String STAGE_TITLE = "Создание нового файла";
    private static final String CREATE_BUTTON_TEXT = "Создать";

    private VBox rootBox;
    private VBox mainBox;

    private Scene scene;

    private Button createButton;


    public void setErrorText(String text) {
        errorTextLabel.setText(text);
    }

    @Override
    public void bindController(NewServerFileNameInputWindowController controller) {
        super.bindController(controller);
        createButton.setOnMouseClicked(controller::processCreateButtonClick);
    }

    @Override
    public void show() {
        rootBox.getChildren().add(mainBox);
        stage = GuiCommonLib.createStage(STAGE_TITLE, scene, ownerScene);
    }


    @Override
    protected void buildWindow() {
        createRootBox();
        createMainBox();
        scene = GuiCommonLib.buildScene(rootBox);
    }


    private void createMainBox() {
        mainBox = new VBox();

        createFileNameInputTextField();
        createErrorTextLabel();
        createCreateButton();
        val createCreateButtonBox = wrapNodeToCenteredHBox(createButton);

        val inputFileNameLabelBox = new HBox();
        val label = new Label(INPUT_NEW_FILE_NAME_TEXT_LABEL);
        label.setAlignment(Pos.CENTER);
        inputFileNameLabelBox.getChildren().add(label);
        inputFileNameLabelBox.setAlignment(Pos.CENTER);

        mainBox.getChildren().addAll(new Label("\n\n\n"),
                inputFileNameLabelBox,
                fileNameInputTextField,
                errorTextLabel,
                createCreateButtonBox);
        mainBox.setAlignment(Pos.CENTER);
    }

    private void createErrorTextLabel() {
        errorTextLabel = createNewLineLabel();
        errorTextLabel.setAlignment(Pos.CENTER);
    }

    private void createFileNameInputTextField() {
        fileNameInputTextField = new TextField();
        fileNameInputTextField.setPromptText(INPUT_TEXT_FIELD_PROMPT_TEXT);
        fileNameInputTextField.setAlignment(Pos.CENTER);
        fileNameInputTextField.setEditable(true);
        fileNameInputTextField.setMinWidth(250);
        fileNameInputTextField.setMaxWidth(250);
    }

    private void createRootBox() {
        rootBox = new VBox();
        rootBox.layout();
    }

    private void createCreateButton() {
        createButton = new Button(CREATE_BUTTON_TEXT);
        createButton.setAlignment(Pos.CENTER);
    }


}
