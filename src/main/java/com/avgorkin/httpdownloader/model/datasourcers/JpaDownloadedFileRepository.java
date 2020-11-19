package com.avgorkin.httpdownloader.model.datasourcers;

import com.avgorkin.httpdownloader.model.data.DownloadedFileDb;
import org.springframework.data.repository.CrudRepository;

/**
 * Jpa репозиторий для скачанных файлов
 */
public interface JpaDownloadedFileRepository extends CrudRepository<DownloadedFileDb, String>
{
}
