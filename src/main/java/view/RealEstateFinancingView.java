package view;

import exceptions.InvalidDownPaymentException;
import exceptions.FinancingNotFoundException;
import model.*;
import service.RealEstateFinancingService;
import util.ScannerUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class RealEstateFinancingView {
    private final RealEstateFinancingService service;

    public RealEstateFinancingView(RealEstateFinancingService service) {
        this.service = service;
    }

    public void realEstateFinancingMenu(Scanner scanner) throws SQLException {
        System.out.println("This is the Real Estate Financing menu.");
        System.out.println("1. Simulate a new financing");
        System.out.println("2. Manage Saved Financings");
        System.out.println("3. Return to Main Menu");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        switch (option) {
            case 1 -> simulationMenu(scanner);
            case 2 -> managementMenu(scanner);
            case 3 -> System.out.println("Returning to the main menu...");
            default -> System.out.println("Invalid option. Please try again.");
        }
    }

    private void managementMenu(Scanner scanner) {
        System.out.println("This is the Real Estate Financing Management Menu.");
        System.out.println("Here you can view all your saved financings, edit them, or view details of a specific financing.");
        System.out.println("1. View saved financings");
        System.out.println("2. Edit saved financings");
        System.out.println("3. View details of a specific financing");
        System.out.println("4. Return to Main Menu");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        switch (option) {
            case 1 -> viewRealEstateFinancings();
            case 2 -> editRealEstateFinancing(scanner);
            case 3 -> viewRealEstateFinancingDetails(scanner);
            case 4 -> System.out.println("Returning to the main menu...");
            default -> System.out.println("Invalid option. Please try again.");
        }
    }

    private void viewRealEstateFinancings() {
        try {
            List<RealEstateFinancing> financing = service.findAllFinancings();

            for (RealEstateFinancing f : financing) {
                System.out.printf("""
            -- Financing ID: %d --
            Financing value: R$ %.2f
            Loan term: %d months
            Interest rate: %.2f%%
            Amortization type: %s
            Property type: %s
            Status: %s
            """,
                        f.getFinancingId(),
                        f.getFinancedAmount(),
                        f.getLoanTermInMonths(),
                        f.getAnnualInterestRate(),
                        f.getAmortizationType(),
                        f.getPropertyType(),
                        f.getStatus()
                );
            }

        } catch (FinancingNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
        }
    }

    private void editRealEstateFinancing(Scanner scanner) {
        try {
            System.out.println("Type the ID of the financing you want to edit and then provide the new values for the financing. If you want to keep a value unchanged, just type the same value as before.");
            System.out.println("ID: ");
            int financingID = ScannerUtil.intScanner(scanner);

            RealEstateFinancing oldFin = service.findFinancingById(financingID);

            PropertyType propertyType = choosePropertyType(scanner);
            AmortizationType amortizationType = chooseAmortizationType(scanner);
            PropertyCondition propertyCondition = definePropertyCondition(scanner);

            System.out.print("New property value: ");
            BigDecimal propertyValue = ScannerUtil.bigDecimalScanner(scanner);

            System.out.print("New down payment: ");
            BigDecimal downPayment = ScannerUtil.bigDecimalScanner(scanner);

            System.out.printf("(Current: %d) New loan term in months: ", oldFin.getLoanTermInMonths());
            int loanTermInMonths = ScannerUtil.intScanner(scanner);

            System.out.printf("(Current: %s) New zoning: ", oldFin.getZoning());
            String zoning = ScannerUtil.stringScanner(scanner);

            Integer rooms = null;
            Integer parkingSpaces = null;
            BigDecimal landArea = null;
            Integer floor = null;
            Boolean elevator = null;
            BigDecimal condominiumValue = null;

            if (propertyType == PropertyType.HOUSE) {
                System.out.printf("(Current: %d) Rooms: ", oldFin.getBedrooms());
                rooms = ScannerUtil.intScanner(scanner);
                System.out.printf("(Current: %d) Parking spaces: ", oldFin.getParkingSpaces());
                parkingSpaces = ScannerUtil.intScanner(scanner);
                System.out.printf("(Current: %.2f) Land area: ", oldFin.getLandArea());
                landArea = ScannerUtil.bigDecimalScanner(scanner);
            }

            if (propertyType == PropertyType.APARTMENT) {
                System.out.printf("(Current: %d) Floor: ", oldFin.getFloor());
                floor = ScannerUtil.intScanner(scanner);
                System.out.printf("(Current: %s) Has elevator? (1 for Yes, 2 for No): ", oldFin.hasElevator() != null && oldFin.hasElevator() ? "Yes" : "No");
                int hasElevator = ScannerUtil.intScanner(scanner);
                elevator = hasElevator == 1;
                System.out.printf("(Current: R$ %.2f) Condominium value: ", oldFin.getCondominiumFee());
                condominiumValue = ScannerUtil.bigDecimalScanner(scanner);
            }

            if (propertyType == PropertyType.LAND) {
                System.out.printf("(Current: %.2f) Land area: ", oldFin.getLandArea());
                landArea = ScannerUtil.bigDecimalScanner(scanner);
            }

            service.updateFinancing(financingID, downPayment, propertyValue, loanTermInMonths,
                    propertyCondition, amortizationType, propertyType, rooms, parkingSpaces,
                    landArea, floor, elevator, condominiumValue, zoning);

            System.out.println("Financing successfully edited!");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
        } catch (InvalidDownPaymentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewRealEstateFinancingDetails(Scanner scanner) {
        try {
            System.out.print("Type the ID of the financing you want to see details for: ");
            int financingID = ScannerUtil.intScanner(scanner);
            RealEstateFinancing financing = service.findFinancingById(financingID);
            System.out.println("===== FINANCING DETAILS =====");
            System.out.printf("Financing ID: %d%n", financing.getFinancingId());
            System.out.printf("Financed value: R$ %.2f%n", financing.getFinancedAmount());
            System.out.printf("Loan term in months: %d months%n", financing.getLoanTermInMonths());
            System.out.printf("Annual interest rate: %.2f%%%n", financing.getAnnualInterestRate());
            System.out.printf("Installment amount: R$ %.2f%n", financing.getInstallmentAmount()); // temporary solution
            System.out.printf("Total amount paid: R$ %.2f%n", financing.getTotalAmountPaid()); // temporary solution
            System.out.printf("Amortization type: %s%n", financing.getAmortizationType());
            System.out.printf("Property type: %s%n", financing.getPropertyType());
            System.out.printf("Financing status: %s%n", financing.getStatus());

            if (financing.getPropertyType() == PropertyType.HOUSE) {
                System.out.printf("Rooms: %d%n", financing.getBedrooms());
                System.out.printf("Parking spaces: %d%n", financing.getParkingSpaces());
                System.out.printf("Land area: %.2f m²%n", financing.getLandArea());
            } else if (financing.getPropertyType() == PropertyType.APARTMENT) {
                System.out.printf("Floor: %d%n", financing.getFloor());
                System.out.printf("Elevator: %s%n", (financing.hasElevator() ? "Yes" : "No"));
                System.out.printf("Condominium value: R$ %.2f%n", financing.getCondominiumFee());
            } else if (financing.getPropertyType() == PropertyType.LAND) {
                System.out.printf("Land area (m²): %.2f m²%n", financing.getLandArea());
            }}
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
        catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
        }}



    private void simulationMenu(Scanner scanner) {
        createRealEstateFinancing(scanner);
        displayRealEstateFinancingSimulation(service.getCurrentFinancing());
        saveRealEstateFinancing(scanner);
    }

    private void saveRealEstateFinancing(Scanner scanner) {
        System.out.println("Do you wish to save this simulation? Type 1 for yes or 2 for no.");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int answer = ScannerUtil.intScanner(scanner);
        try {
            if (answer == 1) {
                service.saveCurrentFinancing();
                System.out.println("Simulation saved successfully!");
            } else if (answer == 2) {
                System.out.println("Simulation not saved.");
            } else {
                System.out.println("Invalid option. Type 1 to save or 2 to not save..");
            }
        } catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void createRealEstateFinancing(Scanner scanner) { // creates a new real estate financing simulation based on user input WITHOUT saving it automatically to the database
        try {
            Integer rooms = null;
            Integer parkingSpaces = null;
            BigDecimal landArea = null;
            Integer floor = null;
            Boolean elevator = null;
            BigDecimal condominiumValue = null;

            PropertyType propertyType = choosePropertyType(scanner);
            AmortizationType amortizationType = chooseAmortizationType(scanner);
            PropertyCondition propertyCondition = definePropertyCondition(scanner);

            System.out.print("Property value: ");
            BigDecimal propertyValue = ScannerUtil.bigDecimalScanner(scanner);

            System.out.print("Down payment: ");
            BigDecimal downPayment = ScannerUtil.bigDecimalScanner(scanner);

            System.out.print("Zoning: ");
            String zoning = ScannerUtil.stringScanner(scanner);

            System.out.print("Desired loan term in months: ");
            int loanTermInMonths = ScannerUtil.intScanner(scanner);

            if (propertyType == PropertyType.HOUSE) {
                System.out.print("Number of rooms: ");
                rooms = ScannerUtil.intScanner(scanner);
                System.out.print("Parking spaces: ");
                parkingSpaces = ScannerUtil.intScanner(scanner);
                System.out.print("Total land area: ");
                landArea = ScannerUtil.bigDecimalScanner(scanner);
            }

            if (propertyType == PropertyType.APARTMENT) {
                System.out.print("Apartment floor: ");
                floor = ScannerUtil.intScanner(scanner);
                System.out.print("Has elevator? (1 for Yes, 2 for No): ");
                int hasElevator = ScannerUtil.intScanner(scanner);
                if (hasElevator == 1) {
                    elevator = true;
                } else if (hasElevator == 2) {
                    elevator = false;
                } else {
                    System.out.println("Invalid option. Considering the default value (No elevator).");
                    elevator = false;
                }
                System.out.print("Condominium value: ");
                condominiumValue = ScannerUtil.bigDecimalScanner(scanner);
            }

            if (propertyType == PropertyType.LAND) {
                System.out.print("Land area: ");
                landArea = ScannerUtil.bigDecimalScanner(scanner);
            }

            service.simulateFinancing(propertyValue, downPayment, loanTermInMonths, propertyCondition,
                    amortizationType, propertyType, parkingSpaces, rooms, landArea,
                    floor, elevator, condominiumValue, zoning);

            System.out.println("The financing simulation was created successfully! You can now view the details and choose to save it if you wish.");

        } catch (InvalidDownPaymentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private AmortizationType chooseAmortizationType(Scanner scanner) {
        System.out.println("Choose the type of amortization:");
        System.out.println("1 - SAC");
        System.out.println("2 - PRICE");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        return switch (option) {
            case 1 -> AmortizationType.SAC;
            case 2 -> AmortizationType.PRICE;
            default -> throw new IllegalArgumentException("Invalid option");
        };
    }

    private PropertyType choosePropertyType(Scanner scanner) {
        System.out.println("Choose the type of property:");
        System.out.println("1 - House");
        System.out.println("2 - Apartment");
        System.out.println("3 - Land");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        return switch (option) {
            case 1 -> PropertyType.HOUSE;
            case 2 -> PropertyType.APARTMENT;
            case 3 -> PropertyType.LAND;
            default -> throw new IllegalArgumentException("Invalid option");
        };
    }

    private PropertyCondition definePropertyCondition(Scanner scanner) {
        System.out.println("Please provide the actual property condition:");
        System.out.println("1 - New");
        System.out.println("2 - Second-hand");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        return switch (option) {
            case 1 -> PropertyCondition.NEW;
            case 2 -> PropertyCondition.SECOND_HAND;
            default -> throw new IllegalArgumentException("Invalid option. Please choose 1 for New or 2 for Second-hand.");
        };
    }

    private void displayRealEstateFinancingSimulation(RealEstateFinancing fin) {
        System.out.println("Here you can see the details of your financing simulation");
        System.out.println("Please note that if this is a simulation, the financing ID will always be null, as it is not saved in the database yet.");
        System.out.println(fin.toString());
    }
}
