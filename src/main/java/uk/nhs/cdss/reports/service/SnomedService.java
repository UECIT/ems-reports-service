package uk.nhs.cdss.reports.service;

import java.util.stream.Stream;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.constants.FHIRSystems;

/**
 * Stub implementation - this would normally query a SNOMED server to determine relationships
 */
@Component
public class SnomedService {

  public boolean isDescendantOf(String code, String ancestorCode) {
    return code.equals(ancestorCode);
  }

  public boolean isDescendantOf(CodeableConcept concept, String ancestorCode) {
    return snomedCodesIn(concept).anyMatch(code -> isDescendantOf(code, ancestorCode));
  }

  public boolean isMemberOf(String code, String refsetCode) {
    return code.equals(refsetCode);
  }

  public boolean isMemberOf(CodeableConcept concept, String refsetCode) {
    return snomedCodesIn(concept).anyMatch(code -> isMemberOf(code, refsetCode));
  }

  public Stream<String> snomedCodesIn(CodeableConcept concept) {
    return concept.getCoding()
        .stream()
        .filter(coding -> FHIRSystems.SNOMED.equals(coding.getSystem()))
        .map(Coding::getCode);
  }
}
