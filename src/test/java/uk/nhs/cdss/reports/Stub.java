package uk.nhs.cdss.reports;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.nhs.cdss.reports.constants.Systems.DIAGNOSIS_ROLE;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Calendar.Builder;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CareConnectIdentifier;
import org.hl7.fhir.dstu3.model.CareConnectPatient;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.NHSNumberIdentifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralCategory;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralPriority;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import org.hl7.fhir.dstu3.model.StringType;
import uk.nhs.cdss.reports.constants.Systems;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.model.EncounterReportInput.EncounterReportInputBuilder;
import uk.nhs.cdss.reports.service.CounterService;
import uk.nhs.cdss.reports.service.SnomedService;
import uk.nhs.cdss.reports.transform.ecds.AttendanceActivityCharacteristicsTransformer;
import uk.nhs.cdss.reports.transform.ecds.AttendanceOccurrenceTransformer;
import uk.nhs.cdss.reports.transform.ecds.ECDSReportTransformer;
import uk.nhs.cdss.reports.transform.ecds.EmergencyCareDiagnosesTransformer;
import uk.nhs.cdss.reports.transform.ecds.EmergencyCareInvestigationsTransformer;
import uk.nhs.cdss.reports.transform.ecds.EmergencyCareTransformer;
import uk.nhs.cdss.reports.transform.ecds.EmergencyCareTreatmentsTransformer;
import uk.nhs.cdss.reports.transform.ecds.GPRegistrationTransformer;
import uk.nhs.cdss.reports.transform.ecds.InjuryCharacteristicsTransformer;
import uk.nhs.cdss.reports.transform.ecds.PatientCharacteristicsTransformer;
import uk.nhs.cdss.reports.transform.ecds.PatientClinicalHistoryTransformer;
import uk.nhs.cdss.reports.transform.ecds.PatientIdentityTransformer;
import uk.nhs.cdss.reports.transform.ecds.PatientInformationTransformer;
import uk.nhs.cdss.reports.transform.ecds.ReferralsToOtherServicesTransformer;
import uk.nhs.cdss.reports.transform.ecds.constants.InjuryCodes;

@UtilityClass
public class Stub {

  public final Calendar CALENDAR = new Builder()
      .setDate(2020, 0, 1)
      .build();

  public EncounterReportInputBuilder input() {
    return EncounterReportInput.builder()
        .dateOfPreparation(CALENDAR)
        .encounter(minimumEncounter());
  }

  private Encounter minimumEncounter() {
    Encounter encounter = new Encounter();
    encounter
        .setServiceProvider(new Reference(serviceProvider()))
        .setId("123");

    return encounter;
  }

  public Encounter encounter() {
    final var COMORBIDITY = new CodeableConcept()
        .addCoding(new Coding(DIAGNOSIS_ROLE, "CM", "Comorbidity diagnosis"));
    Encounter encounter = new Encounter();
    encounter
        .setPeriod(new Period().setStart(CALENDAR.getTime()))
        .setServiceProvider(new Reference(serviceProvider()))
        .addLocation(new EncounterLocationComponent(new Reference(location())))
        .addDiagnosis(new DiagnosisComponent(new Reference(condition())).setRole(COMORBIDITY))
        .addDiagnosis(new DiagnosisComponent(new Reference(condition()))) //NOT CM
        .addDiagnosis(new DiagnosisComponent(new Reference(procedure())).setRole(COMORBIDITY))
        .addDiagnosis(new DiagnosisComponent(new Reference(procedure()))) //NOT CM
        .addDiagnosis(new DiagnosisComponent(new Reference(woundCondition())))
        .setId("123");

    return encounter;
  }

