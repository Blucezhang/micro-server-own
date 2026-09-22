package com.own.file.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.file.domain.OwnedFileObject;
import com.own.file.domain.OwnedFileStatus;
import com.own.file.repository.OwnedFileObjectRepository;
import com.own.file.storage.FileStorageService;
import java.util.Collections;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OwnedFileService {
    private final FileStorageService storage;
    private final OwnedFileObjectRepository files;

    public OwnedFileService(FileStorageService storage, OwnedFileObjectRepository files) {
        this.storage = storage; this.files = files;
    }

    @Transactional
    public OwnedFileObject upload(TradeActor actor, MultipartFile upload) {
        requireOwnerActor(actor);
        String storedName = storage.storeTemporary(upload);
        return files.save(new OwnedFileObject(storedName, actor.getId(), actor.getType().name()));
    }

    @Transactional
    public OwnedFileObject promote(TradeActor actor, String storedName) {
        requireOwnerActor(actor);
        OwnedFileObject file = requireForUpdate(storedName);
        requireOwner(actor, file);
        if (file.getStorageStatus() == OwnedFileStatus.PERMANENT) return file;
        // A previous filesystem move may have completed immediately before a database failure.
        // Detect that case so an idempotent retry repairs metadata rather than failing on a missing temp file.
        if (!storage.hasPermanent(storedName)) storage.promoteTemporary(Collections.singletonList(storedName));
        file.promote();
        return files.save(file);
    }

    public OwnedFileObject requirePermanentOwnedByBuyer(TradeActor actor, String storedName, Long buyerId) {
        actor.require(ActorType.SYSTEM);
        OwnedFileObject file = require(storedName);
        if (file.getStorageStatus() != OwnedFileStatus.PERMANENT || !buyerId.equals(file.getOwnerId())
                || !ActorType.BUYER.name().equals(file.getOwnerType())) {
            throw TradeException.forbidden("file is not a permanent file owned by this buyer");
        }
        return file;
    }

    public OwnedFileObject findForOwner(TradeActor actor, String storedName) {
        requireOwnerActor(actor);
        OwnedFileObject file = require(storedName);
        requireOwner(actor, file);
        return file;
    }

    /** Binary access is available only after the owner promotes the file. */
    public OwnedFileObject requirePermanentForOwner(TradeActor actor, String storedName) {
        OwnedFileObject file = findForOwner(actor, storedName);
        if (file.getStorageStatus() != OwnedFileStatus.PERMANENT) throw TradeException.conflict("file must be promoted before download");
        return file;
    }
    public String permanentContentType(String storedName) { return storage.permanentContentType(storedName); }
    public void writePermanent(String storedName, java.io.OutputStream output) { storage.writePermanent(storedName, output); }

    private OwnedFileObject require(String storedName) {
        OwnedFileObject file = files.findByStoredName(storedName);
        if (file == null) throw TradeException.notFound("owned file was not found");
        return file;
    }
    private OwnedFileObject requireForUpdate(String storedName) {
        OwnedFileObject file = files.findByStoredNameForUpdate(storedName);
        if (file == null) throw TradeException.notFound("owned file was not found");
        return file;
    }
    private void requireOwnerActor(TradeActor actor) {
        if (actor.getType() != ActorType.BUYER && actor.getType() != ActorType.MERCHANT) {
            throw TradeException.forbidden("buyer or merchant actor is required");
        }
    }
    private void requireOwner(TradeActor actor, OwnedFileObject file) {
        if (!actor.getId().equals(file.getOwnerId()) || !actor.getType().name().equals(file.getOwnerType())) {
            throw TradeException.forbidden("actor does not own this file");
        }
    }
}
