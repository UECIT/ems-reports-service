package uk.nhs.cdss.reports.transform;

import ca.uhn.fhir.context.FhirContext;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.CareConnectPatient;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.cdss.reports.Stub;
import uk.nhs.cdss.reports.controllers.EncounterReportController;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.FhirSession;
import uk.nhs.nhsia.datastandards.ecds.CDSXMLInterchangeDocument;

@RunWith(MockitoJUnitRunner.class)
public class ECDSReportTransformerTest {

  @Mock
  private FhirContext fhirContext;

  private FhirSession fhirSession;

  @Before
  public void setup() {
    fhirSession = new FhirSession(new Reference("http://fhir/Encounter/1"), fhirContext);
  }

  @Test
  public void empty_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input()
        .session(fhirSession)
        .patient(new CareConnectPatient()
          .addIdentifier(Stub.nhsNumberIdentifierUnverified()))
        .build();

    CDSXMLInterchangeDocument output = Stub.ecdsTransformer()
        .transform(encounterReportInput);

    URL resource = getClass().getResource("/ecds_empty_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, EncounterReportController.prettyPrint(output));
  }

  @Test
  public void basic_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input()
        .session(fhirSession)
        .encounter(Stub.encounter())
        .patient(Stub.patient())
        .observations(Stub.injuryObservations())
        .build();

    CDSXMLInterchangeDocument output = Stub.ecdsTransformer()
        .transform(encounterReportInput);

    URL resource = getClass().getResource("/ecds_basic_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, EncounterReportController.prettyPrint(output));
  }

}