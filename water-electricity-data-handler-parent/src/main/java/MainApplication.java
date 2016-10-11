import controller.MainWindowController;
import gui.WindowsFactory;
import gui.window.main.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.val;
import server.ClientService;
import server.LockMonitor;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Загрузка данных по воде и электроэнергии");
        primaryStage.setResizable(false);
        createMainWindow(primaryStage);
        addShutdownHook();
        processStartupActions();
        primaryStage.show();
    }

    private void createMainWindow(Stage primaryStage) {
        val mainWindow = WindowsFactory.CreateWindow(MainWindow.class, MainWindowController.class);
        val mainWindowScene = mainWindow.getScene();
        primaryStage.setScene(mainWindowScene);
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
