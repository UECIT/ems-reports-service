package uk.nhs.cdss.reports.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.cdss.reports.model.Counter;

@Repository
public interface CounterRepository
    extends JpaRepository<Counter, String>, CounterIncrementer {

}
