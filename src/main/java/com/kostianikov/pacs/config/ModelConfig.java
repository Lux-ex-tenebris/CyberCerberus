package com.kostianikov.pacs.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Getter
@Configuration
public class ModelConfig {
    @Value("${tensor.model.inputSize}")
    private Integer inputSize;

    @Value("${tensor.model.imageMean}")
    private Integer imageMean;

    @Value("${tensor.model.imageStd}")
    private Float imageStd;

    @Value("${tensor.model.inputLayerName}")
    private String inputLayerName;

    @Value("${tensor.model.outputLayerName}")
    private String outputLayerName;

    @Value("${tensor.model.path}")
    private String modelPath;

    @Value("${tensor.model.labelsResource}")
    private Resource labelsResource;

    @Value("${tensor.model.threshold}")
    private Float threshold;
}
