package uk.nhs.cdss.reports.transform.iucds;

import com.google.common.base.Preconditions;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlString.Factory;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PrimitiveType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.reports.constants.FHIRSystems;
import uk.nhs.cdss.reports.constants.IUCDSSystems;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.transform.iucds.constants.Template;
import uk.nhs.cdss.reports.util.IdentifierUtil;
import uk.nhs.connect.iucds.cda.ucr.IVLTS;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01AssignedEntity;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component2;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component3;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01EncompassingEncounter;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01EncounterParticipant;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01HealthCareFacility;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Location;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Place;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;
import uk.nhs.connect.iucds.cda.ucr.XEncounterParticipant;

@Service
@RequiredArgsConstructor
public class EncounterTransformer {

  private final UUIDProvider uuidProvider;

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

    Encounter encounter = input.getEncounter();

    POCDMT000002UK01Component1 componentOf = clinicalDocument.addNewComponentOf();

    Elements.addId(componentOf::addNewContentId,
        IUCDSSystems.NPFIT_CDA_CONTENT, Template.ENCOMPASSING_ENCOUNTER);

    POCDMT000002UK01EncompassingEncounter encompassingEncounter = componentOf
        .addNewEncompassingEncounter();

    Elements.addId(encompassingEncounter::addNewTemplateId,
        IUCDSSystems.TEMPLATE, Template.ENCOMPASSING_ENCOUNTER);

    if (encounter.hasEpisodeOfCare()) {
      Elements.addId(encompassingEncounter::addNewId,
          IUCDSSystems.NHS111_JOURNEY, encounter.getEpisodeOfCareFirstRep().getReference());
    }
    Elements.addId(encompassingEncounter::addNewId,
        IUCDSSystems.NHS111_ENCOUNTER, input.getSession().getEncounterRef().getReference());

    // TODO check encounter.category
    Elements.addCode(encompassingEncounter::addNewCode,
        "NONAC", IUCDSSystems.ACT_ENCOUNTER_CODE, "Virtual");

    IVLTS effectiveTime = encompassingEncounter.addNewEffectiveTime();
    if (encounter.hasPeriod()) {
      Period period = encounter.getPeriod();
      effectiveTime.addNewLow().setValue(Metadata.format(period.getStart()));
      if (period.hasEnd()) {
        effectiveTime.addNewHigh().setValue(Metadata.format(period.getEnd()));
      } else {
        effectiveTime.addNewHigh().setValue(Metadata.format(input.getDateOfPreparation()));
      }
    } else {
      effectiveTime.addNewLow().setValue(Metadata.format(input.getDateOfPreparation()));
      effectiveTime.addNewHigh().setValue(Metadata.format(input.getDateOfPreparation()));
    }

//    encompassingEncounter.setDischargeDispositionCode(...);

