package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.Calendar;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.model.EncounterReportInput;

@Service
@AllArgsConstructor
public class EncounterReportService {

  private FhirService fhirService;

  public EncounterReportInput createEncounterReportInput(ReferenceParam encounterReference) {
    var encounterId = encounterReference.getValue();
    Encounter encounter = fhirService.getEncounter(encounterId);

    IdType patientId = new IdType(encounter.getSubject().getReference());

    // If no full URL is given, assume it's from the same place as the Encounter
    if (!patientId.isAbsolute()) {
      patientId.setValue(encounterReference.getBaseUrl() + "/" + patientId.getValue());
    }

    return EncounterReportInput.builder()
        .dateOfPreparation(Calendar.getInstance())
        .encounter(encounter)
        .patient(fhirService.getPatient(patientId.getValue()))
        .referralRequest(fhirService.getReferralRequests(encounterId))
        .build();
  }
}
