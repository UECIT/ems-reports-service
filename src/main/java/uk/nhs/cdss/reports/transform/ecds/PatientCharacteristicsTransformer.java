package uk.nhs.cdss.reports.transform.ecds;

import java.util.Optional;
import java.util.function.Function;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import uk.nhs.cdss.reports.constants.FHIRSystems;
import uk.nhs.nhsia.datastandards.ecds.OverseasECType;
import uk.nhs.nhsia.datastandards.ecds.PatientCharacteristicsEmergencyCareStructure;
import uk.nhs.nhsia.datastandards.ecds.PersonStatedGenderCodeECType;
import uk.nhs.nhsia.datastandards.ecds.PersonStatedGenderCodeECType.Enum;

@Component
public class PatientCharacteristicsTransformer {

  public Optional<PatientCharacteristicsEmergencyCareStructure> transform(Patient patient) {

    if (ObjectUtils.isEmpty(patient)) {
      return Optional.empty();
    }

    PatientCharacteristicsEmergencyCareStructure structure = PatientCharacteristicsEmergencyCareStructure
        .Factory.newInstance();

    if (patient.hasGender()) {
      structure.setPersonStatedGenderCode(getGender(patient.getGender()));
    }

    patient.getExtensionsByUrl(FHIRSystems.ETHNIC_CODES_URL).stream().findFirst()
        .map(extensionToCodeableConcept())
        .map(this::getEthicCategory)
        .ifPresent(structure::setEthnicCategory);

    // Would have thought this would come from residentialStatus extension but value sets don't match?
    patient.getExtensionsByUrl(FHIRSystems.RESIDENTIAL_STATUS_URL).stream().findFirst()
        .map(extensionToCodeableConcept())
        .map(this::getAccomodationStatus)
        .ifPresent(structure::setAccommodationStatusSnomedCt);

    Optional<Extension> nhsCommsExtension = patient.getExtensionsByUrl(FHIRSystems.NHS_COMMS_URL).stream()
        .findFirst();

    nhsCommsExtension
        .map(this::getAccessibleInformationProfessionalRequired)
        .ifPresent(structure::setAccessibleInformationProfessionalRequiredCodeSnomedCt);

    nhsCommsExtension
        .map(ext -> ext.getExtensionString("language"))
        .map(this::getInterpreterLanguage)
        .ifPresent(language -> {
          structure.setPreferredSpokenLanguageSnomedCt(language);
          structure.setInterpreterLanguageSnomedCt(language);
        });

    patient.getExtensionsByUrl(FHIRSystems.TREATMENT_CATEGORY_URL).stream().findFirst()
        .map(extensionToCodeableConcept())
        .map(this::getOverseasECType)
        .ifPresent(structure::setOverseasVisitorChargingCategoryAtCdsActivityDate);

    return Optional.of(structure);
  }

  private Enum getGender(AdministrativeGender gender) {
    switch (gender) {
      case MALE:
        return PersonStatedGenderCodeECType.X_1;
      case FEMALE:
        return PersonStatedGenderCodeECType.X_2;
      case OTHER:
        return PersonStatedGenderCodeECType.X_9;
      case UNKNOWN:
      case NULL:
      default:
        return PersonStatedGenderCodeECType.X;
    }
  }

  private Function<Extension, CodeableConcept> extensionToCodeableConcept() {
    return extension -> (CodeableConcept)extension.getValue();
  }

  private String getAccessibleInformationProfessionalRequired(Extension commsExtension) {
    // Would do some proper mapping here...
    return "715963003"; //Requires sight guide
  }

  private String getAccomodationStatus(CodeableConcept residentialStatus) {
    String code = residentialStatus.getCodingFirstRep().getCode();
    //Would do some proper mapping here..
    return "414418009";
  }

  private String getEthicCategory(CodeableConcept ethnicCategory) {
    String code = ethnicCategory.getCodingFirstRep().getCode();
    //Would do some proper mapping here...
    return "99"; //Not set
  }

  private String getLanguage(String language) {
    //Would do some proper mapping here...
    return "315570003"; //English
  }

  private String getInterpreterLanguage(String language) {
    //Would do some proper mapping here...
    return "204331000000107"; //Sign
  }

  private OverseasECType.Enum getOverseasECType(CodeableConcept treatmentCategory) {
    String code = treatmentCategory.getCodingFirstRep().getCode();
    //Would do some proper mapping here...
    return OverseasECType.A; //Standard NHS-funded Patient
  }
}
