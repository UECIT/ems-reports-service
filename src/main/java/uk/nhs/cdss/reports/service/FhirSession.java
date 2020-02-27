package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.context.FhirContext;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;

@RequiredArgsConstructor
public class FhirSession {

  private final Reference encounterRef;
  private final FhirContext fhirContext;

  public Encounter getEncounter() {
    return fhirReader(Encounter.class).apply(encounterRef);
  }

  public List<ReferralRequest> getReferralRequests() {
    String baseUrl = encounterRef.getReferenceElement().getBaseUrl();
    return fhirContext.newRestfulGenericClient(baseUrl).search()
        .byUrl("ReferralRequest?context:Encounter=" + encounterRef.getReference())
        .returnBundle(Bundle.class)
        .execute()
        .getEntry().stream()
        .map(entry -> (ReferralRequest) entry.getResource())
        .collect(Collectors.toList());
  }

  public Patient getPatient(Reference id) {
    return fhirReader(Patient.class).apply(id);
  }

  public Organization getOrganization(Reference id) {
    return fhirReader(Organization.class).apply(id);
  }

  public List<Practitioner> getParticipants(List<Reference> participants) {
    return participants.stream()
        .map(fhirReader(Practitioner.class))
        .collect(Collectors.toUnmodifiableList());
  }

  private <T extends DomainResource> Function<Reference, T> fhirReader(Class<T> type) {
    return ref -> {
      if (type.isInstance(ref.getResource())) {
        return type.cast(ref.getResource());
      }
      String baseUrl = encounterRef.getReferenceElement().getBaseUrl();
      T resource = fhirContext.newRestfulGenericClient(baseUrl)
          .read().resource(type)
          .withUrl(ref.getReferenceElement()).execute();
      ref.setResource(resource);
      return resource;
    };
  }

  public List<Procedure> getProcedures() {
    String baseUrl = encounterRef.getReferenceElement().getBaseUrl();
    return fhirContext.newRestfulGenericClient(baseUrl).search()
        .byUrl("Procedure?context:Encounter=" + encounterRef.getReference())
        .returnBundle(Bundle.class)
        .execute()
        .getEntry().stream()
        .map(entry -> (Procedure) entry.getResource())
        .collect(Collectors.toList());
  }
}
