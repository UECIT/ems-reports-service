package uk.nhs.cdss.reports.repos;

import java.math.BigInteger;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CounterIncrementerImpl implements CounterIncrementer {

  private EntityManager entityManager;

  @Transactional
  public long incrementAndGet(String name) {
    entityManager.createNativeQuery(
        "UPDATE counter "
            + "SET value = LAST_INSERT_ID(value + 1) "
            + "WHERE name = :name")
        .setParameter("name", name)
        .executeUpdate();

    var query = entityManager.createNativeQuery("SELECT LAST_INSERT_ID()");
    return ((BigInteger) query.getSingleResult()).longValue();
  }
}
