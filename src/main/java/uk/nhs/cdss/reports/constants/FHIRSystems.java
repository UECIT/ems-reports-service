package uk.nhs.cdss.reports.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FHIRSystems {
  public final String ODS_ORGANIZATION = "https://fhir.nhs.uk/Id/ods-organization-code";
  public final String ODS_SITE = "https://fhir.nhs.uk/Id/ods-site-code";
  public final String NHS_NUMBER = "https://fhir.nhs.uk/Id/nhs-number";

  public final String ACT_PRIORITY = "http://hl7.org/fhir/v3/ActPriority";
  public final String SNOMED = "http://snomed.info/sct";
  public final String DIAGNOSIS_ROLE = "http://hl7.org/fhir/diagnosis-role";

  public final String ETHNIC_CODES_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-EthnicCategory-1";
  public final String RESIDENTIAL_STATUS_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-ResidentialStatus-1";
  public final String NHS_COMMS_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-NHSCommunication-1";
  public final String TREATMENT_CATEGORY_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-TreatmentCategory-1";

  public final String SERVICE_DELIVERY_LOCATION_ROLE_TYPE = "http://hl7.org/fhir/ValueSet/v3-ServiceDeliveryLocationRoleType";
}
