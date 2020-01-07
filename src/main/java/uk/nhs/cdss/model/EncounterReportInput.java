package uk.nhs.cdss.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ReferralRequest;

@Value
@Builder
public class EncounterReportInput {

  List<ReferralRequest> referralRequest;
  Encounter encounter;
  List<Composition> composition;
  Patient patient;

}
