package bank.loanservice.repository;

import bank.loanservice.entity.Underwriting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnderWritingRepository extends JpaRepository<Underwriting, UUID> {
}
