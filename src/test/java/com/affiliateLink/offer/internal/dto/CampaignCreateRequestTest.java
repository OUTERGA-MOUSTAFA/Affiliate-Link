package com.affiliateLink.offer.internal.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.affiliateLink.offer.internal.entity.PayoutModel;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class CampaignCreateRequestTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private CampaignCreateRequest request(PayoutModel model, String value, LocalDate start, LocalDate end) {
        return new CampaignCreateRequest("Summer", start, end,
                BigDecimal.valueOf(1000), model, new BigDecimal(value));
    }

    private final LocalDate today = LocalDate.now();

    @Test
    void fixedPayoutCanExceed100() {
        var r = request(PayoutModel.CPC, "150", today, today.plusDays(10));
        assertTrue(validator.validate(r).isEmpty()); // 150 أورو: مقبول
    }

    @Test
    void percentagePayoutAbove100IsRejected() {
        var r = request(PayoutModel.CPA_PERCENT, "150", today, today.plusDays(10));
        assertFalse(validator.validate(r).isEmpty()); // 150%: مرفوض
    }

    @Test
    void percentagePayoutBetween0And100IsAccepted() {
        var r = request(PayoutModel.CPA_PERCENT, "25.50", today, today.plusDays(10));
        assertTrue(validator.validate(r).isEmpty());
    }

    @Test
    void zeroPayoutIsRejected() {
        var r = request(PayoutModel.CPL, "0", today, today.plusDays(10));
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        var r = request(PayoutModel.CPC, "2", today, today.minusDays(1));
        assertFalse(validator.validate(r).isEmpty());
    }
}