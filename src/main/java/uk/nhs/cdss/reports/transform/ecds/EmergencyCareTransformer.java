package uk.nhs.cdss.reports.transform.ecds;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.nhsia.datastandards.ecds.AN2ECType;
import uk.nhs.nhsia.datastandards.ecds.EmergencyCareStucture;
import uk.nhs.nhsia.datastandards.ecds.EmergencyCareStucture.EmergencyCareAttendanceLocation;

@Component
@RequiredArgsConstructor
public class EmergencyCareTransformer {

  private final PatientInformationTransformer patientTransformer;
  private final AttendanceOccurrenceTransformer attendanceOccurrenceTransformer;

  public EmergencyCareStucture transform(EncounterReportInput input) {
    var emergencyCare = EmergencyCareStucture.Factory.newInstance();

    emergencyCare.setPersonGroupPatient(patientTransformer.transform(input.getPatient()));
    emergencyCare.setEmergencyCareAttendanceLocation(getAttendanceLocation());
    emergencyCare.setAttendanceOccurrence(attendanceOccurrenceTransformer.transform(input));

    return emergencyCare;
  }

  private EmergencyCareAttendanceLocation getAttendanceLocation() {
    EmergencyCareAttendanceLocation location = EmergencyCareAttendanceLocation.Factory.newInstance();

    // Required
    location.setOrganisationSiteIdentifierOfTreatment("900000000");

    // 01 -> Emergency departments
    location.setEmergencyCareDepartmentType(AN2ECType.X_01);

    return location;
  }
}
