package com.kostianikov.pacs.service;

import com.kostianikov.pacs.controller.error.StorageException;
import com.kostianikov.pacs.controller.error.StorageFileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService implements StorageService {

    private final Path rootDLocation;
    private final Path rootVLocation;
    private final Path rootRLocation;


    public ImageStorageService(@Value("${result.pathToDImage}")
                                            String rootDLocation,
                                    @Value("${result.pathToVImage}")
                                            String rootVLocation,
                                    @Value("${result.pathToRImage}")
                                            String rootRLocation) {
        this.rootDLocation = Paths.get(rootDLocation);
        this.rootVLocation = Paths.get(rootVLocation);
        this.rootRLocation = Paths.get(rootRLocation);
    }

    @Override
    public String save(MultipartFile file, String pathCode) {



        String pathToImage;
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }

            pathToImage = UUID.randomUUID().toString() + "_" + file.getOriginalFilename() ;

            Files.copy(file.getInputStream(), concatenate(pathToImage, pathCode));
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }

        return pathToImage;
    }

    @Override
    public Resource loadAsResource(String filename, String pathCode) {
        try {
            Path file = concatenate(filename, pathCode );
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() && resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public FileSystemResource getResourceFile(String filename) {
        try {
            Path file = concatenate(filename, "D");
            FileSystemResource resource =  new FileSystemResource(file);
            if(resource.exists() && resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (Exception e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }


    @Override
    public Path concatenate(String filename, String pathCode) {

        Path rootLocation;
        switch (pathCode){
            case "V":
                rootLocation = rootVLocation;
                break;
            case "R":
                rootLocation = rootRLocation;
                break;
            case "D":
                rootLocation = rootDLocation;
            default:
                rootLocation = rootDLocation;
                break;
        }

        return rootLocation.resolve(filename);
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootDLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }



}
