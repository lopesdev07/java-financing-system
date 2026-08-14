package view;

import exceptions.InvalidDownPaymentException;
import exceptions.FinancingNotFoundException;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.RealEstateFinancingService;
import util.LoopUtil;
import util.ScannerUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class RealEstateFinancingView {

    private static final Logger logger = LoggerFactory.getLogger(RealEstateFinancingView.class);

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
        option = LoopUtil.getValidOption(scanner, option, 1, 3);
        switch (option) {
            case 1 -> simulationMenu(scanner);
            case 2 -> managementMenu(scanner);
            case 3 -> System.out.println("Returning to the main menu...");
            default -> throw new IllegalStateException(); // unreachable: option already validated by LoopUtil
        }
    }

    private void managementMenu(Scanner scanner) {
        System.out.println("This is the Real Estate Financing Management Menu.");
        System.out.println("Here you can view all your saved financings, edit them, or view details of a specific financing.");
        System.out.println("1. View saved financings");
        System.out.println("2. Edit saved financings");
        System.out.println("3. View details of a specific financing");
        System.out.println("4. Cancel a specific financing");
        System.out.println("5. Return to Main Menu");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        option = LoopUtil.getValidOption(scanner, option, 1, 5);
        switch (option) {
            case 1 -> viewRealEstateFinancings();
            case 2 -> editRealEstateFinancing(scanner);
            case 3 -> viewRealEstateFinancingDetails(scanner);
            case 4 -> cancelRealEstateFinancing(scanner);
            case 5 -> System.out.println("Returning to the main menu...");
            default -> throw new IllegalStateException(); // unreachable: option already validated by LoopUtil
        }
    }

    private void cancelRealEstateFinancing(Scanner scanner) {
        try {
            System.out.print("Type the ID of the financing you want to cancel: ");
            int financingID = ScannerUtil.intScanner(scanner);
            service.cancelFinancing(financingID);
            System.out.println("Financing canceled successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            logger.warn(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
            logger.error("Database error", e);
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
            logger.error("Database error", e);
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
                hasElevator = LoopUtil.getValidOption(scanner, hasElevator, 1, 2);
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

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            logger.warn(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
            logger.error("Database error", e);
        } catch (InvalidDownPaymentException e) {
            System.out.println("Error: " + e.getMessage());
            logger.warn(e.getMessage());
        }
    }

    private void viewRealEstateFinancingDetails(Scanner scanner) {
        try {
            System.out.print("Type the ID of the financing you want to see details for: ");
            int financingID = ScannerUtil.intScanner(scanner);
            RealEstateFinancing financing = service.findFinancingById(financingID);
            System.out.println(financing.toString());}
        catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        } 
        catch (SQLException e) {
            System.out.println("Error: an error occurred with the database. Please try again.");
            logger.error("Database error", e);
        }}

    private void simulationMenu(Scanner scanner) {
        if (createRealEstateFinancing(scanner)) {
            displayRealEstateFinancingSimulation(service.getCurrentFinancing());
            saveRealEstateFinancing(scanner);}
    }

    private void saveRealEstateFinancing(Scanner scanner) {
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
            logger.error("Database error", e);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            logger.warn(e.getMessage());
        }
    }

    private boolean createRealEstateFinancing(Scanner scanner) { // creates a new real estate financing simulation based on user input WITHOUT saving it automatically to the database
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

            System.out.print("Desired loan term in months: ");
            int loanTermInMonths = ScannerUtil.intScanner(scanner);

            System.out.print("Zoning: ");
            String zoning = ScannerUtil.stringScanner(scanner);

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
                hasElevator = LoopUtil.getValidOption(scanner, hasElevator, 1, 2);
                elevator = hasElevator == 1;
                System.out.print("Condominium fee: ");
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

            return true;
        } catch (InvalidDownPaymentException e) {
            System.out.println("Error: " + e.getMessage());
            logger.warn(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            logger.warn(e.getMessage());
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

    private PropertyType choosePropertyType(Scanner scanner) {
        System.out.println("Choose the type of property:");
        System.out.println("1 - House");
        System.out.println("2 - Apartment");
        System.out.println("3 - Land");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        option = LoopUtil.getValidOption(scanner, option, 1, 3);
        return switch (option) {
            case 1 -> PropertyType.HOUSE;
            case 2 -> PropertyType.APARTMENT;
            case 3 -> PropertyType.LAND;
            default -> throw new IllegalStateException(); // unreachable: option already validated by LoopUtil
        };
    }

    private PropertyCondition definePropertyCondition(Scanner scanner) {
        System.out.println("Please provide the actual property condition:");
        System.out.println("1 - New");
        System.out.println("2 - Second-hand");
        System.out.print("Type the number corresponding to the action you want to perform:");
        int option = ScannerUtil.intScanner(scanner);
        option = LoopUtil.getValidOption(scanner, option, 1, 2);
        return switch (option) {
            case 1 -> PropertyCondition.NEW;
            case 2 -> PropertyCondition.SECOND_HAND;
            default -> throw new IllegalStateException(); // unreachable: option already validated by LoopUtil
        };
    }

    private void displayRealEstateFinancingSimulation(RealEstateFinancing fin) {
        System.out.println("Here you can see the details of your financing simulation");
        System.out.println("Please note that if this is a simulation, the financing ID will always be null, as it is not saved in the database yet.");
        System.out.println(fin.toString());
    }
}