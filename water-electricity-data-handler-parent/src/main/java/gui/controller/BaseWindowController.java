package gui.controller;

import gui.window.BaseWindow;
import lombok.Getter;

@Getter
public abstract class BaseWindowController<WindowType extends BaseWindow> {
    protected WindowType window;

    public void showWindow() {
        window.show();
    }
}
