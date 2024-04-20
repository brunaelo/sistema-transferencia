package br.edu.utfpr.bankapi.service;

import br.edu.utfpr.bankapi.dto.AccountDTO;
import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {
    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Test
    public void obtemNumeroDaConta() {

        long accountNumber = 123456;
        Account account = new Account();
        account.setNumber(accountNumber);
        Optional<Account> optionalAccount = Optional.of(account);

        when(accountRepository.getByNumber(accountNumber)).thenReturn(optionalAccount);

        Optional<Account> result = accountService.getByNumber(accountNumber);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(accountNumber, result.get().getNumber());
    }

    @Test
    public void obtemTodasContas() {
        List<Account> accounts = new ArrayList<Account>();
        accounts.add(new Account());
        accounts.add(new Account());

        when(accountRepository.findAll()).thenReturn(accounts);

        List<Account> result = accountService.getAll();

        Assertions.assertEquals(accounts.size(), result.size());
    }

    @Test
    public void salvar() {
        long number = 123456;
        AccountDTO accountDTO = new AccountDTO("John Doe", number, 200.0, 1234567.0);

        Account account = new Account();
        BeanUtils.copyProperties(accountDTO, account);
        account.setBalance(0);

        when(accountRepository.save(any())).thenReturn(account);

        Account savedAccount = accountService.save(accountDTO);

        Assertions.assertNotNull(savedAccount);
        Assertions.assertEquals(accountDTO.name(), savedAccount.getName());
        Assertions.assertEquals(accountDTO.number(), savedAccount.getNumber());
        Assertions.assertEquals(0, savedAccount.getBalance());
    }

    @Test
    public void atualizar() throws NotFoundException {
        long accountId = 1;
        long number = 123456;
        AccountDTO accountDTO = new AccountDTO("Dua Lipa", number, 500.0, 1234567.0);

        Account account = new Account();
        account.setId(accountId);
        BeanUtils.copyProperties(accountDTO, account);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);

        Account updatedAccount = accountService.update(accountId, accountDTO);

        Assertions.assertNotNull(updatedAccount);
        Assertions.assertEquals(accountDTO.name(), updatedAccount.getName());
        Assertions.assertEquals(accountDTO.number(), updatedAccount.getNumber());
        Assertions.assertEquals(accountDTO.specialLimit(), updatedAccount.getSpecialLimit());
    }

    @Test
    public void testUpdateNonexistentAccount() {
        long nonExistentAccountId = 999;
        long number = 123456;
        AccountDTO accountDTO = new AccountDTO("Kenny", number, 200.0, 1234567.0);

        when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> accountService.update(nonExistentAccountId, accountDTO));
    }
}