package controller;

import gui.window.BaseWindow;


public class BaseWindowController<WindowType extends BaseWindow> {

    protected WindowType window;

    BaseWindowController(WindowType window) {
        this.window = window;
        window.bindController(this);
    }
}
