package com.avgorkin.httpdownloader.model.datahandlers;

import com.avgorkin.httpdownloader.model.data.Download;
import com.avgorkin.httpdownloader.model.data.DownloadStates;
import com.avgorkin.httpdownloader.model.data.DownloadedFile;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.Synchronized;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.FilenameUtils;

/**
 * Класс, отвечающий за управлением загрузкой файла
 * (прим. непосредственно загрузкой файла занимается com.avgorkin.httpdownloader.model.datahandlers.DownloadWorker и его производные)
 */
@Data
public class Downloader implements Runnable {
    /**
     * Время, через которое проверяется состояние загрузки
     */
    private static final int SLEEP_TIME_MS = 1000;
    /**
     * Кол-во миллисекунд в 1 секунде
     */
    private static final int MS_IN_S = 1000;

    /**
     * Timeout для попытки соединения
     */
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    /**
     * Timeout для чтения
     */
    private static final int READ_TIMEOUT_MS = 5000;

    /**
     * Для того, чтобы проверить поддерживает ресурс скачивание по частям,
     * ему отправляется запрос с property: "bytes:[начальный байт]-[конечный байт]
     * START_BYTE_FOR_TESTING_RESOURCE отвечает за [начальный байт]
     */
    private static final int START_BYTE_FOR_TESTING_RESOURCE = 0;
    /**
     * см. START_BYTE_FOR_TESTING_RESOURCE
     * отвечает за [конечный байт]
     */
    private static final int END_BYTE_FOR_TESTING_RESOURCE = 1;

    /**
     * Объект синхронизации
     */
    private final Object $lock = new Object[0];

    /**
     * Список слушателей, ожидающих информаци по скачиваемому файлу
     */
    private List<DownloadNewInfoListener> downloadListeners;

    private Download download;
    /**
     * Требуемое число потоков для скачивания файла
     * !!! Если ресурс не поддерживает скачивание по частям, то файл будет скачичваться в 1 потоке
     */
    private int desiredThreadCount;

    /**
     *  Объект, содержащий информацию о временном файле скачанного файла, отправляется слушателям при успешном
     *  скачивании файла
     */
    private DownloadedFile downloadedFile;

    /**
     * Находится ли скачивания в "активной" фазе
     */
    private boolean downloadingInProcess;
    /**
     * Размер файла (в байт)
     */
    private int fileLength;

    /**
     * Скачанная часть файла (в кбайт)
     */
    private int kbDownloaded;

    /**
     * Временный файл, в который выполняется скачивание
     */
    private File tempFile;

    /**
     * Кол-во активных рабочих, когда этот счетчик достигает нуля - считается, что загрузка завершена
     */
    private int activeWorkersCount;

    /**
     * Класс принимает сообщения от рабочих, для обработки этих сообщений реализуется WorkerFinishedDownloadListener
     */
    private WorkerFinishedDownloadListener workerFinishedDownloadListener;

    /**
     * Рабочие, непосредственно скачивают файл (каждый в отдельном потоке)
     */
    private List<DownloadWorker> workers;

    @Builder
    public Downloader(@Singular List<DownloadNewInfoListener> downloadListeners, Download download, int desiredThreadCount) {
        this.downloadListeners = downloadListeners;
        this.download = download;
        this.desiredThreadCount = desiredThreadCount;
        this.workers = new ArrayList<>();
        this.downloadedFile = new DownloadedFile(download);
        this.activeWorkersCount = 0;
        workerFinishedDownloadListener = () ->
        {
            synchronized ($lock) {
                activeWorkersCount--;
            }
        };
    }

    public Downloader() {
        desiredThreadCount = 1;
    }

    @Synchronized
    public int getActiveWorkersCount() {
        return activeWorkersCount;
    }

    @Synchronized
    public void setActiveWorkersCount(int activeWorkersCount) {
        this.activeWorkersCount = activeWorkersCount;
    }

