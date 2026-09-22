package com.own.file.domain;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Metadata for files that must be attributable to a business actor. */
@Entity
@Table(name = "file_owned_object")
public class OwnedFileObject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "stored_name", nullable = false, unique = true, length = 255) private String storedName;
    @Column(name = "owner_id", nullable = false) private Long ownerId;
    @Column(name = "owner_type", nullable = false, length = 16) private String ownerType;
    @Enumerated(EnumType.STRING) @Column(name = "storage_status", nullable = false, length = 16)
    private OwnedFileStatus storageStatus;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "promoted_at") private Date promotedAt;

    protected OwnedFileObject() { }
    public OwnedFileObject(String storedName, Long ownerId, String ownerType) {
        this.storedName = storedName; this.ownerId = ownerId; this.ownerType = ownerType;
        this.storageStatus = OwnedFileStatus.TEMPORARY; this.createdAt = new Date();
    }
    public void promote() {
        if (storageStatus == OwnedFileStatus.TEMPORARY) {
            storageStatus = OwnedFileStatus.PERMANENT; promotedAt = new Date();
        }
    }
    public Long getId() { return id; }
    public String getStoredName() { return storedName; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerType() { return ownerType; }
    public OwnedFileStatus getStorageStatus() { return storageStatus; }
    public Date getCreatedAt() { return createdAt; }
    public Date getPromotedAt() { return promotedAt; }
}
