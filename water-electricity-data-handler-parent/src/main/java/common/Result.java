package common;

public enum Result {
    //Соединение разорвно
    DISCONNECTED,
    //Соединение прошло успешно
    CONNECTED,
    //Вход успешен
    LOGIN,
    //Войти не удалось
    LOGOUT,
    //Файл уже был загружен ранее
    EXISTED_FILE,
    //Не удалось получить файл с сервера
    FILE_FROM_SERVER_WAS_NOT_FOUND,
    //Удалось получить файл с сервера
    FILE_FROM_SERVER_WAS_FOUND,
    //Не удалось загрузить файл на сервер
    FILE_NOT_LOADED,
    //Файл был загружен  на сервер
    FILE_WAS_LOADED,
    //Не совпадает структура таблицы загружаемого файла и файла с сервера
    WRONG_TYPE,
    //Пустой файл
    EMPTY_FILE,
    //Не удалось удалить регион
    FAILED_REGION_DELETION,
    //Удалось удалить регион
    DELETION_SUCCESSED,
    //Не удалось считать ячейку
    WRONG_CELL_TYPE;

}
