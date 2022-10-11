package com.example.donkey.controller;

import com.example.donkey.io.FileStorageService;
import com.example.donkey.model.FileInfo;
import com.example.donkey.model.ResponseMessage;
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

import static com.example.donkey.property.ValidationMessages.*;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@Validated
public class DonkeyController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<List<FileInfo>> upload(
            @RequestParam("files") @NotEmpty(message = LIST_EMPTY) List<@NotNull MultipartFile> multiPartFiles,
            @RequestParam(value = "directory", defaultValue = "") @NoMaliciousCharacters(message = INVALID_DIRECTORY) String directory
    ){
        return ResponseEntity.ok(fileStorageService.saveAll(multiPartFiles, directory));
    }

    @GetMapping("/info")
    public ResponseEntity<List<FileInfo>> findAll(){
       return ResponseEntity.ok(fileStorageService.findAll());
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(
            @PathVariable("filename") @NotBlank(message = FILENAME_EMPTY) @NoMaliciousCharacters(message = INVALID_FILENAME)  String filename){
        Resource resource = fileStorageService.load(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }

    @GetMapping("/files/{directory}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(
            @PathVariable("directory") @NotBlank(message = DIRECTORY_EMPTY) @NoMaliciousCharacters(message = INVALID_DIRECTORY) String directory,
            @PathVariable("filename") @NotBlank(message = FILENAME_EMPTY) @NoMaliciousCharacters(message = INVALID_FILENAME) String filename
    ){
        String path = directory+"/"+filename;
        Resource resource = fileStorageService.load(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }

    @DeleteMapping("/files/{filename:.*}")
    public ResponseEntity<ResponseMessage> delete(
            @PathVariable("filename") @NotBlank(message = FILENAME_EMPTY) @NoMaliciousCharacters(message = INVALID_FILENAME) String filename){
        fileStorageService.delete(filename);
        return ResponseEntity.accepted().body(new ResponseMessage(filename + " was deleted"));
    }

    @DeleteMapping("/files/{directory}/{filename:.+}")
    public ResponseEntity<ResponseMessage> delete(
            @PathVariable("directory") @NotBlank(message = DIRECTORY_EMPTY) @NoMaliciousCharacters(message = INVALID_DIRECTORY) String directory,
            @PathVariable("filename") @NotBlank(message = FILENAME_EMPTY) @NoMaliciousCharacters(message = INVALID_FILENAME) String filename
    ){
        String path = directory+"/"+filename;
        fileStorageService.delete(path);
        return ResponseEntity.accepted().body(new ResponseMessage(path + " was deleted"));
    }


}
