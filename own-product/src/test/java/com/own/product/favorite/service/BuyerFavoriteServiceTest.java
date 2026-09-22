package com.own.product.favorite.service;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;
import com.own.product.dao.ProductDao;
import com.own.product.domain.Product;
import com.own.product.favorite.domain.BuyerFavorite;
import com.own.product.favorite.repository.BuyerFavoriteRepository;
import org.junit.Test;
public class BuyerFavoriteServiceTest {
 @Test public void duplicateFavoriteIsIdempotentAtServiceBoundary(){BuyerFavoriteRepository favorites=mock(BuyerFavoriteRepository.class);ProductDao products=mock(ProductDao.class);Product product=new Product();product.setSaleStatus("AVAILABLE");when(products.queryProductById(3L)).thenReturn(product);BuyerFavorite existing=new BuyerFavorite(1L,3L);when(favorites.findByBuyerIdAndProductId(1L,3L)).thenReturn(existing);BuyerFavoriteService service=new BuyerFavoriteService(favorites,products);assertSame(existing,service.add(1L,3L));verify(favorites,never()).save(any(BuyerFavorite.class));}
}
