package model;

import java.math.BigDecimal;


public abstract class FinancingModel {

    private Integer financingId;
    private BigDecimal financedAmount;
    private Integer loanTermInMonths;
    private BigDecimal annualInterestRate; // Annual rate; monthly calculations are handled in subclasses.
    private AmortizationType amortizationType;
    private FinancingStatus status;
    protected int userId;
    private BigDecimal installmentAmount;
    private BigDecimal totalAmountPaid;

    public FinancingModel(
            BigDecimal financedAmount,
            Integer loanTermInMonths,
            BigDecimal annualInterestRate,
            AmortizationType amortizationType,
            FinancingStatus status,
            int userId
    ) {
        this.financedAmount = financedAmount;
        this.loanTermInMonths = loanTermInMonths;
        this.annualInterestRate = annualInterestRate;
        this.amortizationType = amortizationType;
        this.status = status;
        this.userId = userId;
    }

    public BigDecimal getFinancedAmount() {
        return this.financedAmount;
    }

    public Integer getLoanTermInMonths() {
        return this.loanTermInMonths;
    }

    public BigDecimal getAnnualInterestRate() {
        return this.annualInterestRate;
    }

    public AmortizationType getAmortizationType() {
        return this.amortizationType;
    }

    public FinancingStatus getStatus() {
        return this.status;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getFinancingId() {
        return this.financingId;
    }

    public void setFinancingId(Integer financingId) {
        this.financingId = financingId;
    }

    public BigDecimal getInstallmentAmount() {
        return this.installmentAmount;
    }

    public void setInstallmentAmount(BigDecimal installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public BigDecimal getTotalAmountPaid() {
        return this.totalAmountPaid;
    }

    public void setTotalAmountPaid(BigDecimal totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
    }
}