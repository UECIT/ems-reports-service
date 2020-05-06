package uk.nhs.cdss.reports.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportsDTO {

  private String ecds;
  private String iucds;
}
