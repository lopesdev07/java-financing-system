package view;

import exceptions.InvalidVehicleDownPaymentException;
import exceptions.FinancingNotFoundException;
import model.*;
import service.VehicleFinancingService;
import util.LoopUtil;
import util.ScannerUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class VehicleFinancingView {
    private final VehicleFinancingService service;

    public VehicleFinancingView(VehicleFinancingService service) {
        this.service = service;
    }

    public void vehicleFinancingMenu(Scanner scanner) throws SQLException {
        System.out.println("This is the Vehicle Financing menu.");
        System.out.println("1. Simulate a new financing");
        System.out.println("2. Manage Saved Financings");
        System.out.println("3. Return to Main Menu");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        option = LoopUtil.getValidOption(scanner, option, 1, 3);
        switch (option) {
            case 1 -> simulationMenu(scanner);
            case 2 -> managementMenu(scanner);
            case 3 -> System.out.println("Returning to the main menu...");
            default -> throw new IllegalStateException(); // unreachable: option already validated by LoopUtil
        }
    }

    private void managementMenu(Scanner scanner) {
        System.out.println("This is the Vehicle Financing Management Menu.");
        System.out.println("Here you can view all your saved financings, edit them, or view details of a specific financing.");
        System.out.println("1. View saved financings");
        System.out.println("2. Edit saved financings");
        System.out.println("3. View details of a specific financing");
        System.out.println("4. Return to Main Menu");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        option = LoopUtil.getValidOption(scanner, option, 1, 4);
        switch (option) {
            case 1 -> viewVehicleFinancings();
            case 2 -> editVehicleFinancing(scanner);
            case 3 -> viewVehicleFinancingDetails(scanner);
            case 4 -> System.out.println("Returning to the main menu...");
            default -> throw new IllegalStateException(); // unreachable: option already validated by LoopUtil
        }
    }

    private void viewVehicleFinancings() {
        try {
            List<VehicleFinancing> financing = service.findAllFinancings();

            for (VehicleFinancing f : financing) {
                System.out.printf("""
            -- Financing ID: %d --
            Financing value: R$ %.2f
            Loan term: %d months
            Interest rate: %.2f%%
            Amortization type: %s
            Vehicle type: %s
            Status: %s
            """,
                        f.getFinancingId(),
                        f.getFinancedAmount(),
                        f.getLoanTermInMonths(),
                        f.getAnnualInterestRate(),
                        f.getAmortizationType(),
                        f.getVehicleType(),
                        f.getStatus()
                );
            }

        } catch (FinancingNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
        }
    }

    private void editVehicleFinancing(Scanner scanner) {
        try {
            System.out.println("Type the ID of the financing you want to edit and then provide the new values for the financing. If you want to keep a value unchanged, just type the same value as before.");
            System.out.println("ID: ");
            int financingID = ScannerUtil.intScanner(scanner);

            VehicleFinancing oldFin = service.findFinancingById(financingID);

            VehicleType vehicleType = chooseVehicleType(scanner);
            AmortizationType amortizationType = chooseAmortizationType(scanner);
            VehicleCondition vehicleCondition = chooseVehicleCondition(scanner);

            System.out.printf("(Current: R$ %.2f) New vehicle value: ", oldFin.getVehicleValue());
            BigDecimal vehicleValue = ScannerUtil.bigDecimalScanner(scanner);

            System.out.printf("(Current: R$ %.2f) New down payment: ", oldFin.getDownPayment());
            BigDecimal downPayment = ScannerUtil.bigDecimalScanner(scanner);

            System.out.printf("(Current: %d) New loan term in months: ", oldFin.getLoanTermInMonths());
            int loanTermInMonths = ScannerUtil.intScanner(scanner);

            System.out.printf("(Current: %s) New brand: ", oldFin.getBrand());
            String brand = ScannerUtil.stringScanner(scanner);

            System.out.printf("(Current: %s) New model: ", oldFin.getModel());
            String model = ScannerUtil.stringScanner(scanner);

            System.out.printf("(Current: %d) New manufacture year: ", oldFin.getManufactureYear());
            int manufactureYear = ScannerUtil.intScanner(scanner);

            Integer mileage = null;
            if (vehicleCondition == VehicleCondition.USED) {
                System.out.printf("(Current: %d) New mileage (km): ", oldFin.getMileage() != null ? oldFin.getMileage() : 0);
                mileage = ScannerUtil.intScanner(scanner);
            }

            service.updateFinancing(financingID, downPayment, vehicleValue, loanTermInMonths,
                    vehicleCondition, amortizationType, vehicleType, brand, model, manufactureYear, mileage);

            System.out.println("Financing successfully edited!");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
        } catch (InvalidVehicleDownPaymentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewVehicleFinancingDetails(Scanner scanner) {
        try {
            System.out.print("Type the ID of the financing you want to see details for: ");
            int financingID = ScannerUtil.intScanner(scanner);
            VehicleFinancing financing = service.findFinancingById(financingID);
            System.out.println(financing.toString());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
        }
    }

    private void simulationMenu(Scanner scanner) {
        if (createVehicleFinancing(scanner)) {
            displayVehicleFinancingSimulation(service.getCurrentFinancing());
            saveVehicleFinancing(scanner);
        }
    }

    private void saveVehicleFinancing(Scanner scanner) {
        System.out.println("Do you wish to save this simulation? Type 1 for yes or 2 for no.");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int answer = ScannerUtil.intScanner(scanner);
        answer = LoopUtil.getValidOption(scanner, answer, 1, 2);
        try {
            if (answer == 1) {
                service.saveCurrentFinancing();
                System.out.println("Simulation saved successfully!");
            } else {
                System.out.println("Simulation not saved.");
            }
        } catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private boolean createVehicleFinancing(Scanner scanner) { // creates a new vehicle financing simulation based on user input WITHOUT saving it automatically to the database
        try {
            VehicleType vehicleType = chooseVehicleType(scanner);
            AmortizationType amortizationType = chooseAmortizationType(scanner);
            VehicleCondition vehicleCondition = chooseVehicleCondition(scanner);

            System.out.print("Vehicle value: ");
            BigDecimal vehicleValue = ScannerUtil.bigDecimalScanner(scanner);

            System.out.print("Down payment: ");
            BigDecimal downPayment = ScannerUtil.bigDecimalScanner(scanner);

            System.out.print("Brand: ");
            String brand = ScannerUtil.stringScanner(scanner);

            System.out.print("Model: ");
            String model = ScannerUtil.stringScanner(scanner);

            System.out.print("Manufacture year: ");
            int manufactureYear = ScannerUtil.intScanner(scanner);

            System.out.print("Desired loan term in months: ");
            int loanTermInMonths = ScannerUtil.intScanner(scanner);

            Integer mileage = null;
            if (vehicleCondition == VehicleCondition.USED) {
                System.out.print("Mileage (km): ");
                mileage = ScannerUtil.intScanner(scanner);
            }

            service.simulateFinancing(vehicleValue, downPayment, loanTermInMonths, vehicleCondition,
                    amortizationType, vehicleType, brand, model, manufactureYear, mileage);

            System.out.println("The financing simulation was created successfully! You can now view the details and choose to save it if you wish.");
            return true;
        } catch (InvalidVehicleDownPaymentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    private AmortizationType chooseAmortizationType(Scanner scanner) {
        System.out.println("Choose the type of amortization:");
        System.out.println("1 - SAC");
        System.out.println("2 - PRICE");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        option = LoopUtil.getValidOption(scanner, option, 1, 2);
        return switch (option) {
            case 1 -> AmortizationType.SAC;
            case 2 -> AmortizationType.PRICE;
            default -> throw new IllegalStateException(); // unreachable: option already validated by LoopUtil
        };
    }

    private VehicleType chooseVehicleType(Scanner scanner) {
        System.out.println("Choose the type of vehicle:");
        System.out.println("1 - Car");
        System.out.println("2 - Motorcycle");
        System.out.println("3 - Truck");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        option = LoopUtil.getValidOption(scanner, option, 1, 3);
        return switch (option) {
            case 1 -> VehicleType.CAR;
            case 2 -> VehicleType.MOTORCYCLE;
            case 3 -> VehicleType.TRUCK;
            default -> throw new IllegalStateException(); // unreachable: option already validated by LoopUtil
        };
    }

    private VehicleCondition chooseVehicleCondition(Scanner scanner) {
        System.out.println("Please provide the actual vehicle condition:");
        System.out.println("1 - New");
        System.out.println("2 - Used");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        option = LoopUtil.getValidOption(scanner, option, 1, 2);
        return switch (option) {
            case 1 -> VehicleCondition.NEW;
            case 2 -> VehicleCondition.USED;
            default -> throw new IllegalStateException(); // unreachable: option already validated by LoopUtil
        };
    }

    private void displayVehicleFinancingSimulation(VehicleFinancing fin) {
        System.out.println("Here you can see the details of your financing simulation");
        System.out.println("Please note that this is a simulation, the financing ID will always be null, as it is not saved in the database yet.");
        System.out.println(fin.toString());
    }
}