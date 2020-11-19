package com.avgorkin.httpdownloader.model.datahandlers;

import com.avgorkin.httpdownloader.model.data.Download;
import com.avgorkin.httpdownloader.model.data.DownloadedFile;

/**
 * Слушатель, ожидающий сообщений о скачиваемом файле (реализуется
 * в контроллере, сообщения посылает Downloader)
 */
public interface DownloadNewInfoListener {
    /**
     * Новая информация о скачиваемом файле (например, изменена скорость загрузки)
     * @param download
     */
    void downloadNewInfo(Download download);

    /**
     * Загрузка закончена
     * @param downloadedFile сущность, содержащая временный файл скачанного ресурса
     */
    void downloadFinished(DownloadedFile downloadedFile);

    /**
     * Файл недоступен
     * @param download загрузка, url которой недоступен
     * @param httpResponseCode ответ сервера на попытку доступа к ресурсу,
     *                         -1 при ошибке создания соединения
     */
    void urlIsNotAvailable(Download download, int httpResponseCode);

    void urlIsAvailable(Download download);
}
