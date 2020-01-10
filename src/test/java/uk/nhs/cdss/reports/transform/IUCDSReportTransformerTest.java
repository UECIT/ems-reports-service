package uk.nhs.cdss.reports.transform;

import java.io.IOException;
import org.junit.Test;
import uk.nhs.cdss.reports.model.EncounterReportInput;

public class IUCDSReportTransformerTest extends EncounterReportTestBase {

  @Test
  public void empty_report_input() throws IOException, TransformationException {
    EncounterReportInput encounterReportInput = stubInput().build();

    String output = new IUCDSReportTransformer().transform(encounterReportInput);
    System.out.println(output);

    // TODO create example output document for IUCDS
//    URL resource = getClass().getResource("/ecds_empty_report.xml");
//    String expected = IOUtils.toString(resource, StandardCharsets.UTF_8);
//    Assert.assertEquals(expected, output);
  }

}
