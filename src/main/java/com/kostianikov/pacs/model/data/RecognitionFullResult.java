package com.kostianikov.pacs.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.List;

@Data
@NoArgsConstructor
public class RecognitionFullResult {

    private String imageDPreview;
    private String imageVPreview;
    private String imageRPreview;
    private Recognition recognition;

    public RecognitionFullResult(String imageDPreview, String imageVPreview, String imageRPreview, Recognition recognition) {
        this.imageDPreview = imageDPreview;
        this.imageVPreview = imageVPreview;
        this.imageRPreview = imageRPreview;
        this.recognition = recognition;
    }
}
