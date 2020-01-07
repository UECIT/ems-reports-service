package uk.nhs.cdss.resourceProviders;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.model.EncounterReportInput;
import uk.nhs.cdss.service.FhirService;

@Component
@AllArgsConstructor
public class EncounterReportProvider implements IResourceProvider {

  private FhirService fhirService;

  @Operation(name = "$report")
  public Bundle generateReports(@IdParam IdType encounterRef) {

    EncounterReportInput encounterReportInput = fhirService
        .createEncounterReportInput(new ReferenceParam(encounterRef.getValue()));

    /** TEMPORARILY RETURNING THE BUNDLE **/
    Bundle bundle = new Bundle();
    bundle.addEntry().setResource(encounterReportInput.getEncounter());
    bundle.addEntry().setResource(encounterReportInput.getPatient());

    encounterReportInput.getComposition()
        .forEach(comp -> bundle.addEntry().setResource(comp));

    encounterReportInput.getReferralRequest()
        .forEach(ref -> bundle.addEntry().setResource(ref));

    return bundle;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Encounter.class;
  }
}
