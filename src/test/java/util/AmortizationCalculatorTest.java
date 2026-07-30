package util;

import model.AmortizationType;
import model.FinancingStatus;
import model.VehicleFinancing;
import model.VehicleCondition;
import model.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmortizationCalculatorTest {

    @Test
    void mustCalculateInstallmentsByPriceMethod() {
        VehicleFinancing financing = new VehicleFinancing(
                new BigDecimal("10000"),
                12,
                new BigDecimal("12"),
                AmortizationType.PRICE,
                VehicleType.CAR,
                VehicleCondition.NEW,
                FinancingStatus.SIMULATION,
                1
        );


        AmortizationCalculator.calculateInstallments(financing);

        BigDecimal expectedInstallment = new BigDecimal("888.49");
        BigDecimal totalExpected = new BigDecimal("10661.85");

        assertEquals(0, expectedInstallment.compareTo(
                financing.getInstallmentAmount().setScale(2, java.math.RoundingMode.HALF_UP)));
        assertEquals(0, totalExpected.compareTo(
                financing.getTotalAmountPaid().setScale(2, java.math.RoundingMode.HALF_UP)));
    }

    @Test
    void deveCalcularParcelaCorretamentePeloMetodoSac() {

        VehicleFinancing financing = new VehicleFinancing(
                new BigDecimal("10000"),
                12,
                new BigDecimal("12"),
                AmortizationType.SAC,
                VehicleType.CAR,
                VehicleCondition.NEW,
                FinancingStatus.SIMULATION,
                1
        );

        AmortizationCalculator.calculateInstallments(financing);


        BigDecimal lastExpectedInstallment = new BigDecimal("841.67");
        BigDecimal totalExpected = new BigDecimal("10650.00");

        assertEquals(0, lastExpectedInstallment.compareTo(
                financing.getInstallmentAmount().setScale(2, java.math.RoundingMode.HALF_UP)));
        assertEquals(0, totalExpected.compareTo(
                financing.getTotalAmountPaid().setScale(2, java.math.RoundingMode.HALF_UP)));
    }
}