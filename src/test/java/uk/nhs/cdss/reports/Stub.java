package uk.nhs.cdss.reports;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.nhs.cdss.reports.constants.Systems.DIAGNOSIS_ROLE;

import java.util.Calendar;
import java.util.Calendar.Builder;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CareConnectPatient;
import org.hl7.fhir.dstu3.model.CareConnectIdentifier;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.NHSNumberIdentifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralCategory;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralPriority;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import uk.nhs.cdss.reports.constants.Systems;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.model.EncounterReportInput.EncounterReportInputBuilder;
import uk.nhs.cdss.reports.service.CounterService;
import uk.nhs.cdss.reports.transform.ecds.AttendanceOccurrenceTransformer;
import uk.nhs.cdss.reports.transform.ecds.ECDSReportTransformer;
import uk.nhs.cdss.reports.transform.ecds.EmergencyCareDiagnosesTransformer;
import uk.nhs.cdss.reports.transform.ecds.EmergencyCareInvestigationsTransformer;
import uk.nhs.cdss.reports.transform.ecds.EmergencyCareTransformer;
import uk.nhs.cdss.reports.transform.ecds.EmergencyCareTreatmentsTransformer;
import uk.nhs.cdss.reports.transform.ecds.GPRegistrationTransformer;
import uk.nhs.cdss.reports.transform.ecds.PatientClinicalHistoryTransformer;
import uk.nhs.cdss.reports.transform.ecds.PatientInformationTransformer;
import uk.nhs.cdss.reports.transform.ecds.ReferralsToOtherServicesTransformer;

@UtilityClass
public class Stub {

  public EncounterReportInputBuilder input() {
    return EncounterReportInput.builder()
        .dateOfPreparation(new Calendar.Builder()
            .setDate(2020, 0, 1)
            .build())
        .encounter(minimumEncounter());
  }

  private Encounter minimumEncounter() {
    return new Encounter()
        .setServiceProvider(new Reference(serviceProvider()));
  }

  public Encounter encounter() {
    return new Encounter()
        .setServiceProvider(new Reference(serviceProvider()))
        .addDiagnosis(new DiagnosisComponent(new Reference(condition()))
          .setRole(new CodeableConcept()
            .addCoding(new Coding(DIAGNOSIS_ROLE, "CM", "Comorbidity diagnosis"))))
        .addDiagnosis(new DiagnosisComponent(new Reference(condition()))) //NOT CM
        .addDiagnosis(new DiagnosisComponent(new Reference(procedure()))
          .setRole(new CodeableConcept()
              .addCoding(new Coding(DIAGNOSIS_ROLE, "CM", "Comorbidity diagnosis"))))
        .addDiagnosis(new DiagnosisComponent(new Reference(procedure()))); //NOT CM
  }

  private Condition condition() {
    return new Condition()
        .setCode(new CodeableConcept()
          .addCoding(new Coding("system", "282828", "Comorbid")));
  }

  private Procedure procedure() {
    return new Procedure()
        .setCode(new CodeableConcept()
            .addCoding(new Coding("system", "828282", "Comorbid")));
  }

  public static Location location() {
    return new Location()
        .setName("Location")
        .setType(new CodeableConcept()
            .addCoding(new Coding()
                .setSystem("http://hl7.org/fhir/ValueSet/v3-ServiceDeliveryLocationRoleType")
                .setCode("ER")))
        .addIdentifier(new CareConnectIdentifier()
            .setSystem(Systems.ODS)
            .setValue("ODSLoc"));
  }

  public Organization serviceProvider() {
    return new Organization()
        .setName("Service Provider")
        .addIdentifier(new Identifier()
            .setSystem(Systems.ODS)
            .setValue("AA100"));
  }

  public Organization practitionerOrg() {
    return new Organization()
        .setName("General Practice")
        .addIdentifier(new Identifier()
            .setSystem(Systems.ODS)
            .setValue("BB2003"));
  }

