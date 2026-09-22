package com.own.order.repository;

import com.own.order.domain.CartItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByBuyerIdOrderByIdAsc(Long buyerId);
    CartItem findByIdAndBuyerId(Long id, Long buyerId);
    CartItem findByBuyerIdAndProductId(Long buyerId, Long productId);
}
