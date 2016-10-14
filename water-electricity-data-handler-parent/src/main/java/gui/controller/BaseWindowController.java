package gui.controller;

import gui.window.BaseWindow;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseWindowController<WindowType extends BaseWindow> {
    protected WindowType window;

    public void showWindow() {
        window.show();
    }
}
