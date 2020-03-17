package uk.nhs.cdss.reports.transform.iucds;

import java.util.List;
import java.util.Optional;
import org.apache.xmlbeans.XmlString.Factory;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Organization.OrganizationContactComponent;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.constants.IUCDSSystems;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.util.IdentifierUtil;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01AssociatedEntity;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Participant1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;

@Component
public class ParticipantTransformer {

  public void transformParticipant(POCDMT000002UK01ClinicalDocument1 clinicalDocument,
      EncounterReportInput input) {

    Encounter encounter = input.getEncounter();
    if (!encounter.hasServiceProvider()) {
      return;
    }

    Organization organization = input.getSession().getOrganization(encounter.getServiceProvider());
    POCDMT000002UK01Participant1 participant = clinicalDocument
        .addNewParticipant();

    participant.setTypeCode("CALLBCK");

    POCDMT000002UK01AssociatedEntity associatedEntity =
        buildAssociatedEntity(organization, participant);

    getPhoneNumber(organization.getTelecom()).ifPresent(phone ->
          associatedEntity.addNewTelecom().setValue("tel:" + phone));

    // Assume associatedperson = organization.contact - would search for one with the right code
    if (organization.hasContact()) {
      buildAssociatedPerson(associatedEntity, organization.getContactFirstRep());
    }
    buildScopingOrganization(associatedEntity, organization);
  }

  private POCDMT000002UK01AssociatedEntity buildAssociatedEntity(Organization organization,
      POCDMT000002UK01Participant1 participant) {
    POCDMT000002UK01AssociatedEntity associatedEntity = participant.addNewAssociatedEntity();

    // If available, this is the ODS site code where the patient has been referred to
    IdentifierUtil.getOdsSite(organization)
        .ifPresent(siteCode -> Elements.addId(associatedEntity::addNewId,
            IUCDSSystems.SDS_SITE, siteCode));

    if (organization.hasType() && organization.getTypeFirstRep().hasCoding()) {
      Elements.addCode(
          associatedEntity::addNewCode,
          organization.getTypeFirstRep().getCodingFirstRep());
    }

    associatedEntity.setClassCode("ASSIGNED");
    return associatedEntity;
  }

  private Optional<String> getPhoneNumber(List<ContactPoint> telecoms) {
    return telecoms.stream()
        .filter(telecom -> telecom.getUse().equals(ContactPointUse.WORK))
        .findAny()
        .map(ContactPoint::getValue);
  }

  private void buildAssociatedPerson(POCDMT000002UK01AssociatedEntity associatedEntity,
      OrganizationContactComponent contactFirstRep) {

    POCDMT000002UK01Person associatedPerson = associatedEntity.addNewAssociatedPerson();
    associatedPerson.addNewName()
        .set(Factory.newValue(contactFirstRep.getName().getNameAsSingleString()));
  }

  private void buildScopingOrganization(POCDMT000002UK01AssociatedEntity associatedEntity,
      Organization organization) {

    POCDMT000002UK01Organization scopingOrganization = associatedEntity
        .addNewScopingOrganization();

    IdentifierUtil.getOdsOrganization(organization).ifPresent(code ->
      Elements.addId(scopingOrganization::addNewId, IUCDSSystems.ODS_ORGANIZATION, code)
    );

    scopingOrganization.addNewName()
        .set(Factory.newValue(organization.getName()));
  }

}
