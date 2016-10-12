package gui.window.main;

import controller.MainWindowController;
import gui.window.BaseWindow;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.val;

import static gui.common.GuiCommonLib.*;

public class MainWindow extends BaseWindow<MainWindowController> {
    private static final String SEND_FILE_BUTTON_TEXT = "Отправить";
    private static final String DELETE_REGION_BUTTON_TEXT = "Удалить регион из файла";
    private static final String EXIT_BUTTON_TEXT = "Выход";

    private HBox sendFileAndDeleteRegionButtonsBox;
    private HBox mainBox;
    private HBox exitButtonBox;
    private VBox currentTaskInfoTextBox;

    private VBox rootBox;

    @Getter
    private Scene scene;

    private Label currentTaskInfoTextLabel;

    private Button sendFileButton;
    private Button exitButton;
    private Button deleteRegionButton;

    private VBox progressBarBox;

    private MainWindowLeftBlock leftBlock;
    private MainWindowRightBlock rightBlock;

    @Override
    protected void buildWindow() {
        createRootBox();
        createButtons();
        createLongTaskInfoTextLabel();
        createProgressBarBox();
        createContainers();
        scene = buildScene(rootBox);
    }


    private void createRootBox() {
        rootBox = new VBox();
        val screenSize = getScreenSize();
        val screenWidth = screenSize.getWidth();
        val screenHeight = screenSize.getHeight();
        rootBox.setMinSize(screenHeight / 3, screenWidth / 3);
        rootBox.setAlignment(Pos.CENTER);
        rootBox.layout();
    }

    private void createButtons() {
        createSendFileButton();
        createExitButton();
        createDeleteRegionButton();
    }

    private void createLongTaskInfoTextLabel() {
        currentTaskInfoTextLabel = createNewLineLabel();
    }

    private void createProgressBarBox() {
        progressBarBox = new VBox();
        progressBarBox.setAlignment(Pos.CENTER);
    }

    private void createContainers() {
        createMainBox();
        exitButtonBox = wrapNodeToCenteredHBox(exitButton);
        val sendFileButtonBox = wrapNodeToCenteredHBox(sendFileButton);
        val deleteRegionButtonBox = wrapNodeToCenteredHBox(deleteRegionButton);
        currentTaskInfoTextBox = wrapNodeToCenteredVBox(currentTaskInfoTextLabel);

        sendFileAndDeleteRegionButtonsBox = new HBox();
        sendFileAndDeleteRegionButtonsBox.getChildren().addAll(sendFileButtonBox, deleteRegionButtonBox);
        sendFileAndDeleteRegionButtonsBox.setAlignment(Pos.CENTER);
        sendFileAndDeleteRegionButtonsBox.setSpacing(50);
    }

    private void createMainBox() {
        mainBox = new HBox();
        leftBlock = new MainWindowLeftBlock();
        rightBlock = new MainWindowRightBlock();
        mainBox.getChildren().addAll(leftBlock, rightBlock);
        mainBox.setSpacing(50);
        mainBox.setAlignment(Pos.CENTER);
    }


    private void createExitButton() {
        exitButton = new Button(EXIT_BUTTON_TEXT);
    }

    private void createDeleteRegionButton() {
        deleteRegionButton = new Button(DELETE_REGION_BUTTON_TEXT);
        deleteRegionButton.setDisable(true);
    }

    private void createSendFileButton() {
        sendFileButton = new Button(SEND_FILE_BUTTON_TEXT);
    }


    @Override
    public void show() {
        val windowElements = rootBox.getChildren();
        windowElements.clear();
        rootBox.getChildren().addAll(mainBox,
                currentTaskInfoTextBox,
                progressBarBox,
                createNewLineLabel(),
                sendFileAndDeleteRegionButtonsBox,
                createNewLineLabel(),
                exitButtonBox);
    }

    public void reloadWindowElements() {
        buildWindow();
        show();
    }

    public void clearWindow() {
        val windowElements = rootBox.getChildren();
        windowElements.clear();
    }

    public void setCurrentTaskInfoText(String text) {
        currentTaskInfoTextLabel.setText(text);
    }

    public void setLoadFileInfoText(String text) {
        val loadFileWidget = leftBlock.getLoadFileWidget();
        val loadFileInfoTextLabel = loadFileWidget.getLoadFileInfoTextLabel();
        loadFileInfoTextLabel.setText(text);
    }

    public void showProgressBar() {
        val progressBar = new ProgressBar();
        progressBarBox.getChildren().add(progressBar);
    }

    public void hideProgressBar() {
        progressBarBox.getChildren().clear();
    }


    @Override
    public void bindController(MainWindowController controller) {
        super.bindController(controller);
        val serverFilesBox = rightBlock.getServerFilesBox();
        serverFilesBox.setOnMouseClicked(controller::processServerFilesBoxClick);
        sendFileButton.setOnMouseClicked(controller::processSendFileButtonClick);
        exitButton.setOnMouseClicked(controller::processExitButtonClick);
        deleteRegionButton.setOnMouseClicked(controller::processDeleteRegionButtonClick);
    }
}
