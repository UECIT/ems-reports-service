package uk.nhs.cdss.reports.transform.ecds;

import static uk.nhs.cdss.reports.util.ReferenceUtil.ofType;

import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.CounterService;
import uk.nhs.cdss.reports.service.FhirSession;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.EmergencyCareDiagnosesSnomedCt;

@Component
@AllArgsConstructor
public class EmergencyCareDiagnosesTransformer {

  private final CounterService counterService;

  public EmergencyCareDiagnosesSnomedCt[] transform(EncounterReportInput input) {
    FhirSession session = input.getSession();
    return input.getEncounter().getDiagnosis().stream()
        .map(DiagnosisComponent::getCondition)
        .filter(ofType(Condition.class))
        .map(session::getCondition)
        .map(this::transform)
        .toArray(EmergencyCareDiagnosesSnomedCt[]::new);
  }

  private EmergencyCareDiagnosesSnomedCt transform(Condition condition) {

    EmergencyCareDiagnosesSnomedCt diagnosis = EmergencyCareDiagnosesSnomedCt.Factory.newInstance();
    diagnosis.setEmergencyCareDiagnosisSnomedCt(condition.getCode().getCodingFirstRep().getCode());
    diagnosis.setCodedClinicalEntrySequenceNumber(counterService.incrementAndGetCounter(ECDSCounters.CODED_CLINICAL_ENTRY).toString());

    // TODO: Will fail as needs to be snomed. What should this map from?
//    diagnosis.setEmergencyCareDiagnosisQualifierSnomedCt(condition.getVerificationStatus().toCode());
    diagnosis.setEmergencyCareDiagnosisQualifierSnomedCt("9929229");

    return diagnosis;
  }
}
