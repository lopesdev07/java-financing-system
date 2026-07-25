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

            for (int i = 0; i < installments; i++) {
                BigDecimal interest = outstandingBalance.multiply(monthlyInterestRate);
                installmentAmount = amortization.add(interest);
                totalAmountPaid = totalAmountPaid.add(installmentAmount);
                outstandingBalance = outstandingBalance.subtract(amortization);
            }

        } else if (financing.getAmortizationType() == AmortizationType.PRICE) {

            BigDecimal factor = BigDecimal.ONE.add(monthlyInterestRate).pow(installments);

            BigDecimal numerator = monthlyInterestRate.multiply(factor);
            BigDecimal denominator = factor.subtract(BigDecimal.ONE);

            installmentAmount = outstandingBalance.multiply(
                    numerator.divide(denominator, 10, RoundingMode.HALF_UP));

            totalAmountPaid = installmentAmount.multiply(BigDecimal.valueOf(installments));
        }

        financing.setInstallmentAmount(installmentAmount);
        financing.setTotalAmountPaid(totalAmountPaid);
    }
}
