package uk.nhs.cdss.reports.transform.iucds;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.controllers.EncounterReportController;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.transform.ReportXMLTransformer;
import uk.nhs.cdss.reports.transform.TransformationException;
import uk.nhs.cdss.reports.transform.ValidationException;
import uk.nhs.cdss.reports.transform.iucds.constants.OID;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;

@Service
@AllArgsConstructor
public class IUCDSReportTransformer implements ReportXMLTransformer {

  @Override
  public ClinicalDocumentDocument1 transform(EncounterReportInput input)
      throws TransformationException {
    ClinicalDocumentDocument1 document = ClinicalDocumentDocument1.Factory.newInstance();
    buildMessage(document, input);

    validate(document);

    return document;
  }

  private void buildMessage(ClinicalDocumentDocument1 document, EncounterReportInput input) {
    POCDMT000002UK01ClinicalDocument1 clinicalDocument = buildClinicalDocument(document, input);

    Patient.buildRecordTarget(clinicalDocument, input);
    Metadata.buildAuthor(clinicalDocument, input);
    Metadata.buildCustodian(clinicalDocument, input);
    Metadata.buildInformationRecipient(clinicalDocument, input);
    Metadata.buildConsent(clinicalDocument, input);

    Encounter.buildComponentOf(clinicalDocument, input);
    Encounter.buildBody(clinicalDocument, input);
  }

  private POCDMT000002UK01ClinicalDocument1 buildClinicalDocument(
      ClinicalDocumentDocument1 document, EncounterReportInput input) {
    POCDMT000002UK01ClinicalDocument1 clinicalDocument = document.addNewClinicalDocument();

    CE code = clinicalDocument.addNewCode();
    code.setCodeSystem(OID.SNOMED);
    code.setCode("1066271000000101");

    // TODO determine correct confidentiality level
    code = clinicalDocument.addNewConfidentialityCode();
    code.setCodeSystem(OID.CONFIDENTIALITY);
    code.setCode("V");
    code.setDisplayName("very restricted");

    clinicalDocument.addNewEffectiveTime()
        .setValue(Metadata.format(input.getDateOfPreparation().getTime()));

    clinicalDocument.addNewId()
        .setRoot(UUID.randomUUID().toString().toUpperCase());

    Elements.addId(clinicalDocument::addNewMessageType,
        OID.MESSAGE_TYPE, "POCD_RM200001GB02");

    // TODO Determine set this report belongs to
    clinicalDocument.addNewSetId()
        .setRoot(UUID.randomUUID().toString().toUpperCase());

    clinicalDocument.addNewTitle()
        .set(XmlString.type.newValue("Integrated Urgent Care Report"));

    Elements.addId(clinicalDocument::addNewTypeId,
        OID.HL7_RMIMS, "POCD_HD000040");

    // TODO add document versioning if required
    clinicalDocument.addNewVersionNumber().setValue(BigInteger.ONE);
    return clinicalDocument;
  }

  private void validate(XmlObject document) throws TransformationException {
    XmlOptions validateOptions = new XmlOptions();
    ArrayList<XmlError> errorList = new ArrayList<>();
    validateOptions.setErrorListener(errorList);

    // Validate the XML
    boolean isValid = document.validate(validateOptions);

    // If the XML isn't valid, loop through the listener's contents,
    // printing contained messages.
    if (!isValid) {
      StringBuilder message = new StringBuilder("Validation failed:\n");
      for (XmlError error : errorList) {
        message.append(error.getMessage()).append("\n");
      }

      try {
        System.out.println(message);
        System.out.println(EncounterReportController.prettyPrint(document));
      } catch (IOException e) {
      }
      throw new ValidationException(message.toString());
    }
  }
}
