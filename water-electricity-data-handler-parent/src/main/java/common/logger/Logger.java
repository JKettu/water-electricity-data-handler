package common.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class Logger {
    private FileWriter fileWriter;
    private String className;
    private String methodName;

    private Logger(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
        try {
            Calendar calendar = Calendar.getInstance();
            String year = String.valueOf(calendar.get(Calendar.YEAR));
            String month = String.valueOf(calendar.get(Calendar.MONTH));
            String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String logFilePath = "logs/log_" + year + "." + month + "." + day + ".txt";
            File logFile = new File(logFilePath);
            if (!logFile.exists()) {
                logFile = new File("logs");
                if (!logFile.exists() && !logFile.mkdir()) {
                    throw new IOException("Log file folder creation error");
                }
                logFile = new File(logFilePath);
            }
            fileWriter = new FileWriter(logFile, true);
        } catch (IOException e) {
            System.err.println("Log file writing error: " + e.getMessage());
        }
    }

    synchronized public static Logger getLogger(String className, String methodName) {
        return new Logger(className, methodName);
    }

    synchronized public void log(LogCategory category, String message) {
        try {
            String logLine = formatLogLine(category, message);
            fileWriter.write(logLine);
            fileWriter.flush();
        } catch (IOException e) {
            System.err.println("Log file writing error");
            System.exit(-1);
        }
    }

    private String formatLogLine(LogCategory category, String message) {
        Date date = new Date();
        return "[" + date + "]" +
                "[" + className + "]" +
                "[" + methodName + "]" +
                "[" + category.name() + "]" +
                ": " + message + System.lineSeparator();
    }
}