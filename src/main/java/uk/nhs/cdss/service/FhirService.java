package uk.nhs.cdss.service;

import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.List;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.config.FhirRestfulClient;
import uk.nhs.cdss.model.EncounterReportInput;

@Service
@AllArgsConstructor
public class FhirService {

  private FhirRestfulClient fhirServer;

  public EncounterReportInput createEncounterReportInput(ReferenceParam encounterReference) {

    Encounter encounter = fhirServer.getEncounter(new IdType(encounterReference.getIdPart()));

    Patient patient = encounter.hasSubject()
        ? fhirServer.getPatient(new IdType(encounter.getSubject().getId()))
        : null;

    List<ReferralRequest> referralRequests = fhirServer
        .getReferralRequestsByEncounter(encounterReference);

    List<Composition> compositions = fhirServer
        .getCompositionsByEncounter(encounterReference);

    return EncounterReportInput.builder()
        .composition(compositions)
        .encounter(encounter)
        .patient(patient)
        .referralRequest(referralRequests)
        .build();
  }


}
