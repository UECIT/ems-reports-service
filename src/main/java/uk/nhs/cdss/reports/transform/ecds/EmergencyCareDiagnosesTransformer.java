package uk.nhs.cdss.reports.transform.ecds;

import static uk.nhs.cdss.reports.util.ReferenceUtil.ofType;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.EmergencyCareDiagnosesSnomedCt;

@Component
@AllArgsConstructor
public class EmergencyCareDiagnosesTransformer {

  public EmergencyCareDiagnosesSnomedCt[] transform(EncounterReportInput input) {
    var conditions = input.getEncounter().getDiagnosis().stream()
        .map(DiagnosisComponent::getCondition)
        .filter(ofType(Condition.class))
        .map(input.getSession()::getCondition)
        .collect(Collectors.toUnmodifiableList());

    return IntStream.range(0, conditions.size())
        .mapToObj(i -> this.transform(i, conditions.get(i)))
        .toArray(EmergencyCareDiagnosesSnomedCt[]::new);
  }

  private EmergencyCareDiagnosesSnomedCt transform(int zeroBasedIndex, Condition condition) {

    EmergencyCareDiagnosesSnomedCt diagnosis = EmergencyCareDiagnosesSnomedCt.Factory.newInstance();
    diagnosis.setEmergencyCareDiagnosisSnomedCt(condition.getCode().getCodingFirstRep().getCode());
    diagnosis.setCodedClinicalEntrySequenceNumber(Integer.toString(zeroBasedIndex + 1));

    // TODO: Will fail as needs to be snomed. What should this map from?
//    diagnosis.setEmergencyCareDiagnosisQualifierSnomedCt(condition.getVerificationStatus().toCode());
    diagnosis.setEmergencyCareDiagnosisQualifierSnomedCt("9929229");

    return diagnosis;
  }
}
