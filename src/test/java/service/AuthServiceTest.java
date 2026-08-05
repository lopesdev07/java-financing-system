package service;

import exceptions.AuthenticationFailedException;
import exceptions.CpfAlreadyRegisteredException;
import exceptions.InvalidCpfException;
import model.User;
import org.junit.jupiter.api.AfterEach;
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
    @AfterEach
    void resetSession() {
        Session.logout();
    }

    @Test
    void cpfIsValidMustReturnFalseWhenCpfIsNull() {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String cpf = null;

        boolean result = authService.cpfIsValid(cpf);

        assertFalse(result);
    }

    @Test
    void cpfIsValidMustReturnFalseWhenCpfIsNotOnlyDigits() {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String cpf = "1234567890a";

        boolean result = authService.cpfIsValid(cpf);

        assertFalse(result);
    }

    @Test
    void cpfIsValidMustReturnFalseWhenCpfLengthIsNot11() {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String cpf = "1234567890";

        boolean result = authService.cpfIsValid(cpf);

        assertFalse(result);
    }

    @Test
    void cpfIsValidMustReturnTrueWhenCpfIsValid() {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String cpf = "12345678901";

        boolean result = authService.cpfIsValid(cpf);

        assertTrue(result);
    }

    @Test
    void loginAuthenticateMustThrowInvalidCpfExceptionWhenCpfIsInvalid() {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String invalidCpf = "1234567890"; // Invalid CPF (10)
        String testPassword = "randompassword";




        assertThrows(InvalidCpfException.class, () -> authService.loginAuthenticate(invalidCpf, testPassword));

    }

    @Test
    void loginAuthenticateMustThrowAuthenticationFailedExceptionWhenUserNotFound() throws SQLException {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String validCpf = "12345678901"; // Valid CPF
        String testPassword = "randompassword";

        Mockito.when(authRepository.findByCpf(validCpf)).thenReturn(java.util.Optional.empty());

        assertThrows(exceptions.AuthenticationFailedException.class, () -> authService.loginAuthenticate(validCpf, testPassword));

    }

    @Test
    void loginAuthenticateMustThrowAuthenticationFailedExceptionWhenPasswordIsInvalid() throws SQLException {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String validCpf = "12345678901"; // Valid CPF
        String plainPassword = "randomPassword123";
        String passwordHash = util.PasswordUtil.plainToHash(plainPassword);
        User user = new User(1, validCpf, passwordHash);
        String plainPasswordToNotMatch = "wrongPassword123";


        Mockito.when(authRepository.findByCpf("12345678901")).thenReturn(java.util.Optional.of(user));

        assertThrows(exceptions.AuthenticationFailedException.class, () -> authService.loginAuthenticate(validCpf, plainPasswordToNotMatch));

    }

    @Test
    void loginAuthenticateMustNotThrowExceptionWhenCpfAndPasswordAreValid() throws SQLException, InvalidCpfException, AuthenticationFailedException {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String validCpf = "12345678901"; // Valid CPF
        String plainPassword = "randomPassword123";
        String passwordHash = util.PasswordUtil.plainToHash(plainPassword);
        User user = new User(1, validCpf, passwordHash);

        Mockito.when(authRepository.findByCpf(validCpf)).thenReturn(java.util.Optional.of(user));
        authService.loginAuthenticate(validCpf, plainPassword);


        assertEquals(user.getUserId(), Session.getUserId());
    }
    @Test
    void checkAlreadyExistsMustThrowCpfAlreadyRegisteredExceptionWhenUserExists() throws SQLException {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String cpfAlreadyRegistered = "12345678901"; // Valid CPF
        Mockito.when(authRepository.findByCpf(cpfAlreadyRegistered)).thenReturn(java.util.Optional.of(new User(1, cpfAlreadyRegistered, "hashedPassword")));

        assertThrows(exceptions.CpfAlreadyRegisteredException.class, () -> authService.checkAlreadyExists(cpfAlreadyRegistered));

    }

    @Test
    void checkAlreadyExistsMustNotThrowExceptionWhenUserDoesNotExist() throws SQLException {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String cpfNotRegistered = "12345678901"; // Valid CPF
        Mockito.when(authRepository.findByCpf(cpfNotRegistered)).thenReturn(java.util.Optional.empty());

        assertDoesNotThrow(() -> authService.checkAlreadyExists(cpfNotRegistered));
    }

    @Test
    void registerUserMustThrowInvalidCpfExceptionWhenCpfIsInvalid() throws SQLException {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String invalidCpf = "1234567890"; // Invalid CPF (10)
        String testPassword = "randompassword";
        User user = new User(1, invalidCpf, null);

        assertThrows(InvalidCpfException.class, () -> authService.registerUser(invalidCpf, testPassword, user));
        verify(authRepository, never()).saveUser(any());
        verify(authRepository, never()).findByCpf(any());
    }
    @Test
    void registerUserMustThrowCpfAlreadyRegisteredExceptionWhenCpfIsAlreadyRegistered() throws SQLException {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String validCpf = "12345678901"; // Valid CPF
        String testPassword = "randompassword";
        User user = new User(1, validCpf, null);

        Mockito.when(authRepository.findByCpf(validCpf)).thenReturn(java.util.Optional.of(new User(1, validCpf, "hashedPassword")));

        assertThrows(CpfAlreadyRegisteredException.class, () -> authService.registerUser(validCpf, testPassword, user));
        verify(authRepository, never()).saveUser(any());
    }

    @Test
    void registerUserMustRegisterUserSuccessfullyWhenCpfIsValidAndNotRegistered() throws SQLException, CpfAlreadyRegisteredException, InvalidCpfException {
        AuthRepository authRepository = Mockito.mock(AuthRepository.class);
        AuthService authService = new AuthService(authRepository);
        String validCpf = "12345678901"; // Valid CPF
        String testPassword = "randompassword";
        User user = new User(1, validCpf, null);

        Mockito.when(authRepository.findByCpf(validCpf)).thenReturn(Optional.empty());

        authService.registerUser(validCpf, testPassword, user);

        assertNotNull(user.getPasswordHash());
        assertNotEquals(testPassword, user.getPasswordHash());
        assertTrue(util.PasswordUtil.checkPassword(testPassword, user.getPasswordHash()));

        verify(authRepository).saveUser(user);
    }

}
