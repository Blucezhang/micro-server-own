package com.own.inventory.repository;
import com.own.inventory.domain.AfterSaleInventoryExchange; import org.springframework.data.jpa.repository.JpaRepository;
public interface AfterSaleInventoryExchangeRepository extends JpaRepository<AfterSaleInventoryExchange,Long>{ AfterSaleInventoryExchange findByAfterSaleNoAndProductId(String afterSaleNo,Long productId); }
