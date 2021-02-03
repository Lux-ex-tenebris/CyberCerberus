package com.kostianikov.pacs.service;

import com.kostianikov.pacs.controller.error.NoFaceException;
import com.kostianikov.pacs.controller.error.ServiceException;
import com.kostianikov.pacs.model.data.Recognition;
import com.kostianikov.pacs.model.data.RecognitionFullResult;
import com.kostianikov.pacs.model.data.RecognitionResult;
import com.kostianikov.pacs.model.preProcessing.ImageHandler;
import com.kostianikov.pacs.model.preProcessing.ImageUtil;
import com.kostianikov.pacs.model.preProcessing.Preprocessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class RequestToPyService {

    private final RestTemplate restTemplate;
    private final int previewSize;
    private final String pathToDImage;
    private final String pathToVImage;
    private final String pathToRImage;
    private final ImageHandler imageHandler;
    private final Preprocessor preprocessor;
    private final String pythonServerURL;
    private final StorageService storageService;

    public RequestToPyService(RestTemplateBuilder restTemplateBuilder,
                              @Value("${tensor.previewSize}") int previewSize,
                              @Value("${tensor.pythonServerURL}") String pythonServerURL,
                              @Value("${tensor.pathToDImage}")
                                      String pathToDImage,
                              @Value("${tensor.pathToVImage}")
                                      String pathToVImage,
                              @Value("${tensor.pathToRImage}")
                                      String pathToRImage,
                              ImageHandler imageHandler,
                              Preprocessor preprocessor,
                              StorageService storageService) {
        this.restTemplate = restTemplateBuilder.build();
        this.previewSize = previewSize;
        this.pathToDImage = pathToDImage;
        this.pathToVImage = pathToVImage;
        this.pathToRImage = pathToRImage;
        this.imageHandler = imageHandler;
        this.preprocessor = preprocessor;
        this.pythonServerURL = pythonServerURL;
        this.storageService = storageService;
    }

    public RecognitionResult processImageFile(MultipartFile file) {
            String nameDImage = imageHandler.readAndSave(file,pathToDImage);
            String response = postRequestToPy(pathToDImage+"//"+nameDImage);

            System.out.println(response);

            Recognition recognition = Recognition.recognitionFromString(response);
            //classifier.classify(image);
            //String imageDPreview = imageHandler.getImagePreview(fullPathToDImage,previewSize); //getImagePreview(image);
            return new RecognitionResult(nameDImage, recognition);

    }

//    private List<Recognition> convertToRecognitions(float[] classes) {
//        List<Recognition> found = new ArrayList<>();
//        for (int i = 0; i < classes.length; ++i) {
//            if (classes[i] >= 0.1) {
//                // found.add(new Recognition(labels.get(i), classes[i]));
//                found.add(new Recognition(String.valueOf(classes[i]), classes[i]));
//            }
//        }
//        return found;
//    }

//    private BufferedImage getImagePreview(BufferedImage image) {
//        int width = image.getWidth();
//        int height = image.getHeight();
//        float scale = (float) previewSize / width;
//        return ImageUtil.scaleImage(image, previewSize, (int) (height * scale));
//    }

//    public String getPostsPlainJSON() {
//        String url = "https://jsonplaceholder.typicode.com/posts";
//        return this.restTemplate.getForObject(url, String.class);
//    }

    public String postRequestToPy(String pathToImage) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", storageService.getResourceFile(pathToImage));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity(pythonServerURL, request , String.class ).getBody();
    }


    public RecognitionFullResult processImageFile(RecognitionFullResult recognitionFullResult) throws NoFaceException {

        //String nameVImage = imageHandler.readAndSave(fileV,pathToVImage);
        //String nameRImage = imageHandler.readAndSave(fileR,pathToRImage);

        //String nameDImage = preprocessor.preprocessing(pathToVImage+"//"+nameVImage, pathToRImage+"//"+nameRImage, pathToDImage);
        recognitionFullResult.setImageDPreview(preprocessor.preprocessing(pathToVImage+"//" + recognitionFullResult.getImageVPreview(), pathToRImage+"//"+ recognitionFullResult.getImageRPreview(), pathToDImage));

        String response = postRequestToPy(recognitionFullResult.getImageDPreview());

        System.out.println(response);


        Recognition recognition = Recognition.recognitionFromString(response);

        recognitionFullResult.setRecognition(recognition);

        //classifier.classify(image);
//        BufferedImage imageDP= imageHandler.getImagePreview(pathToDImage,previewSize); //getImagePreview(image);
//        BufferedImage imageVP= imageHandler.getImagePreview(pathToVImage,previewSize); //getImagePreview(image);
//        BufferedImage imageRP= imageHandler.getImagePreview(pathToRImage,previewSize); //getImagePreview(image);
        //return new RecognitionFullResult(nameDImage, nameVImage, nameRImage, recognition);
        return  recognitionFullResult;
    }
}