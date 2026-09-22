package com.own.product.favorite.controller;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.product.favorite.service.BuyerFavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product/api/v1/favorites")
public class BuyerFavoriteController extends BaseController {
    private final BuyerFavoriteService service; public BuyerFavoriteController(BuyerFavoriteService service) { this.service = service; }
    @PostMapping("/{productId}") public Resp add(@PathVariable Long productId, HttpServletRequest request) { TradeActor actor = buyer(request); TradeHeaders.idempotencyKey(request); return new Resp(service.add(actor.getId(), productId), 201, "created"); }
    @DeleteMapping("/{productId}") public Resp remove(@PathVariable Long productId, HttpServletRequest request) { TradeActor actor = buyer(request); TradeHeaders.idempotencyKey(request); service.remove(actor.getId(), productId); return new Resp(productId); }
    @GetMapping public Resp list(HttpServletRequest request) { return new Resp(service.list(buyer(request).getId())); }
    private TradeActor buyer(HttpServletRequest request) { TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.BUYER); return actor; }
}
