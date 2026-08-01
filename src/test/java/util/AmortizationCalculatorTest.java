package util;

import model.AmortizationType;
import model.FinancingStatus;
import model.PropertyType;
import model.RealEstateFinancing;
import model.VehicleFinancing;
import model.VehicleCondition;
import model.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmortizationCalculatorTest {

    @Test
    void mustCalculateVehicleInstallmentsByPriceMethod() {
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
                financing.getInstallmentAmount().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, totalExpected.compareTo(
                financing.getTotalAmountPaid().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    void mustCalculateVehicleInstallmentsBySACMethod() {

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

        BigDecimal firstExpectedInstallment = new BigDecimal("933.33");
        BigDecimal lastExpectedInstallment = new BigDecimal("841.67");
        BigDecimal expectedDecrement = new BigDecimal("8.33");
        BigDecimal totalExpected = new BigDecimal("10650.00");

        assertEquals(0, firstExpectedInstallment.compareTo(
                financing.getInstallmentAmount().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, lastExpectedInstallment.compareTo(
                financing.getLastInstallmentAmount().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, expectedDecrement.compareTo(
                financing.getInstallmentDecrement().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, totalExpected.compareTo(
                financing.getTotalAmountPaid().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    void mustCalculateRealEstateInstallmentsByPriceMethod() {
        RealEstateFinancing financing = new RealEstateFinancing(
                new BigDecimal("200000"),
                240,
                new BigDecimal("9"),
                AmortizationType.PRICE,
                PropertyType.HOUSE,
                FinancingStatus.SIMULATION,
                1
        );

        AmortizationCalculator.calculateInstallments(financing);

        BigDecimal expectedInstallment = new BigDecimal("1799.45");
        BigDecimal totalExpected = new BigDecimal("431868.46");

        assertEquals(0, expectedInstallment.compareTo(
                financing.getInstallmentAmount().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, totalExpected.compareTo(
                financing.getTotalAmountPaid().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    void mustCalculateRealEstateInstallmentsBySacMethod() {
        RealEstateFinancing financing = new RealEstateFinancing(
                new BigDecimal("200000"),
                240,
                new BigDecimal("9"),
                AmortizationType.SAC,
                PropertyType.HOUSE,
                FinancingStatus.SIMULATION,
                1
        );

        AmortizationCalculator.calculateInstallments(financing);

        BigDecimal firstExpectedInstallment = new BigDecimal("2333.33");
        BigDecimal lastExpectedInstallment = new BigDecimal("839.58");
        BigDecimal expectedDecrement = new BigDecimal("6.25");
        BigDecimal totalExpected = new BigDecimal("380750.00");

        assertEquals(0, firstExpectedInstallment.compareTo(
                financing.getInstallmentAmount().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, lastExpectedInstallment.compareTo(
                financing.getLastInstallmentAmount().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, expectedDecrement.compareTo(
                financing.getInstallmentDecrement().setScale(2, RoundingMode.HALF_UP)));
        assertEquals(0, totalExpected.compareTo(
                financing.getTotalAmountPaid().setScale(2, RoundingMode.HALF_UP)));
    }
}