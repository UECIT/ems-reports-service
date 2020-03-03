package uk.nhs.cdss.reports.transform.ecds;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.CareConnectPatient;
import org.hl7.fhir.dstu3.model.NHSNumberIdentifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.nhsia.datastandards.ecds.NHSNumberStatusIndicatorCodeUnverifiedType.Enum;
import uk.nhs.nhsia.datastandards.ecds.NHSNumberStatusIndicatorCodeVerifiedType;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.UnverifiedIdentityStructure;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.VerifiedIdentityStructure;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.VerifiedIdentityStructure.DataElementStructure;
import uk.nhs.nhsia.datastandards.ecds.PersonGroupPatientECStructure;

@Component
@Slf4j
public class PatientInformationTransformer {

  private static final String RESIDENCE_RESPONSIBILITY_HIGH_LEVEL = "Q99";

  public PersonGroupPatientECStructure transform(Patient patient) {
    var patientStructure = PersonGroupPatientECStructure.Factory.newInstance();

    // Required
    var patientIdentity = patientStructure.addNewPatientIdentity();

    if (!(patient instanceof CareConnectPatient)) {
      throw new IllegalArgumentException("Patient must be a CareConnectPatient");
    }

    CareConnectPatient careConnectPatient = (CareConnectPatient) patient;
    NHSNumberIdentifier nhsNumber = careConnectPatient.getIdentifierFirstRep();
    String nhsNumberStatusCode = nhsNumber.getNhsNumberVerificationStatus().getCodingFirstRep().getCode();

    // Could check an 'anonymised' flag and set withheld identity structure

    if ("01".equals(nhsNumberStatusCode)) {
      patientIdentity.setVerifiedIdentityStructure(getVerifiedId(careConnectPatient));
    }
    else {
      patientIdentity.setUnverifiedIdentityStructure(getUnverifiedId(careConnectPatient, nhsNumberStatusCode));
    }

    return patientStructure;
  }

  private VerifiedIdentityStructure getVerifiedId(CareConnectPatient patient) {

    var id = VerifiedIdentityStructure.Factory.newInstance();
    DataElementStructure dataElement = id.addNewDataElementStructure();

    // SCHEMA: requires both the attribute and the element to be present?
    dataElement.setNHSNumber(patient.getIdentifierFirstRep().getValue());
    id.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeVerifiedType.X_01);
    dataElement.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeVerifiedType.X_01);
    dataElement.setPostcodeOfUsualAddress(patient.getAddressFirstRep().getPostalCode());
    dataElement.setOrganisationIdentifierResidenceResponsibility(RESIDENCE_RESPONSIBILITY_HIGH_LEVEL); // Would be worked out from postcode
    if (patient.hasBirthDate()) {
      dataElement.setPersonBirthDate(DateUtils.toCalendar(patient.getBirthDate()));
    }

    return id;
  }

  private UnverifiedIdentityStructure getUnverifiedId(CareConnectPatient patient,
      String nhsNumberStatusCode) {

    var id = UnverifiedIdentityStructure.Factory.newInstance();
    var dataElement = id.addNewDataElementStructure();

    // SCHEMA: XML Schema has use=required for the attribute on the id, but also requires the element in the dataElement
    Enum nhsNumberCodeEnum = Enum.forString(nhsNumberStatusCode);
    id.setNHSNumberStatusIndicatorCode(nhsNumberCodeEnum);
    dataElement.setNHSNumberStatusIndicatorCode(nhsNumberCodeEnum);

    if (patient.getIdentifierFirstRep().hasValue()) {
      dataElement.setNHSNumber(patient.getIdentifierFirstRep().getValue());
    }

    if (patient.hasName()) {
      var name = dataElement.addNewPatientName().addNewPersonNameStructured();
      var nameFirstRep = patient.getNameFirstRep();
      name.setPersonGivenName(nameFirstRep.getGivenAsSingleString());
      name.setPersonFamilyName(nameFirstRep.getFamily());
    }

    if (patient.hasBirthDate() || patient.getAddressFirstRep().hasPostalCode()) {
      var dataElement2 = id.addNewDataElementStructure2();

      if (patient.hasBirthDate()) {
        dataElement2.setPersonBirthDate(DateUtils.toCalendar(patient.getBirthDate()));
      }
      else {
        dataElement2.setPostcodeOfUsualAddress(patient.getAddressFirstRep().getPostalCode());
      }
      dataElement2.setOrganisationIdentifierResidenceResponsibility(RESIDENCE_RESPONSIBILITY_HIGH_LEVEL);
    }

    return id;
  }
}
