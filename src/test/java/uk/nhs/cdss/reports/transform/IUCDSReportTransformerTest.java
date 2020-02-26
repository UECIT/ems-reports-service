package uk.nhs.cdss.reports.transform;

import ca.uhn.fhir.context.FhirContext;
import java.io.IOException;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.nhs.cdss.reports.Stub;
import uk.nhs.cdss.reports.controllers.EncounterReportController;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.FhirSession;
import uk.nhs.cdss.reports.transform.iucds.IUCDSReportTransformer;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;

public class IUCDSReportTransformerTest {

  @Mock
  private FhirContext fhirContext;

  private FhirSession fhirSession;

  @Before
  public void setup() {
    fhirSession = new FhirSession(new Reference("http://fhir/Encounter/123"), fhirContext);
  }

  @Test
  public void empty_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input()
        .session(fhirSession)
        .build();

    ClinicalDocumentDocument1 output = new IUCDSReportTransformer().transform(encounterReportInput);
    System.out.println(EncounterReportController.prettyPrint(output));

    // TODO create example output document for IUCDS
//    URL resource = getClass().getResource("/ecds_empty_report.xml");
//    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
//    Assert.assertEquals(expected, output);
  }

  @Test
  public void basic_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input()
        .session(fhirSession)
        .patient(Stub.patient())
        .build();

    ClinicalDocumentDocument1 output = new IUCDSReportTransformer().transform(encounterReportInput);
    System.out.println(EncounterReportController.prettyPrint(output));

//    URL resource = getClass().getResource("/ecds_basic_report.xml");
//    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
//    Assert.assertEquals(expected, EncounterReportController.prettyPrint(output));
  }

}
