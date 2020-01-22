package uk.nhs.cdss.reports.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.model.Counter;
import uk.nhs.cdss.reports.repos.CounterRepository;

@Service
@AllArgsConstructor
public class CounterService {

  private CounterRepository counterRepository;

  public Long incrementAndGetCounter(String name) {
    if (!counterRepository.existsById(name)) {
      counterRepository.save(new Counter(name));
      return Counter.INITIAL_VALUE;
    }

    return counterRepository.incrementAndGet(name);
  }

}