  private Condition woundCondition() {
    var date = new GregorianCalendar(1996, Calendar.JANUARY, 23, 15, 35, 49);
    return new Condition()
        .setCode(buildSnomedConcept(InjuryCodes.WOUND_FINDING, "Wounded"))
        .setOnset(new DateTimeType(date));
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

  public Location location() {
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
        .addAddress(new Address().setPostalCode("PS1 1AA"))
        .setExtension(careConnectExtensions());

    patient.setIdBase("123");
    return patient;
  }

  private List<Extension> careConnectExtensions() {
    Extension commsExtension = new Extension(Systems.NHS_COMMS_URL, new CodeableConcept());
    commsExtension.addExtension("language", new StringType("English"));
    return Arrays.asList(
        new Extension(Systems.ETHNIC_CODES_URL, new CodeableConcept()),
        commsExtension,
        new Extension(Systems.RESIDENTIAL_STATUS_URL, new CodeableConcept()),
        new Extension(Systems.TREATMENT_CATEGORY_URL, new CodeableConcept())
    );
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
        new CodeableConcept()
            .addCoding(new Coding(Systems.NHS_NUMBER, "01", "Number present and verified")))
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
            .addCoding(new Coding("sys", "reason", "display"))))
        .setReasonReference(List.of(new Reference(new Condition()
            .setCode(buildSnomedConcept("01010101", "display")))));

    referralRequest.setIdBase("123");
    return Collections.singletonList(referralRequest);
  }

  public List<Observation> injuryObservations() {
    return List.of(
        new Observation().setCode(buildSnomedConcept(InjuryCodes.PLACE_REFSET, "Place")),
        new Observation().setCode(buildSnomedConcept(InjuryCodes.INTENT_REFSET, "Intent")),
        new Observation().setCode(
            buildSnomedConcept(InjuryCodes.ACTIVITY_STATUS_REFSET, "Activity status")),
        new Observation().setCode(
            buildSnomedConcept(InjuryCodes.ACTIVITY_TYPE_REFSET, "Activity type")),
        new Observation().setCode(
            buildSnomedConcept(InjuryCodes.MECHANISM_REFSET, "Mechanism")),
        new Observation().setCode(
            buildSnomedConcept(
                InjuryCodes.ALCOHOL_OR_DRUG_INVOLVEMENT_REFSET,
                "Alcohol or drug involvement"))
    );
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
            new PatientInformationTransformer(
                new PatientIdentityTransformer(),
                new PatientCharacteristicsTransformer()),
            new AttendanceOccurrenceTransformer(
                new ReferralsToOtherServicesTransformer(),
                new EmergencyCareDiagnosesTransformer(),
                new EmergencyCareInvestigationsTransformer(),
                new EmergencyCareTreatmentsTransformer(),
                new AttendanceActivityCharacteristicsTransformer(),
                new PatientClinicalHistoryTransformer(),
                new InjuryCharacteristicsTransformer(new SnomedService())),
            new GPRegistrationTransformer()));
  }

  public List<Procedure> procedures() {
    var date = new GregorianCalendar(2013, Calendar.FEBRUARY, 5, 5, 43, 12);
    CodeableConcept codeableConcept = new CodeableConcept()
        .addCoding(new Coding("sys", "9876534", "display"));
    Procedure procedure = new Procedure()
        .setStatus(ProcedureStatus.PREPARATION)
        .addReasonCode(codeableConcept)
        .setCode(codeableConcept)
        .setPerformed(new DateTimeType(date));

    procedure.setIdBase("123");
    return Collections.singletonList(procedure);
  }

  private CodeableConcept buildSnomedConcept(String value, String display) {
    return buildConcept(Systems.SNOMED, value, display);
  }

  private CodeableConcept buildConcept(String system, String value, String display) {
    return new CodeableConcept().addCoding(new Coding(system, value, display));
  }

  public Consent consent() {
    Consent consent = new Consent();
    consent
        .addCategory(buildConcept(
            "http://hl7.org/fhir/ValueSet/consent-category",
            "ICOL", "information collection"))
        .setId("consent");

    return consent;
  }
}
