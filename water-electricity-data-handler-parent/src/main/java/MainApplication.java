import controller.MainWindowController;
import gui.common.WindowsFactory;
import gui.window.main.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.val;
import server.ClientService;
import server.LockMonitor;

public class MainApplication extends Application {

    private static final String APPLICATION_TITLE = "Загрузка данных по воде и электроэнергии";

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setResizable(false);
        addShutdownHook();
        processStartupActions();
        createMainWindow(primaryStage);
        primaryStage.show();
    }

    private void createMainWindow(Stage primaryStage) {
        val mainWindowController = WindowsFactory.createWindow(MainWindow.class, MainWindowController.class);
        mainWindowController.showWindow();
        val mainWindow = mainWindowController.getWindow();
        primaryStage.setScene(mainWindow.getScene());
    }

    private void processStartupActions() {
        LockMonitor.getLockMonitor().startMonitoring();
        ClientService.registerClient();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ClientService.unregisterClient();
            LockMonitor.getLockMonitor().forceDeleteLocks();
        }));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
