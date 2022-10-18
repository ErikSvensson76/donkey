package com.example.donkey.config;

import com.example.donkey.io.util.CopyVisitor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@Slf4j
public class DonkeyLifecycleHandler {
    @Value("${document.upload-directory}")
    private String uploadDirectory;
    @Value("${document.data-directory}")
    private String dataDirectory;
    @Value("${spring.profiles.active}")
    private String profile;


    @PostConstruct
    void postConstruct() {
        if(profile.equals("test")) return;

        File src = Path.of(dataDirectory).toFile();
        File dest = Path.of(uploadDirectory).toFile();

        Path srcPath = src.toPath();
        Path destPath = dest.toPath();
        try{
            if(!Files.exists(srcPath)){
                Files.createDirectory(srcPath);
            }
            if(!Files.exists(destPath)){
                Files.createDirectory(destPath);
            }
            Files.walkFileTree(srcPath, new CopyVisitor(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING));
            FileUtils.cleanDirectory(Paths.get(dataDirectory).toFile());
        }catch (IOException ex){
            throw new RuntimeException("Failed to copy data from " + src + " to " + uploadDirectory);
        }
    }

    @PreDestroy
    void preDestroy(){
        if(profile.equals("test")) return;

        File src = Path.of(uploadDirectory).toFile();
        File dest = Path.of(dataDirectory).toFile();

        Path srcPath = src.toPath();
        Path destPath = dest.toPath();

        try{
            Files.walkFileTree(srcPath, new CopyVisitor(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING));
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy data from " + src + " to " + dest);
        }
    }

}
