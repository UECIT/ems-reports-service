package uk.nhs.cdss.reports.controllers;

import ca.uhn.fhir.rest.param.ReferenceParam;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerErrorException;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.service.FhirService;
import uk.nhs.cdss.reports.transform.ECDSReportTransformer;
import uk.nhs.cdss.reports.transform.TransformationException;

@RestController
@AllArgsConstructor
public class EncounterReportController {

  private FhirService fhirService;

  public final ECDSReportTransformer ecdsReportTransformer;

  @PostMapping("report")
  public ResponseEntity<String> generateReports(@RequestParam("encounter") String encounterRef) {

    EncounterReportInput encounterReportInput = fhirService
        .createEncounterReportInput(new ReferenceParam(encounterRef));

    try {
      String ecdsReport = ecdsReportTransformer.transform(encounterReportInput);

      // TODO store reports somewhere and return a reference
      return ResponseEntity.status(HttpStatus.OK)
          .contentType(MediaType.TEXT_XML)
          .body(ecdsReport);
    } catch (TransformationException e) {
      throw new ServerErrorException("Transformation failed", e);
    }
  }
}
