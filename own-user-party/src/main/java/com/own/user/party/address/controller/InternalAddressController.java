package com.own.user.party.address.controller;
import com.own.face.security.InternalServiceGuard; import com.own.face.trade.ActorType; import com.own.face.trade.TradeHeaders; import com.own.face.util.Resp; import com.own.face.util.base.BaseController; import com.own.user.party.address.service.BuyerAddressService; import javax.servlet.http.HttpServletRequest; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/internal/buyers") public class InternalAddressController extends BaseController {
 private final BuyerAddressService service; private final InternalServiceGuard internalServiceGuard; public InternalAddressController(BuyerAddressService service,InternalServiceGuard internalServiceGuard){this.service=service;this.internalServiceGuard=internalServiceGuard;}
 @GetMapping("/{buyerId}/addresses/{id}") public Resp get(@PathVariable Long buyerId,@PathVariable Long id,HttpServletRequest request){internalServiceGuard.require(request);TradeHeaders.actor(request).require(ActorType.SYSTEM);return new Resp(service.get(buyerId,id));}
}
