package uk.nhs.cdss.reports.repos;

public interface CounterIncrementer {
  long incrementAndGet(String name);
}
