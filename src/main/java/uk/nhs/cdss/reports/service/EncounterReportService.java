package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.Calendar;
import java.util.List;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.model.EncounterReportInput;

@Service
@AllArgsConstructor
public class EncounterReportService {

  private FhirService fhirService;

  public EncounterReportInput createEncounterReportInput(ReferenceParam encounterReference) {
    var encounterId = encounterReference.getIdPart();
    Encounter encounter = fhirService.getEncounter(encounterId);

    Patient patient = encounter.hasSubject()
        ? fhirService.getPatient(encounter.getSubject().getId())
        : null;

    List<ReferralRequest> referralRequests = fhirService
        .getReferralRequests(encounterId);

    List<Composition> compositions = fhirService
        .getCompositions(encounterId);

    return EncounterReportInput.builder()
        .dateOfPreparation(Calendar.getInstance())
        .composition(compositions)
        .encounter(encounter)
        .patient(patient)
        .referralRequest(referralRequests)
        .build();
  }
}
