package uk.nhs.cdss.reports.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Systems {
  public final String ODS = "https://fhir.nhs.uk/Id/ods-organization-code";
  public final String NHS_NUMBER = "https://fhir.nhs.uk/Id/nhs-number";

  public final String ACT_PRIORITY = "http://hl7.org/fhir/v3/ActPriority";
  public final String SNOMED = "http://snomed.info/sct";
  public final String DIAGNOSIS_ROLE = "http://hl7.org/fhir/diagnosis-role";
}
