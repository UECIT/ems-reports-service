package uk.nhs.cdss.reports.transform.ecds;

import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.nhsia.datastandards.ecds.PersonGroupPatientECStructure;

@Component
@AllArgsConstructor
public class PatientInformationTransformer {

  private final PatientIdentityTransformer identityTransformer;
  private final PatientCharacteristicsTransformer characteristicsTransformer;

  public PersonGroupPatientECStructure transform(Patient patient) {
    var patientStructure = PersonGroupPatientECStructure.Factory.newInstance();

    // Required
    patientStructure.setPatientIdentity(identityTransformer.transform(patient));
    characteristicsTransformer.transform(patient)
        .ifPresent(patientStructure::setPatientCharacteristicsEmergencyCare);

    return patientStructure;
  }

}
