package controller;

import gui.window.BaseWindow;


public abstract class BaseWindowController<WindowType extends BaseWindow> {
    protected WindowType window;
}
