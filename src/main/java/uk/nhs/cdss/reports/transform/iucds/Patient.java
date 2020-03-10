package uk.nhs.cdss.reports.transform.iucds;

import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.constants.IUCDSSystems;
import uk.nhs.cdss.reports.transform.iucds.constants.Template;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01AssociatedEntity;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Participant1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01PatientRole;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01RecordTarget;

@UtilityClass
public class Patient {

  /**
   * The recordTarget represents the person whose chart this document belongs to.  Typically this is
   * the patient who is also the subject of the report, although the subject can be a tissue sample,
   * fetus, etc.
   * <p>
   * A clinical document typically has exactly one recordTarget participant. In the uncommon case
   * where a clinical document (such as a group encounter note) is placed into more than one patient
   * chart, more than one recordTarget participants can be stated.
   * <p>
   * The recordTarget(s) of a document are stated in the header and propagate to nested content,
   * where they cannot be overridden.
   *
   * @param clinicalDocument
   * @param input
   */
  void buildRecordTarget(POCDMT000002UK01ClinicalDocument1 clinicalDocument,
      EncounterReportInput input) {

    POCDMT000002UK01RecordTarget recordTarget = clinicalDocument.addNewRecordTarget();

    Elements.addId(recordTarget::addNewContentId,
        IUCDSSystems.NPFIT_CDA_CONTENT, Template.PATIENT_ROLE);

    POCDMT000002UK01PatientRole patientRole = recordTarget.addNewPatientRole();

    if (input.getPatient() != null) {
      // TODO does patient have an NHS ID?
      Elements.addId(patientRole::addNewId, IUCDSSystems.LOCAL_PERSON,
          input.getPatient().getIdBase(), "EMS Test Harness");
    } else {
      Elements.addId(patientRole::addNewId, IUCDSSystems.LOCAL_PERSON,
          "Unknown", "EMS Test Harness");
    }

    // TODO add participant for referring organisation
    Encounter encounter = input.getEncounter();
    EncounterLocationComponent location =
        encounter == null ? null : encounter.getLocationFirstRep();
    if (location != null && location.getId() != null) {
      POCDMT000002UK01Participant1 participant = clinicalDocument.addNewParticipant();

      Elements.addId(participant::addNewContentId,
          IUCDSSystems.NPFIT_CDA_CONTENT, Template.ASSOCIATED_ENTITY);

      POCDMT000002UK01AssociatedEntity associatedEntity = participant.addNewAssociatedEntity();

      Elements.addId(associatedEntity::addNewTemplateId,
          IUCDSSystems.TEMPLATE, Template.ASSOCIATED_ENTITY);

      // If available, this is the ODS site code where the patient has been referred to
      Elements.addId(associatedEntity::addNewId,
          IUCDSSystems.SDS_SITE, location.getIdBase());

      buildScopingOrganization(associatedEntity, input);
    }
  }

  void buildScopingOrganization(POCDMT000002UK01AssociatedEntity associatedEntity,
      EncounterReportInput input) {

    POCDMT000002UK01Organization scopingOrganization = associatedEntity
        .addNewScopingOrganization();

    Elements.addId(scopingOrganization::addNewTemplateId,
        IUCDSSystems.TEMPLATE, Template.SCOPING_ORG);

    // TODO ID
    // TODO Name
  }
}
