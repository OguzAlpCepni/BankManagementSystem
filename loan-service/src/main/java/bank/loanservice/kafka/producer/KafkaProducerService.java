package bank.loanservice.kafka.producer;



import io.github.oguzalpcepni.event.LoanApplicationCreatedEvent;


import java.util.UUID;


public interface KafkaProducerService {
    void sendCreatedEvent(LoanApplicationCreatedEvent loanApplicationCreatedEvent);
}
