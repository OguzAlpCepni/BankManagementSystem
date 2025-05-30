package bank.loanservice.kafka.producer;


import io.github.oguzalpcepni.event.LoanApplicationCreatedEvent;

public interface KafkaProducerService {
    void sendTransferEvent(LoanApplicationCreatedEvent loanApplicationCreatedEvent);
}
