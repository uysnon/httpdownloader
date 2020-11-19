package com.avgorkin.httpdownloader.controller;

import com.avgorkin.httpdownloader.controller.utils.DownloadListWrapper;
import com.avgorkin.httpdownloader.model.data.*;
import com.avgorkin.httpdownloader.model.datahandlers.DownloadNewInfoListener;
import com.avgorkin.httpdownloader.model.datahandlers.Downloader;
import com.avgorkin.httpdownloader.model.datasourcers.JpaDownloadedFileRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class DownloadsController {
    /**
     * Список загрузок
     */
    private List<Download> downloads;
    /**
     * Реализация слушателя, ожидающего сообщения о новой информации скаччиваемых файлов
     * (Сообщения отправляются Downloader-ом)
     */
    private DownloadNewInfoListener downloadNewInfoListener;
    /**
     * Кол-во потоков для скачивания
     */
    private int downloadThreadCount;

    /**
     * Jpa репозиторий скачанных файлов
     */
    @Autowired
    private JpaDownloadedFileRepository repository;


    public DownloadsController() {
        downloadThreadCount = 1;
        downloads = new ArrayList<>();
        /*
        Начальные загрузки, можно было и выбросить
         */
        Download download1 = DownloadsFactory.createEmpty();
        download1.setUrl("https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_1MG.mp3");
        Download download2 = DownloadsFactory.createEmpty();
        download2.setUrl("https://file-examples-com.github.io/uploads/2017/10/file-example_PDF_1MB.pdf");
        Download download3 = DownloadsFactory.createEmpty();
        download3.setUrl("https://upload.wikimedia.org/wikipedia/commons/d/d2/Internet_map_1024.jpg");


        downloads.add(download1);
        downloads.add(download2);
        downloads.add(download3);

        downloadNewInfoListener = new DownloadNewInfoListener() {
            @Override
            public void downloadNewInfo(Download download) {
                int a = 1;
            }

            @Override
            public void downloadFinished(DownloadedFile downloadedFile) {
                DownloadedFileDb fileDb = new DownloadedFileDb(downloadedFile);
                repository.save(fileDb);
                getDownloadByUrl(downloadedFile.getUrl()).setDownloadState(DownloadStates.FINISHED);
            }

            @Override
            public void urlIsNotAvailable(Download download, int httpResponseCode) {
                download.setCode(httpResponseCode);
                download.setAvailable(ResourceAvailabilityStates.NOT_AVAILABILE);
            }

            @Override
            public void urlIsAvailable(Download download) {
                download.setCode(HttpURLConnection.HTTP_OK);
                download.setAvailable(ResourceAvailabilityStates.AVAILABLE);
            }
        };
    }

    /**
     * Получение начальной страницы
     * @param model модель
     * @return начальная страница
     */
    @GetMapping("/")
    public String getIndexPage(Model model) {
        DownloadListWrapper wrapper = new DownloadListWrapper((downloads));
        model.addAttribute("downloadThreadCount", downloadThreadCount);
        model.addAttribute("wrapper", wrapper);
        return "index";
    }

    /**
     * Получение html фрагмента для динамически-изменяющейся части загрузок
     * (напр.: скорость, состояние, доступность и др.)
     * @param model модель
     * @return фрагмент динамически-изменяющейся части загрузок
     */
    @GetMapping("/downloadList")
    public String getIndexPageDownloadList(Model model) {
        DownloadListWrapper wrapper = new DownloadListWrapper((downloads));
        model.addAttribute("wrapper", wrapper);
        return "index :: downloadList";
    }

    /**
     * Открыть файл
     * @param id id, открываемого файла (id загрузки)
     * @return бинарный ресурс
     */
    @GetMapping("/openFile/{id}")
    public ResponseEntity<ByteArrayResource> openFile(@PathVariable(value = "id") UUID id) {
        DownloadedFileDb downloadedFileDb = repository.findById(
                downloads.stream()
                        .filter(download -> download.getId().equals(id))
                        .map(Download::getUrl)
                        .findAny()
                        .orElse(null)
        ).orElse(null);
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + downloadedFileDb.getFullName());
        ByteArrayResource resource = new ByteArrayResource(downloadedFileDb.getData());
        String contentType = "";
        try {
            contentType = Files.probeContentType(Paths.get(downloadedFileDb.getName() + "." + downloadedFileDb.getFormat()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaType mediaType = null;
        try {
            mediaType = MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException e) {
            mediaType = MediaType.MULTIPART_FORM_DATA;
        }

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(downloadedFileDb.getData().length)
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * Удалить выделенные строки в таблице
     * @param model модель
     * @param wrapper обертка над списком загрузок
     * @param request запрос
     * @return redirect на главную страницу
     */
    @PostMapping(value = "/downloadsTableAction", params = "deleteRows")
    public String deleteRowsFromTable(Model model, @ModelAttribute DownloadListWrapper wrapper,
                                      HttpServletRequest request
    ) {
        updateDownloadsByViewWrapper(wrapper);
        downloads.removeIf(Download::isSelected);
        downloadThreadCount = Integer.parseInt(request.getParameter("downloadThreadCount"));
        return "redirect:/";
    }

    /**
     * Добавить пустую строку в таблицу
     * @param wrapper обертка над списком загрузок
     * @param request запрос
     * @return redirect на главную страницу
     */
    @PostMapping(value = "/downloadsTableAction", params = "addRow")
    public String addRow(@ModelAttribute DownloadListWrapper wrapper,
                         HttpServletRequest request) {
        updateDownloadsByViewWrapper(wrapper);
        downloadThreadCount = Integer.parseInt(request.getParameter("downloadThreadCount"));
        downloads.add(DownloadsFactory.createEmpty());
        return "redirect:/";
    }

    /**
     * Скачать выделенные элементы
     * @param wrapper обертка над спискоом загрузок
     * @param request запрос
     * @return redirect на главную страницу
     */
    @PostMapping(value = "/downloadsTableAction", params = "download")
    public String downloadFiles(@ModelAttribute DownloadListWrapper wrapper,
                                HttpServletRequest request) {
        updateDownloadsByViewWrapper(wrapper);
        downloadThreadCount = Integer.parseInt(request.getParameter("downloadThreadCount"));

        downloads.stream().filter(Download::isSelected).forEach(download -> {
            downloadFile(download);
            download.setSelected(false);
        });
        return "redirect:/";
    }

    /**
     * Разрешение конфликта с загружаемом файлом, поведение: не обновлять запись в БД
     * @param id id загрузки
     * @return redirect на главную страницу
     */
    @PostMapping(value = "/conflictWithDatabase/{id}", params = "notUpdate")
    public String conflictResolveNotUpdate(@PathVariable(value = "id") UUID id) {
        Download download = getDownloadById(id);
        download.setConflictWithDatabaseFound(false);
        download.setStrategy(DatabaseFileConflictStrategies.SHOW_DATABASE_FILE);
        download.setDownloadState(DownloadStates.FINISHED);
        download.setFileName(FilenameUtils.getBaseName(download.getUrl()));
        download.setFileFormat(FilenameUtils.getExtension(download.getUrl()));
        return "redirect:/";
    }

    /**
     * Разрешение конфликта с загружаемом файлом, поведение: обновить запись в БД
     * @param id id загрузки
     * @return redirect на главную страницу
     */
    @PostMapping(value = "/conflictWithDatabase/{id}", params = "update")
    public String conflictResolveUpdate(@PathVariable(value = "id") UUID id) {
        Download download = getDownloadById(id);
        download.setConflictWithDatabaseFound(false);
        download.setStrategy(DatabaseFileConflictStrategies.DOWNLOAD_NEW);
        downloadFile(download);
        return "redirect:/";
    }

    private void updateDownloadsByViewWrapper(DownloadListWrapper wrapper) {
        downloads.stream().forEach(download -> {
            wrapper.getDownloadList()
                    .stream()
                    .filter(element -> element.getId().equals(download.getId()))
                    .findAny()
                    .ifPresent(downloadFromView -> {
                        download.setSelected(downloadFromView.isSelected());
                        download.setUrl(downloadFromView.getUrl());
                    });
        });
    }

    private void downloadFile(Download download) {
        if (repository.findById(download.getUrl()).isPresent()) {
            if (download.getStrategy() == DatabaseFileConflictStrategies.NOT_ACTIVE) {
                download.setConflictWithDatabaseFound(true);
                return;
            } else if (download.getStrategy() == DatabaseFileConflictStrategies.SHOW_DATABASE_FILE) {
                download.setDownloadState(DownloadStates.FINISHED);
                download.setStrategy(DatabaseFileConflictStrategies.NOT_ACTIVE);
                return;
            }
        }
        download.setStrategy(DatabaseFileConflictStrategies.NOT_ACTIVE);
        Downloader downloader = Downloader.builder()
                .download(download)
                .desiredThreadCount(downloadThreadCount)
                .downloadListener(downloadNewInfoListener)
                .build();
        Thread thread = new Thread(downloader);
        thread.start();
    }

    private Download getDownloadByUrl(String url) {
        return downloads.stream().filter(download -> download.getUrl().equals(url)).findAny().orElse(null);
    }

    private Download getDownloadById(UUID id) {
        return downloads.stream().filter(download -> download.getId().equals(id)).findAny().orElse(null);
    }


}
