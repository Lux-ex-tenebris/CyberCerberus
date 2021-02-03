package com.kostianikov.pacs.model.preProcessing;

import com.kostianikov.pacs.controller.error.ServiceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Log4j2
@Component
public class ImageHandler {




    public  String readAndSave(MultipartFile file, String path){
        return save(read(file),path,file.getOriginalFilename());
    }

    private  BufferedImage read(MultipartFile file){
        if (file == null) {
            throw new ServiceException("Failed reading image file");
        }
        BufferedImage image = null;
        try {
            image = javax.imageio.ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new ServiceException("Failed reading image file");
            }
            return image;
        } catch (IOException e) {
            log.info("Error during reading file input stream", e);
        }
        return image;
    }

    private  String save(BufferedImage image, String path, String name){



        File uploadDir = new File(path);
        if (!uploadDir.exists()){
            uploadDir.mkdir();
        }

        String pathToImage = UUID.randomUUID().toString() + "_" + name ;

        try { //"image\\imageToSend\\myImage.jpg";
            javax.imageio.ImageIO.write(image, "jpg", new File(path+"\\" +pathToImage));
        } catch (IOException e) {
            log.info("Error during write file output stream", e);
        }
        return pathToImage;
    }

//    public  String getImagePreview(String imagePath, int previewSize) {
//        BufferedImage image = readFromFile(imagePath);
//        int width = image.getWidth();
//        int height = image.getHeight();
//        float scale = (float) previewSize / width;
//        return ImageUtil.scaleImage(image, previewSize, (int) (height * scale));
//    }

//    private  BufferedImage readFromFile(String imagePath) {
//        BufferedImage image = null;
//        try {
//            String pathToImage = imagePath; //"image\\imageToSend\\myImage.jpg";
//            image = javax.imageio.ImageIO.read(new File(pathToImage));
//            return image;
//        } catch (IOException e) {
//            log.info("Error during write file output stream", e);
//        }
//        return image;
//    }
    public FileSystemResource getResourceFile(String pathToImage) {
        return new FileSystemResource(pathToImage);
    }



}
