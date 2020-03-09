package uk.nhs.cdss.reports.transform.ecds;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.controllers.EncounterReportController;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.CounterService;
import uk.nhs.cdss.reports.transform.ReportXMLTransformer;
import uk.nhs.cdss.reports.transform.TransformationException;
import uk.nhs.cdss.reports.transform.ValidationException;
import uk.nhs.nhsia.datastandards.ecds.CDSActivityDateType;
import uk.nhs.nhsia.datastandards.ecds.CDSApplicableDateType;
import uk.nhs.nhsia.datastandards.ecds.CDSApplicableTimeType;
import uk.nhs.nhsia.datastandards.ecds.CDSInterchangeDateOfPreparationType;
import uk.nhs.nhsia.datastandards.ecds.CDSInterchangeHeaderStructure;
import uk.nhs.nhsia.datastandards.ecds.CDSInterchangeStructure.CDSNetChangeAllMessageTypes;
import uk.nhs.nhsia.datastandards.ecds.CDSInterchangeTimeOfPreparationType;
import uk.nhs.nhsia.datastandards.ecds.CDSInterchangeTrailerStructure;
import uk.nhs.nhsia.datastandards.ecds.CDSMessageHeaderStructure;
import uk.nhs.nhsia.datastandards.ecds.CDSMessageTrailerStructure;
import uk.nhs.nhsia.datastandards.ecds.CDSMessageTypeType;
import uk.nhs.nhsia.datastandards.ecds.CDSMessageVersionNumberType;
import uk.nhs.nhsia.datastandards.ecds.CDSProtocolIdentifierCodeType;
import uk.nhs.nhsia.datastandards.ecds.CDSTransactionHeaderNetChangeStructure;
import uk.nhs.nhsia.datastandards.ecds.CDSTypeCodeType;
import uk.nhs.nhsia.datastandards.ecds.CDSUpdateTypeType;
import uk.nhs.nhsia.datastandards.ecds.CDSXMLInterchangeDocument;
import uk.nhs.nhsia.datastandards.ecds.CDSXMLInterchangeDocument.CDSXMLInterchange;

@Component
@RequiredArgsConstructor
public class ECDSReportTransformer implements ReportXMLTransformer {

  private static final long MAX_CONTROL_REF = 9999999L;

  private final CounterService counterService;
  private final EmergencyCareTransformer emergencyCareTransformer;
  private final Identifiers identifiers;

  @Override
  public CDSXMLInterchangeDocument transform(EncounterReportInput input)
      throws TransformationException {

    Preconditions.checkNotNull(input.getEncounter(), "No encounter");

    CDSXMLInterchangeDocument document = CDSXMLInterchangeDocument.Factory.newInstance();
    CDSXMLInterchange interchange = document.addNewCDSXMLInterchange();
    interchange.setSchemaVersion("6-2-2");
    interchange.xsetSchemaDate(XmlDate.Factory.newValue("2012-05-11"));

    CDSInterchangeHeaderStructure header = buildInterchangeHeader(interchange, input);
    buildMessage(interchange, input, header);
    buildInterchangeTrailer(interchange, header);

    validate(document);

    return document;
  }

  private CDSInterchangeHeaderStructure buildInterchangeHeader(CDSXMLInterchange interchange,
      EncounterReportInput input) {
    CDSInterchangeHeaderStructure header = interchange.addNewCDSInterchangeHeader();

    // Where an Organisation acts on behalf of another NHS Organisation, care must be taken to ensure the correct use of the identity.
    // For data submitted to the service, the CDS INTERCHANGE SENDER IDENTITY is the Electronic Data Interchange (EDI) address of the sending site.
    header.setCDSInterchangeSenderIdentity(identifiers.getSenderIdentity());

    // All Commissioning Data Set XML Schema interchanges submitted must contain the CDS INTERCHANGE RECEIVER IDENTITY of the Secondary Uses Service.
    header.setCDSInterchangeReceiverIdentity(identifiers.getReceiverIdentity());

    // For each Interchange submitted, the CDS INTERCHANGE CONTROL REFERENCE must be incremented by 1. The maximum value supported is n7 and wrap around from 9999999 to 1 must be supported.
    var next = counterService.incrementAndGetCounter(ECDSCounters.INTERCHANGE_CONTROL_REFERENCE);
    header.setCDSInterchangeControlReference(String.format("%07d", next % MAX_CONTROL_REF));

    header.xsetCDSInterchangeDateOfPreparation(DateTimeFormatter.formatDate(
        input.getDateOfPreparation(),
        CDSInterchangeDateOfPreparationType.type));
    header.xsetCDSInterchangeTimeOfPreparation(DateTimeFormatter.formatTime(
        input.getDateOfPreparation(),
        CDSInterchangeTimeOfPreparationType.type));

    // This facility enables submitted interchanges to be marked to enable interchange content to be identified and recorded.
    header.setCDSInterchangeApplicationReference("NHSCDS");

    // This optional test facility enables interchanges submitted to be marked and therefore processed as Test or Production data.
    header.setCDSInterchangeTestIndicator("1");

    return header;
  }

