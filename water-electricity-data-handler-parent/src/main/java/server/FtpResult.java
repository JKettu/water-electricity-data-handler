package server;

public enum FtpResult {
    //Соединение разорвно
    DISCONNECTED,
    //Соединение прошло успешно
    CONNECTED,
    //Вход успешен
    LOGIN,
    //Войти не удалось
    LOGOUT,
}
