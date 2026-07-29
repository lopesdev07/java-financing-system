package model;

import java.math.BigDecimal;

public class RealEstateFinancing extends FinancingModel {

    private BigDecimal propertyValue;
    private BigDecimal downPayment;
    private PropertyType propertyType;
    private Integer parkingSpaces; // House and apartment
    private Integer bedrooms;
    private BigDecimal landArea;
    private Integer floor;
    private Boolean elevator;
    private BigDecimal condominiumFee;
    private String zoning;
    private PropertyCondition propertyCondition;

    public RealEstateFinancing(
            BigDecimal financedAmount,
            Integer loanTermInMonths,
            BigDecimal annualInterestRate,
            AmortizationType amortizationType,
            PropertyType propertyType,
            FinancingStatus status,
            Integer bedrooms,
            Integer parkingSpaces,
            BigDecimal landArea,
            Integer floor,
            Boolean elevator,
            BigDecimal condominiumFee,
            String zoning,
            int userId
    ) {
        super(financedAmount, loanTermInMonths, annualInterestRate, amortizationType, status, userId);
        this.propertyType = propertyType;
        this.bedrooms = bedrooms;
        this.parkingSpaces = parkingSpaces;
        this.landArea = landArea;
        this.floor = floor;
        this.elevator = elevator;
        this.condominiumFee = condominiumFee;
        this.zoning = zoning;
    }

    public RealEstateFinancing(BigDecimal financedAmount, int loanTermInMonths, BigDecimal annualInterestRate, AmortizationType amortizationType, PropertyType propertyType, FinancingStatus status, Integer userId) {
        super(financedAmount, loanTermInMonths, annualInterestRate, amortizationType, status, userId);
        this.propertyType = propertyType;
    }

    public BigDecimal getPropertyValue() {
        return this.propertyValue;
    }

    public BigDecimal getDownPayment() {
        return this.downPayment;
    }

    public PropertyType getPropertyType() {
        return this.propertyType;
    }

    public Integer getParkingSpaces() {
        return this.parkingSpaces;
    }

    public Integer getBedrooms() {
        return this.bedrooms;
    }

    public BigDecimal getLandArea() {
        return this.landArea;
    }

    public Integer getFloor() {
        return this.floor;
    }

    public Boolean hasElevator() {
        return this.elevator;
    }

    public BigDecimal getCondominiumFee() {
        return this.condominiumFee;
    }

    public String getZoning() {
        return this.zoning;
    }

    public PropertyCondition getPropertyCondition() {
        return this.propertyCondition;
    }

    public void setPropertyValue(BigDecimal propertyValue) {
        this.propertyValue = propertyValue;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public void setPropertyCondition(PropertyCondition propertyCondition) {
        this.propertyCondition = propertyCondition;
    }

    public void setParkingSpaces(Integer parkingSpaces) {
        this.parkingSpaces = parkingSpaces;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public void setLandArea(BigDecimal landArea) {
        this.landArea = landArea;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public void setElevator(Boolean elevator) {
        this.elevator = elevator;
    }

    public void setCondominiumFee(BigDecimal condominiumFee) {
        this.condominiumFee = condominiumFee;
    }

    public void setZoning(String zoning) {
        this.zoning = zoning;
    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("---- REAL ESTATE FINANCING ----\n");
        sb.append("Financing ID: ").append(getFinancingId()).append("\n");
        sb.append("Property Type: ").append(getPropertyType()).append("\n");
        sb.append("Amortization Type: ").append(getAmortizationType()).append("\n");
        sb.append("Financing Status: ").append(getStatus()).append("\n");
        sb.append("Financed Amount: R$ ").append(getFinancedAmount()).append("\n");
        sb.append("Loan Term: ").append(getLoanTermInMonths()).append(" months\n");
        sb.append("Annual Interest Rate: ").append(getAnnualInterestRate()).append("%\n");
        sb.append("Installment Amount: R$ " ).append(String.format("%.2f", getInstallmentAmount())).append("\n");
        sb.append("Total Amount Paid: R$ ").append(String.format("%.2f", getTotalAmountPaid())).append("\n");
        sb.append("Zoning: ").append(getZoning()).append("\n");
        if (propertyType == PropertyType.HOUSE) {
            sb.append("Rooms: ").append(getBedrooms()).append("\n");
            sb.append("Parking Spaces: ").append(getParkingSpaces()).append("\n");
            sb.append("Land Area: ").append(getLandArea()).append(" m²\n");
        }
        else if (propertyType == PropertyType.APARTMENT) {
            sb.append("Floor: ").append(getFloor()).append("\n");
            sb.append("Elevator: ").append(hasElevator() ? "Yes" : "No").append("\n");
            sb.append("Condominium Fee: R$ ").append(getCondominiumFee()).append("\n");
        }
        else if (propertyType == PropertyType.LAND) {
            sb.append("Land Area: ").append(getLandArea()).append(" m²\n");
        }

        return sb.toString();
    }

}