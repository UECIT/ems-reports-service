package uk.nhs.cdss.reports.transform;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.UUID;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlOptions;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.nhsia.datastandards.ecds.AN2ECType;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.EmergencyCareAttendanceActivityCharacteristics;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.ServiceAgreementDetails;
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
import uk.nhs.nhsia.datastandards.ecds.EmergencyCareStucture;
import uk.nhs.nhsia.datastandards.ecds.EmergencyCareStucture.EmergencyCareAttendanceLocation;
import uk.nhs.nhsia.datastandards.ecds.NHSNumberStatusIndicatorCodeUnverifiedType;
import uk.nhs.nhsia.datastandards.ecds.NHSNumberStatusIndicatorCodeWithheldType;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.UnverifiedIdentityStructure;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.UnverifiedIdentityStructure.DataElementStructure;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.UnverifiedIdentityStructure.DataElementStructure2;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.WithheldIdentityStructure;
import uk.nhs.nhsia.datastandards.ecds.PersonGroupPatientECStructure;
import uk.nhs.nhsia.datastandards.ecds.PersonNameStructure.PersonNameStructured;
import uk.nhs.nhsia.datastandards.ecds.WithheldIdentityReasonType;

@Component
public class ECDSReportTransformer implements ReportXMLTransformer<EncounterReportInput> {

  public static final String TIME_FORMAT = "HH:mm:ss";
  public static final String DATE_FORMAT = "yyyy-MM-dd";
  private long controlRef = 0;
  private long attendanceRef = 0;

  @Override
  public String transform(EncounterReportInput input) {
    CDSXMLInterchangeDocument document = CDSXMLInterchangeDocument.Factory.newInstance();
    CDSXMLInterchange interchange = document.addNewCDSXMLInterchange();
    interchange.setSchemaVersion("6-2-2");
    interchange.xsetSchemaDate(XmlDate.Factory.newValue("2012-05-11"));

    CDSInterchangeHeaderStructure header = buildInterchangeHeader(interchange, input);
    buildMessage(interchange, input, header);
    buildInterchangeTrailer(interchange, input, header);

    document.validate();

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

  private CDSInterchangeHeaderStructure buildInterchangeHeader(CDSXMLInterchange interchange,
      EncounterReportInput input) {
    CDSInterchangeHeaderStructure header = interchange.addNewCDSInterchangeHeader();

    // Where an Organisation acts on behalf of another NHS Organisation, care must be taken to ensure the correct use of the identity.
    // For data submitted to the service, the CDS INTERCHANGE SENDER IDENTITY is the Electronic Data Interchange (EDI) address of the sending site.
    header.setCDSInterchangeSenderIdentity("EMS");

    // All Commissioning Data Set XML Schema interchanges submitted must contain the CDS INTERCHANGE RECEIVER IDENTITY of the Secondary Uses Service.
    header.setCDSInterchangeReceiverIdentity("SUS");

    // For each Interchange submitted, the CDS INTERCHANGE CONTROL REFERENCE must be incremented by 1. The maximum value supported is n7 and wrap around from 9999999 to 1 must be supported.
    header.setCDSInterchangeControlReference(nextControlRef());

    header.xsetCDSInterchangeDateOfPreparation(CDSInterchangeDateOfPreparationType.Factory
        .newValue(formatDate(input, DATE_FORMAT)));
    header.xsetCDSInterchangeTimeOfPreparation(CDSInterchangeTimeOfPreparationType.Factory
        .newValue(formatDate(input, TIME_FORMAT)));

    // This facility enables submitted interchanges to be marked to enable interchange content to be identified and recorded.
    header.setCDSInterchangeApplicationReference(UUID.randomUUID().toString());

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

    // Message body
    // Required elements
    addNetChangeHeader(message, input, interchangeHeader);
    EmergencyCareStucture emergencyCare = message.addNewEmergencyCare();
    addPatientInformation(emergencyCare, input);
    addAttendance(emergencyCare, input);

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
    header.setCDSUniqueIdentifier(UUID.randomUUID().toString());

    // 9 --> Original or Replacement
    header.setCDSUpdateType(CDSUpdateTypeType.X_9);

    header.xsetCDSApplicableDate(CDSApplicableDateType.Factory
        .newValue(formatDate(input, DATE_FORMAT)));
    header.xsetCDSApplicableTime(CDSApplicableTimeType.Factory
        .newValue(formatDate(input, TIME_FORMAT)));

    // TODO populate from encounter
    header.setCDSActivityDate(Calendar.getInstance());

    // CDS SENDER IDENTITY is the mandatory NHS ORGANISATION CODE of the Organisation acting as the physical Sender of Commissioning Data Set submissions.
    header
        .setOrganisationCodeCDSSenderIdentity(interchangeHeader.getCDSInterchangeSenderIdentity());

    // CDS PRIME RECIPIENT IDENTITY is the mandatory NHS ORGANISATION CODE (or valid Organisation Data Service Default Code) representing the Organisation
    // determined to be the Commissioning Data Set Prime Recipient of the Commissioning Data Set Message as indicated in the Commissioning Data Set Addressing Grid.
    // TODO populate from encounter
    header.setOrganisationCodeCDSPrimeRecipientIdentity("Residence Responsibility");
  }

  private String formatDate(EncounterReportInput input, String s) {
    return DateFormatUtils.format(input.getDateOfPreparation(), s);
  }

  private void addPatientInformation(EmergencyCareStucture emergencyCare,
      EncounterReportInput input) {
    PersonGroupPatientECStructure patientStructure = emergencyCare.addNewPersonGroupPatient();

    // Required
    PatientIdentity patientIdentity = patientStructure.addNewPatientIdentity();

    // TODO if we know the NHS number of the patient we can create a verified identity
    // VERIFIED IDENTITY STRUCTURE
    // Must be used where the NHS NUMBER STATUS INDICATOR CODE National Code = 01 (Number present and verified)

    Patient patient = input.getPatient();
    if (patient == null) {
      // WITHHELD IDENTITY STRUCTURE
      // Must be used where the Commissioning Data Set record has been anonymised
      WithheldIdentityStructure id = patientIdentity.addNewWithheldIdentityStructure();

      // 07 -> Number not present and trace not required
      // SCHEMA: requires both the attribute and the element to be present?
      id.setNHSNumberStatusIndicatorCode2(NHSNumberStatusIndicatorCodeWithheldType.X_07);
      id.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeWithheldType.X_07);

      // 97 -> Record anonymised for other reason
      id.setWithheldIdentityReason(WithheldIdentityReasonType.X_97);

    } else {
      // UNVERIFIED IDENTITY STRUCTURE
      // Must be used for all other values of the NHS NUMBER STATUS INDICATOR CODE NOT included in the above
      UnverifiedIdentityStructure id = patientIdentity.addNewUnverifiedIdentityStructure();
      DataElementStructure dataElement = id.addNewDataElementStructure();

      // 03 -> Trace required
      // SCHEMA: XML Schema has use=required for the attribute on the id, but also requires the element in the dataElement
      id.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeUnverifiedType.X_03);
      dataElement.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeUnverifiedType.X_03);

