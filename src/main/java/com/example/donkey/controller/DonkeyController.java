package com.example.donkey.controller;

import com.example.donkey.io.FileStorageService;
import com.example.donkey.model.FileInfo;
import com.example.donkey.validation.NoMaliciousCharacters;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@Validated
public class DonkeyController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<List<FileInfo>> upload(
            @RequestParam("files") @NotEmpty(message = "List must not be empty") List<@NotNull MultipartFile> multiPartFiles,
            @RequestParam(value = "directory", defaultValue = "") @NoMaliciousCharacters(message = "Invalid directory") String directory
    ){

        return ResponseEntity.ok(fileStorageService.saveAll(multiPartFiles, directory));
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable("filename") @NotBlank(message = "Filename must not be empty") String filename){
        Resource resource = fileStorageService.load(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }

}
