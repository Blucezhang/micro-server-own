package com.own.promotion.coupon.service;

import com.own.face.trade.TradeException;
import com.own.promotion.coupon.domain.CouponStatus;
import com.own.promotion.coupon.domain.CouponStock;
import com.own.promotion.coupon.domain.CouponType;
import com.own.promotion.coupon.domain.UserCoupon;
import com.own.promotion.coupon.domain.MerchantCouponTemplate;
import com.own.promotion.coupon.dto.ClaimCouponCommand;
import com.own.promotion.coupon.dto.CouponReservationCommand;
import com.own.promotion.coupon.dto.CouponReservationResult;
import com.own.promotion.coupon.dto.CouponUse;
import com.own.promotion.coupon.dto.MerchantCouponTemplateCommand;
import com.own.promotion.coupon.repository.CouponStockRepository;
import com.own.promotion.coupon.repository.UserCouponRepository;
import com.own.promotion.coupon.repository.MerchantCouponTemplateRepository;
import com.own.promotion.dao.MallTicketDao;
import com.own.promotion.dao.StoreTicketDao;
import com.own.promotion.dao.domain.MallTicket;
import com.own.promotion.dao.domain.StoreTicket;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Service
public class CouponService {

    private final CouponStockRepository couponStockRepository;
    private final UserCouponRepository userCouponRepository;
    private final MallTicketDao mallTicketDao;
    private final StoreTicketDao storeTicketDao;
    private final MerchantCouponTemplateRepository merchantTemplateRepository;
    private final int reservationTtlMinutes;

    @org.springframework.beans.factory.annotation.Autowired
    public CouponService(CouponStockRepository couponStockRepository, UserCouponRepository userCouponRepository,
                         MallTicketDao mallTicketDao, StoreTicketDao storeTicketDao,
                         MerchantCouponTemplateRepository merchantTemplateRepository,
                         @Value("${trade.promotion.reservation-ttl-minutes:15}") int reservationTtlMinutes) {
        this.couponStockRepository = couponStockRepository;
        this.userCouponRepository = userCouponRepository;
        this.mallTicketDao = mallTicketDao;
        this.storeTicketDao = storeTicketDao;
        this.merchantTemplateRepository = merchantTemplateRepository;
        this.reservationTtlMinutes = reservationTtlMinutes;
    }

    public CouponService(CouponStockRepository couponStockRepository, UserCouponRepository userCouponRepository,
                         MallTicketDao mallTicketDao, StoreTicketDao storeTicketDao, int reservationTtlMinutes) {
        this(couponStockRepository, userCouponRepository, mallTicketDao, storeTicketDao, null, reservationTtlMinutes);
    }

    @Transactional
    public UserCoupon claim(Long buyerId, ClaimCouponCommand command) {
        if (buyerId == null || buyerId.longValue() <= 0L || command == null || (command.getTicketId() == null && command.getMerchantTemplateId() == null)) {
            throw TradeException.badRequest("buyerId and ticketId or merchantTemplateId are required");
        }
        CouponType type = parseType(command.getCouponType());
        OfferTerms terms = loadTerms(type, command);
        boolean onePerBuyer = terms.scopeKey.startsWith("MERCHANT_TEMPLATE:");
        if (onePerBuyer && userCouponRepository.findByBuyerIdAndSourceKey(buyerId, terms.scopeKey) != null) {
            throw TradeException.conflict("merchant coupon template has already been claimed by this buyer");
        }
        // Insert first, then take a database lock. This prevents two first-claim requests
        // from both constructing a full in-memory stock record or leaking a unique-key error.
        couponStockRepository.insertIfAbsent(terms.scopeKey, terms.totalQuantity);
        CouponStock stock = couponStockRepository.findByScopeKeyForUpdate(terms.scopeKey);
        if (stock == null) {
            throw new IllegalStateException("coupon stock initialization did not produce a row");
        }
        try {
            stock.claim();
        } catch (IllegalStateException exception) {
            throw TradeException.conflict("coupon stock is exhausted");
        }
        couponStockRepository.save(stock);
        UserCoupon coupon = new UserCoupon("CPN-" + UUID.randomUUID().toString(), buyerId, type,
                command.getMerchantTemplateId() == null ? command.getTicketId() : command.getMerchantTemplateId(), terms.merchantId, terms.minimumAmount, terms.discountAmount, terms.expiresAt, terms.scopeKey);
        try {
            return onePerBuyer ? userCouponRepository.saveAndFlush(coupon) : userCouponRepository.save(coupon);
        } catch (DataIntegrityViolationException exception) {
            throw TradeException.conflict("merchant coupon template has already been claimed by this buyer");
        }
    }

