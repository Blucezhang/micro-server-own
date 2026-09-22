package com.own.file.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.file.domain.OwnedFileObject;
import com.own.file.domain.OwnedFileStatus;
import com.own.file.repository.OwnedFileObjectRepository;
import com.own.file.storage.FileStorageService;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class OwnedFileServiceTest {
    @Test
    public void onlyOwnerCanPromoteAndARepeatIsSafe() {
        FileStorageService storage = Mockito.mock(FileStorageService.class);
        OwnedFileObjectRepository repository = Mockito.mock(OwnedFileObjectRepository.class);
        OwnedFileObject file = new OwnedFileObject("file.jpg", 1L, "BUYER");
        Mockito.when(repository.findByStoredNameForUpdate("file.jpg")).thenReturn(file);
        Mockito.when(repository.save(file)).thenReturn(file);
        OwnedFileService service = new OwnedFileService(storage, repository);

        service.promote(new TradeActor(1L, ActorType.BUYER), "file.jpg");
        service.promote(new TradeActor(1L, ActorType.BUYER), "file.jpg");

        Assert.assertEquals(OwnedFileStatus.PERMANENT, file.getStorageStatus());
        Mockito.verify(storage).promoteTemporary(Collections.singletonList("file.jpg"));
        Mockito.verify(repository, Mockito.times(1)).save(file);
    }

    @Test
    public void buyerCannotUseAnotherBuyersFileAsEvidence() {
        FileStorageService storage = Mockito.mock(FileStorageService.class);
        OwnedFileObjectRepository repository = Mockito.mock(OwnedFileObjectRepository.class);
        OwnedFileObject file = new OwnedFileObject("file.jpg", 2L, "BUYER");
        file.promote();
        Mockito.when(repository.findByStoredName("file.jpg")).thenReturn(file);
        OwnedFileService service = new OwnedFileService(storage, repository);

        try {
            service.requirePermanentOwnedByBuyer(new TradeActor(1L, ActorType.SYSTEM), "file.jpg", 1L);
            Assert.fail("a buyer must not attach a file owned by another buyer");
        } catch (TradeException expected) {
            Assert.assertEquals(403, expected.getStatus());
        }
    }

    @Test
    public void ownerCanDownloadOnlyPromotedFile() {
        FileStorageService storage = Mockito.mock(FileStorageService.class);
        OwnedFileObjectRepository repository = Mockito.mock(OwnedFileObjectRepository.class);
        OwnedFileObject file = new OwnedFileObject("file.jpg", 1L, "BUYER");
        Mockito.when(repository.findByStoredName("file.jpg")).thenReturn(file);
        OwnedFileService service = new OwnedFileService(storage, repository);
        try { service.requirePermanentForOwner(new TradeActor(1L, ActorType.BUYER), "file.jpg"); Assert.fail("temporary file must not download"); }
        catch (TradeException expected) { Assert.assertEquals(409, expected.getStatus()); }
        file.promote();
        Assert.assertSame(file, service.requirePermanentForOwner(new TradeActor(1L, ActorType.BUYER), "file.jpg"));
    }
}
