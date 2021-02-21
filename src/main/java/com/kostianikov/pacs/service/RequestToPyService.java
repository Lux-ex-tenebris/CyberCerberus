package com.kostianikov.pacs.service;

import com.kostianikov.pacs.controller.error.NoFaceException;
import com.kostianikov.pacs.controller.error.RejectException;
import com.kostianikov.pacs.model.data.Recognition;
import com.kostianikov.pacs.model.data.RecognitionFullResult;
import com.kostianikov.pacs.model.data.RecognitionResult;
import com.kostianikov.pacs.model.preProcessing.ImageHandler;
import com.kostianikov.pacs.model.preProcessing.Preprocessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
public class RequestToPyService {

    private final RestTemplate restTemplate;
    private final String pathToDImage;
    private final String pathToVImage;
    private final String pathToRImage;
    private final ImageHandler imageHandler;
    private final Preprocessor preprocessor;
    private final String pythonServerURL;
    private final StorageService storageService;
    private final float threshold;

    public RequestToPyService(RestTemplateBuilder restTemplateBuilder,
                              @Value("${remote.pythonServerURL}") String pythonServerURL,
                              @Value("${result.pathToDImage}")
                                      String pathToDImage,
                              @Value("${result.pathToVImage}")
                                      String pathToVImage,
                              @Value("${result.pathToRImage}")
                                      String pathToRImage,
                              @Value("${model.threshold}")
                                      float threshold,
                              ImageHandler imageHandler,
                              Preprocessor preprocessor,
                              StorageService storageService) {
        this.restTemplate = restTemplateBuilder.build();
        this.pathToDImage = pathToDImage;
        this.pathToVImage = pathToVImage;
        this.pathToRImage = pathToRImage;
        this.imageHandler = imageHandler;
        this.preprocessor = preprocessor;
        this.pythonServerURL = pythonServerURL;
        this.storageService = storageService;
        this.threshold = threshold;
    }

    public RecognitionResult processImageFile(MultipartFile file) {
            String nameDImage = imageHandler.readAndSave(file,pathToDImage);
            String response = postRequestToPy(pathToDImage+"//"+nameDImage);

            System.out.println(response);

            Recognition recognition = Recognition.recognitionFromString(response);
             return new RecognitionResult(nameDImage, recognition);

    }


    public String postRequestToPy(String pathToImage) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", storageService.getResourceFile(pathToImage));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity(pythonServerURL, request , String.class ).getBody();
    }


    public RecognitionFullResult processImageFile(RecognitionFullResult recognitionFullResult
                                                  ) throws NoFaceException, RejectException {

        //String nameVImage = imageHandler.readAndSave(fileV,pathToVImage);
        //String nameRImage = imageHandler.readAndSave(fileR,pathToRImage);

        //String nameDImage = preprocessor.preprocessing(pathToVImage+"//"+nameVImage, pathToRImage+"//"+nameRImage, pathToDImage);
        recognitionFullResult.setImageDPreview(preprocessor.preprocessing(pathToVImage+"//" + recognitionFullResult.getImageVPreview(), pathToRImage+"//"+ recognitionFullResult.getImageRPreview(), pathToDImage));

        String response = postRequestToPy(recognitionFullResult.getImageDPreview());

        System.out.println(response);


        Recognition recognition = Recognition.recognitionFromString(response);

        recognitionFullResult.setRecognition(recognition);

        if(recognitionFullResult.getRecognition().getConfidence() < threshold){
            String msg = String.format("System found %s , but not sure, confidence only %f", recognitionFullResult.getRecognition().getName(), recognitionFullResult.getRecognition().getConfidence());

            throw new RejectException(msg);
        }


        return  recognitionFullResult;
    }
}