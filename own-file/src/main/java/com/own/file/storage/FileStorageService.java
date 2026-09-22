package com.own.file.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FileStorageService {

    private static final Pattern WINDOWS_ABSOLUTE_PATH = Pattern.compile("^[A-Za-z]:.*");
    private static final Pattern SAFE_FILE_NAME = Pattern.compile("[\\p{L}\\p{N}._-]+");

    private final Path temporaryRoot;
    private final Path permanentRoot;

    public FileStorageService(FileStorageProperties properties) {
        this.temporaryRoot = configuredRoot(properties.getTemporaryRoot(), "temporary root");
        this.permanentRoot = configuredRoot(properties.getPermanentRoot(), "permanent root");
    }

    public String storeTemporary(MultipartFile upload) {
        if (upload == null || upload.isEmpty()) {
            throw new InvalidStorageRequestException("An uploaded file is required and must not be empty");
        }

        String originalName = upload.getOriginalFilename();
        validateOriginalFileName(originalName);
        String identifier = UUID.randomUUID().toString();
        String storedName = identifier + extensionOf(originalName);
        Path target = resolveInside(temporaryRoot, storedName);
        Path staging = temporaryRoot.resolve(".upload-" + identifier + ".tmp");

        try {
            Files.createDirectories(temporaryRoot);
            try (InputStream input = upload.getInputStream()) {
                Files.copy(input, staging);
            }
            Files.move(staging, target);
            return storedName;
        } catch (IOException exception) {
            deleteStagingFile(staging);
            throw new FileStorageException("Could not store uploaded file", exception);
        }
    }

    public void writeTemporary(String fileName, OutputStream output) {
        write(requireRegularFile(temporaryRoot, fileName), output);
    }

    public void writePermanent(String fileName, OutputStream output) {
        write(requireRegularFile(permanentRoot, fileName), output);
    }

    public String temporaryContentType(String fileName) {
        return contentType(requireRegularFile(temporaryRoot, fileName));
    }

    public String permanentContentType(String fileName) {
        return contentType(requireRegularFile(permanentRoot, fileName));
    }

    public boolean hasPermanent(String fileName) {
        Path path = resolveInside(permanentRoot, fileName);
        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
    }

    public List<String> promoteTemporary(Collection<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            throw new InvalidStorageRequestException("At least one file name is required");
        }

        List<String> promoted = new ArrayList<String>();
        try {
            Files.createDirectories(permanentRoot);
        } catch (IOException exception) {
            throw new FileStorageException("Could not prepare permanent storage", exception);
        }

        for (String fileName : fileNames) {
            Path source = requireRegularFile(temporaryRoot, fileName);
            Path destination = resolveInside(permanentRoot, fileName);
            promoteOne(source, destination, fileName);
            promoted.add(fileName);
        }
        return promoted;
    }

    public String legacyFileName(String fileName, String suffix) {
        validateFileNamePart(fileName, "file name");
        validateFileNamePart(suffix, "file suffix");
        return validateStoredFileName(fileName + "." + suffix);
    }

    public String validateStoredFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidStoragePathException("File name must not be blank");
        }
        if (!fileName.equals(fileName.trim())
                || ".".equals(fileName)
                || "..".equals(fileName)
                || fileName.indexOf('/') >= 0
                || fileName.indexOf('\\') >= 0
                || Paths.get(fileName).isAbsolute()
                || WINDOWS_ABSOLUTE_PATH.matcher(fileName).matches()
                || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            throw new InvalidStoragePathException("Invalid file name");
        }
        return fileName;
    }

    private Path configuredRoot(String configuredPath, String label) {
        if (configuredPath == null || configuredPath.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " must be configured");
        }
        return Paths.get(configuredPath).toAbsolutePath().normalize();
    }

    private void validateOriginalFileName(String originalName) {
        if (originalName == null || originalName.trim().isEmpty()) {
            return;
        }
        validateStoredFileName(originalName);
    }

    private String extensionOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(lastDot);
    }

    private void validateFileNamePart(String value, String label) {
        if (value == null || value.isEmpty() || !SAFE_FILE_NAME.matcher(value).matches()
                || value.indexOf('/') >= 0 || value.indexOf('\\') >= 0) {
            throw new InvalidStoragePathException("Invalid " + label);
        }
    }

    private Path requireRegularFile(Path root, String fileName) {
        Path path = resolveInside(root, fileName);
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new StoredFileNotFoundException(fileName);
        }
        return path;
    }

    private void promoteOne(Path source, Path destination, String fileName) {
        Path staging = permanentRoot.resolve(".promote-" + UUID.randomUUID().toString() + ".tmp");
        try {
            Files.copy(source, staging, StandardCopyOption.COPY_ATTRIBUTES);
            moveStagedFile(staging, destination);
            Files.delete(source);
        } catch (IOException exception) {
            deleteStagingFile(staging);
            throw new FileStorageException("Could not promote file: " + fileName, exception);
        }
    }

    private void moveStagedFile(Path staging, Path destination) throws IOException {
        Files.move(staging, destination);
    }

    private Path resolveInside(Path root, String fileName) {
        String safeName = validateStoredFileName(fileName);
        Path resolved = root.resolve(safeName).normalize();
        if (!resolved.startsWith(root)) {
            throw new InvalidStoragePathException("File path escapes the configured storage root");
        }
        return resolved;
    }

    private void write(Path source, OutputStream output) {
        if (output == null) {
            throw new InvalidStorageRequestException("Download output is required");
        }
        try (InputStream input = Files.newInputStream(source)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            output.flush();
        } catch (IOException exception) {
            throw new FileStorageException("Could not read stored file: " + source.getFileName(), exception);
        }
    }

    private String contentType(Path path) {
        try {
            String detected = Files.probeContentType(path);
            return detected == null ? "application/octet-stream" : detected;
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }

    private void deleteStagingFile(Path staging) {
        try {
            Files.deleteIfExists(staging);
        } catch (IOException ignored) {
            // The source file is intentionally retained; stale staging files can be cleaned separately.
        }
    }
}
