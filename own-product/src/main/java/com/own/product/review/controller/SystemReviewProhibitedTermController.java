package com.own.product.review.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.product.review.moderation.dto.ReviewProhibitedTermCommand;
import com.own.product.review.moderation.service.ReviewProhibitedTermService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** System-only moderation vocabulary management for product reviews and merchant replies. */
@RestController
@RequestMapping("/api/v1/system/review-prohibited-terms")
public class SystemReviewProhibitedTermController extends BaseController {
    private final ReviewProhibitedTermService service;
    public SystemReviewProhibitedTermController(ReviewProhibitedTermService service) { this.service = service; }

    @GetMapping
    public Resp list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                     HttpServletRequest request) {
        system(request); return new Resp(service.page(page, size));
    }

    @PostMapping
    public Resp create(@RequestBody ReviewProhibitedTermCommand command, HttpServletRequest request) {
        TradeActor actor = system(request); TradeHeaders.idempotencyKey(request);
        return new Resp(service.create(actor.getId(), command), 201, "created");
    }

    @PutMapping("/{id}")
    public Resp update(@PathVariable Long id, @RequestBody ReviewProhibitedTermCommand command, HttpServletRequest request) {
        TradeActor actor = system(request); TradeHeaders.idempotencyKey(request);
        return new Resp(service.update(actor.getId(), id, command));
    }

    private TradeActor system(HttpServletRequest request) { TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.SYSTEM); return actor; }
}
