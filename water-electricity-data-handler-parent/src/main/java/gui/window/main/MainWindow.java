package gui.window.main;

import controller.MainWindowController;
import gui.common.GuiConstants;
import gui.window.BaseWindow;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import static gui.common.GuiCommonLib.*;

@Getter
public class MainWindow extends BaseWindow<MainWindowController> {
    private static final String SEND_FILE_BUTTON_TEXT = "Отправить";
    private static final String DELETE_REGION_BUTTON_TEXT = "Удалить регион из файла";
    private static final String EXIT_BUTTON_TEXT = "Выход";
    private static final int SEND_FILE_AND_DELETE_REGION_BUTTONS_BOX_SPACING = 50;
    private static final int MAIN_BOX_SPACING = 50;

    private VBox rootBox;
    private Scene scene;

    private Label currentTaskInfoTextLabel;

    private Button sendFileButton;
    private Button exitButton;
    private Button deleteRegionButton;

    private VBox progressBarBox;

    private MainWindowLeftBlock leftBlock;
    private MainWindowRightBlock rightBlock;

    @Override
    public void show() {
        createRootBox();
        buildScene();
        reloadWindowElements();
    }

    @SneakyThrows
    private void buildScene() {
        val screenSize = getScreenSize();
        val screenWidth = screenSize.getWidth();
        val screenHeight = screenSize.getHeight();
        scene = new Scene(rootBox, screenWidth / 2, screenHeight / 2);
        val styleResource = MainWindow.class.getResource(GuiConstants.WINDOW_STYLE_CSS_RESOURCE_PATH);
        scene.setUserAgentStylesheet(styleResource.toExternalForm());
    }

    public void reloadWindowElements() {
        clearWindow();

        createButtons();
        createLongTaskInfoTextLabel();
        createProgressBarBox();

        val mainBox = createMainBox();
        val exitButtonBox = wrapNodeToCenteredHBox(exitButton);
        val sendFileButtonBox = wrapNodeToCenteredHBox(sendFileButton);
        val deleteRegionButtonBox = wrapNodeToCenteredHBox(deleteRegionButton);
        val currentTaskInfoTextBox = wrapNodeToCenteredVBox(currentTaskInfoTextLabel);

        val sendFileAndDeleteRegionButtonsBox = new HBox();
        sendFileAndDeleteRegionButtonsBox.getChildren().addAll(sendFileButtonBox, deleteRegionButtonBox);
        sendFileAndDeleteRegionButtonsBox.setAlignment(Pos.CENTER);
        sendFileAndDeleteRegionButtonsBox.setSpacing(SEND_FILE_AND_DELETE_REGION_BUTTONS_BOX_SPACING);

        fillRootBox(mainBox,
                currentTaskInfoTextBox,
                sendFileAndDeleteRegionButtonsBox,
                exitButtonBox);
    }


    public void clearWindow() {
        val windowElements = rootBox.getChildren();
        windowElements.clear();
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


    private HBox createMainBox() {
        val mainBox = new HBox();
        leftBlock = new MainWindowLeftBlock();
        rightBlock = new MainWindowRightBlock();
        mainBox.getChildren().addAll(leftBlock, rightBlock);
        mainBox.setSpacing(MAIN_BOX_SPACING);
        mainBox.setAlignment(Pos.CENTER);
        return mainBox;
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

    private void fillRootBox(HBox mainBox, VBox sendFileInfoTextBox, HBox sendButtonBox, HBox exitButtonBox) {
        rootBox.getChildren().addAll(mainBox,
                sendFileInfoTextBox,
                progressBarBox,
                createNewLineLabel(),
                sendButtonBox,
                createNewLineLabel(),
                exitButtonBox);
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
