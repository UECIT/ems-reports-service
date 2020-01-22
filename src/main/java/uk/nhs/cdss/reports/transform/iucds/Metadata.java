package uk.nhs.cdss.reports.transform.iucds;

import java.util.Date;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.xmlbeans.XmlString;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.transform.iucds.constants.ClassCode;
import uk.nhs.cdss.reports.transform.iucds.constants.DeterminerCode;
import uk.nhs.cdss.reports.transform.iucds.constants.OID;
import uk.nhs.cdss.reports.transform.iucds.constants.Template;
import uk.nhs.cdss.reports.transform.iucds.constants.TypeCode;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.ON;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01AssignedAuthor;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01AssignedCustodian;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Author;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Custodian;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01CustodianOrganization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01InformationRecipient;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01IntendedRecipient;
import uk.nhs.connect.iucds.cda.ucr.XInformationRecipientRoleX;
import uk.nhs.connect.iucds.cda.ucr.XInformationRecipientX;
import uk.nhs.connect.npfit.hl7.localisation.TemplateContent;

@UtilityClass
public class Metadata {

  FastDateFormat DATETIME_FORMAT = FastDateFormat
      .getInstance("yyyyMMddHHmmssZ");

  public static String format(Date date) {
    return DATETIME_FORMAT.format(date);
  }

  /**
   * The author element represents the humans and/or machines that authored the document. Note that
   * author, by inclusion, contains required child elements: time and assignedAuthor.
   * <p>
   * assignedAuthor requires an id. The assignedPerson, and representedOrganization are optional.
   * <p>
   * assignedPerson contains a name, which in turn has child elements given, family and suffix.
   * <p>
   * If present, the optional element representedOrganization contains a child element id and may
   * have an optional name element.
   * <p>
   * There can be one or more authors identified in the header. Their authorship applies to the full
   * document unless overridden.
   *
   * @param clinicalDocument
   * @param input
   */
  void buildAuthor(POCDMT000002UK01ClinicalDocument1 clinicalDocument,
      EncounterReportInput input) {
    POCDMT000002UK01Author author = clinicalDocument.addNewAuthor();
    author.setTypeCode("AUT");
    author.setContextControlCode("OP");

    TemplateContent contentId = author.addNewContentId();
    contentId.setRoot(OID.NPFIT_CDA_CONTENT);
    contentId.setExtension("NPFIT-000081#Role"); // TODO

    CE functionCode = author.addNewFunctionCode();
    functionCode.setCode("OA");
    functionCode.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.17.178");
    functionCode.setDisplayName("Originating Author");

    author.addNewTime().setValue(DATETIME_FORMAT.format(new Date()));

    POCDMT000002UK01AssignedAuthor assignedAuthor = author.addNewAssignedAuthor();
    assignedAuthor.setClassCode(ClassCode.ASSIGNED);

    Elements.addId(assignedAuthor::addNewId, OID.LOCAL_PERSON,
        "author_id", "author_org"); // TODO
  }

  /**
   * The custodian element represents the organization that is in charge of maintaining the
   * document. The custodian is the steward that is entrusted with the care of the document. Every
   * CDA document has exactly one custodian.
   * <p>
   * Note that custodian, by inclusion, contains required child element assignedCustodian.
   * <p>
   * assignedCustodian requires a representedCustodianOrganization, which requires an id and may
   * have an optional name element.
   *
   * @param clinicalDocument
   * @param input
   */
  void buildCustodian(POCDMT000002UK01ClinicalDocument1 clinicalDocument,
      EncounterReportInput input) {
    POCDMT000002UK01Custodian custodian = clinicalDocument.addNewCustodian();
    custodian.setTypeCode(TypeCode.CST);

    Elements.addId(custodian::addNewContentId,
        OID.NPFIT_CDA_CONTENT, Template.ASSIGNED_CUSTODIAN);

    POCDMT000002UK01AssignedCustodian assignedCustodian = custodian.addNewAssignedCustodian();
    assignedCustodian.setClassCode(ClassCode.ASSIGNED);

    Elements.addId(assignedCustodian::addNewTemplateId,
        OID.TEMPLATE, Template.ASSIGNED_CUSTODIAN);

    POCDMT000002UK01CustodianOrganization organization = assignedCustodian
        .addNewRepresentedCustodianOrganization();
    organization.setClassCode(ClassCode.ORG);
    organization.setDeterminerCode(DeterminerCode.INSTANCE);

    Elements.addId(organization::addNewTemplateId,
        OID.TEMPLATE, Template.REPRESENTED_CUSTODIAN_ORGANIZATION);

    // TODO SDS_SITE or SDS_ORG
    // Either the ODS site (preferred), or ODS organisation code of the custodian organisation
    Elements.addId(organization::addNewId,
        OID.SDS_SITE, "site_id");

    ON orgName = organization.addNewName();
    orgName.set(XmlString.Factory.newValue("Custodian Org Name")); // TODO
  }

  /**
   * CDA Concept: recipient – includes both information recipient (i.e. primary recipient) and
   * trackers (i.e. copy to recipient)
   * <p>
   * Recipient details – This document assumes that recipients either organisations (such as an A&E
   * department, GP Practice, OOH OR a person such as a GP)
   *
   * @param clinicalDocument
   * @param input
   */
  void buildInformationRecipient(POCDMT000002UK01ClinicalDocument1 clinicalDocument,
      EncounterReportInput input) {
    POCDMT000002UK01InformationRecipient informationRecipient = clinicalDocument
        .addNewInformationRecipient();
    informationRecipient.setTypeCode(XInformationRecipientX.PRCP);

    // varies depending on whether the receiver is an org or person
    Elements.addId(informationRecipient::addNewContentId,
        OID.NPFIT_CDA_CONTENT, Template.INTENDED_RECIPIENT_ORG);

    POCDMT000002UK01IntendedRecipient intendedRecipient = informationRecipient
        .addNewIntendedRecipient();
    intendedRecipient.setClassCode(XInformationRecipientRoleX.ASSIGNED);

    // TODO received organisation?

  }
}