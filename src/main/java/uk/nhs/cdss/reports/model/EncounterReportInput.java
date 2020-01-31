package uk.nhs.cdss.reports.model;

import java.util.Calendar;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ReferralRequest;

@Value
@Builder
public class EncounterReportInput {

  Calendar dateOfPreparation;
  List<ReferralRequest> referralRequest;
  Encounter encounter;
  Patient patient;

}
