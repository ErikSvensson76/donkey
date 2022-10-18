package com.example.donkey.io;

import com.example.donkey.model.FileInfo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileStorageService {

    Resource load(String fileName);

    FileInfo save(MultipartFile multipartFile);

    List<FileInfo> moveFiles(Map<String, String> sourceAndDestinationMap);

    List<FileInfo> findAll();

    List<FileInfo> saveAll(List<MultipartFile> multipartFiles, String directory);

    void delete(String fileName);

    void deleteAll();
}
