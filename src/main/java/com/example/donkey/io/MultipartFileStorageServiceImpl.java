package com.example.donkey.io;

import com.example.donkey.controller.DonkeyController;
import com.example.donkey.model.FileInfo;
import com.example.donkey.property.DocumentStorageProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * This method fetch the requested file from src/main/resources/documents by default (can be overridden).
     * Method returns a Resource of type UrlResource.
     * If the resource is not readable a RuntimeException is thrown.
     * If the resource is not found a MalformedURLException is caught and rethrown as a RuntimeException     *
     * @return a readable UrlResource
     */
    @Override
    public Resource load(String fileName) throws RuntimeException{
        try{
            Path file = Paths.get(documentStorageProperty.uploadDirectory()).resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()){
                return resource;
            }
            throw new RuntimeException("Could not find file: " + file);
        }catch (Exception ex){
            throw new RuntimeException("Exception: " + ex.getMessage(), ex);
        }
    }

    /**
     * This method saves a MultipartFile object to src/main/resources/documents/ directory by default (can be overridden).
     * If the operation is not successful a checked exception is rethrown as a RuntimeException
     * @return FileInfo object with filename and url
     */
    @Override
    public FileInfo save(MultipartFile multipartFile){
        try{
            String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename()).replaceAll(" ", "-");
            Path path = Path.of(documentStorageProperty.uploadDirectory()).resolve(fileName);
            Files.copy(
                    multipartFile.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );
            log.info("Successfully stored file: {} in directory: {}", fileName, documentStorageProperty.uploadDirectory());
            return new FileInfo(
                    fileName,
                    buildUrl(path)
            );
        }catch (Exception e){
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FileInfo> findAll() {
        try(Stream<Path> walk = Files.walk(Path.of(documentStorageProperty.uploadDirectory()))){
            return walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .map(file -> {
                        log.info(file.getPath());
                        return new FileInfo(
                                file.getName(),
                                buildUrl(Path.of(file.getPath()))
                        );
                    }).toList();

        }catch (IOException ex){
            throw new RuntimeException("Failed to fetch file information", ex);
        }
    }


    public FileInfo save(MultipartFile multipartFile, Path subdirectory) {
        try{
            String filename = Objects.requireNonNull(multipartFile.getOriginalFilename()).replaceAll(" ", "-");
            Path directoryPath = Path.of(documentStorageProperty.uploadDirectory()).resolve(subdirectory);
            if(!directoryPath.toFile().exists()){
                Files.createDirectories(directoryPath);
            }

            Path path = Path.of(documentStorageProperty.uploadDirectory())
                    .resolve(subdirectory)
                    .resolve(filename);

            Files.copy(
                    multipartFile.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );
            log.info("Successfully stored file: {} in directory: {}", filename, directoryPath);

            return new FileInfo(
                    filename,
                    buildUrl(path)
            );

        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage(), e);
        }
    }

    public String buildUrl(final Path fullPath){
        if(fullPath == null) throw new RuntimeException("Path was null");

        var pruned = fullPath.subpath(Path.of(documentStorageProperty.uploadDirectory()).getNameCount(), fullPath.getNameCount());
        String[] split = pruned.toString().replace('\\', '/').split("/");
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<split.length;i++){
            stringBuilder.append(split[i]);
            if(i < split.length -1){
                stringBuilder.append("/");
            }
        }
        return MvcUriComponentsBuilder.fromMethodName(DonkeyController.class, "getFile", stringBuilder.toString()).port(8010).build().toString();
    }

    /**
     * This method saves a list of Multipart files to src/main/resources/documents/ directory by default (can be overridden).
     * If any of the files is not successful a checked exception is rethrown as a RuntimeException
     * @return a list of FileInfos
     */
    @Override
    public List<FileInfo> saveAll(List<MultipartFile> multipartFiles,final String directory) throws RuntimeException{
        List<FileInfo> fileInfos;
        if(directory == null || directory.isBlank()){
            fileInfos =  multipartFiles.stream()
                    .map(this::save)
                    .collect(Collectors.toList());
        }else {
            fileInfos = multipartFiles.stream()
                    .map(multipartFile -> save(multipartFile, Path.of(directory)))
                    .collect(Collectors.toList());
        }
        return fileInfos;
    }

    /**
     * This method delete a file matching the supplied filename.
     * If the operation throws a checked IOException it is rethrown as a RuntimeException
     */
    @Override
    public void delete(String fileName) throws RuntimeException{
        try{
            Files.delete(Paths.get(documentStorageProperty.uploadDirectory()).resolve(fileName));
            log.info("File: {} was successfully deleted", Path.of(documentStorageProperty.uploadDirectory()).resolve(fileName));
        }catch (Exception ex){
            throw new RuntimeException("Could not delete file: " + Path.of(documentStorageProperty.uploadDirectory()).resolve(fileName), ex);
        }
    }

    /**
     * This method delete all files (not directories) in src/main/resources/documents/ directory by default
     */
    @Override
    public void deleteAll(){
        try(Stream<Path> walk = Files.walk(Path.of(documentStorageProperty.uploadDirectory()))){
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .peek(file -> log.info(file.getName()))
                    .forEach(File::delete);
        }catch (IOException ex){
            throw new RuntimeException("Couldn't delete: ", ex);
        }
    }

}
