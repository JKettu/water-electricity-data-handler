package gui.window;

import gui.controller.BaseWindowController;
import lombok.Getter;

@Getter
public abstract class BaseWindow<ControllerType extends BaseWindowController> {
    protected ControllerType controller;

    public BaseWindow() {
        buildWindow();
    }

    public void bindController(ControllerType controller) {
        this.controller = controller;
    }

    public abstract void show();

    protected abstract void buildWindow();
}
