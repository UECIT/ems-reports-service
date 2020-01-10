package uk.nhs.cdss.reports.transform;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.connect.iucds.ucr.ClinicalDocumentDocument1;
import uk.nhs.connect.iucds.ucr.POCDMT200001GB02ClinicalDocument1;

@Component
public class IUCDSReportTransformer implements ReportXMLTransformer<EncounterReportInput> {

  @Override
  public String transform(EncounterReportInput input) throws TransformationException {
    ClinicalDocumentDocument1 document = ClinicalDocumentDocument1.Factory.newInstance();
    POCDMT200001GB02ClinicalDocument1 clinicalDocument = document.addNewClinicalDocument();

    validate(document);

    try {
      StringWriter output = new StringWriter();
      document.save(output, new XmlOptions()
          .setSavePrettyPrint()
      );
      return output.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void validate(XmlObject document) throws TransformationException {
    XmlOptions validateOptions = new XmlOptions();
    ArrayList<XmlError> errorList = new ArrayList<>();
    validateOptions.setErrorListener(errorList);

    // Validate the XML.
    boolean isValid = document.validate(validateOptions);

    // If the XML isn't valid, loop through the listener's contents,
    // printing contained messages.
    if (!isValid) {
      StringBuilder message = new StringBuilder("Validation failed:\n");
      for (XmlError error : errorList) {
        message.append(error.getMessage() + "\n");
      }

      // TODO throw validation exception
      System.out.println(message);
//      throw new ValidationException(message.toString());
    }
  }
}