      PersonNameStructured name = dataElement.addNewPatientName().addNewPersonNameStructured();
      HumanName nameFirstRep = patient.getNameFirstRep();
      name.setPersonGivenName(nameFirstRep.getGivenAsSingleString());
      name.setPersonFamilyName(nameFirstRep.getFamily());

      if (patient.hasBirthDate()) {
        DataElementStructure2 dataElement2 = id.addNewDataElementStructure2();
        dataElement2.setPersonBirthDate(DateUtils.toCalendar(patient.getBirthDate()));
      }
    }
  }

  private void addAttendance(EmergencyCareStucture emergencyCare, EncounterReportInput input) {
    EmergencyCareAttendanceLocation location = emergencyCare
        .addNewEmergencyCareAttendanceLocation();

    // Required
    location.setOrganisationSiteIdentifierOfTreatment("Test ED");

    // 01 -> Emergency departments
    location.setEmergencyCareDepartmentType(AN2ECType.X_01);

    AttendanceOccurrenceECStructure attendanceStructure = emergencyCare
        .addNewAttendanceOccurrence();

    // Required
    EmergencyCareAttendanceActivityCharacteristics activityCharacteristics = attendanceStructure
        .addNewEmergencyCareAttendanceActivityCharacteristics();
    activityCharacteristics.setEmergencyCareAttendanceIdentifier(Long.toString(++attendanceRef));

    activityCharacteristics.setEmergencyCareArrivalDate(input.getDateOfPreparation()); // TODO
    activityCharacteristics.setEmergencyCareArrivalTime(input.getDateOfPreparation()); // TODO
    activityCharacteristics.setAgeAtCdsActivityDate(20); // TODO

    ServiceAgreementDetails serviceAgreement = attendanceStructure
        .addNewServiceAgreementDetails();

    serviceAgreement.setOrganisationIdentifierCodeOfProvider("provider"); // TODO
    serviceAgreement.setOrganisationIdentifierCodeOfCommissioner("commissioner"); // TODO

    // TODO populate from encounter
  }


  private void buildInterchangeTrailer(CDSXMLInterchange interchange,
      EncounterReportInput input,
      CDSInterchangeHeaderStructure header) {
    CDSInterchangeTrailerStructure trailer = interchange.addNewCDSInterchangeTrailer();

    // Required
    trailer.setCDSInterchangeControlReference(header.getCDSInterchangeControlReference());
    trailer.setCDSInterchangeControlCount(1);
  }

  private String nextControlRef() {
    controlRef++;
    if (controlRef >= 9999999) {
      controlRef = 1;
    }

    return Long.toString(controlRef);
  }
}
