package com.own.file.controller.dto;

import java.util.List;

public class FilePromotionRequest {

    private List<String> fileNames;

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }
}
