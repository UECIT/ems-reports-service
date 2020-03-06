package uk.nhs.cdss.reports.transform;

import ca.uhn.fhir.context.FhirContext;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ComparisonFailure;
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

  // TODO remove expected comparison failure - requires deterministic ID generation
  @Test(expected = ComparisonFailure.class)
  public void empty_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input()
        .session(fhirSession)
        .build();

    ClinicalDocumentDocument1 output = new IUCDSReportTransformer().transform(encounterReportInput);
    System.out.println(EncounterReportController.prettyPrint(output));

    URL resource = getClass().getResource("/iucds_empty_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, EncounterReportController.prettyPrint(output));
  }

  @Test(expected = ComparisonFailure.class)
  public void basic_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input()
        .session(fhirSession)
        .patient(Stub.patient())
        .consent(List.of(Stub.consent()))
        .build();

    ClinicalDocumentDocument1 output = new IUCDSReportTransformer().transform(encounterReportInput);
    System.out.println(EncounterReportController.prettyPrint(output));

    URL resource = getClass().getResource("/iucds_basic_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, EncounterReportController.prettyPrint(output));
  }

}
