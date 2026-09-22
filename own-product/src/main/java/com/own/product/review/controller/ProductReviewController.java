package com.own.product.review.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.product.review.dto.CreateProductReviewCommand;
import com.own.product.review.dto.CreateProductReviewReportCommand;
import com.own.product.review.dto.ModerateReviewCommand;
import com.own.product.review.service.ProductReviewService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ProductReviewController extends BaseController {
    private final ProductReviewService service; public ProductReviewController(ProductReviewService service) { this.service = service; }
    @GetMapping public Resp list(@PathVariable Long productId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return new Resp(service.page(productId, page, size)); }
    @PostMapping public Resp create(@PathVariable Long productId, @RequestBody CreateProductReviewCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.BUYER); TradeHeaders.idempotencyKey(request);
        return new Resp(service.create(actor.getId(), productId, command), 201, "created");
    }
    @PostMapping("/{reviewId}/reports") public Resp report(@PathVariable Long productId, @PathVariable Long reviewId, @RequestBody CreateProductReviewReportCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.BUYER); TradeHeaders.idempotencyKey(request);
        return new Resp(service.report(actor.getId(), productId, reviewId, command), 201, "created");
    }
    @PutMapping("/{reviewId}/moderation") public Resp moderate(@PathVariable Long productId, @PathVariable Long reviewId, @RequestBody ModerateReviewCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.SYSTEM); TradeHeaders.idempotencyKey(request);
        return new Resp(service.moderate(productId, reviewId, command));
    }
}
