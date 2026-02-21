package bank.loanservice.repository;

import bank.loanservice.entity.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

//kalem
public interface InstallmentRepository extends JpaRepository<Installment, UUID> {
}
