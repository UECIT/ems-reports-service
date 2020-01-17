package uk.nhs.cdss.reports.controllers;

import ca.uhn.fhir.rest.param.ReferenceParam;
import java.io.IOException;
import java.io.StringWriter;
import lombok.AllArgsConstructor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerErrorException;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.cdss.reports.model.ReportsDTO;
import uk.nhs.cdss.reports.service.FhirService;
import uk.nhs.cdss.reports.transform.TransformationException;
import uk.nhs.cdss.reports.transform.ecds.ECDSReportTransformer;
import uk.nhs.cdss.reports.transform.iucds.IUCDSReportTransformer;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.nhsia.datastandards.ecds.CDSXMLInterchangeDocument;

@RestController
@AllArgsConstructor
public class EncounterReportController {

  private FhirService fhirService;

  public final ECDSReportTransformer ecdsReportTransformer;
  public final IUCDSReportTransformer iucdsReportTransformer;

  @PostMapping("report")
  public ResponseEntity<ReportsDTO> generateReports(
      @RequestParam("encounter") String encounterRef) {

    EncounterReportInput encounterReportInput = fhirService
        .createEncounterReportInput(new ReferenceParam(encounterRef));

    try {
      CDSXMLInterchangeDocument ecdsReport = ecdsReportTransformer.transform(encounterReportInput);
      ClinicalDocumentDocument1 iucdsReport = iucdsReportTransformer
          .transform(encounterReportInput);

      ReportsDTO reportsDTO = ReportsDTO.builder()
          .ecds(prettyPrint(ecdsReport))
          .iucds(prettyPrint(iucdsReport))
          .build();

      // TODO store reports somewhere and return a reference
      return ResponseEntity.status(HttpStatus.OK)
          .contentType(MediaType.APPLICATION_JSON)
          .body(reportsDTO);

    } catch (TransformationException | IOException e) {
      throw new ServerErrorException("Transformation failed", e);
    }
  }

  public static String prettyPrint(XmlObject xmlObject) throws IOException {
    StringWriter output = new StringWriter();
    xmlObject.save(output, new XmlOptions()
        .setSavePrettyPrint()
    );
    return output.toString();
  }
}
