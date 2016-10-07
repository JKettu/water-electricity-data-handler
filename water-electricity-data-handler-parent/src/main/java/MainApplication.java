import controller.MainWindowController;
import gui.window.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.val;
import server.ClientService;
import server.LockMonitor;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Загрузка данных по воде и электроэнергии");
        val mainWindow = new MainWindow();
        new MainWindowController(mainWindow);
        primaryStage.setScene(mainWindow.getScene());
        primaryStage.setResizable(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ClientService.unregisterClient();
            LockMonitor.getLockMonitor().forceDeleteLocks();
        }));
        LockMonitor.getLockMonitor();
        ClientService.registerClient();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
