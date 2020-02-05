package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.Calendar;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.model.EncounterReportInput;

@Service
@AllArgsConstructor
public class EncounterReportService {

  private FhirService fhirService;

  public EncounterReportInput createEncounterReportInput(ReferenceParam encounterReference) {
    var encounterId = encounterReference.getValue();
    var encounter = fhirService.getEncounter(encounterId);
    // If no full URL is given, assume it's from the same place as the Encounter
    var makeAbsolute = absoluteUrl(encounterReference.getBaseUrl());

    var practitioners = encounter.getParticipant()
        .stream()
        .map(EncounterParticipantComponent::getIndividual)
        .map(Reference::getReference)
        .map(IdType::new)
        .filter(id -> id.getResourceType().equals("Practitioner"))
        .map(makeAbsolute)
        .collect(Collectors.toUnmodifiableList());
    var participants = fhirService.getParticipants(practitioners);

    var patientId = new IdType(encounter.getSubject().getReference());
    return EncounterReportInput.builder()
        .dateOfPreparation(Calendar.getInstance())
        .encounter(encounter)
        .patient(fhirService.getPatient(makeAbsolute.apply(patientId)))
        .referralRequest(fhirService.getReferralRequests(encounterId))
        .participants(participants)
        .build();
  }

  private static Function<IdType, String> absoluteUrl(String baseUrl) {
    return id -> id.isAbsolute()
          ? id.getValue()
          : baseUrl + "/" + id.getValue();
  }
}
