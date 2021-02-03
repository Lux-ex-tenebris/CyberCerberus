package com.kostianikov.pacs.service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    String save(MultipartFile file, String pathCode);

    Path concatenate(String filename, String pathCode);

    Resource loadAsResource(String filename, String pathCode);

    FileSystemResource getResourceFile(String filename);


}