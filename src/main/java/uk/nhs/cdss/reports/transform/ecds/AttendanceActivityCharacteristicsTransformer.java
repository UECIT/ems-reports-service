package uk.nhs.cdss.reports.transform.ecds;

import com.google.common.base.Preconditions;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.constants.Systems;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.nhsia.datastandards.ecds.AttendCatECType;
import uk.nhs.nhsia.datastandards.ecds.AttendCatECType.Enum;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.EmergencyCareAttendanceActivityCharacteristics;
import uk.nhs.nhsia.datastandards.ecds.DateType;
import uk.nhs.nhsia.datastandards.ecds.TimeType;

@Service
public class AttendanceActivityCharacteristicsTransformer {

  public EmergencyCareAttendanceActivityCharacteristics transformActivity(
      EncounterReportInput input) {
    var activity = EmergencyCareAttendanceActivityCharacteristics.Factory.newInstance();

    // EMERGENCY CARE ATTENDANCE IDENTIFIER
    Encounter encounter = input.getEncounter();
    activity.setEmergencyCareAttendanceIdentifier(encounter.getIdElement().getIdPart());

    // EMERGENCY CARE ARRIVAL MODE (SNOMED CT)

    // EMERGENCY CARE ATTENDANCE CATEGORY
    // 2.0.0 guidance: MUST NOT be populated
    if (encounter.hasClass_()) {
      activity.setEmergencyCareAttendanceCategory(getCategory(encounter.getClass_()));
    }

    // EMERGENCY CARE ATTENDANCE SOURCE (SNOMED CT)
    // ORGANISATION SITE IDENTIFIER (EMERGENCY CARE ATTENDANCE SOURCE)

    // EMERGENCY CARE ARRIVAL DATE
    // EMERGENCY CARE ARRIVAL TIME
    // 2.0.0 guidance: SHOULD be populated
    Date arrival = encounter.hasPeriod()
        ? encounter.getPeriod().getStart()
        : input.getDateOfPreparation().getTime();
    Preconditions.checkNotNull(arrival, "encounter.period.start not present");

    activity.xsetEmergencyCareArrivalDate(DateTimeFormatter.formatDate(arrival, DateType.type));
    activity.xsetEmergencyCareArrivalTime(DateTimeFormatter.formatTime(arrival, TimeType.type));

    // AGE AT CDS ACTIVITY DATE
    // 2.0.0 guidance: SHOULD be populated
    Patient patient = input.getPatient();
    Date birthDate = patient == null ? null : patient.getBirthDate();
    if (birthDate != null) {
      int ageInYears = (int) ChronoUnit.YEARS
          .between(birthDate.toInstant().atZone(ZoneOffset.UTC),
              arrival.toInstant().atZone(ZoneOffset.UTC));
      activity.setAgeAtCdsActivityDate(ageInYears);
    } else {
      activity.setAgeAtCdsActivityDate(999);
    }

    // EMERGENCY CARE INITIAL ASSESSMENT DATE
    // EMERGENCY CARE INITIAL ASSESSMENT TIME
    activity.xsetEmergencyCareInitialAssessmentDate(
        DateTimeFormatter.formatDate(arrival, DateType.type));
    activity.xsetEmergencyCareInitialAssessmentTime(
        DateTimeFormatter.formatTime(arrival, TimeType.type));

    // EMERGENCY CARE ACUITY (SNOMED CT)
    // 2.0.0 guidance: SHOULD be populated
    if (encounter.hasPriority()) {
      CodeableConcept priority = encounter.getPriority();
      String acuityCode = getAcuityCode(priority);
      if (acuityCode != null) {
        activity.setEmergencyCareAcuitySnomedCt(acuityCode);
      }
    }

    // EMERGENCY CARE CHIEF COMPLAINT (SNOMED CT)
    if (CollectionUtils.isNotEmpty(input.getReferralRequest())) {
      // TODO decide which referral request is the chief complaint if more than one
      ReferralRequest referralRequest = input.getReferralRequest().get(0);

      // TODO more than one reasonRef?
      Preconditions.checkState(referralRequest.hasReasonReference(),
          "No primary concern in referral request");
      Reference reasonRef = referralRequest.getReasonReferenceFirstRep();

      Condition condition = input.getSession().getCondition(reasonRef);
      Coding coding = condition.getCode().getCodingFirstRep();
      Preconditions.checkArgument(Systems.SNOMED.equals(coding.getSystem()),
          "Condition code must be SNOMED");
      Preconditions.checkArgument(coding.getCode().length() <= 18,
          "Primary concern snomed code must be less than 18 characters");
      activity.setEmergencyCareChiefComplaintSnomedCt(coding.getCode());
    }

    // EMERGENCY CARE DATE SEEN FOR TREATMENT
    // EMERGENCY CARE TIME SEEN FOR TREATMENT
    // TODO omitted due to no treatment element to CDS encounter

    return activity;
  }

  private String getAcuityCode(CodeableConcept priority) {
    Coding coding = priority.getCodingFirstRep();
    Preconditions.checkArgument(Systems.ACT_PRIORITY.equals(coding.getSystem()),
        "Unexpected priority coding system");
    switch (coding.getCode()) {
      case "CR": // Callback
      case "EL": // Elective
        return "1077251000000100"; // Non-urgent
      case "R": // Routine
        return "1077241000000103"; // Standard
      case "EM": // Emergency
      case "UR": // Urgent
        return "1064901000000108"; // Urgent
      case "A": // ASAP
        return "1064911000000105"; // Very urgent
      case "S": // Stat
        return "1064891000000107"; // Immediate resuscitation
      default:
        return null;
    }
  }

  private Enum getCategory(Coding class_) {
    switch (class_.getCode()) {
      case "unplanned":
        return AttendCatECType.X_1;
      case "unplanned-follow-up":
        return AttendCatECType.X_2;
      case "unplanned-follow-up-new-location":
        return AttendCatECType.X_3;
      case "planned":
        return AttendCatECType.X_4;
      default:
        throw new IllegalArgumentException("Unrecognised encounter category");
    }
  }
}
