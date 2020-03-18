package uk.nhs.cdss.reports;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.nhs.cdss.reports.constants.FHIRSystems.DIAGNOSIS_ROLE;

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
import org.hl7.fhir.dstu3.model.CareConnectOrganization;
import org.hl7.fhir.dstu3.model.CareConnectPatient;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.NHSNumberIdentifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Organization.OrganizationContactComponent;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Procedure.ProcedureStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralCategory;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralPriority;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StringType;
import uk.nhs.cdss.reports.constants.FHIRSystems;
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
import uk.nhs.cdss.reports.transform.ecds.Identifiers;
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

  public EncounterReportInputBuilder inputECDS() {
    return EncounterReportInput.builder()
        .dateOfPreparation(CALENDAR)
        .encounter(minimumEncounterECDS());
  }

  public EncounterReportInputBuilder inputIUCDS() {
    return EncounterReportInput.builder()
        .dateOfPreparation(CALENDAR)
        .encounter(minimumEncounterIUCDS());
  }

  private Encounter minimumEncounterECDS() {
    Encounter encounter = new Encounter();
    encounter
        .setServiceProvider(ref(minimumServiceProvider()))
        .setId("123");

    return encounter;
  }


  private Encounter minimumEncounterIUCDS() {
    Encounter encounter = new Encounter();
    encounter.setId("123");

    return encounter;
  }

  public Encounter encounter() {
    final var COMORBIDITY = new CodeableConcept()
        .addCoding(new Coding(DIAGNOSIS_ROLE, "CM", "Comorbidity diagnosis"));
    Encounter encounter = new Encounter();
    encounter
        .setPeriod(new Period().setStart(CALENDAR.getTime()))
        .setServiceProvider(ref(serviceProvider()))
        .addLocation(new EncounterLocationComponent(ref(location())))
        .addDiagnosis(new DiagnosisComponent(ref(condition())).setRole(COMORBIDITY))
        .addDiagnosis(new DiagnosisComponent(ref(condition()))) //NOT CM
        .addDiagnosis(new DiagnosisComponent(ref(procedure())).setRole(COMORBIDITY))
        .addDiagnosis(new DiagnosisComponent(ref(procedure()))) //NOT CM
        .addDiagnosis(new DiagnosisComponent(ref(woundCondition())))
        .setEpisodeOfCare(List.of(ref(episodeOfCare())))
        .addParticipant(new EncounterParticipantComponent()
          .setIndividual(ref(practitioner()))
          .addType(new CodeableConcept()
              .addCoding(new Coding("participationtype", "ADM", "admitter"))))
        .addParticipant(new EncounterParticipantComponent()
            .setIndividual(ref(relatedPerson()))
            .addType(new CodeableConcept()
                .addCoding(new Coding("participationtype", "CON", "consultant"))))
        .setId("123");

    return encounter;
  }

  private static Reference ref(Resource resource) {
    Reference reference = new Reference(resource);
    if (resource.hasId()) {
      reference.setReference(resource.getResourceType().name() + "/" + resource.getId());
    }
    return reference;
  }

  private static EpisodeOfCare episodeOfCare() {
    EpisodeOfCare episodeOfCare = new EpisodeOfCare();
    episodeOfCare
        .setManagingOrganization(ref(managingOrg()))
        .setCareManager(ref(practitioner()))
        .setId("123");
    return episodeOfCare;
  }

  private static Organization managingOrg() {
    CareConnectOrganization organization = new CareConnectOrganization();
    organization
        .setName("Managing Organization")
        .addIdentifier(new CareConnectIdentifier()
            .setSystem(FHIRSystems.ODS_ORGANIZATION)
            .setValue("AA123"))
        .setType(List.of(new CodeableConcept()
            .addCoding(new Coding()
                .setSystem("org-type")
                .setCode("MO"))))
        .setId("123");
    return organization;
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
    Location location = new Location()
        .setName("Location")
        .setType(new CodeableConcept()
            .addCoding(new Coding()
                .setSystem("http://hl7.org/fhir/ValueSet/v3-ServiceDeliveryLocationRoleType")
                .setCode("ER")))
        .addIdentifier(new CareConnectIdentifier()
            .setSystem(FHIRSystems.ODS_ORGANIZATION)
            .setValue("ODSLoc"));

    location.setIdBase("123");
    return location;
  }

  public Organization minimumServiceProvider() {
    Organization organization = new Organization();
    organization
        .setName("Service Provider")
        .addIdentifier(new Identifier()
            .setSystem(FHIRSystems.ODS_ORGANIZATION)
            .setValue("AA100"))
        .setType(List.of(new CodeableConcept()
            .addCoding(new Coding()
                .setSystem("org-type")
                .setCode("SP"))))
        .setId("serviceProvider");

    return organization;
  }


  public Organization serviceProvider() {
    Organization organization = new Organization();
    organization
        .setName("Service Provider")
        .addIdentifier(new Identifier()
            .setSystem(FHIRSystems.ODS_ORGANIZATION)
            .setValue("AA100"))
        .addIdentifier(new Identifier()
            .setSystem(FHIRSystems.ODS_SITE)
            .setValue("AA122"))
        .addTelecom(new ContactPoint()
            .setUse(ContactPointUse.WORK)
            .setValue("0123456789"))
        .addContact(new OrganizationContactComponent()
            .setName(new HumanName().addGiven("Homer").setFamily("Simpson")))
        .setType(List.of(new CodeableConcept()
            .addCoding(new Coding()
                .setSystem("org-type")
                .setCode("SP"))))
        .setId("serviceProvider");
    return organization;
  }

  public Organization practitionerOrg() {
    return new Organization()
        .setName("General Practice")
        .addIdentifier(new Identifier()
            .setSystem(FHIRSystems.ODS_ORGANIZATION)
            .setValue("BB2003"));
  }

  public Practitioner practitioner() {
    Practitioner practitioner =  new Practitioner()
        .addName(new HumanName()
            .addGiven("Don")
            .setFamily("Quixote"))
        .addIdentifier(new Identifier()
            .setSystem(FHIRSystems.ODS_ORGANIZATION)
            .setValue("CC2003XX"));

    practitioner.setIdBase("123");
    return practitioner;
  }

  public CareConnectPatient patient() {
    CareConnectPatient patient = new CareConnectPatient();
    patient.setBirthDate(new Builder().setDate(2000, 0, 1).build().getTime())
        .setGender(AdministrativeGender.FEMALE)
        .addName(new HumanName().addGiven("Jane").setFamily("Doe"))
        .addGeneralPractitioner(ref(Stub.practitionerOrg()))
        .addGeneralPractitioner(ref(Stub.practitioner()))
        .addIdentifier(nhsNumberIdentifierVerified())
        .addAddress(new Address().setPostalCode("PS1 1AA"))
        .setExtension(careConnectExtensions());

    patient.setIdBase("123");
    return patient;
  }

  private List<Extension> careConnectExtensions() {
    Extension commsExtension = new Extension(FHIRSystems.NHS_COMMS_URL, new CodeableConcept());
    commsExtension.addExtension("language", new StringType("English"));
    return Arrays.asList(
        new Extension(FHIRSystems.ETHNIC_CODES_URL, new CodeableConcept()),
        commsExtension,
        new Extension(FHIRSystems.RESIDENTIAL_STATUS_URL, new CodeableConcept()),
        new Extension(FHIRSystems.TREATMENT_CATEGORY_URL, new CodeableConcept())
    );
  }

  public NHSNumberIdentifier nhsNumberIdentifierUnverified() {
    NHSNumberIdentifier nhsNumberIdentifier = new NHSNumberIdentifier();
    nhsNumberIdentifier.setNhsNumberVerificationStatus(
        new CodeableConcept().addCoding(new Coding(FHIRSystems.NHS_NUMBER, "03", "Trace required")))
        .setValue("0123456789");

    return nhsNumberIdentifier;
  }

  public NHSNumberIdentifier nhsNumberIdentifierVerified() {
    NHSNumberIdentifier nhsNumberIdentifier = new NHSNumberIdentifier();
    nhsNumberIdentifier.setNhsNumberVerificationStatus(
        new CodeableConcept()
            .addCoding(new Coding(FHIRSystems.NHS_NUMBER, "01", "Number present and verified")))
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
        .setReasonReference(List.of(ref(new Condition()
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
            new GPRegistrationTransformer()),
        identifiers());
  }

  public static Identifiers identifiers() {
    return Identifiers.builder()
        .senderIdentity("1100000000")
        .senderOdsOrganisation("8HW00")
        .receiverIdentity("1100000000")
        .build();
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
    return buildConcept(FHIRSystems.SNOMED, value, display);
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

  public RelatedPerson relatedPerson() {
    RelatedPerson relatedPerson = new RelatedPerson()
        .addName(new HumanName()
          .addGiven("Homer")
          .setFamily("Simpson"));
    relatedPerson.setIdBase("132");
    return relatedPerson;
  }
}
