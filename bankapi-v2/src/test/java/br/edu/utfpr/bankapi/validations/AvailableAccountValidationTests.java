package br.edu.utfpr.bankapi.validations;

import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AvailableAccountValidationTests {

    @Mock
    private AvailableAccountValidation availableAccountValidation;

    @Mock
    private AccountRepository accountRepository;

    @Test
    public void testValidateExistingAccount() throws NotFoundException {
        // Mock data
        long accountNumber = 123456;
        Account account = new Account();
        account.setNumber(accountNumber);

        // Mock repository response
        when(accountRepository.getByNumber(accountNumber)).thenReturn(Optional.of(account));

        // Perform validation
        Account validatedAccount = availableAccountValidation.validate(accountNumber);

        Assertions.assertNotNull(validatedAccount);
        Assertions.assertEquals(accountNumber, validatedAccount.getNumber());
    }

    @Test
    public void testValidateNonExistingAccount() throws NotFoundException {
        // Mock data
        long nonExistingAccountNumber = 999999;


        when(accountRepository.getByNumber(nonExistingAccountNumber)).thenReturn(Optional.empty());

        Account account = availableAccountValidation.validate(nonExistingAccountNumber);

        Assertions.assertNull(account);
    }
}
