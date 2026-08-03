package model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealEstateFinancingTest {
    @Test
    void mustContainHouseSpecificFields() {
        RealEstateFinancing financing = new RealEstateFinancing(
                new BigDecimal("100000"),
                120,
                new BigDecimal("5"),
                AmortizationType.PRICE,
                PropertyType.HOUSE,
                FinancingStatus.SIMULATION,
                3,
                2,
                new BigDecimal("200"),
                1,
                true,
                new BigDecimal("500"),
                "Residential",
                1
        );

        String resultado = financing.toString();

        assertTrue(resultado.contains("Property Type: HOUSE"));
        assertTrue(resultado.contains("Rooms: "));
        assertTrue(resultado.contains("Parking Spaces: "));
        assertTrue(resultado.contains("Land Area: "));
        assertFalse(resultado.contains("Floor: "));
        assertFalse(resultado.contains("Elevator: "));
        assertFalse(resultado.contains("Condominium Fee: R$ "));

    }
    @Test
    void mustContainApartmentSpecificFields() {
        RealEstateFinancing financing = new RealEstateFinancing(
                new BigDecimal("100000"),
                120,
                new BigDecimal("5"),
                AmortizationType.PRICE,
                PropertyType.APARTMENT,
                FinancingStatus.SIMULATION,
                3,
                2,
                new BigDecimal("200"),
                1,
                true,
                new BigDecimal("500"),
                "Residential",
                1
        );

        String resultado = financing.toString();

        assertTrue(resultado.contains("Property Type: APARTMENT"));
        assertFalse(resultado.contains("Rooms: "));
        assertFalse(resultado.contains("Parking Spaces: "));
        assertFalse(resultado.contains("Land Area: "));
        assertTrue(resultado.contains("Floor: "));
        assertTrue(resultado.contains("Elevator: "));
        assertTrue(resultado.contains("Condominium Fee: R$ "));
    }

    @Test
    void mustContainLandSpecificFields() {
        RealEstateFinancing financing = new RealEstateFinancing(
                new BigDecimal("100000"),
                120,
                new BigDecimal("5"),
                AmortizationType.PRICE,
                PropertyType.LAND,
                FinancingStatus.SIMULATION,
                3,
                2,
                new BigDecimal("200"),
                1,
                true,
                new BigDecimal("500"),
                "Residential",
                1
        );

        String resultado = financing.toString();

        assertTrue(resultado.contains("Property Type: LAND"));
        assertFalse(resultado.contains("Rooms: "));
        assertFalse(resultado.contains("Parking Spaces: "));
        assertTrue(resultado.contains("Land Area: "));
        assertFalse(resultado.contains("Floor: "));
        assertFalse(resultado.contains("Elevator: "));
        assertFalse(resultado.contains("Condominium Fee: R$ "));
    }

    @Test
    void mustContainCommonFieldsRegardlessOfPropertyType() {
        RealEstateFinancing financing = new RealEstateFinancing(
                new BigDecimal("100000"),
                120,
                new BigDecimal("5"),
                AmortizationType.PRICE,
                PropertyType.HOUSE,
                FinancingStatus.SIMULATION,
                3,
                2,
                new BigDecimal("200"),
                1,
                true,
                new BigDecimal("500"),
                "Residential",
                1
        );

        String resultado = financing.toString();

        assertTrue(resultado.contains("Financing ID: "));
        assertTrue(resultado.contains("Property Type: "));
        assertTrue(resultado.contains("Amortization Type: "));
        assertTrue(resultado.contains("Financing Status: "));
        assertTrue(resultado.contains("Financed Amount: R$ "));
        assertTrue(resultado.contains("Loan Term: "));
        assertTrue(resultado.contains("Annual Interest Rate: "));
        assertTrue(resultado.contains("Installment Amount: R$ "));
        assertTrue(resultado.contains("Total Amount Paid: R$ "));
        assertTrue(resultado.contains("Zoning: "));
    }


}
