package uk.nhs.cdss.reports.controllers;

import ca.uhn.fhir.rest.param.ReferenceParam;
import java.io.IOException;
import java.io.StringWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.nhs.cdss.reports.service.EncounterReportService;
import uk.nhs.cdss.reports.transform.TransformationException;
import uk.nhs.cdss.reports.transform.ecds.ECDSReportTransformer;
import uk.nhs.cdss.reports.transform.iucds.IUCDSReportTransformer;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.nhsia.datastandards.ecds.CDSXMLInterchangeDocument;

@RestController
@AllArgsConstructor
@Slf4j
public class EncounterReportController {

  private EncounterReportService encounterReportService;

  public final ECDSReportTransformer ecdsReportTransformer;
  public final IUCDSReportTransformer iucdsReportTransformer;

  @PostMapping("report")
  public ResponseEntity<ReportsDTO> generateReports(
      @RequestParam("encounter") String encounterRef) {

    EncounterReportInput encounterReportInput = encounterReportService
        .createEncounterReportInput(new ReferenceParam(encounterRef));

    try {
      CDSXMLInterchangeDocument ecdsReport = ecdsReportTransformer.transform(encounterReportInput);
      ClinicalDocumentDocument1 iucdsReport = iucdsReportTransformer
          .transform(encounterReportInput);

      ReportsDTO reportsDTO = ReportsDTO.builder()
          .ecds(prettyPrint(ecdsReport))
          .iucds(prettyPrint(iucdsReport))
          .build();

      log.info("Transformed ECDS:\n{}", reportsDTO.getEcds());
      log.info("Transformed IUCDS:\n{}", reportsDTO.getIucds());

      return ResponseEntity.status(HttpStatus.OK)
          .contentType(MediaType.APPLICATION_JSON)
          .body(reportsDTO);

    } catch (TransformationException | IOException e) {
      log.error("Transformation failed:\n{}", e.getMessage());
      throw new ServerErrorException("Transformation failed: " + e.getMessage(), e);
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
