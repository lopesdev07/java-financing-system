package service;

import java.math.BigDecimal;
import exceptions.InvalidVehicleDownPaymentException;
import exceptions.FinancingNotFoundException;
import model.*;
import repository.VehicleFinancingRepository;
import util.AmortizationCalculator;

import java.sql.SQLException;
import java.util.List;

public class VehicleFinancingService {

    private static final int MAX_LOAN_TERM_MONTHS = 60;

    private final VehicleFinancingRepository repository;

    private VehicleFinancing currentFinancing;

    public VehicleFinancingService(VehicleFinancingRepository repository) {
        this.repository = repository;
    }

    public VehicleFinancing findFinancingById(int financingId) throws SQLException {
        VehicleFinancing financing = repository.findById(financingId);

        if (financing == null) {
            throw new IllegalArgumentException("Financing not found.");
        }

        if (financing.getFinancingId() == null) {
            throw new IllegalArgumentException("Invalid financing.");
        }

        if (financing.getUserId() != Session.getUserId()) {
            throw new IllegalStateException("User is not authorized to view this financing.");
        }

        return financing;
    }

    private void validateData(BigDecimal downPayment, BigDecimal vehicleValue, int loanTermInMonths,
                              VehicleCondition vehicleCondition, AmortizationType amortizationType, VehicleType vehicleType) {

        if (downPayment.compareTo(BigDecimal.ZERO) < 0
                || vehicleValue.compareTo(BigDecimal.ZERO) <= 0
                || loanTermInMonths <= 0) {
            throw new IllegalArgumentException("Values must be positive.");
        }
        if (loanTermInMonths > MAX_LOAN_TERM_MONTHS) {
            throw new IllegalArgumentException("Vehicle financing term cannot exceed " + MAX_LOAN_TERM_MONTHS + " months.");
        }
        if (vehicleCondition == null)
            throw new IllegalArgumentException("Vehicle condition is required.");

        if (amortizationType == null)
            throw new IllegalArgumentException("Amortization type is required.");

        if (vehicleType == null)
            throw new IllegalArgumentException("Vehicle type is required.");
    }

    private void validateDownPayment(BigDecimal downPayment, BigDecimal vehicleValue)
            throws InvalidVehicleDownPaymentException {

        if (downPayment.compareTo(vehicleValue) > 0) {
            throw new InvalidVehicleDownPaymentException();
        }
    }

    public void simulateFinancing(BigDecimal vehicleValue, BigDecimal downPayment, int loanTermInMonths,
                                  VehicleCondition vehicleCondition, AmortizationType amortizationType, VehicleType vehicleType,
                                  String brand, String model, Integer manufactureYear, Integer mileage)
            throws InvalidVehicleDownPaymentException {

        if (!Session.isLoggedIn()) {
            throw new IllegalStateException("User is not authenticated.");
        }

        validateData(downPayment, vehicleValue, loanTermInMonths, vehicleCondition, amortizationType, vehicleType);
        validateDownPayment(downPayment, vehicleValue);

        BigDecimal financedAmount = vehicleValue.subtract(downPayment);

        BigDecimal interestRate = (vehicleCondition == VehicleCondition.USED)
                ? new BigDecimal("14.0")
                : new BigDecimal("9.0");

        FinancingStatus status = financedAmount.compareTo(BigDecimal.ZERO) > 0
                ? FinancingStatus.APPROVED
                : FinancingStatus.REJECTED;

        currentFinancing = new VehicleFinancing(financedAmount, loanTermInMonths, interestRate, amortizationType,
                vehicleType, vehicleCondition, status, brand, model, manufactureYear, mileage, 0);

        currentFinancing.setUserId(Session.getUserId());
        currentFinancing.setVehicleValue(vehicleValue);
        currentFinancing.setDownPayment(downPayment);

        normalizeVehicleConditionData(currentFinancing);
        validateVehicleConditionData(currentFinancing);

        AmortizationCalculator.calculateInstallments(currentFinancing);
    }

    public VehicleFinancing getCurrentFinancing() {
        return currentFinancing;
    }

    public void saveCurrentFinancing() throws SQLException {
        if (!Session.isLoggedIn()) {
            throw new IllegalStateException("User is not authenticated.");
        }
        if (currentFinancing == null) {
            throw new IllegalStateException("No simulation to save.");
        }

        repository.saveFinancing(currentFinancing);

        currentFinancing = null;
    }

    public void updateFinancing(Integer financingId, BigDecimal downPayment, BigDecimal vehicleValue, int loanTermInMonths,
                                VehicleCondition vehicleCondition, AmortizationType amortizationType, VehicleType vehicleType,
                                String brand, String model, Integer manufactureYear, Integer mileage)
            throws SQLException, InvalidVehicleDownPaymentException {

        if (!Session.isLoggedIn())
            throw new IllegalStateException("User is not authenticated.");

        if (financingId == null)
            throw new IllegalArgumentException("Invalid financing ID.");

        // old fin verifications
        VehicleFinancing existingFinancing = repository.findById(financingId);

        if (existingFinancing == null)
            throw new IllegalArgumentException("Financing not found.");

        if (existingFinancing.getUserId() != Session.getUserId())
            throw new IllegalStateException("User is not authorized to edit this financing.");

        // new fin verifications
        validateData(downPayment, vehicleValue, loanTermInMonths, vehicleCondition, amortizationType, vehicleType);
        validateDownPayment(downPayment, vehicleValue);

        // new fin obj
        simulateFinancing(vehicleValue, downPayment, loanTermInMonths, vehicleCondition,
                amortizationType, vehicleType, brand, model, manufactureYear, mileage);

        VehicleFinancing newFinancing = getCurrentFinancing();
        newFinancing.setFinancingId(financingId);

        repository.updateFinancing(newFinancing);
        currentFinancing = null;
    }

    public void validateVehicleConditionData(VehicleFinancing financing) {
        if (financing.getVehicleCondition() == VehicleCondition.USED && financing.getMileage() == null) {
            throw new IllegalArgumentException("Mileage is required for used vehicles.");
        }
    }

    public void normalizeVehicleConditionData(VehicleFinancing financing) {
        if (financing.getVehicleCondition() == VehicleCondition.NEW) {
            financing.setMileage(null);
        }
    }

    public List<VehicleFinancing> findAllFinancings() throws SQLException, FinancingNotFoundException {
        List<VehicleFinancing> financings = repository.findAllByUser();
        if (financings.isEmpty()) {
            throw new FinancingNotFoundException();
        }

        return financings;
    }
}
