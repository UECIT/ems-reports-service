package uk.nhs.cdss.reports.transform.ecds;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.FhirSession;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.CareProfessionalsEmergencyCare;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.EmergencyCareAttendanceActivityCharacteristics;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.ServiceAgreementDetails;
import uk.nhs.nhsia.datastandards.ecds.CareProfessionalTierECType;
import uk.nhs.nhsia.datastandards.ecds.DateType;
import uk.nhs.nhsia.datastandards.ecds.ProfessionalRegistrationIssuerCodeECType;
import uk.nhs.nhsia.datastandards.ecds.TimeType;
import uk.nhs.nhsia.datastandards.ecds.YesNoECType;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceOccurrenceTransformer {

  // TODO find correct commissioner org code
  private static final String commissionerOrgCode = "2BB00";

  private final FhirSession fhirSession;

  private long attendanceRef = 0;

  public AttendanceOccurrenceECStructure transform(EncounterReportInput input) {
    var attendanceStructure = AttendanceOccurrenceECStructure.Factory.newInstance();

    // Required
    attendanceStructure.setEmergencyCareAttendanceActivityCharacteristics(
        transformActivity(input.getDateOfPreparation()));
    attendanceStructure.setCareProfessionalsEmergencyCareArray(
        transformProfessionals(input.getParticipants()));

    attendanceStructure.setServiceAgreementDetails(transformServiceAgreement(input));

    // TODO populate from encounter
    return attendanceStructure;
  }

  private EmergencyCareAttendanceActivityCharacteristics transformActivity(Calendar date) {
    var activity = EmergencyCareAttendanceActivityCharacteristics.Factory.newInstance();

    activity.setEmergencyCareAttendanceIdentifier(Long.toString(++attendanceRef));
    activity
        .xsetEmergencyCareArrivalDate(DateTimeFormatter.formatDate(date, DateType.type)); // TODO
    activity
        .xsetEmergencyCareArrivalTime(DateTimeFormatter.formatTime(date, TimeType.type)); // TODO
    activity.setAgeAtCdsActivityDate(20); // TODO

    return activity;
  }

  private ServiceAgreementDetails transformServiceAgreement(EncounterReportInput input) {
    Reference serviceProviderRef = checkNotNull(input.getEncounter().getServiceProvider(),
        "Unable to add ServiceAgreement - no Service Provider in Encounter");

    Organization serviceProvider = fhirSession.getOrganization(serviceProviderRef);

    var serviceAgreement = ServiceAgreementDetails.Factory.newInstance();

    // TODO Required if present
//    serviceAgreement.setCommissioningSerialNumber("600000");

    // Determine ODS code of supplier, issued by SUS
    serviceProvider.getIdentifier().stream()
        .filter(identifier -> identifier.getSystem().equals("ods"))
        .findFirst()
        .map(Identifier::getValue)
        .ifPresent(s -> serviceAgreement.setOrganisationIdentifierCodeOfProvider(s));

    // Hardcoded commissioner for this service
    serviceAgreement.setOrganisationIdentifierCodeOfCommissioner(commissionerOrgCode);

    return serviceAgreement;
  }

  private CareProfessionalsEmergencyCare[] transformProfessionals(
      List<Practitioner> practitioners) {
    if (practitioners == null) {
      return null;
    }
    return practitioners.stream()
        .map(Practitioner::getIdentifier)
        .flatMap(Collection::stream)
        .map(this::transformProfessional)
        .toArray(CareProfessionalsEmergencyCare[]::new);
  }

  private CareProfessionalsEmergencyCare transformProfessional(Identifier practitionerId) {
    var professional = CareProfessionalsEmergencyCare.Factory.newInstance();

    var issuerId = practitionerId.getAssigner().getReferenceElement().getIdPart();
    professional.setProfessionalRegistrationIssuerCode(transformIssuerCode(issuerId));
    professional.setProfessionalRegistrationEntryIdentifier(practitionerId.getValue());
    professional.setCareProfessionalTierEmergencyCare(CareProfessionalTierECType.X_01);
    professional.setCareProfessionalDischargeResponsibilityIndicatorEmergencyCare(YesNoECType.N);

    return professional;
  }

  private ProfessionalRegistrationIssuerCodeECType.Enum transformIssuerCode(
      String organisationCode) {
    switch (organisationCode) {
      case "gdc":
        return ProfessionalRegistrationIssuerCodeECType.X_02;
      case "gmc":
        return ProfessionalRegistrationIssuerCodeECType.X_03;
      case "nmc":
        return ProfessionalRegistrationIssuerCodeECType.X_09;
      default:
        return ProfessionalRegistrationIssuerCodeECType.X_08;
    }
  }
}
