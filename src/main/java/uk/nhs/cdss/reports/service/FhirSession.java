package uk.nhs.cdss.reports.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.Resource;

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

  private <T extends DomainResource> List<T> fhirSearchByContext(
      Class<T> type,
      ReferenceClientParam contextParam) {
    return fhirContext.newRestfulGenericClient(getBaseUrl()).search()
        .forResource(type)
        .where(contextParam.hasId(encounterRef.getReferenceElement()))
        .returnBundle(Bundle.class)
        .execute()
        .getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .map(type::cast)
        .collect(Collectors.toList());
  }

  public List<Consent> getConsent() {
    return fhirContext.newRestfulGenericClient(getBaseUrl()).search()
        .forResource(Consent.class)
        .where(Consent.PATIENT.hasId(getEncounter().getSubject().getReferenceElement()))
        .where(Consent.DATA.hasId(encounterRef.getReferenceElement()))
        .returnBundle(Bundle.class)
        .execute()
        .getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .map(Consent.class::cast)
        .collect(Collectors.toList());
  }

  public List<ReferralRequest> getReferralRequests() {
    return fhirSearchByContext(ReferralRequest.class, ReferralRequest.CONTEXT);
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

  public List<Observation> getObservations() {
    return fhirSearchByContext(Observation.class, Observation.CONTEXT);
  }

  public Procedure getProcedure(Reference ref) {
    return fhirReader(Procedure.class).apply(ref);
  }

  public List<Procedure> getProcedures() {
    return fhirSearchByContext(Procedure.class, Procedure.CONTEXT);
  }

  public Location getLocation(Reference ref) {
    return fhirReader(Location.class).apply(ref);
  }
}
