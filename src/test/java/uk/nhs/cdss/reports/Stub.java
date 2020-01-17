package uk.nhs.cdss.reports;

import java.util.Calendar;
import java.util.Calendar.Builder;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.model.EncounterReportInput.EncounterReportInputBuilder;

@UtilityClass
public class Stub {

  public EncounterReportInputBuilder input() {
    return EncounterReportInput.builder()
        .dateOfPreparation(new Calendar.Builder()
            .setDate(2020, 0, 1)
            .build());
  }

  public Patient patient() {
    Patient patient = new Patient()
        .setBirthDate(new Builder().setDate(2000, 0, 1).build().getTime())
        .setGender(AdministrativeGender.FEMALE)
        .addName(new HumanName().addGiven("Jane").setFamily("Doe"));

    patient.setIdBase("123");
    return patient;
  }
}
