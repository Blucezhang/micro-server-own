package com.own.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.trade.TradeException;
import com.own.face.trade.idempotency.*;
import com.own.face.util.Resp;
import com.own.product.dao.ProductDao;
import com.own.product.dto.MerchantProductCommand;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MerchantProductIdempotencyTest {
    @Test
    void publicProductCreateReplaysItsResponseAndRejectsAChangedCommand() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ProductDao products = mock(ProductDao.class);
        when(products.save(any())).thenAnswer(call -> call.getArgument(0));
        TradeIdempotencyRecordTransactions operations = mock(TradeIdempotencyRecordTransactions.class);
        AtomicReference<IdempotencyRecord> current = new AtomicReference<>();
        when(operations.lookup(anyString(), any(), anyString(), anyString())).thenAnswer(call -> current.get());
        when(operations.create(anyString(), any(), anyString(), anyString(), anyString(), anyInt()))
                .thenAnswer(call -> {
                    IdempotencyRecord record = new IdempotencyRecord("own-product", 7L, "MERCHANT",
                            "/product/api/v1/merchant/products", "create-1", call.getArgument(4),
                            new Date(System.currentTimeMillis() + 60_000));
                    current.set(record);
                    return record;
                });
        doAnswer(call -> {
            Resp result = call.getArgument(1);
            current.get().complete(result.getStatus(), mapper.writeValueAsString(result));
            return null;
        }).when(operations).complete(any(), any());
        AspectJProxyFactory proxy = new AspectJProxyFactory(new MerchantProductController(products));
        proxy.addAspect(new TradeIdempotencyAspect(new TradeIdempotencyStore(operations), mapper, "own-product", 24));
        MerchantProductController controller = proxy.getProxy();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/product/api/v1/merchant/products");
        request.addHeader("X-Actor-Id", "7"); request.addHeader("X-Actor-Type", "MERCHANT");
        request.addHeader("Idempotency-Key", "create-1");
        MerchantProductCommand command = new MerchantProductCommand();
        command.setPartyId(7L); command.setName("Tea"); command.setSkuCode("TEA-1"); command.setOriginalPrice("10");
        Resp first = controller.create(command, request);
        Resp repeated = controller.create(command, request);
        assertEquals(mapper.writeValueAsString(first), mapper.writeValueAsString(repeated));
        command.setName("Coffee");
        assertEquals(409, assertThrows(TradeException.class, () -> controller.create(command, request)).getStatus());
        verify(products, times(1)).save(any());
    }
}
