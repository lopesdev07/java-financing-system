package model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VehicleFinancingTest {
    @Test
    void mustContainMileageForUsedVehicle() {
        VehicleFinancing financing = new VehicleFinancing(
                new BigDecimal("50000"),
                60,
                new BigDecimal("7"),
                AmortizationType.PRICE,
                VehicleType.CAR,
                VehicleCondition.USED,
                FinancingStatus.SIMULATION,
                "Toyota",
                "Corolla",
                2020,
                1,1
        );

        String resultado = financing.toString();

        assertTrue(resultado.contains("Mileage: "));
    }

    @Test
    void mustNotContainMileageForNewVehicle() {
        VehicleFinancing financing = new VehicleFinancing(
                new BigDecimal("50000"),
                60,
                new BigDecimal("7"),
                AmortizationType.PRICE,
                VehicleType.CAR,
                VehicleCondition.NEW,
                FinancingStatus.SIMULATION,
                "Toyota",
                "Corolla",
                2023,
                1,1
        );

        String resultado = financing.toString();

        assertFalse(resultado.contains("Mileage: "));
    }

    @Test
    void mustContainCommonFieldsRegardlessOfVehicleCondition() {
        VehicleFinancing financing = new VehicleFinancing(
                new BigDecimal("50000"),
                60,
                new BigDecimal("7"),
                AmortizationType.PRICE,
                VehicleType.CAR,
                VehicleCondition.NEW,
                FinancingStatus.SIMULATION,
                "Toyota",
                "Corolla",
                2023,
                1,1
        );

        String resultado = financing.toString();

        assertTrue(resultado.contains("Financing ID: "));
        assertTrue(resultado.contains("Vehicle: "));
        assertTrue(resultado.contains("Vehicle Type: "));
        assertTrue(resultado.contains("Condition: "));
        assertTrue(resultado.contains("Amortization Type: "));
        assertTrue(resultado.contains("Financing Status: "));
        assertTrue(resultado.contains("Financed Amount: R$ "));
        assertTrue(resultado.contains("Loan Term: "));
        assertTrue(resultado.contains("Annual Interest Rate: "));
        assertTrue(resultado.contains("Installment Amount: R$ "));
        assertTrue(resultado.contains("Total Amount Paid: R$ "));
    }
}
