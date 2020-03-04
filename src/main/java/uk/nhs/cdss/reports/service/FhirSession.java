package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.context.FhirContext;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Location;
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

  private String getBaseUrl() {
    return encounterRef.getReferenceElement().getBaseUrl();
  }

  private <T extends DomainResource> Function<Reference, T> fhirReader(Class<T> type) {
    return ref -> {
      if (type.isInstance(ref.getResource())) {
        return type.cast(ref.getResource());
      }
      T resource = fhirContext.newRestfulGenericClient(getBaseUrl())
          .read().resource(type)
          .withUrl(ref.getReferenceElement()).execute();
      ref.setResource(resource);
      return resource;
    };
  }

  public List<ReferralRequest> getReferralRequests() {
    return fhirContext.newRestfulGenericClient(getBaseUrl()).search()
        .byUrl("ReferralRequest?context:Encounter=" + encounterRef.getReference())
        .returnBundle(Bundle.class)
        .execute()
        .getEntry().stream()
        .map(entry -> (ReferralRequest) entry.getResource())
        .collect(Collectors.toList());
  }

  public Patient getPatient(Reference ref) {
    return fhirReader(Patient.class).apply(ref);
  }

  public Organization getOrganization(Reference ref) {
    return fhirReader(Organization.class).apply(ref);
  }

  public Practitioner getPractitioner(Reference ref) {
    return fhirReader(Practitioner.class).apply(ref);
  }

  public Condition getCondition(Reference ref) {
    return fhirReader(Condition.class).apply(ref);
  }

  public List<Practitioner> getParticipants(List<Reference> participants) {
    return participants.stream()
        .map(fhirReader(Practitioner.class))
        .collect(Collectors.toUnmodifiableList());
  }

  public Procedure getProcedure(Reference ref) {
    return fhirReader(Procedure.class).apply(ref);
  }

  public List<Procedure> getProcedures() {
    return fhirContext.newRestfulGenericClient(getBaseUrl()).search()
        .byUrl("Procedure?context:Encounter=" + encounterRef.getReference())
        .returnBundle(Bundle.class)
        .execute()
        .getEntry().stream()
        .map(entry -> (Procedure) entry.getResource())
        .collect(Collectors.toList());
  }

  public Location getLocation(Reference ref) {
    return fhirReader(Location.class).apply(ref);
  }
}
