package bank.loanservice.service.impl;

import bank.loanservice.entity.LoanApplication;
import bank.loanservice.kafka.producer.KafkaProducerService;
import bank.loanservice.kafka.producer.KafkaProducerServiceImpl;
import bank.loanservice.repository.LoanApplicationRepository;
import bank.loanservice.service.LoanService;
import io.github.oguzalpcepni.dto.LoansDto.LoanRequest;
import io.github.oguzalpcepni.dto.LoansDto.LoanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public LoanResponse createLoan(LoanRequest loanRequest) {
        UUID customerId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        return null;
    }

}
