package com.own.file.repository;

import com.own.file.domain.OwnedFileObject;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OwnedFileObjectRepository extends JpaRepository<OwnedFileObject, Long> {
    OwnedFileObject findByStoredName(String storedName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from OwnedFileObject f where f.storedName = ?1")
    OwnedFileObject findByStoredNameForUpdate(String storedName);
}
