package com.own.file.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "own.file.storage")
public class FileStorageProperties {

    private String temporaryRoot;
    private String permanentRoot;

    public String getTemporaryRoot() {
        return temporaryRoot;
    }

    public void setTemporaryRoot(String temporaryRoot) {
        this.temporaryRoot = temporaryRoot;
    }

    public String getPermanentRoot() {
        return permanentRoot;
    }

    public void setPermanentRoot(String permanentRoot) {
        this.permanentRoot = permanentRoot;
    }
}
