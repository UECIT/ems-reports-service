package uk.nhs.cdss.reports.transform.ecds;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
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
public class AttendanceOccurrenceTransformer {

  private long attendanceRef = 0;

  public AttendanceOccurrenceECStructure transform(EncounterReportInput input) {
    var attendanceStructure = AttendanceOccurrenceECStructure.Factory.newInstance();

    // Required
    attendanceStructure.setEmergencyCareAttendanceActivityCharacteristics(
        transformActivity(input.getDateOfPreparation()));
    attendanceStructure.setCareProfessionalsEmergencyCareArray(
        transformProfessionals(input.getParticipants()));
    attendanceStructure.setServiceAgreementDetails(transformServiceAgreement());

    // TODO populate from encounter
    return attendanceStructure;
  }

  private EmergencyCareAttendanceActivityCharacteristics transformActivity(Calendar date) {
    var activity = EmergencyCareAttendanceActivityCharacteristics.Factory.newInstance();

    activity.setEmergencyCareAttendanceIdentifier(Long.toString(++attendanceRef));
    activity.xsetEmergencyCareArrivalDate(DateTimeFormatter.formatDate(date, DateType.type)); // TODO
    activity.xsetEmergencyCareArrivalTime(DateTimeFormatter.formatTime(date, TimeType.type)); // TODO
    activity.setAgeAtCdsActivityDate(20); // TODO

    return activity;
  }

  private ServiceAgreementDetails transformServiceAgreement() {
    var serviceAgreement = ServiceAgreementDetails.Factory.newInstance();

    serviceAgreement.setOrganisationIdentifierCodeOfProvider("50000"); // TODO
    serviceAgreement.setOrganisationIdentifierCodeOfCommissioner("50000"); // TODO

    return serviceAgreement;
  }

  private CareProfessionalsEmergencyCare[] transformProfessionals(List<Practitioner> practitioners) {
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

    var issuerId = new IdType(practitionerId.getAssigner().getReference()).getIdPart();
    professional.setProfessionalRegistrationIssuerCode(transformIssuerCode(issuerId));
    professional.setProfessionalRegistrationEntryIdentifier(practitionerId.getValue());
    professional.setCareProfessionalTierEmergencyCare(CareProfessionalTierECType.X_01);
    professional.setCareProfessionalDischargeResponsibilityIndicatorEmergencyCare(YesNoECType.N);

    return professional;
  }

  private ProfessionalRegistrationIssuerCodeECType.Enum transformIssuerCode(String organisationCode) {
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
