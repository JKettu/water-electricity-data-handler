package gui.window.main;

import common.config.ConfigProperties;
import common.config.ConfigPropertiesSections;
import controller.MainWindowController;
import gui.window.BaseWindow;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.awt.*;

import static gui.common.GuiCommonLib.*;

@Getter
public class MainWindow extends BaseWindow<MainWindowController> {
    private static final String SEND_FILE_BUTTON_TEXT_CONFIG_PROPERTY = "main.window.load.file.button.text";
    private static final String DELETE_REGION_BUTTON_TEXT_CONFIG_PROPERTY = "main.window.delete.region.button.text";
    private static final String EXIT_BUTTON_TEXT_CONFIG_PROPERTY = "main.window.exit.button.text";

    private VBox rootBox;
    private Scene scene;

    private Label longTaskInfoTextLabel;

    private Button sendFileButton;
    private Button exitButton;
    private Button deleteRegionButton;

    private VBox progressBarBox;

    private MainWindowLeftBlock leftBlock;
    private MainWindowRightBlock rightBlock;


    public MainWindow() {
        createRootBox();
        buildScene();
        reloadWindowElements();
    }

    @SneakyThrows
    private void buildScene() {
        Dimension screenSize = getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        scene = new Scene(rootBox, screenWidth / 2, screenHeight / 2);
        scene.setFill(Paint.valueOf("gray"));
        val styleResource = MainWindow.class.getResource("/WindowStyle.css");
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
        val sendFileInfoTextBox = wrapNodeToCenteredVBox(longTaskInfoTextLabel);

        val sendFileAndDeleteRegionButtonsBox = new HBox();
        sendFileAndDeleteRegionButtonsBox.getChildren().addAll(sendFileButtonBox, deleteRegionButtonBox);
        sendFileAndDeleteRegionButtonsBox.setAlignment(Pos.CENTER);
        sendFileAndDeleteRegionButtonsBox.setSpacing(50);

        fillRootBox(mainBox,
                sendFileInfoTextBox,
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
        longTaskInfoTextLabel = createNewLineLabel();
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
        mainBox.setSpacing(50);
        mainBox.setAlignment(Pos.CENTER);
        return mainBox;
    }


    private void createRootBox() {
        rootBox = new VBox();
        Dimension screenSize = getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
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
        val configProperties = ConfigProperties.getConfigProperties(ConfigPropertiesSections.GUI);
        val propertyValue = configProperties.getPropertyValue(EXIT_BUTTON_TEXT_CONFIG_PROPERTY);
        exitButton = new Button(propertyValue);
    }

    private void createDeleteRegionButton() {
        val configProperties = ConfigProperties.getConfigProperties(ConfigPropertiesSections.GUI);
        val propertyValue = configProperties.getPropertyValue(DELETE_REGION_BUTTON_TEXT_CONFIG_PROPERTY);
        deleteRegionButton = new Button(propertyValue);
        deleteRegionButton.setDisable(true);
    }

    private void createSendFileButton() {
        val configProperties = ConfigProperties.getConfigProperties(ConfigPropertiesSections.GUI);
        val propertyValue = configProperties.getPropertyValue(SEND_FILE_BUTTON_TEXT_CONFIG_PROPERTY);
        sendFileButton = new Button(propertyValue);
    }


    public void setSendFileInfoText(String text) {
        longTaskInfoTextLabel.setText(text);
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
    }
}
