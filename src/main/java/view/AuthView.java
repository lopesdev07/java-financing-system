package view;

import exceptions.AuthenticationFailedException;
import exceptions.CpfAlreadyRegisteredException;
import exceptions.InvalidCpfException;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AuthService;
import util.ScannerUtil;
import java.sql.SQLException;
import java.util.Scanner;

public class AuthView {

    private static final Logger logger = LoggerFactory.getLogger(AuthView.class);

    private final AuthService service;
    public AuthView(AuthService service) {
        this.service = service;
    }

    public void loginOrRegisterMenu(Scanner scanner) { // creates a loop that only breaks when login is true
        boolean authenticated = false;
        while (!authenticated) {
            System.out.println("--- Welcome to the Authentication System ---");
            System.out.println("Before navigating the system, please log in or register if this is your first access.");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.print("Choose an option:");
            int option = ScannerUtil.intScanner(scanner);

            switch(option) {
                case 1:
                    authenticated = loginScreen(scanner);
                    break;
                case 2:
                    registerScreen(scanner);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }}

    public boolean loginScreen(Scanner scanner) {
        try {
            System.out.println("--- Login Screen ---");
            System.out.println("Please enter your CPF and password to log in.");
            System.out.print("Enter your CPF:");
            String cpf = ScannerUtil.stringScanner(scanner);
            System.out.print("Enter your password:");
            String password = ScannerUtil.stringScanner(scanner);
            service.loginAuthenticate(cpf, password);
            return true;

        }
        catch (InvalidCpfException e) {
            logger.warn(e.getMessage());
            System.out.println(e.getMessage());
            return false;
        }
        catch (SQLException e) {
            logger.error("Database error", e);
            System.err.println("Error accessing the database. Please try again.");
            return false;
        }
        catch (AuthenticationFailedException e) {
            logger.warn(e.getMessage());
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void registerScreen(Scanner scanner) {
        boolean registered = false;
        while (!registered) {
            try {
                System.out.println("--- Registration Screen ---");
                System.out.println("Please enter your CPF and password to register.");
                System.out.print("Enter your CPF:");
                String cpf = ScannerUtil.stringScanner(scanner);
                System.out.print("Enter your password:");
                String password = ScannerUtil.stringScanner(scanner);
                int userID = 0;

                User newUser = new User(userID, cpf, password);

                service.registerUser(cpf, password, newUser);
                System.out.println("Registration successful! You can now log in.");
                registered = true;
            } catch (CpfAlreadyRegisteredException e) {
                logger.warn(e.getMessage());
                System.out.println(e.getMessage());
            } catch (InvalidCpfException e) {
                logger.warn(e.getMessage());
                System.out.println(e.getMessage());
            } catch (SQLException e) {
                logger.error("Database error", e);
                System.err.println("Error accessing the database. Please try again.");
            }
        }}
}