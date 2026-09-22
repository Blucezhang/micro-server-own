package com.own.promotion.controller;

import com.own.face.promotion.CartBean;
import com.own.promotion.controller.bean.SellerBean;
import com.own.promotion.dao.MallTicketDao;
import com.own.promotion.dao.ProductDao;
import com.own.promotion.dao.PromotionDao;
import com.own.promotion.dao.SellerDao;
import com.own.promotion.dao.StoreTicketDao;
import com.own.promotion.dao.domain.Promotion;
import com.own.promotion.dao.domain.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PromotionControllerTest {

    @Mock private PromotionDao promotionDao;
    @Mock private SellerDao sellerDao;
    @Mock private ProductDao productDao;
    @Mock private MallTicketDao mallTicketDao;
    @Mock private StoreTicketDao storeTicketDao;

    private PromotionController controller;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new PromotionController();
        ReflectionTestUtils.setField(controller, "promotionDao", promotionDao);
        ReflectionTestUtils.setField(controller, "sellerDao", sellerDao);
        ReflectionTestUtils.setField(controller, "productDao", productDao);
        ReflectionTestUtils.setField(controller, "mallTicketDao", mallTicketDao);
        ReflectionTestUtils.setField(controller, "storeTicketDao", storeTicketDao);
    }

    @Test
    public void productPromotionQueryUsesMethodParameter() throws Exception {
        Method method = PromotionDao.class.getMethod("findProByProductInfo", String.class);
        String cypher = method.getAnnotation(Query.class).value();
        assertFalse(cypher.contains("'111'"));
        assertEquals(true, cypher.contains("$0"));
        assertFalse(cypher.contains("{0}"));
    }

    @Test
    public void discountPromotionCalculatesJoinedTotal() throws Exception {
        Promotion promotion = new Promotion();
        promotion.setDisCount(0.8D);
        when(promotionDao.findNode(7L)).thenReturn(promotion);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("promotionTypeId", "12");
        parameters.put("singlePrice", "25");
        parameters.put("join", "true");
        parameters.put("productId", "p-1");
        parameters.put("amount", "2");
        parameters.put("promotionId", "7");
        parameters.put("productName", "demo");
        parameters.put("productJson", "[]");
        parameters.put("productTotolCount", "2");
        CartBean cart = controller.calculateCart(parameters);
        assertEquals(40D, cart.getAfterTotal(), 0.001D);
    }

    @Test
    public void sellerIsSavedBeforeRelationshipIsCreated() {
        Seller saved = new Seller();
        saved.setId(42L);
        when(sellerDao.save(any(Seller.class))).thenReturn(saved);
        SellerBean request = new SellerBean();
        request.setSellerId("seller-1");
        request.setSellerName("Demo seller");
        request.setPromotionId(9);
        controller.save(request);
        verify(sellerDao).save(any(Seller.class));
        verify(sellerDao).createRelationshipJoin(42, 9, "JOIN");
    }

    @Test
    public void typeAndZoneQueriesDelegateToDao() {
        controller.findPromotionByTypeId(3L);
        controller.findPromotionByZoneId(4L);
        verify(promotionDao).findProByTypeInfo(3L);
        verify(promotionDao).findProByZoneInfo(4L);
    }

    @Test
    public void unknownPromotionTypeIsRejected() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("promotionTypeId", "999");
        assertThrows(IllegalArgumentException.class, () -> controller.save(parameters));
    }
}
