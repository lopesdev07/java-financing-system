package service;

import exceptions.FinancingNotFoundException;
import exceptions.InvalidVehicleDownPaymentException;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import repository.VehicleFinancingRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleFinancingServiceTest {

    private VehicleFinancingRepository repository;
    private VehicleFinancingService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(VehicleFinancingRepository.class);
        service = new VehicleFinancingService(repository);
        Session.login(1);
    }

    @AfterEach
    void resetSession() {
        Session.logout();
    }

    @Test
    void findFinancingByIdMustThrowIllegalArgumentExceptionWhenFinancingNotFound() throws SQLException {
        Mockito.when(repository.findById(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.findFinancingById(1));
        assertEquals("Financing not found.", ex.getMessage());
    }

    @Test
    void findFinancingByIdMustThrowIllegalArgumentExceptionWhenFinancingIdIsNull() throws SQLException {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 60, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.REQUESTED,
                "Toyota", "Corolla", 2023, null, 1
        );
        financing.setFinancingId(null);

        Mockito.when(repository.findById(1)).thenReturn(financing);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.findFinancingById(1));
        assertEquals("Invalid financing.", ex.getMessage());
    }

    @Test
    void findFinancingByIdMustThrowIllegalStateExceptionWhenUserIsNotAuthorized() throws SQLException {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 60, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.REQUESTED,
                "Toyota", "Corolla", 2023, null, 1
        );
        financing.setFinancingId(1);
        Session.login(2); // sobrescreve o login(1) do @BeforeEach de propósito

        Mockito.when(repository.findById(1)).thenReturn(financing);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.findFinancingById(1));
        assertEquals("User is not authorized to view this financing.", ex.getMessage());
    }

    @Test
    void findFinancingByIdMustReturnFinancingCorrectlyAndCalculateInstallments() throws SQLException {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 60, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.REQUESTED,
                "Toyota", "Corolla", 2023, null, 1
        );
        financing.setFinancingId(1);

        Mockito.when(repository.findById(1)).thenReturn(financing);

        VehicleFinancing result = service.findFinancingById(1);
        assertEquals(financing, result);
        assertNotNull(result.getInstallmentAmount());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenDownPaymentIsNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("50000"), new BigDecimal("-1000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Values must be positive.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenVehicleValueIsZeroOrNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("-50000"), new BigDecimal("10000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Values must be positive.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenLoanTermInMonthsIsZeroOrNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("50000"), new BigDecimal("10000"), -48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Values must be positive.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenLoanTermExceedsMaximum() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("50000"), new BigDecimal("10000"), 61,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Vehicle financing term cannot exceed 60 months.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenVehicleConditionIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("50000"), new BigDecimal("10000"), 48,
                null, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Vehicle condition is required.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenAmortizationTypeIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("50000"), new BigDecimal("10000"), 48,
                VehicleCondition.NEW, null, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Amortization type is required.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenVehicleTypeIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.simulateFinancing(
                new BigDecimal("50000"), new BigDecimal("10000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, null,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Vehicle type is required.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenDownPaymentIsGreaterThanVehicleValue() {
        InvalidVehicleDownPaymentException ex = assertThrows(InvalidVehicleDownPaymentException.class, () -> service.simulateFinancing(
                new BigDecimal("50000"), new BigDecimal("70000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("The down payment cannot be greater than the vehicle value.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustThrowExceptionWhenUserIsNotLoggedIn() {
        Session.logout();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.simulateFinancing(
                new BigDecimal("50000"), new BigDecimal("10000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("User is not authenticated.", ex.getMessage());
    }

    @Test
    void simulateFinancingMustReturnFinancingCorrectlyWithRejectedStatus() throws InvalidVehicleDownPaymentException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("100000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        );

        VehicleFinancing result = service.getCurrentFinancing();
        assertEquals(FinancingStatus.REJECTED, result.getStatus());
    }

    @Test
    void simulateFinancingMustReturnFinancingCorrectlyWithApprovedStatus() throws InvalidVehicleDownPaymentException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("10000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        );

        VehicleFinancing result = service.getCurrentFinancing();
        assertEquals(FinancingStatus.APPROVED, result.getStatus());
    }

    @Test
    void simulateFinancingMustReturnFinancingCorrectlyWithInterestRateAt14PercentForUsed() throws InvalidVehicleDownPaymentException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("10000"), 48,
                VehicleCondition.USED, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2015, 80000
        );

        VehicleFinancing result = service.getCurrentFinancing();
        assertEquals(new BigDecimal("14.0"), result.getAnnualInterestRate());
    }

    @Test
    void simulateFinancingMustReturnFinancingCorrectlyWithInterestRateAt9PercentForNew() throws InvalidVehicleDownPaymentException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("10000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        );

        VehicleFinancing result = service.getCurrentFinancing();
        assertEquals(new BigDecimal("9.0"), result.getAnnualInterestRate());
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
    void saveCurrentFinancingMustCallRepositorySaveMethod() throws InvalidVehicleDownPaymentException, SQLException {
        service.simulateFinancing(
                new BigDecimal("100000"), new BigDecimal("10000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        );
        VehicleFinancing result = service.getCurrentFinancing();

        service.saveCurrentFinancing();

        Mockito.verify(repository).saveFinancing(result);
    }

    @Test
    void updateFinancingStatusMustThrowIllegalStateExceptionWhenUserIsNotLoggedIn() {
        Session.logout();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFinancingStatus(1));
        assertEquals("User is not authenticated.", ex.getMessage());
    }

    @Test
    void updateFinancingStatusMustThrowIllegalArgumentExceptionWhenFinancingIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateFinancingStatus(null));
        assertEquals("Invalid financing ID.", ex.getMessage());
    }

    @Test
    void updateFinancingStatusMustThrowIllegalArgumentExceptionWhenFinancingNotFound() throws SQLException {
        Mockito.when(repository.findById(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateFinancingStatus(1));
        assertEquals("Financing not found.", ex.getMessage());
    }

    @Test
    void updateFinancingStatusMustThrowIllegalStateExceptionWhenUserIsNotAuthorized() throws SQLException {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.APPROVED,
                "Toyota", "Corolla", 2023, null, 2
        );
        financing.setFinancingId(2);
        Mockito.when(repository.findById(2)).thenReturn(financing);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFinancingStatus(2));
        assertEquals("User is not authorized to edit this financing.", ex.getMessage());
    }

    @Test
    void updateFinancingStatusMustThrowIllegalStateExceptionWhenFinancingStatusIsNotApproved() throws SQLException {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.REQUESTED,
                "Toyota", "Corolla", 2023, null, 1
        );
        financing.setFinancingId(1);
        Mockito.when(repository.findById(1)).thenReturn(financing);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFinancingStatus(1));
        assertEquals("Only approved financings can be canceled.", ex.getMessage());
    }

    @Test
    void updateFinancingStatusMustUpdateFinancingStatusToCanceled() throws SQLException {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.APPROVED,
                "Toyota", "Corolla", 2023, null, 1
        );
        financing.setFinancingId(1);
        Mockito.when(repository.findById(1)).thenReturn(financing);

        service.updateFinancingStatus(1);

        assertEquals(FinancingStatus.CANCELED, financing.getStatus());
        Mockito.verify(repository).updateFinancingStatus(financing);
    }

    @Test
    void updateFinancingMustThrowIllegalStateExceptionWhenUserIsNotLoggedIn() {
        Session.logout();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFinancing(
                1, new BigDecimal("10000"), new BigDecimal("100000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("User is not authenticated.", ex.getMessage());
    }

    @Test
    void updateFinancingMustThrowIllegalArgumentExceptionWhenFinancingIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateFinancing(
                null, new BigDecimal("10000"), new BigDecimal("100000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Invalid financing ID.", ex.getMessage());
    }

    @Test
    void updateFinancingMustThrowIllegalArgumentExceptionWhenFinancingNotFound() throws SQLException {
        Mockito.when(repository.findById(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateFinancing(
                1, new BigDecimal("10000"), new BigDecimal("100000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Financing not found.", ex.getMessage());
    }

    @Test
    void updateFinancingMustThrowIllegalStateExceptionWhenUserIsNotAuthorized() throws SQLException {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.APPROVED,
                "Toyota", "Corolla", 2023, null, 2
        );
        financing.setFinancingId(2);
        Mockito.when(repository.findById(2)).thenReturn(financing);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFinancing(
                2, new BigDecimal("10000"), new BigDecimal("100000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("User is not authorized to edit this financing.", ex.getMessage());
    }

    @Test
    void updateFinancingMustThrowIllegalStateExceptionWhenFinancingStatusIsCanceled() throws SQLException {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.CANCELED,
                "Toyota", "Corolla", 2023, null, 1
        );
        financing.setFinancingId(1);
        Mockito.when(repository.findById(1)).thenReturn(financing);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFinancing(
                1, new BigDecimal("10000"), new BigDecimal("100000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
        assertEquals("Canceled financings cannot be edited.", ex.getMessage());
    }

    @Test
    void updateFinancingMustCallRepositoryUpdateAndResetCurrentFinancing() throws SQLException, InvalidVehicleDownPaymentException {
        VehicleFinancing existingFinancing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.APPROVED,
                "Toyota", "Corolla", 2023, null, 1
        );
        existingFinancing.setFinancingId(5);

        Mockito.when(repository.findById(5)).thenReturn(existingFinancing);

        service.updateFinancing(5, new BigDecimal("10000"), new BigDecimal("100000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null);

        Mockito.verify(repository).updateFinancing(Mockito.any(VehicleFinancing.class));
        assertNull(service.getCurrentFinancing());
    }

    @Test
    void updateFinancingMustThrowExceptionWhenNewDataIsInvalid() throws SQLException {
        VehicleFinancing existingFinancing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.APPROVED,
                "Toyota", "Corolla", 2023, null, 1
        );
        existingFinancing.setFinancingId(5);

        Mockito.when(repository.findById(5)).thenReturn(existingFinancing);

        assertThrows(IllegalArgumentException.class, () -> service.updateFinancing(
                5, new BigDecimal("10000"), new BigDecimal("-100000"), 48,
                VehicleCondition.NEW, AmortizationType.PRICE, VehicleType.CAR,
                "Toyota", "Corolla", 2023, null
        ));
    }

    @Test
    void validateVehicleConditionDataMustThrowExceptionWhenUsedAndMileageIsNull() {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(14.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.USED, FinancingStatus.REQUESTED,
                "Toyota", "Corolla", 2018, null, 1
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validateVehicleConditionData(financing));
        assertEquals("Mileage is required for used vehicles.", ex.getMessage());
    }

    @Test
    void validateVehicleConditionDataMustNotThrowExceptionWhenUsedAndMileageIsPresent() {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(14.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.USED, FinancingStatus.REQUESTED,
                "Toyota", "Corolla", 2018, 60000, 1
        );

        assertDoesNotThrow(() -> service.validateVehicleConditionData(financing));
    }

    @Test
    void validateVehicleConditionDataMustNotThrowExceptionWhenNew() {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.REQUESTED,
                "Toyota", "Corolla", 2023, null, 1
        );

        assertDoesNotThrow(() -> service.validateVehicleConditionData(financing));
    }

    @Test
    void normalizeVehicleConditionDataMustSetMileageNullWhenNew() {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.REQUESTED,
                "Toyota", "Corolla", 2023, 15000, 1
        );

        service.normalizeVehicleConditionData(financing);

        assertNull(financing.getMileage());
    }

    @Test
    void normalizeVehicleConditionDataMustKeepMileageWhenUsed() {
        VehicleFinancing financing = new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(14.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.USED, FinancingStatus.REQUESTED,
                "Toyota", "Corolla", 2018, 60000, 1
        );

        service.normalizeVehicleConditionData(financing);

        assertEquals(60000, financing.getMileage());
    }

    @Test
    void findAllFinancingsMustThrowFinancingNotFoundExceptionWhenNoFinancingsFound() throws SQLException {
        List<VehicleFinancing> financings = new ArrayList<>();
        Mockito.when(repository.findAllByUser()).thenReturn(financings);

        FinancingNotFoundException ex = assertThrows(FinancingNotFoundException.class, () -> service.findAllFinancings());
        assertEquals("No financing records were found for this user.", ex.getMessage());
    }

    @Test
    void findAllFinancingsMustReturnListWhenFinancingsExist() throws SQLException, FinancingNotFoundException {
        List<VehicleFinancing> financings = new ArrayList<>();
        financings.add(new VehicleFinancing(
                BigDecimal.valueOf(50000), 48, BigDecimal.valueOf(9.0), AmortizationType.PRICE,
                VehicleType.CAR, VehicleCondition.NEW, FinancingStatus.APPROVED,
                "Toyota", "Corolla", 2023, null, 1
        ));
        Mockito.when(repository.findAllByUser()).thenReturn(financings);

        List<VehicleFinancing> result = service.findAllFinancings();

        assertEquals(financings, result);
    }
}