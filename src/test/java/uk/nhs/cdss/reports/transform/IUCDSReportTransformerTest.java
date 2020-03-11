package uk.nhs.cdss.reports.transform;

import ca.uhn.fhir.context.FhirContext;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.nhs.cdss.reports.Stub;
import uk.nhs.cdss.reports.controllers.EncounterReportController;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.FhirSession;
import uk.nhs.cdss.reports.transform.iucds.EncounterTransformer;
import uk.nhs.cdss.reports.transform.iucds.FixedUUIDProvider;
import uk.nhs.cdss.reports.transform.iucds.IUCDSReportTransformer;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;

public class IUCDSReportTransformerTest {

  @Mock
  private FhirContext fhirContext;

  private FhirSession fhirSession;

  private IUCDSReportTransformer iucdsReportTransformer;

  @Before
  public void setup() {
    fhirSession = new FhirSession(new Reference("http://fhir/Encounter/123"), fhirContext);
    FixedUUIDProvider uuidProvider = new FixedUUIDProvider();
    iucdsReportTransformer = new IUCDSReportTransformer(
        uuidProvider,
        new EncounterTransformer(uuidProvider));
  }

  // TODO remove expected comparison failure - requires deterministic ID generation
  @Test
  public void empty_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input()
        .session(fhirSession)
        .build();

    ClinicalDocumentDocument1 output = iucdsReportTransformer.transform(encounterReportInput);
    System.out.println(EncounterReportController.prettyPrint(output));

    URL resource = getClass().getResource("/iucds_empty_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, EncounterReportController.prettyPrint(output));
  }

  @Test
  public void basic_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input()
        .session(fhirSession)
        .encounter(Stub.encounter())
        .patient(Stub.patient())
        .consent(List.of(Stub.consent()))
        .build();

    ClinicalDocumentDocument1 output = iucdsReportTransformer.transform(encounterReportInput);
    System.out.println(EncounterReportController.prettyPrint(output));

    URL resource = getClass().getResource("/iucds_basic_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, EncounterReportController.prettyPrint(output));
  }

}
