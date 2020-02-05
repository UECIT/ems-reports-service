package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FhirService {

  private IGenericClient fhirClient;

  public Encounter getEncounter(String encounterId) {
    return fhirReader(Encounter.class).apply(encounterId);
  }

  public List<ReferralRequest> getReferralRequests(String encounterId) {
    return fhirClient.search()
        .byUrl("ReferralRequest?context:Encounter=Encounter/" + encounterId)
        .returnBundle(Bundle.class)
        .execute()
        .getEntry().stream()
        .map(entry -> (ReferralRequest)entry.getResource())
        .collect(Collectors.toList());
  }

  public Patient getPatient(String id) {
    return fhirReader(Patient.class).apply(id);
  }

  public List<Practitioner> getParticipants(List<String> participants) {
    return participants.stream()
        .map(fhirReader(Practitioner.class))
        .collect(Collectors.toUnmodifiableList());
  }

  private <T extends DomainResource> Function<String, T> fhirReader(Class<T> type) {
    var reader = fhirClient.read().resource(type);
    return id -> reader.withId(id).execute();
  }
}
