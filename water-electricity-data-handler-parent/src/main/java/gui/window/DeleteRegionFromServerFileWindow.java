package gui.window;

import controller.DeleteRegionFromServerFileController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;

import static gui.common.GuiCommonLib.getScreenSize;

/**
 * Created by Jay on 22.09.2016.
 */
public class DeleteRegionFromServerFileWindow extends BaseWindow<DeleteRegionFromServerFileController> {

    private VBox rootField;
    private Label errorTextLabel;
    private Scene scene;
    private Button deleteButton;
    private ComboBox<Integer> regions;
    private Stage stage;
    private VBox progressBarBox;

    public DeleteRegionFromServerFileWindow(Scene ownerScene) {
        createRootField();
        buildScene();
        //элементы сцены
        VBox main = createMainBox();
        rootField.getChildren().add(main);
        createStage(ownerScene);
    }


    @Override
    public void bindController(DeleteRegionFromServerFileController controller) {
        super.bindController(controller);
        deleteButton.setOnMouseClicked(controller.getDeleteButtonClickHandler());
        regions.valueProperty().addListener(controller.getRegionsComboBoxChangeListener());
        regions.setOnMouseClicked(controller.getRegionsComboBoxClickHandler());
    }

    public Label getErrorTextLabel() {
        return errorTextLabel;
    }

    public Stage getStage() {
        return stage;
    }

    public VBox getProgressBarBox() {
        return progressBarBox;
    }

    public ComboBox<Integer> getRegions() {
        return regions;
    }


    private VBox createMainBox() {
        VBox mainBox = new VBox();

        createErrorTextLabel();
        createDeleteButton();
        createRegionsComboBox();
        createProgressBarBox();
        HBox createDeleteButtonBox = new HBox();
        createDeleteButtonBox.setAlignment(Pos.TOP_CENTER);
        createDeleteButtonBox.getChildren().add(deleteButton);
        HBox createRegionsBox = new HBox();
        createRegionsBox.setAlignment(Pos.CENTER);
        createRegionsBox.getChildren().addAll(regions);

        VBox inputFileNameLabelBox = new VBox();
        Label label = new Label("Выберите регион, который хотите удалить");
        label.setAlignment(Pos.CENTER);
        inputFileNameLabelBox.getChildren().add(label);
        inputFileNameLabelBox.setAlignment(Pos.CENTER);


        mainBox.getChildren()
                .addAll(createNewLineLabel(),
                        inputFileNameLabelBox,
                        createNewLineLabel(),
                        createRegionsBox,
                        createNewLineLabel(),
                        errorTextLabel,
                        createNewLineLabel(),
                        progressBarBox,
                        createDeleteButtonBox);
        mainBox.setAlignment(Pos.CENTER);
        return mainBox;
    }

    private Label createNewLineLabel() {
        return new Label("\n");
    }

    private void createProgressBarBox() {
        progressBarBox = new VBox();
        progressBarBox.setAlignment(Pos.CENTER);
    }

    private void createErrorTextLabel() {
        errorTextLabel = new Label();
        errorTextLabel.setAlignment(Pos.CENTER);
    }

    private void createRegionsComboBox() {
        regions = new ComboBox<>(); //в списке должны быть файлы с сервера
        regions.setEditable(false);
        regions.setMaxWidth(150);
    }

    private void createRootField() {
        VBoxBuilder vBoxBuilder = VBoxBuilder.create();
        rootField = vBoxBuilder.build();
        rootField.layout();
    }

    private void createStage(Scene ownerScene) {
        stage = new Stage();
        stage.setTitle("Удаление региона из серверного файла");
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

    private void createDeleteButton() {
        deleteButton = new Button("Удалить регион");
        deleteButton.setAlignment(Pos.CENTER);
    }

    public Button getDeleteButton() {
        return deleteButton;
    }
}
