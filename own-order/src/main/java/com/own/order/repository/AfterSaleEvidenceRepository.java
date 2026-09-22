package com.own.order.repository;

import com.own.order.domain.AfterSaleEvidence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfterSaleEvidenceRepository extends JpaRepository<AfterSaleEvidence, Long> {
    List<AfterSaleEvidence> findByAfterSaleNoOrderByIdAsc(String afterSaleNo);
}
