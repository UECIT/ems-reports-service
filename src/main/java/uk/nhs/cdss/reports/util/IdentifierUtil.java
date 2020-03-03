package uk.nhs.cdss.reports.util;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import uk.nhs.cdss.reports.constants.Systems;

@UtilityClass
public class IdentifierUtil {

  public Optional<String> getOdsCode(Organization organization) {
    return getOdsCode(organization.getIdentifier());
  }

  public Optional<String> getOdsCode(Practitioner practitioner) {
    return getOdsCode(practitioner.getIdentifier());
  }

  public Optional<String> getOdsCode(Collection<Identifier> identifiers) {
    return identifiers.stream()
        .filter(identifier -> identifier.getSystem().equals(Systems.ODS))
        .findFirst()
        .map(Identifier::getValue);
  }

}
