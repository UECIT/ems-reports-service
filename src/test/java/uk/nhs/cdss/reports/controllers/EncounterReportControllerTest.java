package uk.nhs.cdss.reports.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import uk.nhs.cdss.reports.Stub;
import uk.nhs.cdss.reports.model.ReportsDTO;
import uk.nhs.cdss.reports.service.EncounterReportService;
import uk.nhs.cdss.reports.transform.iucds.IUCDSReportTransformer;

public class EncounterReportControllerTest {

  @Test
  public void generateReports() {
    var mockFhirService = mock(EncounterReportService.class);
    when(mockFhirService.createEncounterReportInput(any())).thenReturn(
        Stub.input()
            .patient(Stub.patient())
            .build()
    );

    EncounterReportController reportController =
        new EncounterReportController(
            mockFhirService,
            Stub.ecdsTransformer(),
            new IUCDSReportTransformer());

    ResponseEntity<ReportsDTO> result = reportController.generateReports("123");
    System.out.println(result);
  }
}