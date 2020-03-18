package uk.nhs.cdss.reports;

import java.io.IOException;
import java.net.URL;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import uk.nhs.cdss.reports.transform.TransformationException;
import uk.nhs.cdss.reports.transform.XMLValidator;
import uk.nhs.itk.envelope.DistributionEnvelopeDocument;

public class IUCDSValidationTest {

  @Test
  public void iucds_basic_validates() throws IOException, XmlException, TransformationException {

    URL resource = getClass().getResource("/iucds_basic_report.xml");
    var iucdsReport = DistributionEnvelopeDocument.Factory.parse(resource);
    XMLValidator.validate(iucdsReport);
  }
}
