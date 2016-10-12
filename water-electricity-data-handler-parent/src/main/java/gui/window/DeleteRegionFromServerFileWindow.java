package gui.window;

import controller.DeleteRegionFromServerFileWindowController;
import gui.common.GuiConstants;
import gui.window.main.MainWindow;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.val;

import static gui.common.GuiCommonLib.createNewLineLabel;
import static gui.common.GuiCommonLib.getScreenSize;

public class DeleteRegionFromServerFileWindow extends BaseWindow<DeleteRegionFromServerFileWindowController> {

    private static final String DELETE_REGION_BUTTON_TEXT = "Удалить регион";
    private static final String DELETE_REGION_STAGE_TITLE = "Удаление региона из серверного файла";
    private static final String SELECT_DELETING_REGION_TEXT_LABEL = "Выберите регион, который хотите удалить";

    private VBox rootBox;
    private VBox mainBox;
    private Label currentTaskInfoLabel;
    private Button deleteRegionButton;
    private ComboBox<Integer> regionsComboBox;
    private VBox progressBarBox;
    private Scene scene;
    private Stage stage;

    @Override
    protected void buildWindow() {
        createRootBox();
        buildScene();
        createMainBox();
        rootBox.getChildren().addAll(mainBox);
    }


    private void createMainBox() {
        mainBox = new VBox();

        createErrorTextLabel();
        createRegionDeleteButton();
        createRegionsComboBox();
        createProgressBarBox();

        val deleteButtonBox = new HBox();
        deleteButtonBox.setAlignment(Pos.TOP_CENTER);
        deleteButtonBox.getChildren().add(deleteRegionButton);

        val regionsBox = new HBox();
        regionsBox.setAlignment(Pos.CENTER);
        regionsBox.getChildren().addAll(regionsComboBox);

        val selectDeletingRegionLabel = new Label(SELECT_DELETING_REGION_TEXT_LABEL);
        selectDeletingRegionLabel.setAlignment(Pos.CENTER);

        val selectDeletingRegionLabelBox = new VBox();
        selectDeletingRegionLabelBox.setAlignment(Pos.CENTER);
        selectDeletingRegionLabelBox.getChildren().add(selectDeletingRegionLabel);

        mainBox.setAlignment(Pos.CENTER);
        mainBox.getChildren()
                .addAll(createNewLineLabel(),
                        selectDeletingRegionLabelBox,
                        createNewLineLabel(),
                        regionsBox,
                        createNewLineLabel(),
                        currentTaskInfoLabel,
                        createNewLineLabel(),
                        progressBarBox,
                        deleteButtonBox);
    }

    private void createProgressBarBox() {
        progressBarBox = new VBox();
        progressBarBox.setAlignment(Pos.CENTER);
    }

    private void createErrorTextLabel() {
        currentTaskInfoLabel = new Label();
        currentTaskInfoLabel.setAlignment(Pos.CENTER);
    }

    private void createRegionsComboBox() {
        regionsComboBox = new ComboBox<>(); //в списке должны быть файлы с сервера
        regionsComboBox.setEditable(false);
        regionsComboBox.setMaxWidth(150);
    }

    private void createRootBox() {
        rootBox = new VBox();
        rootBox.layout();
    }

    private void buildScene() {
        val screenSize = getScreenSize();
        val screenWidth = screenSize.width;
        val screenHeight = screenSize.height;

        scene = new Scene(rootBox, screenWidth / 3, screenHeight / 3);
        val styleResource = MainWindow.class.getResource(GuiConstants.WINDOW_STYLE_CSS_RESOURCE_PATH);
        scene.setUserAgentStylesheet(styleResource.toExternalForm());
    }

    private void createRegionDeleteButton() {
        deleteRegionButton = new Button(DELETE_REGION_BUTTON_TEXT);
        deleteRegionButton.setAlignment(Pos.CENTER);
    }


    public void setCurrentTaskInfo(String text) {
        currentTaskInfoLabel.setText(text);
    }

    public void showProgressBar() {
        val progressBar = new ProgressBar();
        progressBarBox.getChildren().addAll(progressBar);
    }

    public void hideProgressBar() {
        progressBarBox.getChildren().clear();
    }

    public void createStage(Scene ownerScene) {
        stage = new Stage();
        stage.setTitle(DELETE_REGION_STAGE_TITLE);
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(ownerScene.getWindow());
        stage.show();
    }


    @Override
    public void bindController(DeleteRegionFromServerFileWindowController controller) {
        super.bindController(controller);
        deleteRegionButton.setOnMouseClicked(controller::processDeleteRegionButtonClick);
        regionsComboBox.valueProperty().addListener(controller::processRegionsComboBoxValueChanging);
        regionsComboBox.setOnMouseClicked(controller::processRegionsComboBoxClick);
    }

    @Override
    public void show() {
        rootBox.getChildren().add(mainBox);
    }

}
