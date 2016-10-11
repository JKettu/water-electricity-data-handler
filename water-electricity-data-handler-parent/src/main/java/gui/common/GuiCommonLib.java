package gui.common;


import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

}
