package com.own.product.favorite.repository;
import com.own.product.favorite.domain.BuyerFavorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BuyerFavoriteRepository extends JpaRepository<BuyerFavorite, Long> { BuyerFavorite findByBuyerIdAndProductId(Long buyerId, Long productId); List<BuyerFavorite> findByBuyerIdOrderByIdDesc(Long buyerId); }
