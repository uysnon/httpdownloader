package com.avgorkin.httpdownloader.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Synchronized;

import java.util.UUID;

/**
 * Загрузка (используется во view и controller-e для управления загрузками в таблице)
 */
@AllArgsConstructor
@Builder
@Data
public class Download {
    /**
     * id
     */
    private UUID id;
    /**
     * url адрес загрузки
     */
    private String url;
    /**
     * Название файла (без расширения)
     */
    private String fileName;
    /**
     * Формат файла
     */
    private String fileFormat;
    /**
     * Выбран ли файл
     */
    private boolean selected;
    /**
     * Текущая скорость скачивания
     */
    private double speedCurrent;
    /**
     * Средняя скорость скачивания
     */
    private double speedAverage;
    /**
     * Статус доступности файла
     */
    private ResourceAvailabilityStates available;
    /**
     * Статус загрузки файла
     */
    private DownloadStates downloadState;
    /**
     * http код, возвращенный при попытке доступа к ресурсу
     */
    private int code;
    /**
     * Были ли обнаружены конфликты с БД при загрузке (напр.: при попытке скачивания была найдена запись с таким же url в БД)
     */
    private boolean conflictWithDatabaseFound;
    /**
     * Стратегия по разрешению конфликта
     */
    private DatabaseFileConflictStrategies strategy;


    public Download(){
        id = UUID.randomUUID();
    }

    @Synchronized
    public UUID getId() {
        return id;
    }

    @Synchronized
    public void setId(UUID id) {
        this.id = id;
    }

    @Synchronized
    public String getUrl() {
        return url;
    }

    @Synchronized
    public void setUrl(String url) {
        this.url = url;
    }

    @Synchronized
    public String getFileName() {
        return fileName;
    }

    @Synchronized
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Synchronized
    public String getFileFormat() {
        return fileFormat;
    }

    @Synchronized
    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    @Synchronized
    public boolean isSelected() {
        return selected;
    }

    @Synchronized
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Synchronized
    public double getSpeedCurrent() {
        return speedCurrent;
    }

    @Synchronized
    public void setSpeedCurrent(double speedCurrent) {
        this.speedCurrent = speedCurrent;
    }

    @Synchronized
    public double getSpeedAverage() {
        return speedAverage;
    }

    @Synchronized
    public void setSpeedAverage(double speedAverage) {
        this.speedAverage = speedAverage;
    }


    public ResourceAvailabilityStates getAvailable() {
        return available;
    }

    public void setAvailable(ResourceAvailabilityStates available) {
        this.available = available;
    }

    public DownloadStates getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(DownloadStates downloadState) {
        this.downloadState = downloadState;
    }

    /**
     * Получить полное имя файла (имя файла + его расширение)
     * @return полное имя файла
     */
    @Synchronized
    public String getFullName() {
        if ("".equals(fileName) && "".equals(fileFormat)){
            return "";
        }
        return String.format(
                "%s.%s", fileName, fileFormat);
    }
}
