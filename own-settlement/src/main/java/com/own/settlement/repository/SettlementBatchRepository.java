package com.own.settlement.repository;
import com.own.settlement.domain.SettlementBatch;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long> { SettlementBatch findByBatchNo(String batchNo); }
