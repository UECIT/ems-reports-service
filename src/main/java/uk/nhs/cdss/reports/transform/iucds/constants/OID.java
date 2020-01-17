package uk.nhs.cdss.reports.transform.iucds.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OID {

  /**
   * Local Person Identifier
   */
  public String LOCAL_PERSON = "2.16.840.1.113883.2.1.3.2.4.18.24";

  /**
   * Attribute used to indicate the content (template) type of the following section in an NPfIT CDA
   * document. The attribute is intended as a structural navigation aid within the document and
   * carries no semantic information.
   */
  public String NPFIT_CDA_CONTENT = "2.16.840.1.113883.2.1.3.2.4.18.16";

  /**
   * Identifier of an organisation registered with the SDS
   */
  public String SDS_ORG = "2.16.840.1.113883.2.1.3.2.4.19.1";

  /**
   * Identifier of a site registered with the SDS
   */
  public String SDS_SITE = "2.16.840.1.113883.2.1.3.2.4.19.2";

  /**
   * Approved NPfIT number for all template IDs. Further refinements will be described by the
   * extension.
   */
  public String TEMPLATE = "2.16.840.1.113883.2.1.3.2.4.18.2";

  public String NHS111_JOURNEY = "2.16.840.1.113883.2.1.3.2.4.18.49";
  public String NHS111_ENCOUNTER = "2.16.840.1.113883.2.1.3.2.4.17.326";

  public String SNOMED = "2.16.840.1.113883.2.1.3.2.4.15";

  /**
   * Attribute used to indicate the HL7 message artefact id
   */
  public String MESSAGE_TYPE = "2.16.840.1.113883.2.1.3.2.4.18.17";

  public String CONFIDENTIALITY = "2.16.840.1.113883.1.11.16926";

  /**
   * Health Level 7 (HL7) registered Refined Message Information Models (RMIMs)
   */
  public String HL7_RMIMS = "2.16.840.1.113883.1.3";
}
