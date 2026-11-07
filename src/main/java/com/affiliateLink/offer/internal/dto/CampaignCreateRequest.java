package com.affiliateLink.offer.internal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.affiliateLink.offer.internal.entity.PayoutModel;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CampaignCreateRequest(
                @NotBlank @Size(max = 100) String name,
                @NotNull LocalDate startDate,
                @NotNull LocalDate endDate,
                @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal budget,
                @NotNull PayoutModel payoutModel,
                @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal payoutValue) {

        private static final BigDecimal /* 100 */ Meya = BigDecimal.valueOf(100);

        //assertTrue katgol l method khas terje3 trus ola 400, ila rj3at 400 donc sift had message
        // o had method sans parametters bash Spring i3taberha property 
        @AssertTrue(message = "endDate must be after startDate")
        public boolean isDateRangeValid() {
                if (startDate == null || endDate == null) {
                        return true;
                }
                return endDate.isAfter(startDate);

        }

        @AssertTrue(message = "a percentage payout (CPA_PERCENT) must be between 0 and 100")
        public boolean isPayoutValueValid() {
                if (payoutModel == null || payoutValue == null) {
                        return true;
                }
                if (payoutModel == PayoutModel.CPA_PERCENT) {
                        return payoutValue.compareTo(Meya) <= 0; // الـ > 0 كيتحقق بـ @Positive
                }
                return true; // CPC / CPL / CPA_FIXED: أي مبلغ > 0 كافي
        }

}