  private void buildMessage(CDSXMLInterchange interchange, EncounterReportInput input,
      CDSInterchangeHeaderStructure interchangeHeader) {
    CDSNetChangeAllMessageTypes message = interchange.addNewCDSNetChangeAllMessageTypes();

    // Required
    message.setCDSTypeCode(CDSTypeCodeType.X_011);
    message.setCDSProtocolIdentifierCode(CDSProtocolIdentifierCodeType.X_010);

    CDSMessageHeaderStructure header = message.addNewCDSMessageHeader();

    // Required
    header.setCDSMessageType(CDSMessageTypeType.NHSCDS);
    header.setCDSMessageVersionNumber(CDSMessageVersionNumberType.CDS_062);
    header.setCDSMessageReferenceNumber(1);

    var controlRef = interchangeHeader.getCDSInterchangeControlReference();
    var messageRef = String.format("%07d", header.getCDSMessageReferenceNumber());
    header.setCDSMessageRecordIdentifier(identifiers.getSenderOdsOrganisation() + "  " + controlRef + messageRef);

    // Message body
    // Required elements
    addNetChangeHeader(message, input, interchangeHeader);

    message.setEmergencyCare(emergencyCareTransformer.transform(input));

    CDSMessageTrailerStructure trailer = message.addNewCDSMessageTrailer();

    // Required
    trailer.setCDSMessageReferenceNumber(header.getCDSMessageReferenceNumber());
  }

  private void addNetChangeHeader(CDSNetChangeAllMessageTypes message,
      EncounterReportInput input,
      CDSInterchangeHeaderStructure interchangeHeader) {
    CDSTransactionHeaderNetChangeStructure header = message.addNewCDSTransactionHeaderNetChange();

    // 010 --> A&E Attendance
    // 011 --> Emergency Care Attendance
    header.setCDSTypeCode(CDSTypeCodeType.X_011);

    // 010 --> Net Change Update Mechanism
    header.setCDSProtocolIdentifierCode(CDSProtocolIdentifierCodeType.X_010);

    // TODO populate from encounter
    Long encounterId = input.getEncounter().getIdElement().getIdPartAsLong();
    header.setCDSUniqueIdentifier(
        "B" + identifiers.getSenderOdsOrganisation() + String.format("%07d", encounterId));

    // 9 --> Original or Replacement
    header.setCDSUpdateType(CDSUpdateTypeType.X_9);

    header.xsetCDSApplicableDate(DateTimeFormatter.formatDate(
        input.getDateOfPreparation(),
        CDSApplicableDateType.type));
    header.xsetCDSApplicableTime(DateTimeFormatter.formatTime(
        input.getDateOfPreparation(),
        CDSApplicableTimeType.type));

    // TODO populate from encounter
    header.xsetCDSActivityDate(
        DateTimeFormatter.formatDate(input.getDateOfPreparation(), CDSActivityDateType.type));

    // CDS SENDER IDENTITY is the mandatory NHS ORGANISATION CODE of the Organisation acting as the physical Sender of Commissioning Data Set submissions.
    header
        .setOrganisationCodeCDSSenderIdentity(interchangeHeader.getCDSInterchangeSenderIdentity());

    // CDS PRIME RECIPIENT IDENTITY is the mandatory NHS ORGANISATION CODE (or valid Organisation Data Service Default Code) representing the Organisation
    // determined to be the Commissioning Data Set Prime Recipient of the Commissioning Data Set Message as indicated in the Commissioning Data Set Addressing Grid.
    // TODO populate from encounter
    header.setOrganisationCodeCDSPrimeRecipientIdentity("120000000000");
  }

  private void buildInterchangeTrailer(CDSXMLInterchange interchange,
      CDSInterchangeHeaderStructure header) {
    CDSInterchangeTrailerStructure trailer = interchange.addNewCDSInterchangeTrailer();

    // Required
    trailer.setCDSInterchangeControlReference(header.getCDSInterchangeControlReference());
    trailer.setCDSInterchangeControlCount(1);
  }

  private void validate(CDSXMLInterchangeDocument document) throws TransformationException {
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
