package gui.window;

import controller.BaseWindowController;


public class BaseWindow<ControllerType extends BaseWindowController> {

    protected ControllerType controller;

    public void bindController(ControllerType controller)  {
        this.controller = controller;
    }


}