    @Override
    public void run() {
        createEmptyDownloadedFile();
        createWorkers(desiredThreadCount);
        startDownloadingProcess();
        Stopwatch stopwatch = Stopwatch.createStarted();
        while (getActiveWorkersCount() > 0) {
            try {
                Thread.sleep(SLEEP_TIME_MS);
                iterationActions();
                notifyListeners(listener -> listener.downloadNewInfo(download));
                long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                download.setSpeedAverage((double) kbDownloaded / ((double) millis / MS_IN_S));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        stopwatch.stop();
        long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        download.setSpeedAverage((double) fileLength / 1024 / ((double) millis / MS_IN_S));
        notifyListeners(listener -> listener.downloadFinished(downloadedFile));
    }

    private void createEmptyDownloadedFile() {
        try {
            tempFile = File.createTempFile("httpdownloader-", ".tmp");
            downloadedFile.setFile(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tempFile.deleteOnExit();
    }

    private void createWorkers(int threadCount) {
        String address = download.getUrl();
        try {
            HttpURLConnection connectionWithoutParams = null;
            try {
                connectionWithoutParams = HttpConnectionUtils.builder().build().buildConnection(address, CONNECTION_TIMEOUT_MS, READ_TIMEOUT_MS);
            } catch (IllegalArgumentException e) {
                notifyListeners(listener -> listener.urlIsNotAvailable(download, -1));
                return;
            }
            fileLength = connectionWithoutParams.getContentLength();
            int code = connectionWithoutParams.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {

                download.setFileName(FilenameUtils.getBaseName(address));
                download.setFileFormat(FilenameUtils.getExtension(address));
                downloadedFile.setName(FilenameUtils.getBaseName(address));
                downloadedFile.setFormat(FilenameUtils.getExtension(address));

                notifyListeners(listener -> listener.urlIsAvailable(download));
                HttpURLConnection connectionWithProperty = HttpConnectionUtils.builder().build().buildConnection(address, CONNECTION_TIMEOUT_MS, READ_TIMEOUT_MS);
                connectionWithProperty.setRequestProperty("Range", String.format("bytes:%d-%d", START_BYTE_FOR_TESTING_RESOURCE, END_BYTE_FOR_TESTING_RESOURCE));
                if (connectionWithProperty.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    addPartitionDownloadWorkersToWorkers(fileLength, threadCount);
                } else {
                    addFullDownloadWorkerToWorkers();
                }
                connectionWithoutParams.disconnect();
                connectionWithProperty.disconnect();
            } else {
                notifyListeners(listener -> listener.urlIsNotAvailable(download, code));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iterationActions() {
        addInfoDownloaded();
        download.setSpeedCurrent(calculateSpeed());
        resetInfoDownloadedWorkersCounters();
    }

    private void startDownloadingProcess() {
        download.setDownloadState(DownloadStates.IN_PROGRESS);
        for (DownloadWorker worker : workers) {
            new Thread(worker).start();
        }
        downloadingInProcess = true;
    }

    private void addInfoDownloaded() {
        kbDownloaded += workers.stream().mapToInt(DownloadWorker::getInfoDownloadedCountKb).sum();
    }

    private double calculateSpeed() {
        return (double) workers.stream().mapToInt(DownloadWorker::getInfoDownloadedCountKb).sum() /
                ((double) MS_IN_S / SLEEP_TIME_MS);
    }

    private void resetInfoDownloadedWorkersCounters() {
        workers.forEach(DownloadWorker::resetInfoDownloadedCounter);
    }

    private void addFullDownloadWorkerToWorkers() {
        int threadId = 1;
        workers.add(FullDownloadWorker.builder()
                .threadId(threadId)
                .connectTimeout(CONNECTION_TIMEOUT_MS)
                .readTimeout(READ_TIMEOUT_MS)
                .address(download.getUrl())
                .finishDownloadListener(workerFinishedDownloadListener)
                .fileToWriteContent(tempFile)
                .build());
        activeWorkersCount++;
    }

    private void addPartitionDownloadWorkersToWorkers(int fileLength, int threadCount) {
        int blockSize = fileLength / threadCount;
        for (int threadId = 0; threadId < threadCount; threadId++) {
            int startIndex = threadId * blockSize;
            int endIndex = (threadCount + 1) * blockSize - 1;
            if (threadId == threadCount - 1) {
                endIndex = fileLength - 1;
            }

            workers.add(PartitionDownloadWorker.builder()
                    .threadId(threadId)
                    .startIndex(startIndex)
                    .endIndex(endIndex)
                    .connectTimeout(CONNECTION_TIMEOUT_MS)
                    .readTimeout(READ_TIMEOUT_MS)
                    .address(download.getUrl())
                    .finishDownloadListener(workerFinishedDownloadListener)
                    .fileToWriteContent(tempFile)
                    .build());
            activeWorkersCount++;
        }
    }

    private void notifyListeners(Consumer<DownloadNewInfoListener> notifyFunction) {
        for (DownloadNewInfoListener listener : downloadListeners) {
            notifyFunction.accept(listener);
        }
    }
}
