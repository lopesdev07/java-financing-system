package service;

import exceptions.FinancingNotFoundException;
import exceptions.InvalidDownPaymentException;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import repository.RealEstateFinancingRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class RealEstateFinancingServiceTest {
    @AfterEach
    void resetSession() {
        Session.logout();
    }

    private RealEstateFinancingRepository repository;
    private RealEstateFinancingService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(RealEstateFinancingRepository.class);
        service = new RealEstateFinancingService(repository);
        Session.login(1);
    }

    @Test
    void findFinancingByIdMustThrowIllegalArgumentExceptionWhenFinancingNotFound() throws SQLException {
        Mockito.when(repository.findById(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.findFinancingById(1));
        assertEquals("Financing not found.", ex.getMessage());
    }

    @Test
    void findFinancingByIdMustThrowIllegalArgumentExceptionWhenFinancingIdIsNull() throws SQLException {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.REQUESTED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );
        financing.setFinancingId(null);

        Mockito.when(repository.findById(1)).thenReturn(financing);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.findFinancingById(1)
        );
        assertEquals("Invalid financing.", ex.getMessage());
    }

    @Test
    void findFinancingByIdMustThrowIllegalStateExceptionWhenUserIsNotAuthorized() throws SQLException {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.REQUESTED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );
        financing.setFinancingId(1);
        Session.login(2); // sobrescreve o login(1) do @BeforeEach de propósito — dono é 1, sessão é 2

        Mockito.when(repository.findById(1)).thenReturn(financing);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.findFinancingById(1)
        );
        assertEquals("User is not authorized to view this financing.", ex.getMessage());
    }

    @Test
    void findFinancingByIdMustReturnFinancingCorrectlyAndCalculateInstallments() throws SQLException {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.REQUESTED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );
        financing.setFinancingId(1);

        Mockito.when(repository.findById(1)).thenReturn(financing);

        RealEstateFinancing result = service.findFinancingById(1);
        assertEquals(financing, result);
        assertNotNull(result.getInstallmentAmount());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenDownPaymentIsNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("-5000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        ));
        assertEquals("Values must be positive.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenPropertyValueIsZeroOrNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("-100000"), new BigDecimal("50000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        ));
        assertEquals("Values must be positive.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenLoanTermInMonthsIsZeroOrNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("50000"), -360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        ));
        assertEquals("Values must be positive.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenPropertyConditionIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("50000"), 360,
                null, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        ));
        assertEquals("Property condition is required.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenAmortizationTypeIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("50000"), 360,
                PropertyCondition.NEW, null,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        ));
        assertEquals("Amortization type is required.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenPropertyTypeIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("50000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                null, 2, 3, null, null, null, null, "Residential"
        ));
        assertEquals("Property type is required.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenDownPaymentIsGreaterThanPropertyValue() {
        InvalidDownPaymentException ex = assertThrows(InvalidDownPaymentException.class, () -> service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("150000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        ));
        assertEquals("The down payment cannot be greater than the property value.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenUserIsNotLoggedIn() {
        Session.logout();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("50000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        ));
        assertEquals("User is not authenticated.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustReturnFinancingCorrectlyWithRejectedStatus() throws InvalidDownPaymentException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("100000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        );

        RealEstateFinancing result = service.getCurrentFinancing();
        assertEquals(FinancingStatus.REJECTED, result.getStatus());
    }

    @Test
    void simulateFinancingMustReturnFinancingCorrectlyWithApprovedStatus() throws InvalidDownPaymentException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("10000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        );

        RealEstateFinancing result = service.getCurrentFinancing();
        assertEquals(FinancingStatus.APPROVED, result.getStatus());
    }

    @Test
    void simulateFinancingMustReturnFinancingCorrectlyWithInterestRateAt7Percent() throws InvalidDownPaymentException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("10000"), 360,
                PropertyCondition.SECOND_HAND, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        );

        RealEstateFinancing result = service.getCurrentFinancing();
        assertEquals(new BigDecimal("7.0"), result.getAnnualInterestRate());
    }

    @Test
    void simulateFinancingMustReturnFinancingCorrectlyWithInterestRateAt5Percent() throws InvalidDownPaymentException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("10000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        );

        RealEstateFinancing result = service.getCurrentFinancing();
        assertEquals(new BigDecimal("5.0"), result.getAnnualInterestRate());
    }

    @Test
    void saveCurrentFinancingMustThrowIllegalStateExceptionWhenUserIsNotLoggedIn() {
        Session.logout();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.saveCurrentFinancing());
        assertEquals("User is not authenticated.", ex.getMessage());
    }

    @Test
    void saveCurrentFinancingMustThrowIllegalStateExceptionWhenCurrentFinancingIsNull() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.saveCurrentFinancing());
        assertEquals("No simulation to save.", ex.getMessage());
    }

    @Test
    void saveCurrentFinancingMustCallRepositorySaveMethod() throws InvalidDownPaymentException, SQLException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("10000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE,
                PropertyType.HOUSE, 2, 3, null, null, null, null, "Residential"
        );
        RealEstateFinancing result = service.getCurrentFinancing();

        service.saveCurrentFinancing();

        Mockito.verify(repository).saveFinancing(result);
    }

    @Test
    void cancelFinancingMustThrowIllegalStateExceptionWhenUserIsNotLoggedIn() {
        Session.logout();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.cancelFinancing(1));
        assertEquals("User is not authenticated.", ex.getMessage());
    }

    @Test
    void cancelFinancingMustThrowIllegalArgumentExceptionWhenCurrentFinancingIdIsNull() throws InvalidDownPaymentException {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.cancelFinancing(null));
        assertEquals("Invalid financing ID.", ex.getMessage());
    }

    @Test
    void cancelFinancingMustThrowIllegalArgumentExceptionWhenFinancingNotFound() throws SQLException {
        Mockito.when(repository.findById(1)).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.cancelFinancing(1));
        assertEquals("Financing not found.", ex.getMessage());
    }

    @Test
    void cancelFinancingMustThrowIllegalStateExceptionWhenUserIsNotAuthorized() throws SQLException {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.APPROVED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 2
        );
        financing.setFinancingId(2);
        Mockito.when(repository.findById(2)).thenReturn(financing);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.cancelFinancing(2));
        assertEquals("User is not authorized to edit this financing.", ex.getMessage());
    }

    @Test
    void cancelFinancingMustThrowIllegalStateExceptionWhenFinancingStatusIsNotApproved() throws SQLException {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.REQUESTED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );
        financing.setFinancingId(1);
        Mockito.when(repository.findById(1)).thenReturn(financing);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.cancelFinancing(1));
        assertEquals("Only approved financings can be canceled.", ex.getMessage());

    }

    @Test
    void cancelFinancingMustUpdateFinancingStatusToCanceled() throws SQLException {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.APPROVED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );
        financing.setFinancingId(1);
        Mockito.when(repository.findById(1)).thenReturn(financing);

        service.cancelFinancing(1);


        assertEquals(FinancingStatus.CANCELED, financing.getStatus());
        Mockito.verify(repository).updateFinancingStatus(financing);
    }

    @Test
    void updateFinancingMustThrowIllegalStateExceptionWhenUserIsNotLoggedIn() {
        Session.logout();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFinancing(
                1, new BigDecimal("10000"), new BigDecimal("100000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE, PropertyType.HOUSE,
                3, 2, null, null, null, null, "Residential"
        ));
        assertEquals("User is not authenticated.", ex.getMessage());
    }

    @Test
    void updateFinancingMustThrowIllegalArgumentExceptionWhenFinancingIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateFinancing(
                null, new BigDecimal("10000"), new BigDecimal("100000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE, PropertyType.HOUSE,
                3, 2, null, null, null, null, "Residential"
        ));
        assertEquals("Invalid financing ID.", ex.getMessage());
    }

    @Test
    void updateFinancingMustThrowIllegalArgumentExceptionWhenFinancingNotFound() throws SQLException {
        Mockito.when(repository.findById(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateFinancing(
                1, new BigDecimal("10000"), new BigDecimal("100000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE, PropertyType.HOUSE,
                3, 2, null, null, null, null, "Residential"
        ));
        assertEquals("Financing not found.", ex.getMessage());
    }

    @Test
    void updateFinancingMustThrowIllegalStateExceptionWhenUserIsNotAuthorized() throws SQLException {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.APPROVED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 2
        );
        financing.setFinancingId(2);
        Mockito.when(repository.findById(2)).thenReturn(financing);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFinancing(
                2, new BigDecimal("10000"), new BigDecimal("100000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE, PropertyType.HOUSE,
                3, 2, null, null, null, null, "Residential"
        ));
        assertEquals("User is not authorized to edit this financing.", ex.getMessage());
    }

    @Test
    void updateFinancingMustThrowIllegalStateExceptionWhenFinancingStatusIsCanceled() throws SQLException {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.CANCELED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );
        financing.setFinancingId(1);
        Mockito.when(repository.findById(1)).thenReturn(financing);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFinancing(
                1, new BigDecimal("10000"), new BigDecimal("100000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE, PropertyType.HOUSE,
                3, 2, null, null, null, null, "Residential"
        ));
        assertEquals("Canceled financings cannot be edited.", ex.getMessage());
    }

    @Test
    void updateFinancingMustCallRepositoryUpdateAndResetCurrentFinancing() throws SQLException, InvalidDownPaymentException {
        RealEstateFinancing existingFinancing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.APPROVED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );
        existingFinancing.setFinancingId(5);

        Mockito.when(repository.findById(5)).thenReturn(existingFinancing);

        service.updateFinancing(5, new BigDecimal("10000"), new BigDecimal("100000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE, PropertyType.HOUSE,
                3, 2, null, null, null, null, "Residential");

        Mockito.verify(repository).updateFinancing(Mockito.any(RealEstateFinancing.class));
        assertNull(service.getCurrentFinancing());
    }

    @Test
    void updateFinancingMustThrowExceptionWhenNewDataIsInvalid() throws SQLException {
        RealEstateFinancing existingFinancing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.APPROVED, 3, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );
        existingFinancing.setFinancingId(5);

        Mockito.when(repository.findById(5)).thenReturn(existingFinancing);

        assertThrows(IllegalArgumentException.class, () -> service.updateFinancing(
                5, new BigDecimal("10000"), new BigDecimal("-100000"), 360,
                PropertyCondition.NEW, AmortizationType.PRICE, PropertyType.HOUSE,
                3, 2, null, null, null, null, "Residential"
        ));
    }

    @Test
    void validatePropertyTypeDataMustThrowExceptionWhenPropertyTypeIsHouseAndSpecificAttributesAreNotValid() {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.REQUESTED, null, null, BigDecimal.valueOf(450.0),
                2, true, BigDecimal.valueOf(1200), "Residential", 1
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validatePropertyTypeData(financing));
        assertEquals("Invalid data for property type HOUSE.", ex.getMessage());
    }

    @Test
    void validatePropertyTypeDataMustThrowExceptionWhenPropertyTypeIsApartmentAndSpecificAttributesAreNotValid() {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.APARTMENT, FinancingStatus.REQUESTED, null, null, BigDecimal.valueOf(450.0),
                2, true, BigDecimal.valueOf(1200), "Residential", 1
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validatePropertyTypeData(financing));
        assertEquals("Invalid data for property type APARTMENT.", ex.getMessage());
    }

    @Test
    void validatePropertyTypeDataMustThrowExceptionWhenPropertyTypeIsLandAndSpecificAttributesAreNotValid() {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.LAND, FinancingStatus.REQUESTED, 2, 3, BigDecimal.valueOf(450.0),
                2, true, BigDecimal.valueOf(1200), "Residential", 1
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validatePropertyTypeData(financing));
        assertEquals("Invalid data for property type LAND.", ex.getMessage());
    }

    @Test
    void validatePropertyTypeDataMustNotThrowExceptionWhenPropertyTypeIsHouseAndSpecificAttributesAreValid() {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.REQUESTED, null, 2, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );

        assertDoesNotThrow(() -> service.validatePropertyTypeData(financing));
    }

    @Test
    void validatePropertyTypeDataMustNotThrowExceptionWhenPropertyTypeIsApartmentAndSpecificAttributesAreValid() {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.APARTMENT, FinancingStatus.REQUESTED, 2, 3, null,
                2, true, BigDecimal.valueOf(1200), "Residential", 1
        );

        assertDoesNotThrow(() -> service.validatePropertyTypeData(financing));
    }

    @Test
    void validatePropertyTypeDataMustNotThrowExceptionWhenPropertyTypeIsLandAndSpecificAttributesAreValid() {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.LAND, FinancingStatus.REQUESTED, null, null, BigDecimal.valueOf(450.0),
                null, null, null, "Residential", 1
        );

        assertDoesNotThrow(() -> service.validatePropertyTypeData(financing));
    }

    @Test
    void normalizePropertyTypeDataMustSetNullForSpecificInvalidAttributesIfPropertyTypeIsHouse() {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.HOUSE, FinancingStatus.REQUESTED, 2, 3, BigDecimal.valueOf(450.0),
                2, true, BigDecimal.valueOf(1200), "Residential", 1
        );

        service.normalizePropertyTypeData(financing);

        assertNull(financing.getFloor());
        assertNull(financing.hasElevator());
        assertNull(financing.getCondominiumFee());
        assertEquals(3, financing.getParkingSpaces());
        assertEquals(2, financing.getBedrooms());
        assertEquals(BigDecimal.valueOf(450.0), financing.getLandArea());
    }
    @Test
    void normalizePropertyTypeDataMustSetNullForSpecificInvalidAttributesIfPropertyTypeIsApartment() {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.APARTMENT, FinancingStatus.REQUESTED, 2, 3, BigDecimal.valueOf(450.0),
                2, true, BigDecimal.valueOf(1200), "Residential", 1
        );

        service.normalizePropertyTypeData(financing);

        assertNull(financing.getLandArea());
        assertEquals(2, financing.getBedrooms());
        assertEquals(3, financing.getParkingSpaces());
        assertEquals(2, financing.getFloor());
        assertEquals(true, financing.hasElevator());
        assertEquals(BigDecimal.valueOf(1200), financing.getCondominiumFee());
}
    @Test
    void normalizePropertyTypeDataMustSetNullForSpecificInvalidAttributesIfPropertyTypeIsLand() {
        RealEstateFinancing financing = new RealEstateFinancing(
                BigDecimal.valueOf(300000), 360, BigDecimal.valueOf(10.5), AmortizationType.PRICE,
                PropertyType.LAND, FinancingStatus.REQUESTED, 2, 3, BigDecimal.valueOf(450.0),
                2, true, BigDecimal.valueOf(1200), "Residential", 1
        );

        service.normalizePropertyTypeData(financing);

        assertNull(financing.getParkingSpaces());
        assertNull(financing.getFloor());
        assertNull(financing.hasElevator());
        assertNull(financing.getCondominiumFee());
        assertNull(financing.getBedrooms());
        assertEquals(BigDecimal.valueOf(450.0), financing.getLandArea());
    }

    @Test
    void findAllFinancingsMustThrowFinancingNotFoundExceptionWhenNoFinancingsFound() throws SQLException {
        List<RealEstateFinancing> financings = new ArrayList<>();
        Mockito.when(repository.findAllByUser()).thenReturn(financings);

        FinancingNotFoundException ex = assertThrows(FinancingNotFoundException.class, () -> service.findAllFinancings());
        assertEquals("No financing records were found for this user.", ex.getMessage());
    }

}