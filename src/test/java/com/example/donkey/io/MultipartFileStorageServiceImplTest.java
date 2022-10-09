package com.example.donkey.io;

import com.example.donkey.model.FileInfo;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MultipartFileStorageServiceImplTest {

    @Autowired
    MultipartFileStorageServiceImpl testObject;



    MockMultipartFile file;
    Path targetDirectory;

    @BeforeEach
    void setUp() throws IOException {
        file = new MockMultipartFile(
                "file",
                "testfile.md",
                "text/markdown",
                Files.readAllBytes(Path.of("src/test/resources/testfile.md"))
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        if(targetDirectory != null){
            FileUtils.cleanDirectory(targetDirectory.toFile());
        }
    }

    @Test
    void load() {
        FileInfo fileInfo = testObject.save(file);
        assertNotNull(fileInfo);

        Resource resource = testObject.load(fileInfo.name());
        assertNotNull(resource);
        assertTrue(resource.isReadable());
    }

    @Test
    void save() {
    }

    @Test
    void saveAll() {
    }

    @Test
    void delete() {
    }

    @Test
    void deleteAll() {
    }

    @AfterAll
    static void afterAll() throws IOException {
        FileUtils.deleteDirectory(new File("test"));
    }
}