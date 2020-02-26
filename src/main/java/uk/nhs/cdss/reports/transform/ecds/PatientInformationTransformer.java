package uk.nhs.cdss.reports.transform.ecds;

import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.CareConnectPatient;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.nhsia.datastandards.ecds.NHSNumberStatusIndicatorCodeUnverifiedType;
import uk.nhs.nhsia.datastandards.ecds.NHSNumberStatusIndicatorCodeVerifiedType;
import uk.nhs.nhsia.datastandards.ecds.NHSNumberStatusIndicatorCodeWithheldType;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.UnverifiedIdentityStructure;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.VerifiedIdentityStructure;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.VerifiedIdentityStructure.DataElementStructure;
import uk.nhs.nhsia.datastandards.ecds.PatientIdentity.WithheldIdentityStructure;
import uk.nhs.nhsia.datastandards.ecds.PersonGroupPatientECStructure;
import uk.nhs.nhsia.datastandards.ecds.WithheldIdentityReasonType;

@Component
public class PatientInformationTransformer {

  public PersonGroupPatientECStructure transform(Patient patient) {
    var patientStructure = PersonGroupPatientECStructure.Factory.newInstance();

    // Required
    var patientIdentity = patientStructure.addNewPatientIdentity();

    if (patient == null) {
      patientIdentity.setWithheldIdentityStructure(getWithheldId());
    } else if (patient instanceof CareConnectPatient && false) { //TODO: NCTH-523 Fix NhsNumberIdentifier Profile
      // TODO check verification status - NCTH-364
      patientIdentity.setVerifiedIdentityStructure(getVerifiedId((CareConnectPatient)patient));
    }
    else {
      patientIdentity.setUnverifiedIdentityStructure(getUnverifiedId(patient));
    }

    return patientStructure;
  }

  private VerifiedIdentityStructure getVerifiedId(CareConnectPatient patient) {
    // VERIFIED IDENTITY STRUCTURE
    // Must be used where the NHS NUMBER STATUS INDICATOR CODE National Code = 01 (Number present and verified)
    var id = VerifiedIdentityStructure.Factory.newInstance();
    DataElementStructure dataElement = id.addNewDataElementStructure();

    // TODO is LocalIdentifier used? Maybe the FHIR reference of the patient?
//    LocalIdentifierStructure localIdentifier = id.addNewLocalIdentifierStructure();

    id.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeVerifiedType.X_01);
    dataElement.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeVerifiedType.X_01);

    dataElement.setNHSNumber(patient.getIdentifierFirstRep().getValue());
    if (patient.hasBirthDate()) {
      dataElement.setPersonBirthDate(DateUtils.toCalendar(patient.getBirthDate()));
    }

    return id;
  }

  private WithheldIdentityStructure getWithheldId() {
    // WITHHELD IDENTITY STRUCTURE
    // Must be used where the Commissioning Data Set record has been anonymised
    var id = WithheldIdentityStructure.Factory.newInstance();

    // 07 -> Number not present and trace not required
    // SCHEMA: requires both the attribute and the element to be present?
    id.setNHSNumberStatusIndicatorCode2(NHSNumberStatusIndicatorCodeWithheldType.X_07);
    id.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeWithheldType.X_07);

    // 97 -> Record anonymised for other reason
    id.setWithheldIdentityReason(WithheldIdentityReasonType.X_97);

    return id;
  }

  private UnverifiedIdentityStructure getUnverifiedId(Patient patient) {
    // UNVERIFIED IDENTITY STRUCTURE
    // Must be used for all other values of the NHS NUMBER STATUS INDICATOR CODE NOT included in the above
    var id = UnverifiedIdentityStructure.Factory.newInstance();
    var dataElement = id.addNewDataElementStructure();

    // 03 -> Trace required
    // SCHEMA: XML Schema has use=required for the attribute on the id, but also requires the element in the dataElement
    id.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeUnverifiedType.X_03);
    dataElement.setNHSNumberStatusIndicatorCode(NHSNumberStatusIndicatorCodeUnverifiedType.X_03);

    var name = dataElement.addNewPatientName().addNewPersonNameStructured();
    var nameFirstRep = patient.getNameFirstRep();
    name.setPersonGivenName(nameFirstRep.getGivenAsSingleString());
    name.setPersonFamilyName(nameFirstRep.getFamily());

    if (patient.hasBirthDate()) {
      var dataElement2 = id.addNewDataElementStructure2();
      dataElement2.setPersonBirthDate(DateUtils.toCalendar(patient.getBirthDate()));
    }

    return id;
  }
}
