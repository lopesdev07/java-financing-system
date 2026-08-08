package service;

import model.AmortizationType;
import model.FinancingStatus;
import model.PropertyType;
import model.RealEstateFinancing;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import repository.RealEstateFinancingRepository;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class RealEstateFinancingTest {
    @Test
    void findFinancingByIdMustThrowIllegalArgumentExceptionWhenFinancingNotFound() throws SQLException {
        RealEstateFinancingRepository realEstateFinancingRepository = Mockito.mock(RealEstateFinancingRepository.class);
        RealEstateFinancingService realEstateFinancingService = new RealEstateFinancingService(realEstateFinancingRepository);

        Mockito.when(realEstateFinancingRepository.findById(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> realEstateFinancingService.findFinancingById(1));
        assertEquals("Financing not found.", ex.getMessage());
    }

    @Test
    void findFinancingByIdMustThrowIllegalArgumentExceptionWhenFinancingIdIsNull() throws SQLException {
        RealEstateFinancingRepository realEstateFinancingRepository = Mockito.mock(RealEstateFinancingRepository.class);
        RealEstateFinancingService realEstateFinancingService = new RealEstateFinancingService(realEstateFinancingRepository);
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000),
                360,
                BigDecimal.valueOf(10.5),
                AmortizationType.PRICE,
                PropertyType.HOUSE,
                FinancingStatus.REQUESTED,
                3,
                2,
                BigDecimal.valueOf(450.0),
                null,
                null,
                null,
                "Residential",
                1
        );
        financing.setFinancingId(null);

        Mockito.when(realEstateFinancingRepository.findById(1)).thenReturn(financing);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> realEstateFinancingService.findFinancingById(1)
        );

        assertEquals("Invalid financing.", ex.getMessage());
    }

    @Test
    void findFinancingByIdMustThrowIllegalStateExceptionWhenUserIsNotAuthorized() throws SQLException {
        RealEstateFinancingRepository realEstateFinancingRepository = Mockito.mock(RealEstateFinancingRepository.class);
        RealEstateFinancingService realEstateFinancingService = new RealEstateFinancingService(realEstateFinancingRepository);
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000),
                360,
                BigDecimal.valueOf(10.5),
                AmortizationType.PRICE,
                PropertyType.HOUSE,
                FinancingStatus.REQUESTED,
                3,
                2,
                BigDecimal.valueOf(450.0),
                null,
                null,
                null,
                "Residential",
                1
        );
        financing.setFinancingId(1);
        Session.login(2);

        Mockito.when(realEstateFinancingRepository.findById(1)).thenReturn(financing);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> realEstateFinancingService.findFinancingById(1)
        );
        assertEquals("User is not authorized to view this financing.", ex.getMessage());
    }



}
