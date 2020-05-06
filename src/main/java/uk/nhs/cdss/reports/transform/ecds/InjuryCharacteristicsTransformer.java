package uk.nhs.cdss.reports.transform.ecds;

import static uk.nhs.cdss.reports.util.ReferenceUtil.ofType;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.PrimitiveType;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.SnomedService;
import uk.nhs.cdss.reports.transform.ecds.constants.InjuryCodes;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.InjuryCharacteristics;
import uk.nhs.nhsia.datastandards.ecds.DateType;
import uk.nhs.nhsia.datastandards.ecds.TimeType;

@Component
@RequiredArgsConstructor
public class InjuryCharacteristicsTransformer {

  private final SnomedService snomedService;

  public Optional<InjuryCharacteristics> transform(EncounterReportInput input) {

    var injuryOnset = input.getEncounter()
        .getDiagnosis()
        .stream()
        .map(DiagnosisComponent::getCondition)
        .filter(ofType(Condition.class))
        .map(input.getSession()::getCondition)
        .filter(c -> snomedService.isDescendantOf(c.getCode(), InjuryCodes.WOUND_FINDING))
        .map(Condition::getOnsetDateTimeType)
        .map(PrimitiveType::getValue)
        .findFirst();

    if (injuryOnset.isEmpty()) {
      return Optional.empty();
    }

    var injury = InjuryCharacteristics.Factory.newInstance();

    injury.xsetInjuryDate(DateTimeFormatter.formatDate(injuryOnset.get(), DateType.type));
    injury.xsetInjuryTime(DateTimeFormatter.formatTime(injuryOnset.get(), TimeType.type));

    findObservationsInRefset(input, InjuryCodes.PLACE_REFSET)
        .findFirst()
        .ifPresent(injury::setEmergencyCarePlaceOfInjurySnomedCt);
    findObservationsInRefset(input, InjuryCodes.INTENT_REFSET)
        .findFirst()
        .ifPresent(injury::setEmergencyCareInjuryIntentSnomedCt);
    findObservationsInRefset(input, InjuryCodes.ACTIVITY_STATUS_REFSET)
        .findFirst()
        .ifPresent(injury::setEmergencyCareInjuryActivityStatusSnomedCt);
    findObservationsInRefset(input, InjuryCodes.ACTIVITY_TYPE_REFSET)
        .findFirst()
        .ifPresent(injury::setEmergencyCareInjuryActivityTypeSnomedCt);
    findObservationsInRefset(input, InjuryCodes.MECHANISM_REFSET)
        .findFirst()
        .ifPresent(injury::setEmergencyCareInjuryMechanismSnomedCt);

    findObservationsInRefset(input, InjuryCodes.ALCOHOL_OR_DRUG_INVOLVEMENT_REFSET)
        .forEach(injury::addEmergencyCareInjuryAlcoholOrDrugInvolvementSnomedCt);

    return Optional.of(injury);
  }

  private Stream<String> findObservationsInRefset(EncounterReportInput input, String refset) {
    return input.getObservations()
        .stream()
        .map(Observation::getCode)
        .flatMap(snomedService::snomedCodesIn)
        .filter(code -> snomedService.isMemberOf(code, refset));
  }
}