    buildLocation(encompassingEncounter, input);
    buildResponsibleParty(encompassingEncounter, input);
    buildEncounterParticipant(encompassingEncounter, input);
  }

  private void buildEncounterParticipant(POCDMT000002UK01EncompassingEncounter encompassingEncounter,
      EncounterReportInput input) {
    Encounter encounter = input.getEncounter();

    if (!encounter.hasParticipant()) {
      return;
    }

    for (EncounterParticipantComponent participant : encounter.getParticipant()) {
      Reference individual = participant.getIndividual();

      if (individual.getReferenceElement().getResourceType()
          .equals(ResourceType.Practitioner.name())) {
        POCDMT000002UK01EncounterParticipant encounterParticipant = encompassingEncounter
            .addNewEncounterParticipant();
        CodeableConcept code = participant.getTypeFirstRep();
        encounterParticipant.setTypeCode(
            XEncounterParticipant.Enum.forString(code.getCodingFirstRep().getCode()));

        POCDMT000002UK01AssignedEntity assignedEntity = encounterParticipant
            .addNewAssignedEntity();
        Elements.addId(assignedEntity::addNewId, uuidProvider.get());

        Practitioner practitioner = input.getSession().getPractitioner(individual);
        assignedEntity.addNewAssignedPerson()
            .addNewName()
            .set(Factory.newValue(practitioner.getNameFirstRep().getNameAsSingleString()));
      }
    }
  }

  private void buildResponsibleParty(
      POCDMT000002UK01EncompassingEncounter encompassingEncounter,
      EncounterReportInput input) {

    Encounter encounter = input.getEncounter();
    if (!encounter.hasEpisodeOfCare()) {
      return;
    }

    EpisodeOfCare episodeOfCare = input.getSession()
        .getEpisodeOfCare(encounter.getEpisodeOfCareFirstRep());

    // Even if careManager is present, scoping org is required
    if (!episodeOfCare.hasManagingOrganization()) {
      return;
    }

    POCDMT000002UK01AssignedEntity responsibleParty = encompassingEncounter
        .addNewResponsibleParty().addNewAssignedEntity();

    Elements
        .addId(responsibleParty::addNewId, uuidProvider.get());

    if (episodeOfCare.hasCareManager()) {
      Practitioner careManager = input.getSession().getPractitioner(episodeOfCare.getCareManager());
      POCDMT000002UK01Person person = responsibleParty.addNewAssignedPerson();
      person.addNewName()
          .set(XmlString.Factory.newValue(
              careManager.getNameFirstRep().getNameAsSingleString()));
    }
    if (episodeOfCare.hasManagingOrganization()) {
      Organization managingOrg = input.getSession()
          .getOrganization(episodeOfCare.getManagingOrganization());
      responsibleParty.addNewRepresentedOrganization()
          .addNewName().set(XmlString.Factory.newValue(managingOrg.getName()));
    }
  }

  private  POCDMT000002UK01Location buildLocation(
      POCDMT000002UK01EncompassingEncounter encompassingEncounter,
      EncounterReportInput input) {

    if (!input.getEncounter().hasLocation()) {
      return null;
    }
    Location location = input.getSession()
        .getLocation(input.getEncounter().getLocationFirstRep().getLocation());

    POCDMT000002UK01Location locationElement = encompassingEncounter.addNewLocation();
    POCDMT000002UK01HealthCareFacility facility = locationElement
        .addNewHealthCareFacility();

    // TODO use OID for ODS_SITE
    IdentifierUtil.getOdsSite(location).ifPresent(odsCode ->
        Elements.addId(facility::addNewId, FHIRSystems.ODS_SITE, odsCode));

    if (location.hasType()) {
      Coding locationType = location.getType().getCodingFirstRep();
      Preconditions.checkArgument(
          locationType.getSystem().equals(FHIRSystems.SERVICE_DELIVERY_LOCATION_ROLE_TYPE),
          "Expected location.type to have system "
              + FHIRSystems.SERVICE_DELIVERY_LOCATION_ROLE_TYPE);
      Elements.addCode(facility::addNewCode, IUCDSSystems.SERVICE_DELIVERY_LOCATION_ROLE_TYPE,
          locationType.getCode(), locationType.getDisplay());
    }

    if (location.hasName() || location.hasAddress()) {
      POCDMT000002UK01Place place = facility.addNewLocation();
      if (location.hasName()) {
        place.addNewName()
            .set(XmlString.Factory.newValue(location.getName()));
      }

      if (location.hasAddress()) {
        place.addNewAddr()
            .set(XmlString.Factory.newValue(formatAddress(location.getAddress())));
      }
    }

    if (input.getEncounter().hasServiceProvider()) {
      Organization organization = input.getSession()
          .getOrganization(input.getEncounter().getServiceProvider());
      POCDMT000002UK01Organization orgElement = facility.addNewServiceProviderOrganization();

      Elements.addId(orgElement::addNewId, organization.getId());
      IdentifierUtil.getOdsOrganization(organization)
          .ifPresent(odsId ->
              Elements.addId(orgElement::addNewId, IUCDSSystems.ODS_ORGANIZATION, odsId));

      orgElement.addNewName().set(XmlString.Factory.newValue(organization.getName()));

      if (organization.hasTelecom()) {
        for (ContactPoint contactPoint : organization.getTelecom()) {
          orgElement.addNewTelecom().setValue(contactPoint.getValue());
        }
      }
      if (organization.hasAddress()) {
        for (Address address : organization.getAddress()) {
          orgElement.addNewAddr().set(XmlString.Factory.newValue(formatAddress(address)));
        }
      }
      if (organization.hasType()) {
        CodeableConcept orgType = organization.getTypeFirstRep();

        // TODO map to correct code system
        Elements.addCode(orgElement::addNewStandardIndustryClassCode, orgType.getCodingFirstRep());
      }
    }

    return locationElement;
  }

  private static String formatAddress(Address address) {
    StringBuilder sb = new StringBuilder();
    sb.append(address.getLine().stream()
        .map(PrimitiveType::getValue)
        .collect(Collectors.joining(", ")));
    if (address.hasCity()) {
      sb.append(", ").append(address.getCity());
    }
    if (address.hasPostalCode()) {
      sb.append(", ").append(address.getPostalCode());
    }
    if (address.hasCountry()) {
      sb.append(", ").append(address.getCountry());
    }
    return sb.toString();
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
    section.addNewId().setRoot(uuidProvider.get());
  }
}
