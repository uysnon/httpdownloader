package com.avgorkin.httpdownloader.model.data;

import java.util.UUID;

/**
 * Класс фабрика для создания "шаблонных" объектов класса com.avgorkin.httpdownloader.model.data.Download
 */
public class DownloadsFactory {
    /**
     * Создание пустой загрузки
     * @return пустая загрузка
     */
    public static Download createEmpty(){
        return Download.builder()
                .id(UUID.randomUUID())
                .selected(false)
                .available(ResourceAvailabilityStates.NOT_DEFINED)
                .downloadState(DownloadStates.NOT_STARTED)
                .url("")
                .speedAverage(0)
                .fileName("")
                .fileFormat("")
                .speedCurrent(0)
                .strategy(DatabaseFileConflictStrategies.NOT_ACTIVE)
                .conflictWithDatabaseFound(false)
                .build();
    }
}
