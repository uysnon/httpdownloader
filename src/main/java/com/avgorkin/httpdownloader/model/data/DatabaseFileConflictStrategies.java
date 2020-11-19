package com.avgorkin.httpdownloader.model.data;

/**
 * Перечисление стратегий разрешения конфликтов при попытке скачать файл, который уже был ранее скачан
 */
public enum DatabaseFileConflictStrategies {
    /**
     * Стратегия не выбрана
     */
    NOT_ACTIVE,

    /**
     * Заново не качачть, показать файл, который ранее уже был загружен в БД
     */
    SHOW_DATABASE_FILE,

    /**
     * Заменить файл в БД, скачать новую версию
     */
    DOWNLOAD_NEW;
}
