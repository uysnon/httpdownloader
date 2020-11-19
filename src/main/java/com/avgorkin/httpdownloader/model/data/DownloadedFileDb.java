package com.avgorkin.httpdownloader.model.data;

import lombok.*;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Сущность файла для БД
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "file")
public class DownloadedFileDb {
    /**
     * Бинарное "наполнение" файла
     */
    private byte[] data;
    @Id
    /**
     * url, по которому был скачан файл
     */
    private String url;
    /**
     * Название файла
     */
    private String name;
    /**
     * Расширение
     */
    private String format;

    /**
     * Получение из сущности, созданной Downloader-ом сущности, которую можно записать с БД
     * @param downloadedFile сущность для записи в БД
     */
    public DownloadedFileDb(DownloadedFile downloadedFile) {
        this.url = downloadedFile.getUrl();
        this.name = downloadedFile.getName();
        this.format = downloadedFile.getFormat();
        try {
            this.data = Files.readAllBytes(downloadedFile.getFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получить полное имя файла (имя файла + его расширение)
     * @return полное имя файла
     */
    public String getFullName() {
        if ("".equals(name) && "".equals(format)){
            return "";
        }
        return String.format(
                "%s.%s", name, format);
    }
}
