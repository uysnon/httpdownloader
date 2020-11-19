package com.avgorkin.httpdownloader.model.datahandlers;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Рабочий, который в одиночку скачивает файл
 * Запускается Worker-ом в тех случаях, когда желаемое кол-во потоков для скачивания = 1
 * или ресурс не поддерживает загрузку по частям
 */
@Data
@NoArgsConstructor
public class FullDownloadWorker extends DownloadWorker {

    @Builder
    public FullDownloadWorker(int threadId, String address, int connectTimeout, int readTimeout, File fileToWriteContent, @Singular List<WorkerFinishedDownloadListener> finishDownloadListeners) {
        super(threadId, address, connectTimeout, readTimeout, fileToWriteContent, finishDownloadListeners);
    }

    @Override
    public void run() {
        System.out.println("thread" + getThreadId() + "Download start");
        super.run();
    }

    @Override
    protected HttpURLConnection buildHttpConnection() {
        URL url = null;
        HttpURLConnection httpConnection = null;
        try {
            url = new URL(getAddress());
            httpConnection = (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return httpConnection;
    }

    @Override
    protected int getStartIndexFileWriting() {
        return 0;
    }
}
