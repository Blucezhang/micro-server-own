package com.own.product.review.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.product.review.dto.ResolveProductReviewReportCommand;
import com.own.product.review.service.ProductReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/review-reports")
public class SystemProductReviewReportController extends BaseController {
    private final ProductReviewService service;
    public SystemProductReviewReportController(ProductReviewService service) { this.service = service; }
    @GetMapping public Resp list(@RequestParam(defaultValue = "PENDING") String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, HttpServletRequest request) {
        system(request); return new Resp(service.reportPage(status, page, size));
    }
    @PostMapping("/{reportId}/resolve") public Resp resolve(@PathVariable Long reportId, @RequestBody ResolveProductReviewReportCommand command, HttpServletRequest request) {
        TradeActor actor = system(request); TradeHeaders.idempotencyKey(request); return new Resp(service.resolveReport(actor.getId(), reportId, command));
    }
    private TradeActor system(HttpServletRequest request) { TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.SYSTEM); return actor; }
}
