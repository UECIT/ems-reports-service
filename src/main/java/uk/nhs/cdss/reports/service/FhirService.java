package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FhirService {

  private IGenericClient fhirClient;

  public Encounter getEncounter(String encounterId) {
    return fhirClient.read()
        .resource(Encounter.class)
        .withId(encounterId)
        .execute();
  }

  public List<ReferralRequest> getReferralRequests(String encounterId) {
    return fhirClient.search()
        .byUrl("ReferralRequest?context:Encounter=" + encounterId)
        .returnBundle(Bundle.class)
        .execute()
        .getEntry().stream()
        .map(entry -> (ReferralRequest)entry.getResource())
        .collect(Collectors.toList());
  }

  public Patient getPatient(String id) {
    return fhirClient.read()
        .resource(Patient.class)
        .withId(id)
        .execute();
  }
}
