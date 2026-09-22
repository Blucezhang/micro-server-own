package com.own.file.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileStorageServiceTest {

    @TempDir
    Path folder;

    private Path temporaryRoot;
    private Path permanentRoot;
    private FileStorageService storageService;

    @BeforeEach
    public void setUp() throws IOException {
        temporaryRoot = Files.createDirectory(folder.resolve("temporary"));
        permanentRoot = Files.createDirectory(folder.resolve("permanent"));

        FileStorageProperties properties = new FileStorageProperties();
        properties.setTemporaryRoot(temporaryRoot.toString());
        properties.setPermanentRoot(permanentRoot.toString());
        storageService = new FileStorageService(properties);
    }

    @Test
    public void storesUploadUnderConfiguredTemporaryRoot() throws IOException {
        MockMultipartFile upload = new MockMultipartFile(
                "file", "photo.JPG", "image/jpeg", "image".getBytes(StandardCharsets.UTF_8));

        String storedName = storageService.storeTemporary(upload);

        assertThat(storedName, endsWith(".JPG"));
        assertThat(new String(Files.readAllBytes(temporaryRoot.resolve(storedName)), StandardCharsets.UTF_8),
                is("image"));
    }

    @Test
    public void supportsFilesWithoutAnExtension() throws IOException {
        MockMultipartFile upload = new MockMultipartFile(
                "file", "README", "text/plain", "content".getBytes(StandardCharsets.UTF_8));

        String storedName = storageService.storeTemporary(upload);

        assertFalse(storedName.contains("."));
        assertTrue(Files.isRegularFile(temporaryRoot.resolve(storedName)));
    }

    @Test
    public void rejectsEmptyUploads() throws IOException {
        assertThrows(InvalidStorageRequestException.class,
                () -> storageService.storeTemporary(new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0])));
    }

    @Test
    public void rejectsUploadNamesContainingPathSeparators() throws IOException {
        assertThrows(InvalidStoragePathException.class, () -> storageService.storeTemporary(new MockMultipartFile(
                "file", "../photo.jpg", "image/jpeg", new byte[]{1})));
    }

    @Test
    public void rejectsTraversalAbsolutePathsAndSeparators() throws IOException {
        assertInvalidName("../secret.txt");
        assertInvalidName("/etc/passwd");
        assertInvalidName("C:\\secret.txt");
        assertInvalidName("nested/file.txt");
        assertInvalidName("nested\\file.txt");
    }

    @Test
    public void reportsMissingDownloads() throws IOException {
        assertThrows(StoredFileNotFoundException.class,
                () -> storageService.writeTemporary("missing.txt", new ByteArrayOutputStream()));
    }

    @Test
    public void doesNotFollowSymbolicLinksDuringDownload() throws IOException {
        Path outside = Files.createFile(folder.resolve("outside.txt"));
        Files.write(outside, "secret".getBytes(StandardCharsets.UTF_8));
        Files.createSymbolicLink(temporaryRoot.resolve("link.txt"), outside);

        assertThrows(StoredFileNotFoundException.class,
                () -> storageService.writeTemporary("link.txt", new ByteArrayOutputStream()));
    }

    @Test
    public void promotesFileAndOnlyThenRemovesTemporaryCopy() throws IOException {
        Path source = temporaryRoot.resolve("file.txt");
        Files.write(source, "content".getBytes(StandardCharsets.UTF_8));

        storageService.promoteTemporary(Arrays.asList("file.txt"));

        assertFalse(Files.exists(source));
        assertThat(new String(Files.readAllBytes(permanentRoot.resolve("file.txt")), StandardCharsets.UTF_8),
                is("content"));
    }

    @Test
    public void keepsTemporaryFileWhenPromotionFails() throws IOException {
        Path source = temporaryRoot.resolve("file.txt");
        Files.write(source, "content".getBytes(StandardCharsets.UTF_8));
        Files.createDirectory(permanentRoot.resolve("file.txt"));

        try {
            storageService.promoteTemporary(Arrays.asList("file.txt"));
        } catch (FileStorageException expected) {
            assertTrue(Files.isRegularFile(source));
            return;
        }

        throw new AssertionError("promotion should fail when the destination is a directory");
    }

    private void assertInvalidName(String name) throws IOException {
        try {
            storageService.writeTemporary(name, new ByteArrayOutputStream());
        } catch (InvalidStoragePathException expected) {
            return;
        }
        throw new AssertionError("expected invalid storage path for: " + name);
    }
}
