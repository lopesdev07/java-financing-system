package service;

import java.math.BigDecimal;
import exceptions.InvalidDownPaymentException;
import exceptions.FinancingNotFoundException;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.RealEstateFinancingRepository;
import util.AmortizationCalculator;

import java.sql.SQLException;
import java.util.List;

public class RealEstateFinancingService {

    private static final Logger logger = LoggerFactory.getLogger(RealEstateFinancingService.class);

    private final RealEstateFinancingRepository repository;

    private RealEstateFinancing currentFinancing;

    public RealEstateFinancingService(RealEstateFinancingRepository repository) {
        this.repository = repository;
    }

    public RealEstateFinancing findFinancingById(int financingId) throws SQLException {
        RealEstateFinancing financing = repository.findById(financingId);

        if (financing == null) {
            throw new IllegalArgumentException("Financing not found.");
        }

        if (financing.getFinancingId() == null) {
            throw new IllegalArgumentException("Invalid financing.");
        }

        if (financing.getUserId() != Session.getUserId()) {
            throw new IllegalStateException("User is not authorized to view this financing.");
        }

        AmortizationCalculator.calculateInstallments(financing); // Calculate installments for the retrieved financing

        return financing;
    }

    private void validateData(BigDecimal downPayment, BigDecimal propertyValue, int loanTermInMonths,
                              PropertyCondition propertyCondition, AmortizationType amortizationType, PropertyType propertyType) {

        if (downPayment.compareTo(BigDecimal.ZERO) < 0
                || propertyValue.compareTo(BigDecimal.ZERO) <= 0
                || loanTermInMonths <= 0) {
            throw new IllegalArgumentException("Values must be positive.");
        }
        if (propertyCondition == null)
            throw new IllegalArgumentException("Property condition is required.");

        if (amortizationType == null)
            throw new IllegalArgumentException("Amortization type is required.");

        if (propertyType == null)
            throw new IllegalArgumentException("Property type is required.");
    }

    private void validateDownPayment(BigDecimal downPayment, BigDecimal propertyValue)
            throws InvalidDownPaymentException {

        if (downPayment.compareTo(propertyValue) > 0) {
            throw new InvalidDownPaymentException();
        }
    }

    public void simulateFinancing(BigDecimal propertyValue, BigDecimal downPayment, int loanTermInMonths,
                                  PropertyCondition propertyCondition, AmortizationType amortizationType, PropertyType propertyType,
                                  Integer parkingSpaces, Integer bedrooms, BigDecimal landArea,
                                  Integer floor, Boolean elevator, BigDecimal condominiumFee, String zoning)
            throws InvalidDownPaymentException {

        if (!Session.isLoggedIn()) {
            throw new IllegalStateException("User is not authenticated.");
        }

        validateData(downPayment, propertyValue, loanTermInMonths, propertyCondition, amortizationType, propertyType);
        validateDownPayment(downPayment, propertyValue);

        BigDecimal financedAmount = propertyValue.subtract(downPayment);

        BigDecimal interestRate = (propertyCondition == PropertyCondition.SECOND_HAND)
                ? new BigDecimal("7.0")
                : new BigDecimal("5.0");

        FinancingStatus status = financedAmount.compareTo(BigDecimal.ZERO) > 0
                ? FinancingStatus.APPROVED
                : FinancingStatus.REJECTED;

        currentFinancing = new RealEstateFinancing(financedAmount, loanTermInMonths, interestRate, amortizationType, propertyType, status, 0);

        currentFinancing.setUserId(Session.getUserId());
        currentFinancing.setParkingSpaces(parkingSpaces);
        currentFinancing.setBedrooms(bedrooms);
        currentFinancing.setLandArea(landArea);
        currentFinancing.setFloor(floor);
        currentFinancing.setElevator(elevator);
        currentFinancing.setCondominiumFee(condominiumFee);
        currentFinancing.setZoning(zoning);
        currentFinancing.setPropertyValue(propertyValue);
        currentFinancing.setDownPayment(downPayment);
        currentFinancing.setPropertyCondition(propertyCondition);
        currentFinancing.setPropertyType(propertyType);

        normalizePropertyTypeData(currentFinancing);
        validatePropertyTypeData(currentFinancing);


        AmortizationCalculator.calculateInstallments(currentFinancing);

        logger.info("Real estate financing simulated for user {}: propertyType={}, status={}",
                Session.getUserId(), propertyType, status);
    }

    public RealEstateFinancing getCurrentFinancing() {
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
        logger.info("Real estate financing saved for user {}", Session.getUserId());

        currentFinancing = null;
    }

