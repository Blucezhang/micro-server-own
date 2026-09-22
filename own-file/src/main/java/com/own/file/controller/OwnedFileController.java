package com.own.file.controller;

import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.security.InternalServiceGuard;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.file.domain.OwnedFileObject;
import com.own.file.service.OwnedFileService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** New actor-bound endpoint. It intentionally does not change legacy anonymous file APIs. */
@RestController
@RequestMapping("/file/api/v1/files")
public class OwnedFileController extends BaseController {
    private final OwnedFileService service;
    private final InternalServiceGuard internalServiceGuard;
    public OwnedFileController(OwnedFileService service, InternalServiceGuard internalServiceGuard) { this.service = service; this.internalServiceGuard = internalServiceGuard; }

    @PostMapping
    public Resp upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return new Resp(service.upload(TradeHeaders.actor(request), file), 201, "created");
    }

    @PostMapping("/{storedName}/promote")
    public Resp promote(@PathVariable String storedName, HttpServletRequest request) {
        return new Resp(service.promote(TradeHeaders.actor(request), storedName));
    }

    @GetMapping("/{storedName}")
    public Resp mine(@PathVariable String storedName, HttpServletRequest request) {
        return new Resp(service.findForOwner(TradeHeaders.actor(request), storedName));
    }

    @GetMapping("/{storedName}/content")
    public ResponseEntity<StreamingResponseBody> content(@PathVariable String storedName, HttpServletRequest request) {
        OwnedFileObject file = service.requirePermanentForOwner(TradeHeaders.actor(request), storedName);
        MediaType contentType;
        try { contentType = MediaType.parseMediaType(service.permanentContentType(file.getStoredName())); }
        catch (RuntimeException ignored) { contentType = MediaType.APPLICATION_OCTET_STREAM; }
        StreamingResponseBody body = output -> service.writePermanent(file.getStoredName(), output);
        return ResponseEntity.ok().contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getStoredName() + "\"")
                .body(body);
    }

    @GetMapping("/internal/buyers/{buyerId}/{storedName}")
    public Resp verifyBuyerFile(@PathVariable Long buyerId, @PathVariable String storedName,
                                HttpServletRequest request) {
        internalServiceGuard.require(request);
        return new Resp(service.requirePermanentOwnedByBuyer(TradeHeaders.actor(request), storedName, buyerId));
    }
}
