package com.own.product.review.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.product.review.dto.ReviewReplyCommand;
import com.own.product.review.service.ProductReviewService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant/reviews")
public class MerchantReviewController extends BaseController {
    private final ProductReviewService service; public MerchantReviewController(ProductReviewService service) { this.service = service; }
    @PostMapping("/{reviewId}/reply") public Resp reply(@PathVariable Long reviewId, @RequestBody ReviewReplyCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.MERCHANT); TradeHeaders.idempotencyKey(request);
        return new Resp(service.publicView(service.reply(actor.getId(), reviewId, command)));
    }
}