    public void cancelFinancing(Integer financingId) throws SQLException {
        if (!Session.isLoggedIn())
            throw new IllegalStateException("User is not authenticated.");

        if (financingId == null)
            throw new IllegalArgumentException("Invalid financing ID.");

        RealEstateFinancing existingFinancing = repository.findById(financingId);

        if (existingFinancing == null)
            throw new IllegalArgumentException("Financing not found.");

        if (existingFinancing.getUserId() != Session.getUserId())
            throw new IllegalStateException("User is not authorized to edit this financing.");

        if (existingFinancing.getStatus() != FinancingStatus.APPROVED)
            throw new IllegalStateException("Only approved financings can be canceled.");

        existingFinancing.setFinancingStatus(FinancingStatus.CANCELED);
        repository.updateFinancingStatus(existingFinancing);
        logger.info("Real estate financing {} canceled by user {}", financingId, Session.getUserId());
    }

    public void updateFinancing(Integer financingId, BigDecimal downPayment, BigDecimal propertyValue, int loanTermInMonths, PropertyCondition propertyCondition, AmortizationType amortizationType, PropertyType propertyType, Integer bedrooms, Integer parkingSpaces, BigDecimal landArea, Integer floor, Boolean elevator, BigDecimal condominiumFee, String zoning) throws SQLException, InvalidDownPaymentException {


        if (!Session.isLoggedIn())
            throw new IllegalStateException("User is not authenticated.");

        if (financingId == null)
            throw new IllegalArgumentException("Invalid financing ID.");

        RealEstateFinancing existingFinancing = repository.findById(financingId);

        if (existingFinancing == null)
            throw new IllegalArgumentException("Financing not found.");

        if (existingFinancing.getUserId() != Session.getUserId())
            throw new IllegalStateException("User is not authorized to edit this financing.");

        if (existingFinancing.getStatus() == FinancingStatus.CANCELED)
            throw new IllegalStateException("Canceled financings cannot be edited.");

        validateData(downPayment, propertyValue, loanTermInMonths, propertyCondition, amortizationType, propertyType);
        validateDownPayment(downPayment, propertyValue);

        simulateFinancing(propertyValue, downPayment, loanTermInMonths, propertyCondition,
                amortizationType, propertyType, parkingSpaces, bedrooms, landArea,
                floor, elevator, condominiumFee, zoning);

        RealEstateFinancing newFinancing = getCurrentFinancing();
        newFinancing.setFinancingId(financingId);

        repository.updateFinancing(newFinancing);
        currentFinancing = null;
        logger.info("Real estate financing {} updated by user {}", financingId, Session.getUserId());
    }

    public void validatePropertyTypeData(RealEstateFinancing financing) {
        if (financing.getPropertyType() == PropertyType.HOUSE) {
            if (financing.getParkingSpaces() == null || financing.getFloor() != null ||
                    financing.hasElevator() != null || financing.getCondominiumFee() != null) {
                throw new IllegalArgumentException("Invalid data for property type HOUSE.");
            }
        } else if (financing.getPropertyType() == PropertyType.APARTMENT) {
            if (financing.getLandArea() != null) {
                throw new IllegalArgumentException("Invalid data for property type APARTMENT.");
            }
        }
        else if (financing.getPropertyType() == PropertyType.LAND) {
            if (financing.getParkingSpaces() != null || financing.getFloor() != null ||
                    financing.hasElevator() != null || financing.getCondominiumFee() != null ||
                    financing.getBedrooms() != null) {
                throw new IllegalArgumentException("Invalid data for property type LAND.");
            }
        }
    }

    public void normalizePropertyTypeData(RealEstateFinancing financing) {
        if (financing.getPropertyType() == PropertyType.HOUSE) {
            financing.setFloor(null);
            financing.setElevator(null);
            financing.setCondominiumFee(null);
        } else if (financing.getPropertyType() == PropertyType.APARTMENT) {
            financing.setLandArea(null);
        }
        else if (financing.getPropertyType() == PropertyType.LAND) {
            financing.setParkingSpaces(null);
            financing.setFloor(null);
            financing.setElevator(null);
            financing.setCondominiumFee(null);
            financing.setBedrooms(null);
        }}
    public List<RealEstateFinancing> findAllFinancings() throws SQLException, FinancingNotFoundException {
        List<RealEstateFinancing> financings = repository.findAllByUser();
        if (financings.isEmpty()) {
            throw new FinancingNotFoundException();
        }

        return financings;
    }
}