package util;

import model.AmortizationType;
import model.FinancingModel;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmortizationCalculator {


    public static void calculateInstallments(FinancingModel financing) {

        BigDecimal outstandingBalance = financing.getFinancedAmount();
        int installments = financing.getLoanTermInMonths();
        BigDecimal monthlyInterestRate = financing.getAnnualInterestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal totalAmountPaid = BigDecimal.ZERO;
        BigDecimal installmentAmount = BigDecimal.ZERO;

        if (financing.getAmortizationType() == AmortizationType.SAC) {

            BigDecimal amortization = outstandingBalance
                    .divide(BigDecimal.valueOf(installments), 10, RoundingMode.HALF_UP);

            BigDecimal firstInstallmentAmount = null;
            BigDecimal lastInstallmentAmount = BigDecimal.ZERO;

            for (int i = 0; i < installments; i++) {
                BigDecimal interest = outstandingBalance.multiply(monthlyInterestRate);
                BigDecimal currentInstallment = amortization.add(interest);

                if (i == 0) {
                    firstInstallmentAmount = currentInstallment;
                }
                lastInstallmentAmount = currentInstallment;

                totalAmountPaid = totalAmountPaid.add(currentInstallment);
                outstandingBalance = outstandingBalance.subtract(amortization);
            }

            installmentAmount = firstInstallmentAmount;
            BigDecimal installmentDecrement = amortization.multiply(monthlyInterestRate);

            financing.setLastInstallmentAmount(lastInstallmentAmount);
            financing.setInstallmentDecrement(installmentDecrement);

        } else if (financing.getAmortizationType() == AmortizationType.PRICE) {

            BigDecimal factor = BigDecimal.ONE.add(monthlyInterestRate).pow(installments);

            BigDecimal numerator = monthlyInterestRate.multiply(factor);
            BigDecimal denominator = factor.subtract(BigDecimal.ONE);

            installmentAmount = outstandingBalance.multiply(
                    numerator.divide(denominator, 10, RoundingMode.HALF_UP));

            totalAmountPaid = installmentAmount.multiply(BigDecimal.valueOf(installments));

            financing.setLastInstallmentAmount(installmentAmount);
            financing.setInstallmentDecrement(BigDecimal.ZERO);
        }

        financing.setInstallmentAmount(installmentAmount);
        financing.setTotalAmountPaid(totalAmountPaid);
    }

    public static BigDecimal getInstallmentAtMonth(FinancingModel financing, int month) {
        if (month < 1 || month > financing.getLoanTermInMonths()) {
            throw new IllegalArgumentException("Month must be between 1 and the loan term.");
        }

        BigDecimal decrement = financing.getInstallmentDecrement();

        if (decrement == null || decrement.compareTo(BigDecimal.ZERO) == 0) {
            return financing.getInstallmentAmount();
        }

        return financing.getInstallmentAmount()
                .subtract(decrement.multiply(BigDecimal.valueOf(month - 1)));
    }
}