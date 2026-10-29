package com.affiliateLink.offer.internal.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.affiliateLink.common.InvalidStatusTransitionException;

@Entity
@Table(name = "offer_campaigns")
public class Campaign {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_model", nullable = false, length = 20)
    private PayoutModel payoutModel;

    @Column(name = "payout_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal payoutValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status = CampaignStatus.DRAFT;

    public Campaign() {

    }

    public Campaign(Offer offer, String name, LocalDate startDate, LocalDate endDate, BigDecimal budget,
            PayoutModel payoutModel, BigDecimal payoutValue, CampaignStatus status) {

        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }
        if (budget.signum() <= 0) {
            throw new IllegalArgumentException("budget must be greater than 0");
        }
        validatePayout(payoutModel, payoutValue);

        this.offer = offer;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.payoutModel = payoutModel;
        this.payoutValue = payoutValue;
        this.status = status;
    }

    // ---- قواعد الـ business (محمولة من الـ entity) ----

    private static void validatePayout(PayoutModel model, BigDecimal value) {
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("payoutValue must be greater than 0");
        }
        if (model == PayoutModel.CPA_PERCENT && value.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("A percentage payout cannot exceed 100");
        }
    }

    // DRAFT -> ACTIVE أو PAUSED -> ACTIVE
    // f service campaign.activate()
    public void activate() {
        if (status != CampaignStatus.DRAFT && status != CampaignStatus.PAUSED) {
            throw invalidTransition(CampaignStatus.ACTIVE);
        }
        if (endDate.isBefore(LocalDate.now())) {
            throw new InvalidStatusTransitionException("A campaign whose end date has passed cannot be activated");
        }
        status = CampaignStatus.ACTIVE;
    }

    // ACTIVE -> PAUSED
    public void pause() {
        if (status != CampaignStatus.ACTIVE) {
            throw invalidTransition(CampaignStatus.PAUSED);
        }
        status = CampaignStatus.PAUSED;
    }

    // ACTIVE -> ENDED أو PAUSED -> ENDED
    public void end() {
        if (status != CampaignStatus.ACTIVE && status != CampaignStatus.PAUSED) {
            throw invalidTransition(CampaignStatus.ENDED);
        }
        status = CampaignStatus.ENDED;
    }

    private InvalidStatusTransitionException invalidTransition(CampaignStatus target) {
        return new InvalidStatusTransitionException("Cannot go from " + status + " to " + target);
    }

    // package-private: كيناديه غير Offer.addCampaign() (نفس الـ package)
    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public static BigDecimal getOneHundred() {
        return ONE_HUNDRED;
    }

    public Long getId() {
        return id;
    }

    public Offer getOffer() {
        return offer;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public PayoutModel getPayoutModel() {
        return payoutModel;
    }

    public BigDecimal getPayoutValue() {
        return payoutValue;
    }

    public CampaignStatus getStatus() {
        return status;
    }

}