    public List<UserCoupon> findByBuyer(Long buyerId) {
        return userCouponRepository.findByBuyerIdOrderByIdDesc(buyerId);
    }

    /** Aggregated only: merchant staff must not receive buyers' coupon records. */
    public Map<String, Object> merchantSummary(Long merchantId) {
        if (merchantId == null || merchantId.longValue() <= 0L) throw TradeException.badRequest("merchantId is required");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("issued", userCouponRepository.countByMerchantId(merchantId));
        for (CouponStatus status : CouponStatus.values()) result.put(status.name().toLowerCase(), userCouponRepository.countByMerchantIdAndStatus(merchantId, status));
        return result;
    }

    @Transactional
    public MerchantCouponTemplate createMerchantTemplate(Long merchantId, MerchantCouponTemplateCommand command) {
        if (merchantTemplateRepository == null) throw new IllegalStateException("merchant coupon templates are not configured");
        if (merchantId == null || merchantId.longValue() <= 0L) throw TradeException.badRequest("merchantId is required");
        TemplateTerms terms = validateTemplate(command);
        return merchantTemplateRepository.save(new MerchantCouponTemplate(merchantId, terms.name, terms.totalQuantity, terms.minimumAmount, terms.discountAmount, terms.claimStartsAt, terms.claimEndsAt, terms.expiresAt));
    }

    public List<Map<String, Object>> merchantTemplates(Long merchantId) {
        if (merchantTemplateRepository == null) throw new IllegalStateException("merchant coupon templates are not configured");
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (MerchantCouponTemplate template : merchantTemplateRepository.findByMerchantIdOrderByIdDesc(merchantId)) {
            CouponStock stock = couponStockRepository.findByScopeKey("MERCHANT_TEMPLATE:" + template.getId());
            Map<String, Object> item = new HashMap<String, Object>(); item.put("template", template);
            item.put("availableQuantity", stock == null ? template.getTotalQuantity() : stock.getAvailableQuantity());
            result.add(item);
        }
        return result;
    }

