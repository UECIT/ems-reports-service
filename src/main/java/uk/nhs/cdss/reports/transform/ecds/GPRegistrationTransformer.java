package uk.nhs.cdss.reports.transform.ecds;

import static com.mysql.cj.util.StringUtils.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;
import java.util.Optional;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.FhirSession;
import uk.nhs.cdss.reports.util.IdentifierUtil;
import uk.nhs.cdss.reports.util.ReferenceUtil;
import uk.nhs.nhsia.datastandards.ecds.GPRegistrationStructure;
import uk.nhs.nhsia.datastandards.ecds.GPRegistrationStructure.Factory;

@Component
public class GPRegistrationTransformer {

  private static final String PPD_CODE_NOT_KNOWN = "G9999998";
  private static final String GP_PRACTICE_NOT_KNOWN = "V81999";
  private static final String NO_REGISTERED_GP = "V81997";

  public Optional<GPRegistrationStructure> transform(EncounterReportInput input) {
    FhirSession session = input.getSession();

    if (input.getPatient() == null) {
      return Optional.empty();
    }

    List<Reference> gpRefs = input.getPatient().getGeneralPractitioner();

    String practiceCode = gpRefs.stream()
        .filter(ReferenceUtil.ofType(Organization.class))
        .findFirst()
        .map(session::getOrganization)
        .map(IdentifierUtil::getOdsCode) //This could be other codes
        .map(odsCode -> odsCode.orElse(GP_PRACTICE_NOT_KNOWN))
        .orElse(NO_REGISTERED_GP);

    String practitionerCode = gpRefs.stream()
        .filter(ReferenceUtil.ofType(Practitioner.class))
        .findFirst()
        .map(session::getPractitioner)
        .map(IdentifierUtil::getOdsCode) //PPD Code
        .map(odsCode -> odsCode.orElse(PPD_CODE_NOT_KNOWN))
        .orElse(null);

    if (isNullOrEmpty(practiceCode) && isNullOrEmpty(practitionerCode)) {
      return Optional.empty();
    }

    GPRegistrationStructure gpRegistrationStructure = Factory.newInstance();

    if (isNotEmpty(practiceCode)) {
      gpRegistrationStructure.setGeneralPracticePatientRegistration(practiceCode);
    }

    if (isNotEmpty(practitionerCode)) {
      gpRegistrationStructure.setGeneralMedicalPractitionerSpecified(practitionerCode);
    }

    return Optional.of(gpRegistrationStructure);
  }

}
