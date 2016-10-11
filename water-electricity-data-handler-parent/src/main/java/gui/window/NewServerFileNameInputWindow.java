package gui.window;

import controller.NewServerFileNameInputWindowController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;

import static gui.common.GuiCommonLib.*;

/**
 * Created by Anton on 20.07.2016.
 */
public class NewServerFileNameInputWindow extends BaseWindow<NewServerFileNameInputWindowController> {

    private VBox rootField;
    private Label errorTextLabel;
    private Scene scene;
    private TextField fileNameInputTextField;
    private Button createButton;
    private Stage stage;

    public NewServerFileNameInputWindow(Scene ownerScene) {

        createRootField();
        buildScene();
        //элементы сцены
        VBox main = createMainBox();
        rootField.getChildren().add(main);
        createStage(ownerScene);
    }

    @Override
    public void bindController(NewServerFileNameInputWindowController controller) {
        super.bindController(controller);
        createButton.setOnMouseClicked(controller.getCreateButtonClickHandler());
    }


    public TextField getFileNameInputTextField() {
        return fileNameInputTextField;
    }

    public Label getErrorTextLabel() {
        return errorTextLabel;
    }

    public Stage getStage() {
        return stage;
    }


    private VBox createMainBox() {

        VBox mainBox = new VBox();

        createFileNameInputTextField();
        createErrorTextLabel();
        createCreateButton();
        HBox createCreateButtonBox = wrapNodeToCenteredHBox(createButton);

        HBox inputFileNameLabelBox = new HBox();
        Label label = new Label("    Введите название нового файла.\nВнимание! Название вводится без расширения.\n");
        label.setAlignment(Pos.CENTER);
        inputFileNameLabelBox.getChildren().add(label);
        inputFileNameLabelBox.setAlignment(Pos.CENTER);

        mainBox.getChildren().addAll(new Label("\n\n\n"), inputFileNameLabelBox, fileNameInputTextField, errorTextLabel, createCreateButtonBox);
        mainBox.setAlignment(Pos.CENTER);
        return mainBox;
    }

    private void createErrorTextLabel() {
        errorTextLabel = createNewLineLabel();
        errorTextLabel.setAlignment(Pos.CENTER);
    }

    private void createFileNameInputTextField() {
        fileNameInputTextField = new TextField(); //поле для чтения имени нового файла
        fileNameInputTextField.setPromptText("Введите название файла без расширения");
        fileNameInputTextField.setAlignment(Pos.CENTER);
        fileNameInputTextField.setEditable(true);
        fileNameInputTextField.setMinWidth(250);
        fileNameInputTextField.setMaxWidth(250);
    }

    private void createRootField() {
        VBoxBuilder vBoxBuilder = VBoxBuilder.create();
        rootField = vBoxBuilder.build();
        rootField.layout();
    }

    private void createStage(Scene ownerScene) {
        stage = new Stage();
        stage.setTitle("Создание нового файла");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(ownerScene.getWindow());
        stage.show();
    }

    private void buildScene() {
        SceneBuilder sceneBuilder = SceneBuilder.create();
        sceneBuilder.fill(javafx.scene.paint.Paint.valueOf("gray"));
        sceneBuilder.stylesheets(this.getClass().getResource("WindowStyle.css").toExternalForm());
        Dimension screenSize = getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        sceneBuilder.height(screenHeight / 3);
        sceneBuilder.width(screenWidth / 3);
        sceneBuilder.root(rootField);
        scene = sceneBuilder.build();
    }

    private void createCreateButton() {
        createButton = new Button("Создать");
        createButton.setAlignment(Pos.CENTER);
    }

}
