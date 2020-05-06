package uk.nhs.cdss.reports.transform;

import org.apache.xmlbeans.XmlObject;
import uk.nhs.cdss.reports.model.EncounterReportInput;

public interface ReportXMLTransformer {

  XmlObject transform(EncounterReportInput input) throws TransformationException;
}
