package com.avgorkin.httpdownloader.controller.utils;


import com.avgorkin.httpdownloader.model.data.Download;

import java.util.List;

/**
 * Обертка над списком загрузок (используется для работы с Thymeleaf)
 */
public class DownloadListWrapper {
    private List<Download> downloadList;

    public DownloadListWrapper() {
    }

    public DownloadListWrapper(List<Download> downloadList) {
        this.downloadList = downloadList;
    }

    public List<Download> getDownloadList() {
        return downloadList;
    }

    public void setDownloadList(List<Download> downloadList) {
        this.downloadList = downloadList;
    }
}
