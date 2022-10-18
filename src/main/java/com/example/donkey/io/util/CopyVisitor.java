package com.example.donkey.io.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


public class CopyVisitor extends SimpleFileVisitor<Path> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Path fromPath;
    private final Path toPath;
    private final CopyOption copyOption;

    public CopyVisitor(Path fromPath, Path toPath, CopyOption copyOption) {
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
