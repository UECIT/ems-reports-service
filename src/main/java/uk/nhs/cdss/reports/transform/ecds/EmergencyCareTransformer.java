package uk.nhs.cdss.reports.transform.ecds;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.util.IdentifierUtil;
import uk.nhs.nhsia.datastandards.ecds.AN2ECType;
import uk.nhs.nhsia.datastandards.ecds.EmergencyCareStucture;
import uk.nhs.nhsia.datastandards.ecds.EmergencyCareStucture.EmergencyCareAttendanceLocation;

@Component
@RequiredArgsConstructor
public class EmergencyCareTransformer {

  private final PatientInformationTransformer patientTransformer;
  private final AttendanceOccurrenceTransformer attendanceOccurrenceTransformer;
  private final GPRegistrationTransformer gpRegistrationTransformer;

  public EmergencyCareStucture transform(EncounterReportInput input) {

    var emergencyCare = EmergencyCareStucture.Factory.newInstance();

    emergencyCare.setPersonGroupPatient(patientTransformer.transform(input.getPatient()));
    emergencyCare.setEmergencyCareAttendanceLocation(getAttendanceLocation(input));
    emergencyCare.setAttendanceOccurrence(attendanceOccurrenceTransformer.transform(input));

    gpRegistrationTransformer.transform(input)
        .ifPresent(emergencyCare::setGPRegistration);

    return emergencyCare;
  }

  private EmergencyCareAttendanceLocation getAttendanceLocation(EncounterReportInput input) {
    EmergencyCareAttendanceLocation location =
        EmergencyCareAttendanceLocation.Factory.newInstance();

    // Required
    if (input.getEncounter().hasLocation()) {

      // Defaults
      // 89999 - Non-NHS UK Provider where no ORGANISATION IDENTIFIER has been issued
      location.setOrganisationSiteIdentifierOfTreatment("89999");
      location.setEmergencyCareDepartmentType(AN2ECType.X_04);

      List<EncounterLocationComponent> encounterLocations = input.getEncounter().getLocation();
      for (EncounterLocationComponent encounterLocation : encounterLocations) {
        Reference locationRef = encounterLocation.getLocation();
        Location locationResource = input.getSession().getLocation(locationRef);

        // Organization site identifier
        Optional<String> odsCode = IdentifierUtil.getOdsCode(locationResource.getIdentifier());
        if (odsCode.isPresent()) {
          location.setOrganisationSiteIdentifierOfTreatment(odsCode.get());

          // Location Type
          CodeableConcept locationType = locationResource.getType();
          // TODO A mapping is required here from the locationType concept
          // 01 -> Emergency departments
          location.setEmergencyCareDepartmentType(AN2ECType.X_01);
        }
      }
    } else {
      // If no location is specified, assume this is a virtual encounter

      // R9998 - Not a hospital site
      location.setOrganisationSiteIdentifierOfTreatment("R9998");

      // 04	-> NHS walk in centres
      // NOTE: there is no valid code for virtual encounters, so choosing 04 as a compromise
      location.setEmergencyCareDepartmentType(AN2ECType.X_04);
    }

    return location;
  }
}
