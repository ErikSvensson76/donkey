package com.example.donkey.io;

import com.example.donkey.controller.DonkeyController;
import com.example.donkey.model.FileInfo;
import com.example.donkey.property.DocumentStorageProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MultipartFileStorageServiceImpl implements FileStorageService{

    private final DocumentStorageProperty documentStorageProperty;

    public MultipartFileStorageServiceImpl(DocumentStorageProperty documentStorageProperty) {
        this.documentStorageProperty = documentStorageProperty;
        try{
            Files.createDirectories(Path.of(documentStorageProperty.uploadDirectory()));
            log.info("Initialized storage location: {}", documentStorageProperty.uploadDirectory());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource load(String fileName){
        try{
            Path file = Paths.get(documentStorageProperty.uploadDirectory()).resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()){
                return resource;
            }
            throw new RuntimeException("Could not read the file");
        }catch (MalformedURLException ex){
            throw new RuntimeException("Exception: " + ex.getMessage());
        }
    }

    @Override
    public FileInfo save(MultipartFile multipartFile){
        try{
            Path path = Path.of(documentStorageProperty.uploadDirectory()).resolve(Objects.requireNonNull(multipartFile.getOriginalFilename()));

            Files.copy(
                    multipartFile.getInputStream(),
                    Paths.get(documentStorageProperty
                            .uploadDirectory())
                            .resolve(Objects.requireNonNull(multipartFile.getOriginalFilename())),
                    StandardCopyOption.REPLACE_EXISTING
            );
            log.info("Successfully stored file: {} in directory: {}", multipartFile.getOriginalFilename(), documentStorageProperty.uploadDirectory());
            return new FileInfo(
                    multipartFile.getOriginalFilename(),
                    MvcUriComponentsBuilder.fromMethodName(DonkeyController.class, "getFile", path.getFileName().toString()).build().toString()
            );
        }catch (Exception e){
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Override
    public List<FileInfo> saveAll(List<@NotNull MultipartFile> multipartFiles){
        return multipartFiles.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String fileName){
        try{
            Files.delete(Paths.get(documentStorageProperty.uploadDirectory()).resolve(fileName));
            log.info("File: {} was successfully deleted", Path.of(documentStorageProperty.uploadDirectory()).resolve(fileName));
        }catch (Exception ex){
            throw new RuntimeException("Could not delete file: " + Path.of(documentStorageProperty.uploadDirectory()).resolve(fileName), ex);
        }
    }

    @Override
    public void deleteAll(){
        FileSystemUtils.deleteRecursively(Path.of(documentStorageProperty.uploadDirectory()).toFile());
    }

}
