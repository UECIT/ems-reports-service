package uk.nhs.cdss.reports.transform;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import uk.nhs.cdss.reports.Stub;
import uk.nhs.cdss.reports.controllers.EncounterReportController;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.transform.ecds.ECDSReportTransformer;
import uk.nhs.nhsia.datastandards.ecds.CDSXMLInterchangeDocument;

public class ECDSReportTransformerTest {

  @Test
  public void empty_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input().build();

    CDSXMLInterchangeDocument output = new ECDSReportTransformer(Stub.counterService())
        .transform(encounterReportInput);

    URL resource = getClass().getResource("/ecds_empty_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, EncounterReportController.prettyPrint(output));
  }

  @Test
  public void basic_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = Stub.input()
        .patient(Stub.patient())
        .build();

    CDSXMLInterchangeDocument output = new ECDSReportTransformer(Stub.counterService())
        .transform(encounterReportInput);

    URL resource = getClass().getResource("/ecds_basic_report.xml");
    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
    Assert.assertEquals(expected, EncounterReportController.prettyPrint(output));
  }

}