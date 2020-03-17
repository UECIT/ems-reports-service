package uk.nhs.cdss.reports.transform;

import java.io.IOException;
import java.util.ArrayList;
import lombok.experimental.UtilityClass;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import uk.nhs.cdss.reports.controllers.EncounterReportController;

@UtilityClass
public class XMLValidator {
  public static void validate(XmlObject document) throws TransformationException {
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
        message.append(error.getMessage()).append("\n");
      }

      try {
        message.append(EncounterReportController.prettyPrint(document));
      } catch (IOException e) {
        e.printStackTrace();
      }

      throw new ValidationException(message.toString());
    }
  }
}
