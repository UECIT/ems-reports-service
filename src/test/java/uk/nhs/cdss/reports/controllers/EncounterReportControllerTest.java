package uk.nhs.cdss.reports.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.nhs.cdss.reports.Stub;
import uk.nhs.cdss.reports.model.ReportsDTO;
import uk.nhs.cdss.reports.service.EncounterReportService;
import uk.nhs.cdss.reports.service.FhirSession;
import uk.nhs.cdss.reports.transform.iucds.FixedUUIDProvider;
import uk.nhs.cdss.reports.transform.iucds.EncounterTransformer;
import uk.nhs.cdss.reports.transform.iucds.IUCDSReportTransformer;
import uk.nhs.cdss.reports.transform.iucds.ParticipantTransformer;

@RunWith(MockitoJUnitRunner.class)
public class EncounterReportControllerTest {

  @Mock
  private FhirContext fhirContext;

  @Mock
  private EncounterReportService encounterReportService;

  private FhirSession fhirSession;

  private IUCDSReportTransformer iucdsReportTransformer;

  @Before
  public void setup() {
    fhirSession = new FhirSession(new Reference("http://fhir/Encounter/123"), fhirContext);
    FixedUUIDProvider uuidProvider = new FixedUUIDProvider();
    iucdsReportTransformer = new IUCDSReportTransformer(
        uuidProvider,
        new EncounterTransformer(uuidProvider),
        new ParticipantTransformer()
    );
  }

  @Test
  public void generateReports() {
    when(encounterReportService.createEncounterReportInput(any())).thenReturn(
        Stub.inputECDS()
            .session(fhirSession)
            .patient(Stub.patient())
            .referralRequest(Stub.referralRequest())
            .procedures(Stub.procedures())
            .build()
    );

    EncounterReportController reportController =
        new EncounterReportController(
            encounterReportService,
            Stub.ecdsTransformer(),
            iucdsReportTransformer);

    ResponseEntity<ReportsDTO> result = reportController.generateReports("123");
    System.out.println(result);
  }
}