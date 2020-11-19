package com.avgorkin.httpdownloader.model.data;

/**
 * Стадии жизненного цикла загрузки
 */
public enum DownloadStates {
    /**
     * Загрузка только добавлена в список
     */
    NOT_STARTED("ожидает загрузки"),
    /**
     * Загрузка находится в процессе скачивания
     */
    IN_PROGRESS("скачивание"),
    /**
     * Ошибка скачивания
     */
    ERROR("ошибка скачивания"),
    /**
     * Скачивание заврешено успешно
     */
    FINISHED("готово");

    private String title;

    DownloadStates(String title) {
        this.title = title;
    }

    public boolean ifFileAvailableToRead(){
        return this == FINISHED;
    }

    public String getTitle() {
        return title;
    }
}
