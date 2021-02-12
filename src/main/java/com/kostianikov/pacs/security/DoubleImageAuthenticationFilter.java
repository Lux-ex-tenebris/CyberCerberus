package com.kostianikov.pacs.security;

import com.kostianikov.pacs.controller.error.NoFaceException;
import com.kostianikov.pacs.model.data.RecognitionFullResult;
import com.kostianikov.pacs.service.RequestToPyService;
import com.kostianikov.pacs.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import javax.servlet.http.HttpServletRequest;

public class DoubleImageAuthenticationFilter  extends UsernamePasswordAuthenticationFilter {


    private final String fileVParameter = "fileV";

    private final String fileRParameter = "fileR";

    @Autowired
    private RequestToPyService requestToPyService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private MultipartResolver multipartResolver;

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return "1";
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        MultipartHttpServletRequest req = multipartResolver.resolveMultipart(request);
        //MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
        MultipartFile fileV = req.getFile(this.fileVParameter);
        MultipartFile fileR = req.getFile(this.fileRParameter);

        RecognitionFullResult recognitionFullResult = null;

        try {
            recognitionFullResult = new RecognitionFullResult();
            recognitionFullResult.setImageVPreview(storageService.save(fileV,"V"));
            recognitionFullResult.setImageRPreview(storageService.save(fileR,"R"));
            recognitionFullResult = requestToPyService.processImageFile(recognitionFullResult);
        } catch (NoFaceException e) {
            return "I can`t found good faces, maybe it is spoofing?";
        }

        return recognitionFullResult.getRecognition().getName();
    }
}
