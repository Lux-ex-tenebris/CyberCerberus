package com.kostianikov.pacs.model.data;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recognition {
    private String name;
    private Float confidence;

    public static Recognition recognitionFromString(String predict){
        return new Recognition(predict.split(",")[0], Float.valueOf(predict.split(", ")[1]));
    }
}
