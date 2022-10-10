package com.example.donkey.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Component
@Slf4j
public class DonkeyStartupHandler {

    @Value("${document.upload-directory}")
    private String uploadDirectory;

    public static class CopyDirectoryVisitor extends SimpleFileVisitor<Path> {
        private final Path fromPath;
        private final Path toPath;
        private final CopyOption copyOption;

        public CopyDirectoryVisitor(Path fromPath, Path toPath, CopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            if(!Files.exists(targetPath)){
                Files.createDirectory(targetPath);
                log.info("Created directory: {}", targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path destination = toPath.resolve(fromPath.relativize(file));
            Files.copy(file, destination, copyOption);
            log.info("Copied file: {} to destination: {}", file, destination);
            return FileVisitResult.CONTINUE;
        }
    }


    @PostConstruct
    void postConstruct() {
        File src = Path.of("indata").toFile();
        File dest = Path.of(uploadDirectory).toFile();

        Path srcPath = src.toPath();
        Path destPath = dest.toPath();
        try{
            Files.walkFileTree(srcPath, new CopyDirectoryVisitor(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING));
            FileUtils.cleanDirectory(src);
        }catch (IOException ex){
            throw new RuntimeException("Failed to copy data from " + src + " to " + uploadDirectory);
        }
    }
}
