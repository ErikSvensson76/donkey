package com.example.donkey.property;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocumentStoragePropertyTest {

    @Autowired DocumentStorageProperty documentStorageProperty;

    @Test
    void getUploadDirectory() {
        assertNotNull(documentStorageProperty.uploadDirectory());
        assertEquals("test", documentStorageProperty.uploadDirectory());
    }
}