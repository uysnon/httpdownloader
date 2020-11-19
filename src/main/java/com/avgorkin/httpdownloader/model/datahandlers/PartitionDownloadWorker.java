package com.avgorkin.httpdownloader.model.datahandlers;

import lombok.*;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Рабочий, который скачивает "частичку" ресурса
 * "Частичка" определяется startIndex (начальный байт) и endIndex (конечный байт)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartitionDownloadWorker extends DownloadWorker {
    /**
     * Начальный байт ресурса зоны ответственности рабочего
     */
    private int startIndex;
    /**
     * Конечный байт ресурса зоный ответственности рабочего
     */
    private int endIndex;

    @Builder
    public PartitionDownloadWorker(int threadId, String address, int connectTimeout, int readTimeout, File fileToWriteContent, @Singular List<WorkerFinishedDownloadListener> finishDownloadListeners, int startIndex, int endIndex) {
        super(threadId, address, connectTimeout, readTimeout, fileToWriteContent, finishDownloadListeners);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    protected HttpURLConnection buildHttpConnection() {
        URL url = null;
        HttpURLConnection httpConnection = null;
        try {
            url = new URL(getAddress());
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setConnectTimeout(getConnectTimeout());
            httpConnection.setReadTimeout(getReadTimeout());
            httpConnection.setRequestProperty("Range", "bytes:" + startIndex + "-" + endIndex);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return httpConnection;
    }

    @Override
    protected int getStartIndexFileWriting() {
        return startIndex;
    }
}

