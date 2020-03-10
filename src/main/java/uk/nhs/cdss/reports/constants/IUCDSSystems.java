package uk.nhs.cdss.reports.constants;

import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Coding;

@UtilityClass
public class IUCDSSystems {

  /**
   * ActStatus HL7 codes
   */
  public final String ACT_STATUS = "2.16.840.1.113883.5.14";

  public final String ACT_CODE = "2.16.840.1.113883.5.4";
  public final String ACT_CONSENT_TYPE = "2.16.840.1.113883.1.11.19897";
  public final String ACT_ENCOUNTER_CODE = "2.16.840.1.113883.1.11.13955";

  /**
   * Local Person Identifier
   */
  public final String LOCAL_PERSON = "2.16.840.1.113883.2.1.3.2.4.18.24";

  /**
   * Attribute used to indicate the content (template) type of the following section in an NPfIT CDA
   * document. The attribute is intended as a structural navigation aid within the document and
   * carries no semantic information.
   */
  public final String NPFIT_CDA_CONTENT = "2.16.840.1.113883.2.1.3.2.4.18.16";

  /**
   * Identifier of an organisation registered with the SDS
   */
  public final String SDS_ORG = "2.16.840.1.113883.2.1.3.2.4.19.1";

  /**
   * Identifier of a site registered with the SDS
   */
  public final String SDS_SITE = "2.16.840.1.113883.2.1.3.2.4.19.2";

  public final String ODS_ORGANIZATION = "2.16.840.1.113883.2.1.4.3";

  /**
   * Approved NPfIT number for all template IDs. Further refinements will be described by the
   * extension.
   */
  public final String TEMPLATE = "2.16.840.1.113883.2.1.3.2.4.18.2";

  public final String NHS111_JOURNEY = "2.16.840.1.113883.2.1.3.2.4.18.49";
  public final String NHS111_ENCOUNTER = "2.16.840.1.113883.2.1.3.2.4.17.326";

  public final String SNOMED = "2.16.840.1.113883.2.1.3.2.4.15";

  /**
   * Attribute used to indicate the HL7 message artefact id
   */
  public final String MESSAGE_TYPE = "2.16.840.1.113883.2.1.3.2.4.18.17";

  public final String CONFIDENTIALITY = "2.16.840.1.113883.1.11.16926";

  /**
   * Health Level 7 (HL7) registered Refined Message Information Models (RMIMs)
   */
  public final String HL7_RMIMS = "2.16.840.1.113883.1.3";

  public final String SERVICE_DELIVERY_LOCATION_ROLE_TYPE = "2.16.840.1.113883.1.11.17660";
}
