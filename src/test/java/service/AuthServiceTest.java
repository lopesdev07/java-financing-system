package service;

import exceptions.AuthenticationFailedException;
import exceptions.InvalidCpfException;
import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import repository.AuthRepository;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
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



}