    @Transactional
    public MerchantCouponTemplate changeMerchantTemplateStatus(Long merchantId, Long templateId, String status) {
        if (merchantTemplateRepository == null) throw new IllegalStateException("merchant coupon templates are not configured");
        MerchantCouponTemplate template = templateId == null ? null : merchantTemplateRepository.findByIdForUpdate(templateId);
        if (template == null) throw TradeException.notFound("merchant coupon template was not found");
        if (!merchantId.equals(template.getMerchantId())) throw TradeException.forbidden("merchant does not own coupon template");
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!"ACTIVE".equals(normalized) && !"DISABLED".equals(normalized)) throw TradeException.unprocessable("status must be ACTIVE or DISABLED");
        template.setStatus(normalized); return merchantTemplateRepository.save(template);
    }

    /** Pure coupon calculation for checkout previews. It never reserves or releases a coupon. */
    public CouponReservationResult preview(CouponReservationCommand command) {
        validateReservationCommand(command, false);
        List<CouponUse> uses = command.getCoupons();
        if (uses == null || uses.isEmpty()) {
            return new CouponReservationResult(BigDecimal.ZERO, new ArrayList<String>(), new HashMap<Long, BigDecimal>());
        }
        Set<String> requestedCouponNos = requestedCouponNos(uses);
        int mallCount = 0;
        Set<Long> storeMerchants = new HashSet<Long>();
        BigDecimal total = BigDecimal.ZERO;
        List<String> couponNos = new ArrayList<String>();
        Map<Long, BigDecimal> merchantDiscounts = new HashMap<Long, BigDecimal>();
        for (CouponUse use : uses) {
            UserCoupon coupon = requireCouponForPreview(use.getCouponNo(), command.getBuyerId());
            validateCouponUse(coupon, use, command.getParentAmount(), storeMerchants, mallCount);
            if (coupon.getCouponType() == CouponType.MALL) mallCount++; else storeMerchants.add(coupon.getMerchantId());
            total = total.add(coupon.getDiscountAmount());
            couponNos.add(coupon.getCouponNo());
            allocateDiscount(coupon, command, merchantDiscounts);
        }
        return new CouponReservationResult(total, couponNos, merchantDiscounts);
    }

    @Transactional
    public CouponReservationResult reserve(CouponReservationCommand command) {
        validateReservationCommand(command, true);
        List<CouponUse> uses = command.getCoupons();
        if (uses == null || uses.isEmpty()) {
            return new CouponReservationResult(BigDecimal.ZERO, new ArrayList<String>(), new HashMap<Long, BigDecimal>());
        }
        requestedCouponNos(uses);
        int mallCount = 0;
        Set<Long> storeMerchants = new HashSet<Long>();
        BigDecimal total = BigDecimal.ZERO;
        List<String> couponNos = new ArrayList<String>();
        Map<Long, BigDecimal> merchantDiscounts = new HashMap<Long, BigDecimal>();
        for (CouponUse use : uses) {
            String couponNo = use.getCouponNo();
            UserCoupon coupon = requireCoupon(couponNo, command.getBuyerId());
            if (coupon.getStatus() == CouponStatus.RESERVED
                    && command.getOrderNo().equals(coupon.getReservedOrderNo())) {
                total = total.add(coupon.getDiscountAmount());
                couponNos.add(coupon.getCouponNo());
                allocateDiscount(coupon, command, merchantDiscounts);
                continue;
            }
            validateCouponUse(coupon, use, command.getParentAmount(), storeMerchants, mallCount);
            if (coupon.getCouponType() == CouponType.MALL) {
                mallCount++;
            } else {
                storeMerchants.add(coupon.getMerchantId());
            }
            Date now = new Date();
            Date configuredDeadline = new Date(now.getTime() + reservationTtlMinutes * 60L * 1000L);
            coupon.reserve(command.getOrderNo(), coupon.getExpiresAt().before(configuredDeadline) ? coupon.getExpiresAt() : configuredDeadline);
            userCouponRepository.save(coupon);
            total = total.add(coupon.getDiscountAmount());
            couponNos.add(coupon.getCouponNo());
            allocateDiscount(coupon, command, merchantDiscounts);
        }
        return new CouponReservationResult(total, couponNos, merchantDiscounts);
    }

    @Transactional
    public void consumeOrder(String orderNo) {
        for (UserCoupon coupon : userCouponRepository.findByReservedOrderNoAndStatus(orderNo, CouponStatus.RESERVED)) {
            coupon.consume();
            userCouponRepository.save(coupon);
        }
    }

    @Transactional
    public void releaseOrder(String orderNo) {
        for (UserCoupon coupon : userCouponRepository.findByReservedOrderNoAndStatus(orderNo, CouponStatus.RESERVED)) {
            coupon.release(new Date());
            userCouponRepository.save(coupon);
        }
        for (UserCoupon coupon : userCouponRepository.findByReservedOrderNoAndStatus(orderNo, CouponStatus.USED)) {
            coupon.restoreAfterRefund(new Date());
            userCouponRepository.save(coupon);
        }
    }

    @Scheduled(fixedDelayString = "${trade.promotion.reservation-expiry-scan-delay-millis:60000}")
    @Transactional
    public int releaseExpiredReservations() {
        Date now = new Date(); int released = 0;
        for (UserCoupon coupon : userCouponRepository.findByStatusAndReservationExpiresAtBefore(CouponStatus.RESERVED, now)) {
            coupon.release(now); userCouponRepository.save(coupon); released++;
        }
        return released;
    }

    private void validateCouponUse(UserCoupon coupon, CouponUse use, BigDecimal parentAmount,
                                   Set<Long> storeMerchants, int mallCount) {
        if (coupon.getStatus() != CouponStatus.AVAILABLE || coupon.getExpiresAt().before(new Date())) {
            throw TradeException.conflict("coupon is not available");
        }
        BigDecimal amount = coupon.getCouponType() == CouponType.MALL
                ? parentAmount : (use == null ? null : use.getApplicableAmount());
        if (amount == null || amount.compareTo(coupon.getMinimumAmount()) < 0) {
            throw TradeException.unprocessable("coupon minimum amount is not met");
        }
        if (coupon.getDiscountAmount().compareTo(amount) > 0) {
            throw TradeException.unprocessable("coupon discount cannot exceed its applicable amount");
        }
        if (coupon.getCouponType() == CouponType.MALL && mallCount > 0) {
            throw TradeException.unprocessable("only one mall coupon can be used per order");
        }
        if (coupon.getCouponType() == CouponType.STORE) {
            if (use == null || use.getMerchantId() == null || !coupon.getMerchantId().equals(use.getMerchantId())) {
                throw TradeException.unprocessable("store coupon merchant does not match order");
            }
            if (storeMerchants.contains(coupon.getMerchantId())) {
                throw TradeException.unprocessable("only one store coupon can be used per merchant");
            }
        }
    }

    private void allocateDiscount(UserCoupon coupon, CouponReservationCommand command,
                                  Map<Long, BigDecimal> merchantDiscounts) {
        if (coupon.getCouponType() == CouponType.STORE) {
            addDiscount(merchantDiscounts, coupon.getMerchantId(), coupon.getDiscountAmount());
            return;
        }
        Map<Long, BigDecimal> amounts = command.getMerchantAmounts();
        if (amounts == null || amounts.isEmpty()) {
            throw TradeException.unprocessable("merchant amounts are required for a mall coupon");
        }
        BigDecimal allocated = BigDecimal.ZERO;
        Long lastMerchant = null;
        for (Map.Entry<Long, BigDecimal> entry : amounts.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                throw TradeException.unprocessable("merchant amounts are invalid");
            }
            lastMerchant = entry.getKey();
            BigDecimal share = coupon.getDiscountAmount().multiply(entry.getValue())
                    .divide(command.getParentAmount(), 2, RoundingMode.DOWN);
            addDiscount(merchantDiscounts, entry.getKey(), share);
            allocated = allocated.add(share);
        }
        if (lastMerchant != null) {
            addDiscount(merchantDiscounts, lastMerchant, coupon.getDiscountAmount().subtract(allocated));
        }
    }

    private void addDiscount(Map<Long, BigDecimal> discounts, Long merchantId, BigDecimal amount) {
        BigDecimal current = discounts.get(merchantId);
        discounts.put(merchantId, current == null ? amount : current.add(amount));
    }

    private UserCoupon requireCoupon(String couponNo, Long buyerId) {
        if (couponNo == null || couponNo.trim().isEmpty()) {
            throw TradeException.badRequest("couponNo is required");
        }
        UserCoupon coupon = userCouponRepository.findByCouponNoForUpdate(couponNo);
        if (coupon == null || !coupon.getBuyerId().equals(buyerId)) {
            throw TradeException.notFound("coupon was not found for buyer");
        }
        return coupon;
    }

    private UserCoupon requireCouponForPreview(String couponNo, Long buyerId) {
        if (couponNo == null || couponNo.trim().isEmpty()) throw TradeException.badRequest("couponNo is required");
        UserCoupon coupon = userCouponRepository.findByCouponNo(couponNo);
        if (coupon == null || !coupon.getBuyerId().equals(buyerId)) throw TradeException.notFound("coupon was not found for buyer");
        return coupon;
    }

    private void validateReservationCommand(CouponReservationCommand command, boolean requireOrderNo) {
        if (command == null || command.getBuyerId() == null || (requireOrderNo && (command.getOrderNo() == null || command.getOrderNo().trim().isEmpty()))) {
            throw TradeException.badRequest(requireOrderNo ? "orderNo and buyerId are required" : "buyerId is required");
        }
    }

    private Set<String> requestedCouponNos(List<CouponUse> uses) {
        Set<String> requested = new HashSet<String>();
        for (CouponUse use : uses) {
            String couponNo = use == null ? null : use.getCouponNo();
            if (couponNo == null || couponNo.trim().isEmpty() || !requested.add(couponNo.trim())) {
                throw TradeException.unprocessable("a coupon can be used only once per order");
            }
        }
        return requested;
    }

    private CouponType parseType(String value) {
        try {
            return CouponType.valueOf(value == null ? "" : value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw TradeException.badRequest("couponType must be MALL or STORE");
        }
    }

    private OfferTerms loadTerms(CouponType type, ClaimCouponCommand command) {
        if (command.getMerchantTemplateId() != null) {
            if (type != CouponType.STORE) throw TradeException.unprocessable("merchant coupon template must use STORE type");
            if (merchantTemplateRepository == null) throw new IllegalStateException("merchant coupon templates are not configured");
            MerchantCouponTemplate template = merchantTemplateRepository.findByIdForUpdate(command.getMerchantTemplateId());
            if (template == null) throw TradeException.notFound("merchant coupon template was not found");
            if (!"ACTIVE".equals(template.getStatus())) throw TradeException.conflict("merchant coupon template is disabled");
            if (command.getMerchantId() != null && !command.getMerchantId().equals(template.getMerchantId())) throw TradeException.unprocessable("merchantId does not match coupon template");
            validateClaimWindow(template.getClaimStartsAt(), template.getClaimEndsAt());
            if (template.getExpiresAt().before(new Date())) throw TradeException.unprocessable("coupon ticket is expired");
            return new OfferTerms("MERCHANT_TEMPLATE:" + template.getId(), template.getMerchantId(), template.getTotalQuantity(), template.getMinimumAmount(), template.getDiscountAmount(), template.getExpiresAt());
        }
        if (type == CouponType.MALL) {
            MallTicket ticket = mallTicketDao.getFromId(command.getTicketId().intValue());
            if (ticket == null) {
                throw TradeException.notFound("mall ticket was not found");
            }
            validateClaimWindow(ticket.getReceiveStartTime(), ticket.getReceivieEndTime());
            return terms(type, command.getTicketId(), null, ticket.getNumber(), ticket.getFull(), ticket.getMinux(),
                    ticket.getApplyEndTime());
        }
        if (command.getMerchantId() == null || command.getMerchantId().longValue() <= 0L) {
            throw TradeException.badRequest("merchantId is required for a store coupon");
        }
        StoreTicket ticket = storeTicketDao.getFromId(command.getTicketId().intValue());
        if (ticket == null) {
            throw TradeException.notFound("store ticket was not found");
        }
        validateClaimWindow(ticket.getReceiveStartTime(), ticket.getReceiveEndTime());
        int discount = ticket.getMinux() > 0 ? ticket.getMinux() : ticket.getValue();
        return terms(type, command.getTicketId(), command.getMerchantId(), ticket.getNumber(), ticket.getFull(), discount,
                ticket.getApplyEndTime());
    }

    private OfferTerms terms(CouponType type, Long ticketId, Long merchantId, int number, int full,
                             int discount, Date expiresAt) {
        if (number <= 0 || discount <= 0) {
            throw TradeException.unprocessable("coupon ticket must define positive stock and discount");
        }
        Date validUntil = expiresAt == null ? defaultExpiry() : expiresAt;
        if (validUntil.before(new Date())) {
            throw TradeException.unprocessable("coupon ticket is expired");
        }
        return new OfferTerms(type + ":" + ticketId + ":" + (merchantId == null ? 0 : merchantId),
                merchantId, number, BigDecimal.valueOf(full), BigDecimal.valueOf(discount), validUntil);
    }

    private Date defaultExpiry() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        return calendar.getTime();
    }

    private void validateClaimWindow(Date start, Date end) {
        Date now = new Date();
        if (start != null && now.before(start)) throw TradeException.conflict("coupon claim has not started");
        if (end != null && now.after(end)) throw TradeException.conflict("coupon claim has ended");
    }

    private TemplateTerms validateTemplate(MerchantCouponTemplateCommand command) {
        if (command == null || command.getName() == null || command.getName().trim().isEmpty() || command.getName().trim().length() > 128 || command.getTotalQuantity() == null || command.getTotalQuantity().intValue() <= 0 || command.getExpiresAt() == null) throw TradeException.unprocessable("name, positive totalQuantity and expiresAt are required");
        BigDecimal minimum = decimal(command.getMinimumAmount(), "minimumAmount"); BigDecimal discount = decimal(command.getDiscountAmount(), "discountAmount");
        if (discount.compareTo(BigDecimal.ZERO) <= 0 || minimum.compareTo(BigDecimal.ZERO) < 0) throw TradeException.unprocessable("coupon amounts are invalid");
        Date start = command.getClaimStartsAt() == null ? null : new Date(command.getClaimStartsAt()); Date end = command.getClaimEndsAt() == null ? null : new Date(command.getClaimEndsAt()); Date expires = new Date(command.getExpiresAt());
        if (expires.before(new Date()) || start != null && end != null && start.after(end) || end != null && end.after(expires)) throw TradeException.unprocessable("coupon time range is invalid");
        return new TemplateTerms(command.getName().trim(), command.getTotalQuantity(), minimum, discount, start, end, expires);
    }

    private BigDecimal decimal(String value, String name) { try { return new BigDecimal(value); } catch (Exception exception) { throw TradeException.unprocessable(name + " is invalid"); } }

    private static final class OfferTerms {
        private final String scopeKey;
        private final Long merchantId;
        private final int totalQuantity;
        private final BigDecimal minimumAmount;
        private final BigDecimal discountAmount;
        private final Date expiresAt;

        private OfferTerms(String scopeKey, Long merchantId, int totalQuantity, BigDecimal minimumAmount,
                           BigDecimal discountAmount, Date expiresAt) {
            this.scopeKey = scopeKey;
            this.merchantId = merchantId;
            this.totalQuantity = totalQuantity;
            this.minimumAmount = minimumAmount;
            this.discountAmount = discountAmount;
            this.expiresAt = expiresAt;
        }
    }

    private static final class TemplateTerms {
        private final String name; private final Integer totalQuantity; private final BigDecimal minimumAmount; private final BigDecimal discountAmount; private final Date claimStartsAt; private final Date claimEndsAt; private final Date expiresAt;
        private TemplateTerms(String name, Integer totalQuantity, BigDecimal minimumAmount, BigDecimal discountAmount, Date claimStartsAt, Date claimEndsAt, Date expiresAt) { this.name=name;this.totalQuantity=totalQuantity;this.minimumAmount=minimumAmount;this.discountAmount=discountAmount;this.claimStartsAt=claimStartsAt;this.claimEndsAt=claimEndsAt;this.expiresAt=expiresAt; }
    }
}
