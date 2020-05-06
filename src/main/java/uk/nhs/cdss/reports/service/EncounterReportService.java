package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.Calendar;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.model.Participants;

@Service
@AllArgsConstructor
public class EncounterReportService {

  private FhirContext fhirContext;

  public EncounterReportInput createEncounterReportInput(ReferenceParam encounterReference) {

    var fhirSession = new FhirSession(new Reference(encounterReference.getValue()), fhirContext);

    var encounter = fhirSession.getEncounter();

    var participantRefs = encounter.getParticipant()
        .stream()
        .map(EncounterParticipantComponent::getIndividual)
        .filter(id -> id.getReferenceElement().getResourceType().equals("Practitioner"))
        .collect(Collectors.toUnmodifiableList());

    Participants participants = Participants.builder()
        .practitioners(fhirSession.getPractitioners(participantRefs))
        .relatedPeople(fhirSession.getRelatedPeople(participantRefs))
        .build();

    var patientRef = encounter.getSubject();
    return EncounterReportInput.builder()
        .dateOfPreparation(Calendar.getInstance())
        .encounter(encounter)
        .patient(fhirSession.getPatient(patientRef))
        .referralRequest(fhirSession.getReferralRequests())
        .participants(participants)
        .procedures(fhirSession.getProcedures())
        .observations(fhirSession.getObservations())
        .consent(fhirSession.getConsent())
        .session(fhirSession)
        .build();
  }
}
