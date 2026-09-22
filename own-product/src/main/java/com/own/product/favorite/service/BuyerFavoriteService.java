package com.own.product.favorite.service;
import com.own.face.trade.TradeException;
import com.own.product.dao.ProductDao;
import com.own.product.domain.Product;
import com.own.product.favorite.domain.BuyerFavorite;
import com.own.product.favorite.repository.BuyerFavoriteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuyerFavoriteService {
    private final BuyerFavoriteRepository favorites; private final ProductDao products;
    public BuyerFavoriteService(BuyerFavoriteRepository favorites, ProductDao products) { this.favorites = favorites; this.products = products; }
    @Transactional public BuyerFavorite add(Long buyerId, Long productId) {
        if (buyerId == null || buyerId.longValue() <= 0 || productId == null || productId.longValue() <= 0) throw TradeException.unprocessable("buyerId and productId are required");
        Product product = products.queryProductById(productId); if (product == null) throw TradeException.notFound("product was not found");
        if (product.getSaleStatus() != null && !"AVAILABLE".equals(product.getSaleStatus())) throw TradeException.conflict("product is off shelf");
        BuyerFavorite existing = favorites.findByBuyerIdAndProductId(buyerId, productId); return existing == null ? favorites.save(new BuyerFavorite(buyerId, productId)) : existing;
    }
    @Transactional public void remove(Long buyerId, Long productId) { BuyerFavorite favorite = favorites.findByBuyerIdAndProductId(buyerId, productId); if (favorite == null) throw TradeException.notFound("favorite was not found"); favorites.delete(favorite); }
    public List<BuyerFavorite> list(Long buyerId) { return favorites.findByBuyerIdOrderByIdDesc(buyerId); }
}
