package com.kostianikov.pacs.controller;


import com.kostianikov.pacs.controller.error.ServiceException;
import com.kostianikov.pacs.model.data.Recognition;
import com.kostianikov.pacs.model.data.RecognitionResult;
import com.kostianikov.pacs.service.RequestToPyService;
import lombok.extern.log4j.Log4j2;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import javax.validation.ConstraintViolationException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@Log4j2
@ApiIgnore
@Controller
public class PredictPreparedController {
    private static final String IMAGE_ATTR = "IMAGE";

    //private final ClassifierService classifierService;
    private final RequestToPyService requestToPyService;

    @Autowired
    public PredictPreparedController(RequestToPyService requestToPyService) {
        this.requestToPyService = requestToPyService;
    }

    @RequestMapping(value = "/uploadPForm", method = RequestMethod.GET)
    public String handleGetForm() {
        return "upload-prepared-form";
    }

//    @RequestMapping(value = "/imgdoublepreview", method = RequestMethod.GET)
//    public void handleGetImg(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
//        BufferedImage image = (BufferedImage) httpServletRequest.getSession().getAttribute(IMAGE_ATTR);
//        if (image != null) {
//            httpServletResponse.setContentType("image/png");
//            ImageIO.write(image, "png", httpServletResponse.getOutputStream());
//        } else {
//            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
//        }
//    }

    @RequestMapping(value = "/uploadPForm", method = RequestMethod.POST)
    public String handleUploadForm(@RequestParam(value = "file") MultipartFile file, Model model, HttpServletRequest httpServletRequest) {
        log.debug("Image upload requested");
        //RecognitionResult recognitionResult = classifierService.processImageFile(file);
        RecognitionResult recognitionResult = requestToPyService.processImageFile(file);
        //Recognition recognition = recognitionResult.getRecognition();
        //httpServletRequest.getSession().setAttribute(IMAGE_ATTR, recognitionResult.getImagePreview());
        log.debug("Found objects: {}", recognitionResult.getRecognition());
        if (recognitionResult.getRecognition() == null) {
            model.addAttribute("message", "No objects found");
        } else {
            //model.addAttribute("recognition", recognition);
            model.addAttribute("recognitionResult", recognitionResult);
        }

        return "upload-prepared-form";
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView doResolveException(RuntimeException e) {
        if (e instanceof MaxUploadSizeExceededException) {
            return getModelAndView("upload-prepared-form", "File is too large");
        } else if (e instanceof ConstraintViolationException) {
            return getModelAndView("upload-prepared-form", "Malformed request: " + e.getMessage());
        } else if (e instanceof ServiceException) {
            log.info("Error during processing request", e);
            return getModelAndView("upload-prepared-form", e.getMessage());
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
