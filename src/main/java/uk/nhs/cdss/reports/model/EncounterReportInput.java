package uk.nhs.cdss.reports.model;

import java.util.Calendar;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import uk.nhs.cdss.reports.service.FhirSession;

@Value
@Builder
public class EncounterReportInput {

  Calendar dateOfPreparation;
  List<ReferralRequest> referralRequest;
  Encounter encounter;
  Patient patient;
  List<Practitioner> participants;
  List<Procedure> procedures;
  List<Observation> observations;
  FhirSession session;

}
