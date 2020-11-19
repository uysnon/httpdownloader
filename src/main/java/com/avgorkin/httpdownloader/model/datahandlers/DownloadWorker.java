package com.avgorkin.httpdownloader.model.datahandlers;

import lombok.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;

/**
 * "Рабочий", занимается скачиванием файла
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class DownloadWorker implements Runnable {
    /**
     * Размер буфера
     */
    private static final int BUFFER_SIZE = 1024;

    private final Object $lock = new Object[0];

    /**
     * id потока
     */
    private int threadId;
    /**
     * url адрес, из которого скачивается файл
     */
    private String address;
    /**
     * timeout на попытку соединения
     */
    private int connectTimeout;
    /**
     * timeout на чтение
     */
    private int readTimeout;
    /**
     * Файл, в который ведется запись
     */
    private File fileToWriteContent;
    /**
     * Кол-во кб, которое было загружено (считывается Downloader-ом для вычисления скорости скачивания)
     */
    private int infoDownloadedCountKb;

    /**
     * Слушатели, ожидающие информацию о заверении скачивания (в нашем случае слушателем является Downloader)
     */
    private List<WorkerFinishedDownloadListener> finishDownloadListeners;


    public DownloadWorker(int threadId, String address, int connectTimeout, int readTimeout, File fileToWriteContent, @Singular List<WorkerFinishedDownloadListener> finishDownloadListeners) {
        this.threadId = threadId;
        this.address = address;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.fileToWriteContent = fileToWriteContent;
        this.finishDownloadListeners = finishDownloadListeners;
    }

    protected abstract HttpURLConnection buildHttpConnection();

    protected abstract int getStartIndexFileWriting();

    @Synchronized
    public int getInfoDownloadedCountKb() {
        return infoDownloadedCountKb;
    }

    @Synchronized
    public void setInfoDownloadedCountKb(int infoDownloadedCountKb) {
        this.infoDownloadedCountKb = infoDownloadedCountKb;
    }

    @Override
    public void run() {
        HttpURLConnection httpConnection = buildHttpConnection();
        try {
            InputStream inputStream = httpConnection.getInputStream();
            RandomAccessFile raf = new RandomAccessFile(fileToWriteContent, "rw");
            raf.seek(getStartIndexFileWriting());
            byte[] array = new byte[BUFFER_SIZE];
            int len = -1;
            while ((len = inputStream.read(array)) != -1) {
                raf.write(array, 0, len);
                synchronized ($lock) {
                    infoDownloadedCountKb++;
                }
            }
            inputStream.close();
            raf.close();
            httpConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finishDownloadListeners.forEach(WorkerFinishedDownloadListener::downloadFinished);
    }

    @Synchronized
    public void resetInfoDownloadedCounter() {
        infoDownloadedCountKb = 0;
    }
}
