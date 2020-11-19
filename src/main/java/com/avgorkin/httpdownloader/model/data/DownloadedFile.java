package com.avgorkin.httpdownloader.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * Скачанный файл - сущность, которую создает Downloader при успешной загрузке файла
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadedFile {
    /**
     * Файл, в который производится загрузка Downloader-ом (является временным, промежуточное звено между
     * интернет-файлом и файлом в БД)
     */
    private File file;
    /**
     * url по которому был скачан файл
     */
    private String url;
    /**
     * название файла
     */
    private String name;
    /**
     * Расширение файла
     */
    private String format;

    public DownloadedFile(Download download) {
        this.url = download.getUrl();
        this.name = download.getFileName();
        this.format = download.getFileFormat();
    }
}
