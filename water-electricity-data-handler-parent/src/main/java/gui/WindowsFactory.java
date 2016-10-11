package gui;

import controller.BaseWindowController;
import gui.window.BaseWindow;
import lombok.SneakyThrows;
import lombok.val;

public class WindowsFactory {
    @SneakyThrows
    public static <WindowClass extends BaseWindow<ControllerClass>, ControllerClass extends BaseWindowController<WindowClass>>
    WindowClass CreateWindow(Class<WindowClass> windowClass, Class<ControllerClass> controllerClass) {
        val window = windowClass.newInstance();
        val controller = controllerClass.newInstance();
        window.bindController(controller);
        return window;
    }
}
