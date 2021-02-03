package com.kostianikov.pacs.controller;


import com.kostianikov.pacs.controller.error.NoFaceException;
import com.kostianikov.pacs.controller.error.ServiceException;
import com.kostianikov.pacs.model.data.Recognition;
import com.kostianikov.pacs.model.data.RecognitionFullResult;
import com.kostianikov.pacs.service.RequestToPyService;
import com.kostianikov.pacs.service.StorageService;
import lombok.extern.log4j.Log4j2;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@Log4j2
@ApiIgnore
@Controller
public class PredictFullController {
    private static final String IMAGE_DP_ATTR = "IMAGE_DP";
    private static final String IMAGE_VP_ATTR = "IMAGE_VP";
    private static final String IMAGE_RP_ATTR = "IMAGE_RP";

    //private final ClassifierService classifierService;
    private final RequestToPyService requestToPyService;
    private final StorageService storageService;


    @Autowired
    public PredictFullController(RequestToPyService requestToPyService, StorageService storageService) {
        this.requestToPyService = requestToPyService;
        this.storageService = storageService;
    }

    @RequestMapping(value = "/uploadForm", method = RequestMethod.GET)
    public String handleGetForm() {
        return "upload-full-form";
    }

    @GetMapping("/img/{pathCode}/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String pathCode, @PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename, pathCode);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

//    @RequestMapping(value = "/img/{name}", method = RequestMethod.GET)
//    public void handleGetImg(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable String name) throws IOException {
//        BufferedImage image = null;
//        switch (name){
//            case("DP"):
//                image = (BufferedImage) httpServletRequest.getSession().getAttribute(IMAGE_DP_ATTR);
//                break;
//            case("VP"):
//                image = (BufferedImage) httpServletRequest.getSession().getAttribute(IMAGE_VP_ATTR);
//                break;
//            case("RP"):
//                image = (BufferedImage) httpServletRequest.getSession().getAttribute(IMAGE_RP_ATTR);
//                break;
//            default:
//                break;
//        }
//
//        //BufferedImage image = (BufferedImage) httpServletRequest.getSession().getAttribute(IMAGE_DP_ATTR);
//        if (image != null) {
//            httpServletResponse.setContentType("image/png");
//            ImageIO.write(image, "png", httpServletResponse.getOutputStream());
//        } else {
//            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
//        }
//    }

    @RequestMapping(value = "/uploadForm", method = RequestMethod.POST)
    public String handleUploadForm(@RequestParam(value = "fileV") MultipartFile fileV,
                                   @RequestParam(value = "fileR") MultipartFile fileR,
                                   @Value("${tensor.model.threshold}") float threshold,
                                   Model model,
                                   HttpServletRequest httpServletRequest) {
        log.debug("Image upload requested");
        //RecognitionResult recognitionResult = classifierService.processImageFile(file);
        RecognitionFullResult recognitionFullResult = null;

        try {
            recognitionFullResult = new RecognitionFullResult();
            recognitionFullResult.setImageVPreview(storageService.save(fileV,"V"));
            recognitionFullResult.setImageRPreview(storageService.save(fileR,"R"));
            recognitionFullResult = requestToPyService.processImageFile(recognitionFullResult);
        } catch (NoFaceException e) {
            model.addAttribute("message", "I can`t found good faces, maybe it is spoofing?");
            return "rejected-form";
        }

        //Recognition recognition = recognitionFullResult.getRecognition();
        //httpServletRequest.getSession().setAttribute(IMAGE_VP_ATTR, recognitionFullResult.getImageVPreview());
        //ttpServletRequest.getSession().setAttribute(IMAGE_RP_ATTR, recognitionFullResult.getImageRPreview());
        //httpServletRequest.getSession().setAttribute(IMAGE_DP_ATTR, recognitionFullResult.getImageDPreview());
        log.debug("Found objects: {}", recognitionFullResult.getRecognition());
        if (recognitionFullResult.getRecognition() == null) {
            model.addAttribute("message", "No objects found");
        } else {
            if(recognitionFullResult.getRecognition().getConfidence() < threshold){
                String msg = String.format("System found %s , but not sure, confidence only %f", recognitionFullResult.getRecognition().getName(), recognitionFullResult.getRecognition().getConfidence());
                model.addAttribute("message", msg);
                return "rejected-form";
            }
            //model.addAttribute("recognition", recognition);
            model.addAttribute("recognitionFullResult", recognitionFullResult);
        }

        return "upload-full-form";
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView doResolveException(RuntimeException e) {
        if (e instanceof MaxUploadSizeExceededException) {
            return getModelAndView("upload-full-form", "File is too large");
        } else if (e instanceof ConstraintViolationException) {
            return getModelAndView("upload-full-form", "Malformed request: " + e.getMessage());
        } else if (e instanceof ServiceException) {
            log.info("Error during processing request", e);
            return getModelAndView("upload-full-form", e.getMessage());
        } else {
            log.info("Error during processing request", e);
            return getModelAndView("error", "Internal server error");
        }
    }

    private ModelAndView getModelAndView(String uploadForm, String s) {
        ModelAndView modelAndView;
        modelAndView = new ModelAndView(uploadForm);
        modelAndView.getModel().put("message", s);
        return modelAndView;
    }
}
