package bank.loanservice.kafka.producer;


import io.github.oguzalpcepni.event.LoanApplicationCreatedEvent;
import org.springframework.stereotype.Component;


public interface KafkaProducerService {
    void sendTransferEvent(LoanApplicationCreatedEvent loanApplicationCreatedEvent);
}
