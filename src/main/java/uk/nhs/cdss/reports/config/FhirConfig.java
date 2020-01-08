package uk.nhs.cdss.reports.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {

  @Value("${fhir.server}")
  private String fhirServer;


  @Bean
  public FhirContext fhirContext() {
    FhirContext fhirContext = FhirContext.forDstu3();
    fhirContext.setParserErrorHandler(new StrictErrorHandler());

    return fhirContext;
  }

  @Bean
  public IParser fhirParser() {
    return fhirContext().newJsonParser();
  }

  @Bean
  public FhirRestfulClient fhirServerClient() {
    return fhirContext()
        .newRestfulClient(FhirRestfulClient.class,fhirServer);
  }
}
