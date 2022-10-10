package com.example.donkey.io;

import com.example.donkey.model.FileInfo;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class MultipartFileStorageServiceImplTest {

    @Autowired
    MultipartFileStorageServiceImpl testObject;

    MockMultipartFile file;
    Path targetDirectory = Path.of("test");

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
    void tearDown() {
        File directory = targetDirectory.toFile();
        for(File file : Objects.requireNonNull(directory.listFiles())){
            if(!file.isDirectory()){
                file.delete();
            }
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
        FileInfo fileInfo = testObject.save(file);
        assertNotNull(fileInfo);
        assertEquals(file.getOriginalFilename(), fileInfo.name());
        assertNotNull(fileInfo.url());
        log.info(fileInfo.name());
        log.info(fileInfo.url());
    }

    @Test
    void save_in_subdirectory() {
        // http://localhost/files/testfile.md
        //http://localhost/files/markdown/testfile.md
        Path path = Path.of("markdown");
        FileInfo fileInfo = testObject.save(file, path);
        assertNotNull(fileInfo);
        log.info(fileInfo.name());
        log.info(fileInfo.url());
        assertEquals(file.getOriginalFilename(), fileInfo.name());
    }

    @Test
    void saveAll() {
        String content1 = "#### **Test 1**";
        String content2 = "#### **Test 2**";
        MockMultipartFile file1 = new MockMultipartFile(
                "test1", "test1.md", "text/markdown", content1.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "test2", "test2.md", "text/markdown", content2.getBytes(StandardCharsets.UTF_8)
        );

        List<FileInfo> result = testObject.saveAll(List.of(file1, file2), null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(fileInfo -> fileInfo.name() != null && fileInfo.url() != null));
    }

    @Test
    void delete() {
        FileInfo fileInfo = testObject.save(file);
        assertNotNull(fileInfo);

        String name = fileInfo.name();
        testObject.delete(name);

        assertFalse(Paths.get("test/" + fileInfo.name()).toFile().exists());
    }

    @Test
    void deleteAll() {
        String content1 = "#### **Test 1**";
        String content2 = "#### **Test 2**";
        MockMultipartFile file1 = new MockMultipartFile(
                "test1", "test1.md", "text/markdown", content1.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "test2", "test2.md", "text/markdown", content2.getBytes(StandardCharsets.UTF_8)
        );

       testObject.saveAll(List.of(file1, file2), null);

        testObject.deleteAll();

        assertFalse(Paths.get("test/"+ "test1.md").toFile().exists());
        assertFalse(Paths.get("test/"+ "test2.md").toFile().exists());

    }

    @AfterAll
    static void afterAll() throws IOException {
        FileUtils.deleteDirectory(new File("test"));
    }
}