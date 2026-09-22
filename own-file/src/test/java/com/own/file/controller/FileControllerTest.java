package com.own.file.controller;

import com.own.file.storage.FileStorageProperties;
import com.own.file.storage.FileStorageService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FileControllerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path temporaryRoot;
    private Path permanentRoot;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws IOException {
        temporaryRoot = folder.newFolder("temporary").toPath();
        permanentRoot = folder.newFolder("permanent").toPath();

        FileStorageProperties properties = new FileStorageProperties();
        properties.setTemporaryRoot(temporaryRoot.toString());
        properties.setPermanentRoot(permanentRoot.toString());
        FileStorageService storageService = new FileStorageService(properties);
        mockMvc = MockMvcBuilders.standaloneSetup(new FileController(storageService)).build();
    }

    @Test
    public void rejectsEmptyMultipartUploadWithBadRequest() throws Exception {
        MockMultipartFile upload = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        mockMvc.perform(fileUpload("/file/picture").file(upload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("must not be empty")));
    }

    @Test
    public void returnsNotFoundForMissingTemporaryFile() throws Exception {
        mockMvc.perform(get("/file/tmpPicture/missing/jpg"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("missing.jpg")));
    }

    @Test
    public void streamsTemporaryFileThroughLegacyRoute() throws Exception {
        Files.write(temporaryRoot.resolve("picture.jpg"), "image".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/file/tmpPicture/picture/jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"picture.jpg\""))
                .andExpect(content().bytes("image".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void acceptsPromotionNamesInRequestBody() throws Exception {
        Files.write(temporaryRoot.resolve("file.txt"), "content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post("/file/promote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileNames\":[\"file.txt\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("file.txt")));
    }
}
