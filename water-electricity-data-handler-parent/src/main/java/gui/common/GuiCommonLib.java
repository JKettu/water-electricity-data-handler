package gui.common;


import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.awt.*;

/**
 * Created by Anton on 16.07.2016.
 */
public class GuiCommonLib {

    public static Dimension getScreenSize() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    public static Label createNewLineLabel() {
        return new Label("\n");
    }


    public static HBox wrapButtonToCenteredHBox(Button button) {
        HBox createButtonBox = new HBox();
        createButtonBox.getChildren().add(button);
        createButtonBox.setAlignment(Pos.CENTER);
        return createButtonBox;
    }
}
