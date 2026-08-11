package service;

import exceptions.AuthenticationFailedException;
import exceptions.CpfAlreadyRegisteredException;
import exceptions.InvalidCpfException;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import repository.AuthRepository;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class AuthServiceTest {

    private AuthRepository repository;
    private AuthService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AuthRepository.class);
        service = new AuthService(repository);
    }

    @AfterEach
    void resetSession() {
        Session.logout();
    }

    @Test
    void cpfIsValidMustReturnFalseWhenCpfIsNull() {
        String cpf = null;

        boolean result = service.cpfIsValid(cpf);

        assertFalse(result);
    }

    @Test
    void cpfIsValidMustReturnFalseWhenCpfIsNotOnlyDigits() {
        String cpf = "1234567890a";

        boolean result = service.cpfIsValid(cpf);

        assertFalse(result);
    }

    @Test
    void cpfIsValidMustReturnFalseWhenCpfLengthIsNot11() {
        String cpf = "1234567890";

        boolean result = service.cpfIsValid(cpf);

        assertFalse(result);
    }

    @Test
    void cpfIsValidMustReturnTrueWhenCpfIsValid() {
        String cpf = "12345678901";

        boolean result = service.cpfIsValid(cpf);

        assertTrue(result);
    }

    @Test
    void loginAuthenticateMustThrowInvalidCpfExceptionWhenCpfIsInvalid() {
        String invalidCpf = "1234567890";
        String testPassword = "randompassword";

        assertThrows(InvalidCpfException.class, () -> service.loginAuthenticate(invalidCpf, testPassword));
    }

    @Test
    void loginAuthenticateMustThrowAuthenticationFailedExceptionWhenUserNotFound() throws SQLException {
        String validCpf = "12345678901";
        String testPassword = "randompassword";

        Mockito.when(repository.findByCpf(validCpf)).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class, () -> service.loginAuthenticate(validCpf, testPassword));
    }

    @Test
    void loginAuthenticateMustThrowAuthenticationFailedExceptionWhenPasswordIsInvalid() throws SQLException {
        String validCpf = "12345678901";
        String plainPassword = "randomPassword123";
        String passwordHash = util.PasswordUtil.plainToHash(plainPassword);
        User user = new User(1, validCpf, passwordHash);
        String plainPasswordToNotMatch = "wrongPassword123";

        Mockito.when(repository.findByCpf(validCpf)).thenReturn(Optional.of(user));

        assertThrows(AuthenticationFailedException.class, () -> service.loginAuthenticate(validCpf, plainPasswordToNotMatch));
    }

    @Test
    void loginAuthenticateMustNotThrowExceptionWhenCpfAndPasswordAreValid() throws SQLException, InvalidCpfException, AuthenticationFailedException {
        String validCpf = "12345678901";
        String plainPassword = "randomPassword123";
        String passwordHash = util.PasswordUtil.plainToHash(plainPassword);
        User user = new User(1, validCpf, passwordHash);

        Mockito.when(repository.findByCpf(validCpf)).thenReturn(Optional.of(user));
        service.loginAuthenticate(validCpf, plainPassword);

        assertEquals(user.getUserId(), Session.getUserId());
    }

    @Test
    void checkAlreadyExistsMustThrowCpfAlreadyRegisteredExceptionWhenUserExists() throws SQLException {
        String cpfAlreadyRegistered = "12345678901";
        Mockito.when(repository.findByCpf(cpfAlreadyRegistered)).thenReturn(Optional.of(new User(1, cpfAlreadyRegistered, "hashedPassword")));

        assertThrows(CpfAlreadyRegisteredException.class, () -> service.checkAlreadyExists(cpfAlreadyRegistered));
    }

    @Test
    void checkAlreadyExistsMustNotThrowExceptionWhenUserDoesNotExist() throws SQLException {
        String cpfNotRegistered = "12345678901";
        Mockito.when(repository.findByCpf(cpfNotRegistered)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.checkAlreadyExists(cpfNotRegistered));
    }

    @Test
    void registerUserMustThrowInvalidCpfExceptionWhenCpfIsInvalid() throws SQLException {
        String invalidCpf = "1234567890";
        String testPassword = "randompassword";
        User user = new User(1, invalidCpf, null);

        assertThrows(InvalidCpfException.class, () -> service.registerUser(invalidCpf, testPassword, user));
        verify(repository, never()).saveUser(any());
        verify(repository, never()).findByCpf(any());
    }

    @Test
    void registerUserMustThrowCpfAlreadyRegisteredExceptionWhenCpfIsAlreadyRegistered() throws SQLException {
        String validCpf = "12345678901";
        String testPassword = "randompassword";
        User user = new User(1, validCpf, null);

        Mockito.when(repository.findByCpf(validCpf)).thenReturn(Optional.of(new User(1, validCpf, "hashedPassword")));

        assertThrows(CpfAlreadyRegisteredException.class, () -> service.registerUser(validCpf, testPassword, user));
        verify(repository, never()).saveUser(any());
    }

    @Test
    void registerUserMustRegisterUserSuccessfullyWhenCpfIsValidAndNotRegistered() throws SQLException, CpfAlreadyRegisteredException, InvalidCpfException {
        String validCpf = "12345678901";
        String testPassword = "randompassword";
        User user = new User(1, validCpf, null);

        Mockito.when(repository.findByCpf(validCpf)).thenReturn(Optional.empty());

        service.registerUser(validCpf, testPassword, user);

        assertNotNull(user.getPasswordHash());
        assertNotEquals(testPassword, user.getPasswordHash());
        assertTrue(util.PasswordUtil.checkPassword(testPassword, user.getPasswordHash()));

        verify(repository).saveUser(user);
    }
}