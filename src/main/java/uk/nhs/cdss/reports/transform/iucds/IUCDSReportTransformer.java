package uk.nhs.cdss.reports.transform.iucds;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import org.apache.xmlbeans.XmlString;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.constants.IUCDSSystems;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.transform.ReportXMLTransformer;
import uk.nhs.cdss.reports.transform.TransformationException;
import uk.nhs.cdss.reports.transform.XMLValidator;
import uk.nhs.cdss.reports.transform.ecds.Identifiers;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.itk.envelope.DistributionEnvelopeDocument;

@Service
@AllArgsConstructor
public class IUCDSReportTransformer implements ReportXMLTransformer {

  private final static String SERVICE_ID = "urn:nhs-itk:services:201005:SendNHS111Report-v3-0";
  private final static String PROFILE_ID = "urn:nhs-en:profile:nhs111CDADocument-v3-0";
  private final static String HANDLING_KEY = "urn:nhs-itk:ns:201005:interaction";
  private final static String CDA_MIMETYPE = "application/cda+xml";

  private final UUIDProvider uuidProvider;
  private final EncounterTransformer encounterTransformer;
  private final ParticipantTransformer participantTransformer;
  private final Identifiers identifiers;

  @Override
  public DistributionEnvelopeDocument transform(EncounterReportInput input)
      throws TransformationException {
    ClinicalDocumentDocument1 document = ClinicalDocumentDocument1.Factory.newInstance();
    buildMessage(document, input);

    XMLValidator.validate(document);

    return buildEnvelope(document);
  }

  private String getAuditUri() {
    return "urn:nhs-uk:identity:ods:" + identifiers.getSenderOdsOrganisation();
  }

  private String getSenderUri() {
    return String.format(
        "urn:nhs-uk:addressing:ods:%s:%s",
        identifiers.getSenderOdsOrganisation(),
        identifiers.getSenderIdentity());
  }

  private DistributionEnvelopeDocument buildEnvelope(ClinicalDocumentDocument1 document)
      throws TransformationException {
    var envelope = DistributionEnvelopeDocument.Factory.newInstance();
    var type = envelope.addNewDistributionEnvelope();
    var header = type.addNewHeader();

    header.setService(SERVICE_ID);
    header.setTrackingid(uuidProvider.get());
    header.addNewAuditIdentity().addNewId().setUri(getAuditUri());
    header.addNewSenderAddress().setUri(getSenderUri());

    var payloadId = "uuid_" + uuidProvider.get();

    var manifestList = header.addNewManifest();
    manifestList.setCount(BigInteger.ONE);
    var manifest = manifestList.addNewManifestitem();
    manifest.setMimetype(CDA_MIMETYPE);
    manifest.setId(payloadId);
    manifest.setProfileid(PROFILE_ID);

    var handlingSpec = header.addNewHandlingSpecification().addNewSpec();
    handlingSpec.setKey(HANDLING_KEY);
    // value varies depending on recipient
    handlingSpec.setValue("urn:nhs-itk:interaction:primaryOutofHourRecipientNHS111CDADocument-v3-0");

    var payloadList = type.addNewPayloads();
    payloadList.setCount(BigInteger.ONE);
    var payload = payloadList.addNewPayload();
    payload.set(document);
    payload.setId(payloadId);

    XMLValidator.validate(envelope);
    return envelope;
  }

  private void buildMessage(ClinicalDocumentDocument1 document, EncounterReportInput input) {
    POCDMT000002UK01ClinicalDocument1 clinicalDocument = buildClinicalDocument(document, input);

    Patient.buildRecordTarget(clinicalDocument, input);
    participantTransformer.transformParticipant(clinicalDocument, input);

    Metadata.buildAuthor(clinicalDocument, input);
    Metadata.buildCustodian(clinicalDocument, input);
    Metadata.buildInformationRecipient(clinicalDocument, input);
    Metadata.buildConsent(clinicalDocument, input);

    encounterTransformer.buildComponentOf(clinicalDocument, input);
    encounterTransformer.buildBody(clinicalDocument, input);
  }

  private POCDMT000002UK01ClinicalDocument1 buildClinicalDocument(
      ClinicalDocumentDocument1 document, EncounterReportInput input) {
    POCDMT000002UK01ClinicalDocument1 clinicalDocument = document.addNewClinicalDocument();

    CE code = clinicalDocument.addNewCode();
    code.setCodeSystem(IUCDSSystems.SNOMED);
    code.setCode("1066271000000101");

    // TODO determine correct confidentiality level
    code = clinicalDocument.addNewConfidentialityCode();
    code.setCodeSystem(IUCDSSystems.CONFIDENTIALITY);
    code.setCode("V");
    code.setDisplayName("very restricted");

    clinicalDocument.addNewEffectiveTime()
        .setValue(Metadata.format(input.getDateOfPreparation().getTime()));

    clinicalDocument.addNewId()
        .setRoot(uuidProvider.get());

    Elements.addId(clinicalDocument::addNewMessageType,
        IUCDSSystems.MESSAGE_TYPE, "POCD_RM200001GB02");

    // TODO Determine set this report belongs to
    clinicalDocument.addNewSetId()
        .setRoot(uuidProvider.get());

    clinicalDocument.addNewTitle()
        .set(XmlString.type.newValue("Integrated Urgent Care Report"));

    Elements.addId(clinicalDocument::addNewTypeId,
        IUCDSSystems.HL7_RMIMS, "POCD_HD000040");

    // TODO add document versioning if required
    clinicalDocument.addNewVersionNumber().setValue(BigInteger.ONE);
    return clinicalDocument;
  }
}
