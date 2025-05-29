package bank.customerservice.repository;

import bank.customerservice.entity.Customers;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomersRepository extends MongoRepository<Customers, String> {

    Optional<Customers> findByEmail(String email);
    Optional<Customers> findByPhone(String phone);
}
