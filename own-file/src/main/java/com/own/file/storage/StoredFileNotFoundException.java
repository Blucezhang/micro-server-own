package com.own.file.storage;

public class StoredFileNotFoundException extends FileStorageException {

    public StoredFileNotFoundException(String fileName) {
        super("File not found: " + fileName);
    }
}
