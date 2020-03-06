package uk.nhs.cdss.reports.transform.iucds;

import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Period;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.transform.iucds.constants.OID;
import uk.nhs.cdss.reports.transform.iucds.constants.Template;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.IVLTS;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component2;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component3;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01EncompassingEncounter;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;

@UtilityClass
public class Encounter {

  /**
   * CDA Concept: EncompassingEncounter
   * <p>
   * Contains details of where the 111 incident took place, when it took place, the NHS 111
   * disposition code, 111 case reference
   *
   * @param clinicalDocument
   * @param input
   */
  void buildComponentOf(POCDMT000002UK01ClinicalDocument1 clinicalDocument,
      EncounterReportInput input) {

    POCDMT000002UK01Component1 componentOf = clinicalDocument.addNewComponentOf();

    Elements.addId(componentOf::addNewContentId,
        OID.NPFIT_CDA_CONTENT, Template.ENCOMPASSING_ENCOUNTER);

    POCDMT000002UK01EncompassingEncounter encompassingEncounter = componentOf
        .addNewEncompassingEncounter();

    Elements.addId(encompassingEncounter::addNewTemplateId,
        OID.TEMPLATE, Template.ENCOMPASSING_ENCOUNTER);

    Elements.addId(encompassingEncounter::addNewId,
        OID.NHS111_JOURNEY, "journey_id"); // TODO add other IDs

    CE code = encompassingEncounter.addNewCode();
    code.setCode("NHS111Encounter");
    code.setCodeSystem(OID.NHS111_ENCOUNTER);
    code.setDisplayName("NHS111 Encounter");

    // TODO review use of period
    IVLTS effectiveTime = encompassingEncounter.addNewEffectiveTime();
    if (input.getEncounter().hasPeriod()) {
      Period period = input.getEncounter().getPeriod();
      effectiveTime.addNewLow().setValue(Metadata.format(period.getStart()));
      effectiveTime.addNewHigh().setValue(Metadata.format(period.getEnd()));
    } else {
      effectiveTime.addNewLow().setValue(Metadata.format(input.getDateOfPreparation()));
      effectiveTime.addNewHigh().setValue(Metadata.format(input.getDateOfPreparation()));
    }
  }

  /**
   * A structuredBody contains one or more section elements, which in turn can contain a number of
   * other elements such as a code, title, and text. In this case, the text element may contain a
   * content element which is used to add further granularity to the text portion of the entry or to
   * add additional formatting information.
   *
   * @param clinicalDocument
   * @param input
   */
  void buildBody(POCDMT000002UK01ClinicalDocument1 clinicalDocument,
      EncounterReportInput input) {
    POCDMT000002UK01Component2 outerComponent = clinicalDocument.addNewComponent();

    POCDMT000002UK01StructuredBody structuredBody = outerComponent
        .addNewStructuredBody();

    POCDMT000002UK01Component3 bodyComponent = structuredBody
        .addNewComponent();

    POCDMT000002UK01Section section = bodyComponent.addNewSection();

    // TODO Where should ID be generated?
    section.addNewId().setRoot(UUID.randomUUID().toString().toUpperCase());
  }
}
