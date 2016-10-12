package gui.window;

import controller.BaseWindowController;
import lombok.Getter;

@Getter
public abstract class BaseWindow<ControllerType extends BaseWindowController> {
    protected ControllerType controller;

    public void bindController(ControllerType controller)  {
        this.controller = controller;
    }

    public abstract void show();
}
