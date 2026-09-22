package com.own.user.party.address.controller;
import com.own.face.trade.ActorType; import com.own.face.trade.TradeActor; import com.own.face.trade.TradeHeaders; import com.own.face.util.Resp; import com.own.face.util.base.BaseController; import com.own.user.party.address.dto.AddressCommand; import com.own.user.party.address.service.BuyerAddressService; import javax.servlet.http.HttpServletRequest; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/addresses") public class BuyerAddressController extends BaseController {
 private final BuyerAddressService service; public BuyerAddressController(BuyerAddressService service){this.service=service;} private TradeActor buyer(HttpServletRequest r){TradeActor a=TradeHeaders.actor(r);a.require(ActorType.BUYER);return a;}
 @GetMapping public Resp list(HttpServletRequest r){return new Resp(service.list(buyer(r).getId()));}
 @PostMapping public Resp create(@RequestBody AddressCommand c,HttpServletRequest r){TradeActor a=buyer(r);TradeHeaders.idempotencyKey(r);return new Resp(service.create(a.getId(),c),201,"created");}
 @GetMapping("/{id}") public Resp get(@PathVariable Long id,HttpServletRequest r){return new Resp(service.get(buyer(r).getId(),id));}
 @PutMapping("/{id}") public Resp update(@PathVariable Long id,@RequestBody AddressCommand c,HttpServletRequest r){TradeActor a=buyer(r);TradeHeaders.idempotencyKey(r);return new Resp(service.update(a.getId(),id,c));}
 @PostMapping("/{id}/default") public Resp setDefault(@PathVariable Long id,HttpServletRequest r){TradeActor a=buyer(r);TradeHeaders.idempotencyKey(r);return new Resp(service.setDefault(a.getId(),id));}
 @DeleteMapping("/{id}") public Resp delete(@PathVariable Long id,HttpServletRequest r){TradeActor a=buyer(r);TradeHeaders.idempotencyKey(r);service.delete(a.getId(),id);return new Resp(id);}
}