  public Practitioner practitioner() {
    return new Practitioner()
        .addName(new HumanName()
            .addGiven("Don")
            .setFamily("Quixote"))
        .addIdentifier(new Identifier()
            .setSystem(Systems.ODS)
            .setValue("CC2003XX"));
  }

  public CareConnectPatient patient() {
    CareConnectPatient patient = new CareConnectPatient();
    patient.setBirthDate(new Builder().setDate(2000, 0, 1).build().getTime())
        .setGender(AdministrativeGender.FEMALE)
        .addName(new HumanName().addGiven("Jane").setFamily("Doe"))
        .addGeneralPractitioner(new Reference(Stub.practitionerOrg()))
        .addGeneralPractitioner(new Reference(Stub.practitioner()))
        .addIdentifier(nhsNumberIdentifierVerified())
        .addAddress(new Address().setPostalCode("PS1 1AA"));

    patient.setIdBase("123");
    return patient;
  }

  public NHSNumberIdentifier nhsNumberIdentifierUnverified() {
    NHSNumberIdentifier nhsNumberIdentifier = new NHSNumberIdentifier();
    nhsNumberIdentifier.setNhsNumberVerificationStatus(
        new CodeableConcept().addCoding(new Coding(Systems.NHS_NUMBER, "03", "Trace required")))
          .setValue("0123456789");

    return nhsNumberIdentifier;
  }

  public NHSNumberIdentifier nhsNumberIdentifierVerified() {
    NHSNumberIdentifier nhsNumberIdentifier = new NHSNumberIdentifier();
    nhsNumberIdentifier.setNhsNumberVerificationStatus(
        new CodeableConcept().addCoding(new Coding(Systems.NHS_NUMBER, "01", "Number present and verified")))
        .setValue("0123456789");

    return nhsNumberIdentifier;
  }

  public List<ReferralRequest> referralRequest() {
    ReferralRequest referralRequest = new ReferralRequest()
        .setStatus(ReferralRequestStatus.ACTIVE)
        .setIntent(ReferralCategory.PLAN)
        .setPriority(ReferralPriority.ROUTINE)
        .setAuthoredOn(new Date())
        .setServiceRequested(Collections.singletonList(new CodeableConcept()
            .addCoding(new Coding("sys", "1234567", "display"))))
        .setReasonCode(Collections.singletonList(new CodeableConcept()
            .addCoding(new Coding("sys", "reason", "display"))));

    referralRequest.setIdBase("123");
    return Collections.singletonList(referralRequest);
  }

  public CounterService counterService() {
    var mockCounterService = mock(CounterService.class);
    when(mockCounterService.incrementAndGetCounter(any())).thenReturn(1L);
    return mockCounterService;
  }

  public ECDSReportTransformer ecdsTransformer() {
    return new ECDSReportTransformer(
        Stub.counterService(),
        new EmergencyCareTransformer(
            new PatientInformationTransformer(),
            new AttendanceOccurrenceTransformer(
                new ReferralsToOtherServicesTransformer(),
                new EmergencyCareDiagnosesTransformer(Stub.counterService()),
                new EmergencyCareInvestigationsTransformer(),
                new EmergencyCareTreatmentsTransformer(),
                new PatientClinicalHistoryTransformer()),
            new GPRegistrationTransformer()));
  }

  public static List<Procedure> procedures() {
    CodeableConcept codeableConcept = new CodeableConcept()
        .addCoding(new Coding("sys", "9876534", "display"));
    Procedure procedure = new Procedure()
        .setStatus(ProcedureStatus.PREPARATION)
        .addReasonCode(codeableConcept)
        .setCode(codeableConcept)
        .setPerformed(
            new DateTimeType(new GregorianCalendar(2013, Calendar.FEBRUARY, 5, 5, 43, 12)));

    procedure.setIdBase("123");
    return Collections.singletonList(procedure);
  }

}
