package uk.nhs.cdss.reports.transform;

import java.util.Calendar;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.model.EncounterReportInput.EncounterReportInputBuilder;

public class EncounterReportTestBase {

  protected EncounterReportInputBuilder stubInput() {
    return EncounterReportInput.builder()
        .dateOfPreparation(new Calendar.Builder()
            .setDate(2020, 0, 1)
            .build());
  }

  protected Patient stubPatient() {
    return new Patient()
        .setBirthDate(new Calendar.Builder().setDate(2000, 0, 1).build().getTime())
        .setGender(AdministrativeGender.FEMALE)
        .addName(new HumanName().addGiven("Jane").setFamily("Doe"));
  }
}
