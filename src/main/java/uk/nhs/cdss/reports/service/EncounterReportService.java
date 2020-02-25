package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.Calendar;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IIdType;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.model.EncounterReportInput;

@Service
@AllArgsConstructor
public class EncounterReportService {

  private FhirContext fhirContext;

  public EncounterReportInput createEncounterReportInput(ReferenceParam encounterReference) {

    var fhirSession = new FhirSession(new Reference(encounterReference.getValue()), fhirContext);

    var encounter = fhirSession.getEncounter();

    var practitioners = encounter.getParticipant()
        .stream()
        .map(EncounterParticipantComponent::getIndividual)
        .filter(id -> id.getReferenceElement().getResourceType().equals("Practitioner"))
        .collect(Collectors.toUnmodifiableList());
    var participants = fhirSession.getParticipants(practitioners);

    var patientRef = encounter.getSubject();
    return EncounterReportInput.builder()
        .dateOfPreparation(Calendar.getInstance())
        .encounter(encounter)
        .patient(fhirSession.getPatient(patientRef))
        .referralRequest(fhirSession.getReferralRequests())
        .participants(participants)
        .build();
  }
}
