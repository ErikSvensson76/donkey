package com.example.donkey.io;

import com.example.donkey.model.FileInfo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    Resource load(String fileName);

    FileInfo save(MultipartFile multipartFile);

    List<FileInfo> saveAll(List<MultipartFile> multipartFiles);

    void delete(String fileName);

    void deleteAll();
}
