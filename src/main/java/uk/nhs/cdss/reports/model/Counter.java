package uk.nhs.cdss.reports.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "counter")
@NoArgsConstructor
public class Counter {

  public static final Long INITIAL_VALUE = 0L;

  @Id
  private String name;
  private long value;

  public Counter(String name) {
    this.name = name;
    this.value = INITIAL_VALUE;
  }

}
