package model;

import java.math.BigDecimal;

public class VehicleFinancing extends FinancingModel {

    private BigDecimal vehicleValue;
    private BigDecimal downPayment;
    private VehicleType vehicleType;
    private VehicleCondition vehicleCondition;
    private String brand;
    private String model;
    private Integer manufactureYear;
    private Integer mileage; // Only applicable when vehicleCondition == USED

    public VehicleFinancing(
            BigDecimal financedAmount,
            Integer loanTermInMonths,
            BigDecimal annualInterestRate,
            AmortizationType amortizationType,
            VehicleType vehicleType,
            VehicleCondition vehicleCondition,
            FinancingStatus status,
            String brand,
            String model,
            Integer manufactureYear,
            Integer mileage,
            int userId
    ) {
        super(financedAmount, loanTermInMonths, annualInterestRate, amortizationType, status, userId);
        this.vehicleType = vehicleType;
        this.vehicleCondition = vehicleCondition;
        this.brand = brand;
        this.model = model;
        this.manufactureYear = manufactureYear;
        this.mileage = mileage;
    }

    public VehicleFinancing(BigDecimal financedAmount, int loanTermInMonths, BigDecimal annualInterestRate, AmortizationType amortizationType, VehicleType vehicleType, VehicleCondition vehicleCondition, FinancingStatus status, Integer userId) {
        super(financedAmount, loanTermInMonths, annualInterestRate, amortizationType, status, userId);
        this.vehicleType = vehicleType;
        this.vehicleCondition = vehicleCondition;
    }

    public BigDecimal getVehicleValue() { return this.vehicleValue; }
    public BigDecimal getDownPayment() { return this.downPayment; }
    public VehicleType getVehicleType() { return this.vehicleType; }
    public VehicleCondition getVehicleCondition() { return this.vehicleCondition; }
    public String getBrand() { return this.brand; }
    public String getModel() { return this.model; }
    public Integer getManufactureYear() { return this.manufactureYear; }
    public Integer getMileage() { return this.mileage; }

    public void setVehicleValue(BigDecimal vehicleValue) { this.vehicleValue = vehicleValue; }
    public void setDownPayment(BigDecimal downPayment) { this.downPayment = downPayment; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public void setVehicleCondition(VehicleCondition vehicleCondition) { this.vehicleCondition = vehicleCondition; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setManufactureYear(Integer manufactureYear) { this.manufactureYear = manufactureYear; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("---- VEHICLE FINANCING ----\n");
        sb.append("Financing ID: ").append(getFinancingId()).append("\n");
        sb.append("Vehicle: ").append(getBrand()).append(" ").append(getModel())
                .append(" (").append(getManufactureYear()).append(")\n");
        sb.append("Vehicle Type: ").append(getVehicleType()).append("\n");
        sb.append("Condition: ").append(getVehicleCondition()).append("\n");
        sb.append("Amortization Type: ").append(getAmortizationType()).append("\n");
        sb.append("Financing Status: ").append(getStatus()).append("\n");
        sb.append("Financed Amount: R$ ").append(getFinancedAmount()).append("\n");
        sb.append("Loan Term: ").append(getLoanTermInMonths()).append(" months\n");
        sb.append("Annual Interest Rate: ").append(getAnnualInterestRate()).append("%\n");
        sb.append("Installment Amount: R$ ").append(getInstallmentAmount()).append("\n");
        sb.append("Total Amount Paid: R$ ").append(getTotalAmountPaid()).append("\n");

        if (vehicleCondition == VehicleCondition.USED) {
            sb.append("Mileage: ").append(getMileage()).append(" km\n");
        }
        return sb.toString();
    }
}