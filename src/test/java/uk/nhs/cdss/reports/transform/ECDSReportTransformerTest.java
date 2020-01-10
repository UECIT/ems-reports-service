package uk.nhs.cdss.reports.transform;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Assert;
import org.junit.Test;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.model.EncounterReportInput.EncounterReportInputBuilder;

public class ECDSReportTransformerTest extends EncounterReportTestBase {

  @Test
  public void empty_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = stubInput().build();

    String output = new ECDSReportTransformer().transform(encounterReportInput);

    URL resource = getClass().getResource("/ecds_empty_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, output);
  }

  @Test
  public void basic_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = stubInput()
        .patient(stubPatient())
        .build();

    String output = new ECDSReportTransformer().transform(encounterReportInput);

    URL resource = getClass().getResource("/ecds_basic_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, output);
  }

}