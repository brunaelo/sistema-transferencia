package br.edu.utfpr.bankapi.service;

import br.edu.utfpr.bankapi.dto.DepositDTO;
import br.edu.utfpr.bankapi.dto.TransferDTO;
import br.edu.utfpr.bankapi.dto.WithdrawDTO;
import br.edu.utfpr.bankapi.exception.NotFoundException;
import br.edu.utfpr.bankapi.model.Account;
import br.edu.utfpr.bankapi.model.Transaction;
import br.edu.utfpr.bankapi.model.TransactionType;
import br.edu.utfpr.bankapi.repository.AccountRepository;
import br.edu.utfpr.bankapi.repository.TransactionRepository;
import br.edu.utfpr.bankapi.validations.AvailableAccountValidation;
import br.edu.utfpr.bankapi.validations.AvailableBalanceValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTests {
    @InjectMocks
    TransactionService transactionService;
    @Mock
    TransactionRepository transactionRepository;
    @Mock
    AccountRepository accountRepository;

    @Mock
    private AvailableAccountValidation availableAccountValidation;

    @Mock
    private AvailableBalanceValidation availableBalanceValidation;



    @Captor
    ArgumentCaptor<Transaction> transactionCaptor;
    @DisplayName("A conta de destino deve existir para uma transação.")
    @Test
    public void shouldExistTargetAccount() throws Exception {
        var dto = new DepositDTO(
                12345, 1000
        );
        var destinoConta = new Account("Elo Pinga", 12345, 0.0, 0);

        BDDMockito.given(accountRepository.getByNumber(12345)).willReturn(Optional.of(destinoConta));

        transactionService.deposit(dto);

        BDDMockito.then(transactionRepository).should().save(transactionCaptor.capture());
        Transaction transaction = transactionCaptor.getValue();

        Assertions.assertEquals(destinoConta, transaction.getSourceAccount());
        Assertions.assertEquals(1000, transaction.getAmount());
        Assertions.assertEquals(TransactionType.DEPOSIT, transaction.getType());
        Assertions.assertNotEquals(destinoConta.getBalance(), 0);
    }

    @Test
    public void deveRealizarOSaque() throws NotFoundException {
        // Mock data
        long contaOrigem = 123456;
        double initialBalance = 500.0;
        double withdrawalAmount = 100.0;
        double expectedFinalBalance = initialBalance - withdrawalAmount;

        Account origemConta = new Account();
        origemConta.setNumber(contaOrigem);
        origemConta.setBalance(initialBalance);

        WithdrawDTO withdrawDTO = new WithdrawDTO(contaOrigem, withdrawalAmount);

        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(withdrawDTO, transaction);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setReceiverAccount(origemConta);

        when(availableAccountValidation.validate(contaOrigem)).thenReturn(origemConta);

        // Perform withdraw
        transactionService.withdraw(withdrawDTO);
        Assertions.assertEquals(expectedFinalBalance, origemConta.getBalance());
    }

    @Test
    public void deveTransferir() throws NotFoundException {
        long contaOrigem = 123456;
        long contaDestino = 654321;
        double saldoInicial = 500.0;
        double saldoInicialRecebedor = 200.0;
        double valorTransferencia = 100.0;
        double finalOrigemEsperado = saldoInicial - valorTransferencia;
        double finalDestinoEsperado = saldoInicialRecebedor + valorTransferencia;

        Account origemConta = new Account();
        origemConta.setNumber(contaOrigem);
        origemConta.setBalance(saldoInicial);

        Account destinoConta = new Account();
        destinoConta.setNumber(contaDestino);
        destinoConta.setBalance(saldoInicialRecebedor);

        TransferDTO transferDTO = new TransferDTO(contaOrigem, contaDestino, valorTransferencia);

        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(transferDTO, transaction);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setSourceAccount(origemConta);
        transaction.setReceiverAccount(destinoConta);

        when(availableAccountValidation.validate(contaOrigem)).thenReturn(origemConta);
        when(availableAccountValidation.validate(contaDestino)).thenReturn(destinoConta);

        transactionService.transfer(transferDTO);

        Assertions.assertEquals(finalOrigemEsperado, origemConta.getBalance());
        
        Assertions.assertEquals(finalDestinoEsperado, destinoConta.getBalance());
    }
}
