package gui.window;

import controller.MainWindowController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Paint;
import lombok.SneakyThrows;
import lombok.val;

import java.awt.*;

import static gui.common.GuiCommonLib.*;

public class MainWindow extends BaseWindow<MainWindowController> {

    private VBox rootField;
    private Scene scene;
    private RadioButton electricityRadioButton;
    private RadioButton waterRadioButton;
    private Label loadFileErrorTextLabel;
    private Label sendFileInfoTextLabel;
    private Button loadFileButton;
    private ComboBox<String> serverFilesBox;
    private Button sendFileButton;
    private Button exitButton;
    private Button deleteFileButton;
    private VBox sendErrorTextBox;
    private VBox progressBarBox;


    public MainWindow() {
        createRootField();
        buildScene();
        updateWindow();
    }

    @Override
    public void bindController(MainWindowController controller) {
        super.bindController(controller);

        electricityRadioButton.setOnMouseClicked(controller.getElectricityRadioButtonClickHandler());
        waterRadioButton.setOnMouseClicked(controller.getWaterRadioButtonClickHandler());
        loadFileButton.setOnMouseClicked(controller.getLoadFileButtonClickHandler());
        sendFileButton.setOnMouseClicked(controller.getSendButtonClickHandler());
        serverFilesBox.valueProperty().addListener(controller.getServerFileNamesComboBoxChangeListener());
        exitButton.setOnMouseClicked(controller.getExitButtonClickHandler());
        deleteFileButton.setOnMouseClicked(controller.getDeleteRegionButtonClickHandler());
        serverFilesBox.setOnMouseClicked(controller.getServerFileNamesComboboxClickHandler());
    }

    public RadioButton getElectricityRadioButton() {
        return electricityRadioButton;
    }

    public RadioButton getWaterRadioButton() {
        return waterRadioButton;
    }

    public Label getLoadFileErrorTextLabel() {
        return loadFileErrorTextLabel;
    }

    public Label getSendFileInfoTextLabel() {
        return sendFileInfoTextLabel;
    }

    public VBox getSendErrorTextBox() {
        return sendErrorTextBox;
    }

    public Button getLoadFileButton() {
        return loadFileButton;
    }

    public Button getDeleteButton() {
        return deleteFileButton;
    }

    public ComboBox<String> getServerFilesBox() {
        return serverFilesBox;
    }

    public Button getSendFileButton() {
        return sendFileButton;
    }

    public Button getExitButton() {
        return exitButton;
    }

    public VBox getRootField() {
        return rootField;
    }

    public Scene getScene() {
        return scene;
    }


    public void updateWindow() {
        clearWindow();

        createElectricityRadioButton();
        createWaterRadioButton();
        createLoadFileButton();
        createSendFileButton();
        createExitButton();
        createFileErrorTextLabel();
        createSendFileErrorTextLabel();
        createServerFilesBox();
        createSendErrorTextBox();
        createDeleteButton();
        createProgressBarBox();

        HBox mainBox = createMainBox();
        HBox sendButtonBox = wrapButtonToCenteredHBox(sendFileButton);
        HBox exitButtonBox = wrapButtonToCenteredHBox(exitButton);
        HBox deleteButtonBox = wrapButtonToCenteredHBox(deleteFileButton);
        HBox twoButtonsBox = new HBox();
        twoButtonsBox.getChildren().addAll(sendButtonBox, deleteButtonBox);
        twoButtonsBox.setAlignment(Pos.CENTER);
        twoButtonsBox.setSpacing(50);

        updateMainField(mainBox, twoButtonsBox, exitButtonBox);
    }


    @SneakyThrows
    private void buildScene() {
        SceneBuilder sceneBuilder = SceneBuilder.create();
        sceneBuilder.fill(Paint.valueOf("gray"));
        val styleResource = MainWindow.class.getResource("/WindowStyle.css");
        sceneBuilder.stylesheets(styleResource.toExternalForm());
        Dimension screenSize = getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        sceneBuilder.height(screenHeight / 2);
        sceneBuilder.width(screenWidth / 2);
        sceneBuilder.root(rootField);
        scene = sceneBuilder.build();
    }

