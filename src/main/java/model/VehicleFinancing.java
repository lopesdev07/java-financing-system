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
        return String.format(
                "===== VEHICLE FINANCING =====%n" +
                        "Financing ID: %d%n" +
                        "Vehicle: %s %s (%d)%n" +
                        "Vehicle Type: %s%n" +
                        "Condition: %s%n" +
                        "Amortization Type: %s%n%n" +

                        "--- Financing Information ---%n" +
                        "Financing Status: %s%n" +
                        "Financed Amount: R$ %.2f%n" +
                        "Loan Term: %d months%n" +
                        "Annual Interest Rate: %.2f%%%n" +
                        "Installment Amount: R$ %.2f%n" +
                        "Total Amount Paid: R$ %.2f%n" +
                        "Mileage: %d km%n" +
                        "===================================%n",
                getFinancingId(),
                getBrand(), getModel(), (getManufactureYear() != null ? getManufactureYear() : 0),
                getVehicleType(),
                getVehicleCondition(),
                getAmortizationType(),
                getStatus(),
                (getFinancedAmount() != null ? getFinancedAmount().doubleValue() : 0.0),
                getLoanTermInMonths(),
                (getAnnualInterestRate() != null ? getAnnualInterestRate().doubleValue() : 0.0),
                (getInstallmentAmount() != null ? getInstallmentAmount().doubleValue() : 0.0),
                (getTotalAmountPaid() != null ? getTotalAmountPaid().doubleValue() : 0.0),
                (getMileage() != null ? getMileage() : 0)
        );
    }
}