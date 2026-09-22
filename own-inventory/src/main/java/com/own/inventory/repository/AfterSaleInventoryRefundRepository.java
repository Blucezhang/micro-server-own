package com.own.inventory.repository;
import com.own.inventory.domain.AfterSaleInventoryRefund;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AfterSaleInventoryRefundRepository extends JpaRepository<AfterSaleInventoryRefund, Long> { AfterSaleInventoryRefund findByAfterSaleNoAndProductId(String afterSaleNo, Long productId); }