    private void createRootField() {
        VBoxBuilder vBoxBuilder = VBoxBuilder.create();
        rootField = vBoxBuilder.build();
        Dimension screenSize = getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        rootField.setMinSize(screenHeight / 3, screenWidth / 3);
        rootField.setAlignment(Pos.CENTER);
        rootField.layout();
    }


    private void createSendFileErrorTextLabel() {
        sendFileInfoTextLabel = createNewLineLabel();
    }

    private void createFileErrorTextLabel() {
        loadFileErrorTextLabel = createNewLineLabel(); // меняющаяся надпись с ошибкой
    }

    private void createExitButton() {
        exitButton = new Button("Выход");
    }

    private void createDeleteButton() {
        deleteFileButton = new Button("Удалить регион из файла");
        deleteFileButton.setDisable(true);
    }

    private void createSendFileButton() {
        sendFileButton = new Button("Отправить");
    }

    private void createLoadFileButton() {
        loadFileButton = new Button("Загрузить файл с данными");
    }

    private void createWaterRadioButton() {
        waterRadioButton = new RadioButton("Вода");
        waterRadioButton.setAlignment(Pos.BOTTOM_LEFT);
        waterRadioButton.setSelected(true);
        waterRadioButton.setStyle(".radio");
    }

    private void createElectricityRadioButton() {
        electricityRadioButton = new RadioButton("Электроэнергия");
        electricityRadioButton.setAlignment(Pos.BOTTOM_LEFT);
        electricityRadioButton.setSelected(false);
        electricityRadioButton.setStyle(".radio");
    }

    private void createServerFilesBox() {
        serverFilesBox = new ComboBox<>(); //в списке должны быть файлы с сервера
        serverFilesBox.setEditable(false);
        serverFilesBox.setMaxWidth(250);
    }

    private void createSendErrorTextBox() {
        sendErrorTextBox = new VBox(); // место для ошибки при отправке
        sendErrorTextBox.setAlignment(Pos.CENTER);
        sendErrorTextBox.getChildren().addAll(sendFileInfoTextLabel);
    }

    private void createProgressBarBox(){
        progressBarBox = new VBox();
        progressBarBox.setAlignment(Pos.CENTER);
    }

    public void clearWindow() {
        rootField.getChildren().clear();
    }

    private void updateMainField(HBox mainBox, HBox sendButtonBox, HBox exitButtonBox) {
        rootField.getChildren()
                .addAll(mainBox,
                        sendErrorTextBox,
                        progressBarBox,
                        createNewLineLabel(),
                        sendButtonBox,
                        createNewLineLabel(),
                        exitButtonBox);
    }


    private HBox createMainBox() {
        HBox mainBox = new HBox(); //основная ячейка

        VBox leftBox = createLeftBox();
        VBox rightBox = createRightBox();

        mainBox.getChildren().addAll(leftBox, rightBox);
        mainBox.setSpacing(50);
        mainBox.setAlignment(Pos.CENTER);

        return mainBox;
    }

    private VBox createLeftBox() {
        HBox loadFileErrorTextBox = new HBox(); // место для ошибки при загрузке файла с компьютера
        loadFileErrorTextBox.getChildren().add(loadFileErrorTextLabel);
        loadFileErrorTextBox.setAlignment(Pos.CENTER);

        VBox filesBox = new VBox(); // место для загрузки файлов
        filesBox.getChildren().addAll(loadFileButton, loadFileErrorTextBox);
        filesBox.setAlignment(Pos.CENTER);

        Label selectResourceTextLabel = new Label(
                "Выберите ресурс:\n"); //выбор ресурса. надо запомнить для определения того, по какой структуре работать

        VBox leftBox = new VBox(); //расположение элементов слева
        leftBox.getChildren()
                .addAll(selectResourceTextLabel,
                        waterRadioButton,
                        electricityRadioButton,
                        createNewLineLabel(),
                        filesBox);
        leftBox.setAlignment(Pos.CENTER);

        return leftBox;
    }

    private VBox createRightBox() {
        Label selectServerFileTextLabel = new Label("Выберите файл с сервера");
        VBox rightBox = new VBox(); //расположение элементов справа
        rightBox.getChildren()
                .addAll(selectServerFileTextLabel, createNewLineLabel(), serverFilesBox, createNewLineLabel());
        rightBox.setAlignment(Pos.CENTER);
        return rightBox;
    }

    public VBox getProgressBarBox() {
        return progressBarBox;
    }

}
