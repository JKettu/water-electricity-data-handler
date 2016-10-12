package gui.common;


import gui.window.main.MainWindow;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.val;

import java.awt.*;

public class GuiCommonLib {

    public static Dimension getScreenSize() {
        val toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    public static Label createNewLineLabel() {
        return new Label("\n");
    }

    public static HBox wrapNodeToCenteredHBox(Node node) {
        val createButtonBox = new HBox();
        createButtonBox.getChildren().add(node);
        createButtonBox.setAlignment(Pos.CENTER);
        return createButtonBox;
    }

    public static VBox wrapNodeToCenteredVBox(Node node) {
        val createButtonBox = new VBox();
        createButtonBox.getChildren().add(node);
        createButtonBox.setAlignment(Pos.CENTER);
        return createButtonBox;
    }

    public static Stage createStage(String title, Scene scene, Scene ownerScene) {
        val stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(ownerScene.getWindow());
        stage.show();
        return stage;
    }

    public static Scene buildScene(Parent rootBox) {
        val screenSize = getScreenSize();
        val screenWidth = screenSize.getWidth();
        val screenHeight = screenSize.getHeight();
        val scene = new Scene(rootBox, screenWidth / 2, screenHeight / 2);
        val styleResource = MainWindow.class.getResource(GuiConstants.WINDOW_STYLE_CSS_RESOURCE_PATH);
        scene.setUserAgentStylesheet(styleResource.toExternalForm());
        return scene;
    }
}
