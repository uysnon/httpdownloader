package com.avgorkin.httpdownloader.model.datahandlers;

/**
 * Слушатель, ожидающий сообщения о завершении скачивании файла
 * (реализуется в Worker-e, сообщения посылают рабочие - производные класса DownloadWorker)
 */
public interface WorkerFinishedDownloadListener {
    void downloadFinished();
}
