package uk.nhs.cdss.reports.util;

import java.util.Collection;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import uk.nhs.cdss.reports.constants.FHIRSystems;

@UtilityClass
public class IdentifierUtil {

  public Optional<String> getOdsOrganization(Organization organization) {
    return getIdForSystem(organization.getIdentifier(), FHIRSystems.ODS_ORGANIZATION);
  }

  public Optional<String> getOdsOrganization(Practitioner practitioner) {
    return getIdForSystem(practitioner.getIdentifier(), FHIRSystems.ODS_ORGANIZATION);
  }

  public Optional<String> getOdsSite(Location location) {
    return getIdForSystem(location.getIdentifier(), FHIRSystems.ODS_SITE);
  }

  public Optional<String> getIdForSystem(Collection<Identifier> identifiers, String system) {
    return identifiers.stream()
        .filter(identifier -> identifier.getSystem().equals(system))
        .findFirst()
        .map(Identifier::getValue);
  }

}
