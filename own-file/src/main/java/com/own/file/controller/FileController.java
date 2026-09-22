package com.own.file.controller;

import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.file.controller.dto.FilePromotionRequest;
import com.own.file.storage.FileStorageException;
import com.own.file.storage.FileStorageService;
import com.own.file.storage.InvalidStoragePathException;
import com.own.file.storage.InvalidStorageRequestException;
import com.own.file.storage.StoredFileNotFoundException;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController extends BaseController {

    private final FileStorageService storageService;

    public FileController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @ApiOperation(value = "图片上传接口")
    @PostMapping("/picture")
    public @ResponseBody Resp uploadFile(@RequestParam("file") MultipartFile file) {
        String storedName = storageService.storeTemporary(file);
        return new Resp(storedName, HttpStatus.ACCEPTED.value(), "success");
    }

    @ApiOperation(value = "下载临时文件图片 根据imgUri获得图片并输出到response")
    @GetMapping("/tmpPicture/{fileName}/{fileSuffix}")
    public @ResponseBody void downloadTmpFile(HttpServletResponse response,
                                               @PathVariable String fileName,
                                               @PathVariable String fileSuffix) throws IOException {
        String storedName = storageService.legacyFileName(fileName, fileSuffix);
        prepareDownload(response, storedName, storageService.temporaryContentType(storedName));
        storageService.writeTemporary(storedName, response.getOutputStream());
    }

    @ApiOperation(value = "下载正式文件图片 根据imgUri获得图片并输出到response")
    @RequestMapping(value = "/Picture/{fileName}/{fileSuffix}", method = RequestMethod.GET)
    public @ResponseBody void downloadFile(HttpServletResponse response,
                                            @PathVariable String fileName,
                                            @PathVariable String fileSuffix) throws IOException {
        String storedName = storageService.legacyFileName(fileName, fileSuffix);
        prepareDownload(response, storedName, storageService.permanentContentType(storedName));
        storageService.writePermanent(storedName, response.getOutputStream());
    }

    @ApiOperation(value = "将图片从临时目录移动到正式目录")
    @PostMapping("/Copy/{fileNames}")
    public @ResponseBody void copy(@PathVariable String[] fileNames) {
        storageService.promoteTemporary(Arrays.asList(fileNames));
    }

    @ApiOperation(value = "将文件从临时目录移动到正式目录（请求体接口）")
    @PostMapping("/promote")
    public ResponseEntity<List<String>> promote(@RequestBody FilePromotionRequest request) {
        List<String> promoted = storageService.promoteTemporary(
                request == null ? null : request.getFileNames());
        return ResponseEntity.ok(promoted);
    }

    @ExceptionHandler({InvalidStoragePathException.class, InvalidStorageRequestException.class})
    public ResponseEntity<Map<String, Object>> invalidRequest(FileStorageException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(StoredFileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(StoredFileNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Map<String, Object>> storageFailure(FileStorageException exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "File storage operation failed");
    }

    private void prepareDownload(HttpServletResponse response, String fileName, String contentType) {
        response.setContentType(MediaType.parseMediaType(contentType).toString());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
