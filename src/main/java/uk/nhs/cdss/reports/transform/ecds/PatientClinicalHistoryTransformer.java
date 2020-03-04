package uk.nhs.cdss.reports.transform.ecds;

import static uk.nhs.cdss.reports.util.ReferenceUtil.ofType;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.constants.Systems;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.FhirSession;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.PatientClinicalHistory;

@Component
public class PatientClinicalHistoryTransformer {

  private static final String COMORBIDITY_CODE = "CM";

  public Optional<PatientClinicalHistory> transform(EncounterReportInput input) {
    FhirSession session = input.getSession();

    // Conditions/Procedures where the Encounter.diagnosis.role is 'CM'
    List<Reference> diagnosisRefs = input.getEncounter().getDiagnosis().stream()
        .filter(diagnosisComponent -> diagnosisComponent.getRole().getCoding().stream()
            .anyMatch(isComorbidCode()))
        .map(DiagnosisComponent::getCondition)
        .collect(Collectors.toUnmodifiableList());

    Stream<CodeableConcept> conditionCodes = diagnosisRefs.stream()
        .filter(ofType(Condition.class))
        .map(session::getCondition)
        .map(Condition::getCode);

    Stream<CodeableConcept> procedureCodes = diagnosisRefs.stream()
        .filter(ofType(Procedure.class))
        .map(session::getProcedure)
        .map(Procedure::getCode);

    String[] comorbidityCodes = Stream.concat(conditionCodes, procedureCodes)
        .map(CodeableConcept::getCodingFirstRep)
        .map(Coding::getCode)
        .toArray(String[]::new);

    if (ArrayUtils.isEmpty(comorbidityCodes)) {
      return Optional.empty();
    }

    PatientClinicalHistory clinicalHistory = PatientClinicalHistory.Factory.newInstance();
    clinicalHistory.setComorbiditySnomedCtArray(comorbidityCodes);

    return Optional.of(clinicalHistory);
  }

  private Predicate<Coding> isComorbidCode() {
    return coding -> coding.getCode().equals(COMORBIDITY_CODE)
      && coding.getSystem().equals(Systems.DIAGNOSIS_ROLE);
  }

}
